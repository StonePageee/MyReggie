package com.lts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lts.common.CustomException;
import com.lts.dto.SetmealDto;
import com.lts.entity.Dish;
import com.lts.entity.Setmeal;
import com.lts.entity.SetmealDish;
import com.lts.mapper.SetmealMapper;
import com.lts.service.DishService;
import com.lts.service.SetmealDishService;
import com.lts.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    @Lazy
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 将数据存放到两张表
     *
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
//        将基本信息存到setmeal表
        this.save(setmealDto);

//        将套餐中菜品信息存到setmeal_dish表中
//        获取当前套餐id
        Long setmealId = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

//        将套餐id添加给每个菜品
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

//        删除redis中对应的数据
        String key = "category_setmeal" + setmealDto.getCategoryId();
        redisTemplate.delete(key);
    }

    @Override
    public SetmealDto getByDish(Long id) {
//        将根据id查询出来的信息封装到sermealDto中
        SetmealDto setmealDto = new SetmealDto();
        Setmeal setmeal = this.getById(id);
        BeanUtils.copyProperties(setmeal, setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
//      将查询出来的菜品封装到setmealDto中
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

        String key = "category_setmeal" + setmealDto.getCategoryId();
        redisTemplate.delete(key);
    }

    @Override
    @Transactional
    public void removeWithStatus(Long[] ids) {

//        判断套餐是否在售卖
//        通过id查询套餐
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        for (Long id : ids) {
            queryWrapper.eq(Setmeal::getId, id)
                    .or();
        }
        List<Setmeal> setmeals = this.list(queryWrapper);

        for (Setmeal setmeal : setmeals) {
            if (setmeal.getStatus() == 1) {
                throw new CustomException("正在售卖的商品不可以删除！");
            }
        }
//        套餐中不包含正在售卖的菜品
        this.remove(queryWrapper);

//        循环删除redis中对应的套餐记录
        for (Setmeal setmeal : setmeals) {
            String key = "category_setmeal" + setmeal.getCategoryId();
            redisTemplate.delete(key);
        }

//      删除setmealDish表中套餐中的菜品
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        for (Long id : ids) {
            setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id)
                    .or();
        }
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }

    @Override
    public void updateWithDishStatus(int status, List<Long> ids) {

//        如果是停售套餐，则直接停售，不需要考虑菜品
        if (status == 0) {
            LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Setmeal::getStatus, status);
            updateWrapper.in(Setmeal::getId, ids);
            this.update(updateWrapper);

            LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Setmeal::getId, ids);
            List<Setmeal> setmealList = this.list(queryWrapper);

            for (Setmeal setmeal : setmealList) {
                String key = "category_setemeal" + setmeal.getCategoryId();
                redisTemplate.delete(key);
            }
            return;
        }

//        如果是启售套餐，则需要判断套餐中包含菜品的状态，如果有未启售的菜品，则套餐也不能启售
//        定义一个包含停售菜品的套餐的集合
        int containsDishStatusIsZeroCount = 0;

//        记录更新套餐数量
        int updateSetmealCount = 0;

//      记录所有套餐中包含的停售菜品
        List<String> dishStatusIsZeroName = new ArrayList<>();

        List<String> dishesName;

        List<Dish> dishes;

        List<Integer> dishStatus;

//        判断每一个套餐中是否包含停售菜品
        for (Long setmealId : ids) {
            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealId);
            List<SetmealDish> setmealDishes = setmealDishService.list(setmealDishLambdaQueryWrapper);
            List<Long> dishIds = setmealDishes.stream().map(SetmealDish::getDishId).collect(Collectors.toList());

            LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishLambdaQueryWrapper.in(Dish::getId, dishIds);
            dishes = dishService.list(dishLambdaQueryWrapper);
            dishStatus = dishes.stream().map(Dish::getStatus).collect(Collectors.toList());
            dishesName = dishes.stream().map(Dish::getName).collect(Collectors.toList());

            if (dishStatus.contains(0)) {
//                套餐中包含停售菜品
                containsDishStatusIsZeroCount++;

//              通过菜品status为0的索引查询相对应的菜品name
                for (int i = 0; i < dishes.size(); i++) {
                    if (dishStatus.get(i).equals(0)) {
                        dishStatusIsZeroName.add(dishesName.get(i));
                    }
                }
            } else {
//                套餐中不包含停售菜品，直接启售
                LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(Setmeal::getStatus, status).eq(Setmeal::getId, setmealId);
                this.update(updateWrapper);
                updateSetmealCount++;

                LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Setmeal::getId, setmealId);
                Setmeal setmeal = this.getOne(queryWrapper);
                String key = "category_setmeal" + setmeal.getCategoryId();
                redisTemplate.delete(key);
            }
        }
//      去重
        dishStatusIsZeroName = dishStatusIsZeroName.stream().distinct().collect(Collectors.toList());

        if (containsDishStatusIsZeroCount > 0 && updateSetmealCount > 0) {
            throw new CustomException("已更新部分套餐状态，包含停售菜品" + dishStatusIsZeroName + "的套餐暂不可启售！");
        } else if (containsDishStatusIsZeroCount == ids.size() && updateSetmealCount == 0) {
            throw new CustomException("选中套餐包含停售菜品" + dishStatusIsZeroName + "，暂不可启售！");
        }
    }

    @Override
    public List<Setmeal> getListById(Setmeal setmeal) {

        String key = "category_setmeal" + setmeal.getCategoryId();
//        先从redis中获取数据
        List<Setmeal> setmealList = (List<Setmeal>) redisTemplate.opsForValue().get(key);

        if (setmealList != null) {
//            从redis中获取到数据，直接返回
            return setmealList;
        }

//      从redis中没有获取到数据，从数据库中获取
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, setmeal.getCategoryId())
                .eq(Setmeal::getStatus,1);
        List<Setmeal> setmeals = this.list(setmealLambdaQueryWrapper);

//        将数据存入redis中
        redisTemplate.opsForValue().set(key, setmeals);
        return setmeals;
    }
}

