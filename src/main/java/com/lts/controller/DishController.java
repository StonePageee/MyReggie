package com.lts.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lts.common.R;
import com.lts.dto.DishDto;
import com.lts.entity.Category;
import com.lts.entity.Dish;
import com.lts.entity.DishFlavor;
import com.lts.service.CategoryService;
import com.lts.service.DishFlavorService;
import com.lts.service.DishService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 分页查询菜品信息
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getSort);

        dishService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> dishDtoRecords = records.stream().map((item) -> {
//            创建一个新的dishDto对象保存数据
            DishDto dishDto = new DishDto();

//            将原来的数据拷贝到新的dishDto中
            BeanUtils.copyProperties(item, dishDto);

//            通过categoryId查询categoryName
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

//            将name赋值到dishDto中
            String categoryName = category.getName();
            if (category != null) {
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

//        设置新的page
        dishDtoPage.setRecords(dishDtoRecords);

        return R.success(dishDtoPage);
    }

    /**
     * 添加菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {

        dishService.saveWithFlavor(dishDto);
        return R.success("添加成功");
    }

    /**
     * 根据id获取菜品信息回显
     *
     * @param id
     * @return
     */

    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {

        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 根据id修改菜品信息
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {

        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }

    /**
     * 批量修改停/启售状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @Transactional
    public R<String> sellStatus(@PathVariable int status, @RequestParam(required = false) List<Long> ids) {

        String s = dishService.updateSellStatusWithSetmeal(status, ids);

        if (s.equals("")) {
            return R.success("菜品已停售！");
        } else if (s.equals("status1")) {
            return R.success("菜品已启售，请自行更改套餐状态！");
        } else {
            return R.success("菜品已停售，套餐" + s + "即将停售！");
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids) {

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        for (Long id : ids) {
            queryWrapper.eq(Dish::getId, id)
                    .or();
        }

        dishService.remove(queryWrapper);
        return R.success("删除成功！");
    }

    /**
     * 根据分类id查询所有菜品以及对应的口味
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> getDishByCategoryId(Dish dish) {

        return R.success(dishService.getDishListById(dish));
    }

}