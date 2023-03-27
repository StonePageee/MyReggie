package com.lts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lts.common.CustomException;
import com.lts.entity.Category;
import com.lts.entity.Dish;
import com.lts.entity.Setmeal;
import com.lts.mapper.CategoryMapper;
import com.lts.service.CategoryService;
import com.lts.service.DishService;
import com.lts.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int dishCount = dishService.count(dishLambdaQueryWrapper);

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);

        if (dishCount > 0){
            throw new CustomException("删除失败，当前分类下已关联菜品");
        }

        if (setmealCount > 0){
            throw new CustomException("删除失败，当前分类下已关联套餐");
        }

        super.removeById(id);
    }
}
