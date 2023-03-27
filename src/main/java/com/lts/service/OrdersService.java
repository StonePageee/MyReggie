package com.lts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lts.entity.Orders;

public interface OrdersService extends IService<Orders> {

    void goPay(Orders orders, Long currentId);
}
