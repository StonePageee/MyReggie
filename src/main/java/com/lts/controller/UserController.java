package com.lts.controller;

import com.lts.common.R;
import com.lts.entity.User;
import com.lts.service.UserService;
import com.lts.utils.SMSUtils;
import com.lts.utils.ValidateCodeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> code(@RequestBody User user, HttpSession httpSession) {

        Boolean isSuccess = userService.getCode(user, httpSession);

        if (isSuccess) {
            return R.success("验证码发送成功");
        } else {
            return R.error("验证码发送失败");
        }

    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession httpSession) {

        User user = userService.login(map, httpSession);

        if (user != null){
            return R.success(user);
        }
        return R.error("登录失败！");
    }

}
