package com.lts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lts.dto.SetmealDto;
import com.lts.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    SetmealDto getByDish(Long id);

    void updateWithDish(SetmealDto setmealDto);

    void removeWithStatus(Long[] ids);

    void updateWithDishStatus(int status, List<Long> ids);

    List<Setmeal> getListById(Setmeal setmeal);
}

