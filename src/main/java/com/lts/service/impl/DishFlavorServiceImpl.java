package com.lts.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lts.entity.DishFlavor;
import com.lts.mapper.DishFlavorMapper;
import com.lts.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
