package com.lts.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lts.dto.OrdersDto;
import com.lts.entity.Orders;

import java.util.List;

public interface OrdersService extends IService<Orders> {

    void goPay(Orders orders, Long currentId);

    Page<OrdersDto> getListByCondition(int page, int pageSize, String number, String beginTime, String endTime);

    Page<OrdersDto> getListByConditionForUser(int page, int pageSize);
}
