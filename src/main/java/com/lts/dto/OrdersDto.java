package com.lts.dto;

import com.lts.entity.OrderDetail;
import com.lts.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private List<OrderDetail> orderDetails;
}
