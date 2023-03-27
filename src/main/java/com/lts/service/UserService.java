package com.lts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lts.entity.User;

import javax.servlet.http.HttpSession;
import java.util.Map;

public interface UserService extends IService<User> {

    Boolean getCode(User user, HttpSession httpSession);

    User login(Map map, HttpSession httpSession);
}
