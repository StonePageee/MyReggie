package com.lts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lts.dto.DishDto;
import com.lts.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);

    String updateSellStatusWithSetmeal(int status, List<Long> ids);

    List<DishDto> getDishListById(Dish dish);
}
