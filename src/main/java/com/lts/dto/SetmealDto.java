package com.lts.dto;

import com.lts.entity.Setmeal;
import com.lts.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private String categoryName;

    private List<SetmealDish> setmealDishes;
}
