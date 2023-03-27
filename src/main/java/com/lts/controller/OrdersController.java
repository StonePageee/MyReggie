package com.lts.controller;

import com.lts.common.BaseContext;
import com.lts.common.R;
import com.lts.entity.Orders;
import com.lts.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}


