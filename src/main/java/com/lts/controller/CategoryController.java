package com.lts.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lts.common.R;
import com.lts.entity.Category;
import com.lts.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加菜品分类或套餐分类信息
     *
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        categoryService.save(category);
        String key = "categoryList";
        redisTemplate.delete(key);
        return R.success("添加成功！");
    }

    /**
     * 查询所有分类信息
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        Page pageInfo = new Page(page, pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.orderByDesc(Category::getSort);

        categoryService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 修改分类信息
     *
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        String key = "categoryList";
        redisTemplate.delete(key);
        return R.success("修改成功！");
    }

    /**
     * 根据id删除分类
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        categoryService.remove(ids);

        return R.success("删除成功");
    }

    /**
     * 根据类型返回目标分类集合
     *
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {

        List<Category> categoryList;
        String key = "categoryList";

//      先从redis中获取数据
        categoryList = (List<Category>) redisTemplate.opsForValue().get(key);
        if (categoryList != null) {
            return R.success(categoryList);
        }

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        categoryList = categoryService.list(queryWrapper);
        redisTemplate.opsForValue().set(key, categoryList);

        return R.success(categoryList);
    }
}
