package com.lts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lts.entity.Category;

public interface CategoryService extends IService<Category> {

    void remove(Long id);
}
