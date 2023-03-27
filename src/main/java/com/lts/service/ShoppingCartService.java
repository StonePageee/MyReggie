package com.lts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lts.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    ShoppingCart addToShoppingCart(ShoppingCart shoppingCart);

    ShoppingCart subToShoppingCart(ShoppingCart shoppingCart);

    List<ShoppingCart> shoppingCartList(Long userId);

    void cleanShoppingCart(Long currentId);
}
