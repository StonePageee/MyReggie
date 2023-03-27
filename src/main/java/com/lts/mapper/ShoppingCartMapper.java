package com.lts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lts.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
