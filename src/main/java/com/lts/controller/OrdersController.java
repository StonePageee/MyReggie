package com.lts.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lts.common.BaseContext;
import com.lts.common.R;
import com.lts.entity.Orders;
import com.lts.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @PostMapping("/submit")
    public R<String> goPay(@RequestBody Orders orders){

        ordersService.goPay(orders, BaseContext.getCurrentId());
        return R.success("下单成功");
    }

    @GetMapping("/page")
    public R<Page> orderList(int page, int pageSize, String number, String beginTime, String endTime){

        return R.success(ordersService.getListByCondition(page,pageSize,number,beginTime,endTime));
    }

    @GetMapping("/userPage")
    public R<Page> userOrderList(int page, int pageSize){

        return R.success(ordersService.getListByConditionForUser(page,pageSize));
    }
}