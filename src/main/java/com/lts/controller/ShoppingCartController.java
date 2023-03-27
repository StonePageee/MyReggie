package com.lts.controller;

import com.lts.common.BaseContext;
import com.lts.common.R;
import com.lts.entity.ShoppingCart;
import com.lts.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 选择菜品或套餐
     * @param shoppingCart
     * @param httpSession
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> addToShoppingCart(@RequestBody ShoppingCart shoppingCart, HttpSession httpSession){

        Long userId = (Long) httpSession.getAttribute("user");
        shoppingCart.setUserId(userId);

        return R.success(shoppingCartService.addToShoppingCart(shoppingCart));
    }

    /**
     * 减少选择的菜品或套餐数量
     * @param shoppingCart
     * @param httpSession
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> subToShoppingCart(@RequestBody ShoppingCart shoppingCart, HttpSession httpSession){

        Long userId = (Long) httpSession.getAttribute("user");
        shoppingCart.setUserId(userId);

        return R.success(shoppingCartService.subToShoppingCart(shoppingCart));
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> shoppingCartList(){

        return R.success(shoppingCartService.shoppingCartList(BaseContext.getCurrentId()));
    }

    @DeleteMapping("/clean")
    public R<String> cleanShoppingCart(){

        shoppingCartService.cleanShoppingCart(BaseContext.getCurrentId());
        return R.success("购物车已清空！");
    }

}
