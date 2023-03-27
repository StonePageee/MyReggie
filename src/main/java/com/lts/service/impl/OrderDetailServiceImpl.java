package com.lts.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lts.entity.OrderDetail;
import com.lts.mapper.OrderDetailMapper;
import com.lts.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
