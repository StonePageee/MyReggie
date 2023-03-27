package com.lts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lts.entity.ShoppingCart;
import com.lts.mapper.ShoppingCartMapper;
import com.lts.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Override
    public ShoppingCart addToShoppingCart(ShoppingCart shoppingCart) {
//        根据用户和菜品或套餐id判断是否已经存在

        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, shoppingCart.getUserId());

        if (dishId != null) {
//            添加的是菜品
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
//            添加的是套餐
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart one = this.getOne(shoppingCartLambdaQueryWrapper);

        if (one != null) {
//        如果存在，将数量加一
            one.setNumber(one.getNumber() + 1);
            this.updateById(one);
        } else {
//        如果不存在，添加数据
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            this.save(shoppingCart);
            one = shoppingCart;
        }
        return one;
    }

    @Override
    public ShoppingCart subToShoppingCart(ShoppingCart shoppingCart) {

        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, shoppingCart.getUserId());

        Long dishId = shoppingCart.getDishId();
        ShoppingCart shoppingCartIsDishOrSetmeal;

        if (dishId != null) {
//            是菜品
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
//            是套餐
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        shoppingCartIsDishOrSetmeal = this.getOne(shoppingCartLambdaQueryWrapper);
        shoppingCartIsDishOrSetmeal.setNumber(shoppingCartIsDishOrSetmeal.getNumber() - 1);

        if (shoppingCartIsDishOrSetmeal.getNumber() == 0) {
            this.removeById(shoppingCartIsDishOrSetmeal);
        } else {
            this.updateById(shoppingCartIsDishOrSetmeal);
        }

        return shoppingCartIsDishOrSetmeal;
    }

    @Override
    public List<ShoppingCart> shoppingCartList(Long userId) {
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        return this.list(shoppingCartLambdaQueryWrapper);
    }

    @Override
    public void cleanShoppingCart(Long currentId) {

        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        this.remove(shoppingCartLambdaQueryWrapper);
    }
}
