package com.lts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lts.dto.DishDto;
import com.lts.entity.Dish;
import com.lts.entity.DishFlavor;
import com.lts.entity.Setmeal;
import com.lts.entity.SetmealDish;
import com.lts.mapper.DishMapper;
import com.lts.service.DishFlavorService;
import com.lts.service.DishService;
import com.lts.service.SetmealDishService;
import com.lts.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional //操作多张表，开启事务
    public void saveWithFlavor(DishDto dishDto) {
//        将菜品基本信息添加到Dish表
        this.save(dishDto);

//        获取菜品id
        Long dishId = dishDto.getId();

//        获取口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();

//        为每个口味信息添加DishId
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

//        批处理保存
        dishFlavorService.saveBatch(flavors);

//        更新完之后清空redis中对应的缓存数据
        String key = "category_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
    }


    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();

        Dish dish = this.getById(id);

        BeanUtils.copyProperties(dish, dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(list);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            Long id = dishDto.getId();
            item.setDishId(id);

            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

//        更新完之后清空redis中对应的缓存数据
        String key = "category_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
    }

    @Override
    @Transactional
    public String updateSellStatusWithSetmeal(int status, List<Long> ids) {

//      根据菜品id删除redis中对应分类的缓存
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId, ids);
        List<Dish> dishList = this.list(dishLambdaQueryWrapper);

        for (Dish dish : dishList) {
            String key = "category_" + dish.getCategoryId() + "_1";
            redisTemplate.delete(key);
        }

//      根据dishId更改dish表中的status
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Dish::getStatus, status)
                .in(Dish::getId, ids);
        this.update(updateWrapper);

//      根据dishId更新setmeal中的status
        if (status == 0) {
//          设置某个菜品status为停售，则将该菜品对应的套餐status也设为停售
//          根据dishId在setmeal_dish表中查找setmealId
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SetmealDish::getDishId, ids);
            List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
            List<Long> setmealIds = setmealDishes.stream().map(SetmealDish::getSetmealId).collect(Collectors.toList());

//            如果没有查询出套餐信息，直接返回
            if (setmealDishes.size() == 0) {
                return "";
            }

            setmealIds = setmealIds.stream().distinct().collect(Collectors.toList());

//          查询菜品对应的套餐状态
            LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealLambdaQueryWrapper.in(Setmeal::getId, setmealIds);
            List<Setmeal> setmeals = setmealService.list(setmealLambdaQueryWrapper);
            List<Integer> setmealStatus = setmeals.stream().map(Setmeal::getStatus).collect(Collectors.toList());

            if (setmealStatus.contains(1)) {
//          如果有未停售套餐，则进行停售
//          根据setmealId在setmeal表中更新status
                LambdaUpdateWrapper<Setmeal> setmealLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                setmealLambdaUpdateWrapper.set(Setmeal::getStatus, status)
                        .in(Setmeal::getId, setmealIds);
                setmealService.update(setmealLambdaUpdateWrapper);

                LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                setmealLambdaQueryWrapper1.in(Setmeal::getId, setmealIds);
                List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper1);
                List<String> setmealNames = setmealList.stream().map(Setmeal::getName).collect(Collectors.toList());
                return setmealNames.toString();
            } else {
//          已经全部停售，跳出方法
                return "";
            }
        } else {
            return "status1";
        }

    }

    /**
     * 根据分类id获取对应菜品数据
     *
     * @param dish
     * @return
     */
    @Override
    public List<DishDto> getDishListById(Dish dish) {

        List<DishDto> dishDtoList;
//        从redis中获取数据
        String key = "category_" + dish.getCategoryId() + "_" + dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
//        如果能获取到，直接返回
            return dishDtoList;
        }

//        如果获取不到，查询数据库，再封装到redis中
        synchronized (this) {
//            进入synchronized要再次查询一次redis，防止上一个抢到锁的线程更新了数据
            dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
            if (dishDtoList != null) {
                return dishDtoList;
            }

            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dish::getCategoryId, dish.getCategoryId());
            List<Dish> dishList = this.list(queryWrapper);

            dishDtoList = dishList.stream().map((item) -> {
                DishDto dishDto = new DishDto();

                BeanUtils.copyProperties(item, dishDto);
                LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
                dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
                List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);

                dishDto.setFlavors(dishFlavorList);
                return dishDto;
            }).collect(Collectors.toList());

//        将结果添加到redis中
            Duration expire = Duration.ofHours(2L).plus(Duration.ofSeconds((int) (Math.random() * 100)));
            redisTemplate.opsForValue().set(key, dishDtoList, expire);
            return dishDtoList;
        }
    }
}