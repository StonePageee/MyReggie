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
    }

    @Override
    @Transactional
    public void removeWithStatus(Long[] ids) {

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
        this.remove(queryWrapper);

        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        for (Long id : ids) {
            setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id)
                    .or();
        }
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }

//    @Override
//    @Transactional
//    public void updateWIthDishStatus(int status, List<Long> ids) {
//
//        for (int i = 0; i < ids.size(); i++) {
//            Long setmealId = ids.get(i);
//
//        }

////        起售之前判断套餐中的菜品是否有停售
////        通过setmealId查询包含的DishId
//        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
//        List<SetmealDish> setmealDishes = setmealDishService.list(setmealDishLambdaQueryWrapper);
//        List<Long> dishIds = setmealDishes.stream().map(SetmealDish::getDishId).collect(Collectors.toList());
//
////        通过dishId查询菜品状态
//        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        dishLambdaQueryWrapper.in(Dish::getId, dishIds);
//        List<Dish> dishes = dishService.list(dishLambdaQueryWrapper);
//        List<Integer> dishStatus = dishes.stream().map(Dish::getStatus).collect(Collectors.toList());
//
////          获取菜品name
//        List<String> dishesName = dishes.stream().map(Dish::getName).collect(Collectors.toList());
//
////        判断是否有停售菜品
//        if (dishStatus.contains(0)) {
////            查询出停售菜品在集合中的索引
//            List<Integer> dishStatusIsZero = new ArrayList<>();
//            for (int i = 0; i < dishes.size(); i++) {
//                if (dishStatus.get(i).equals(0)) {
//                    dishStatusIsZero.add(i);
//                }
//            }
////            通过停售菜品的索引查询对应的name
//            List<String> dishStatusIsZeroName = new ArrayList<>();
//            for (Integer integer : dishStatusIsZero) {
//                dishStatusIsZeroName.add(dishesName.get(integer));
//            }
//
//            throw new CustomException("选中套餐中关联停售菜品" + dishStatusIsZeroName + "，暂时不可启售！");
//        } else {
//            LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
//            updateWrapper.set(Setmeal::getStatus, status);
//            updateWrapper.in(Setmeal::getId, ids);
//            this.update(updateWrapper);
//        }
//    }

    @Override
    public void updateWIthDishStatus(int status, List<Long> ids) {

//        定义一个保存包含停售菜品的套餐的集合
        int containsDishStatusIsZeroCount = 0;

//        记录更新套餐数量
        int updateSetmealCount = 0;

//      记录所有套餐中包含的停售菜品
        List<String> dishStatusIsZeroName = new ArrayList<>();

        List<String> dishesName;

        List<Dish> dishes;

        List<Integer> dishStatus;

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
            }
        }

        dishStatusIsZeroName = dishStatusIsZeroName.stream().distinct().collect(Collectors.toList());

        if (containsDishStatusIsZeroCount > 0 && updateSetmealCount > 0) {

            throw new CustomException("已更新部分套餐状态，包含停售菜品" + dishStatusIsZeroName + "的套餐暂不可启售！");
        } else if (containsDishStatusIsZeroCount == ids.size() && updateSetmealCount == 0) {

            throw new CustomException("选中套餐均包含停售菜品" + dishStatusIsZeroName + "，暂不可启售！");
        } else if (containsDishStatusIsZeroCount == 0 && updateSetmealCount == ids.size()) {
            return;
        }
    }

    @Override
    public List<Setmeal> getListById(Setmeal setmeal) {

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        return this.list(setmealLambdaQueryWrapper);
    }
}

