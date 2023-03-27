package com.lts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lts.entity.User;
import com.lts.mapper.UserMapper;
import com.lts.service.UserService;
import com.lts.utils.ValidateCodeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Boolean getCode(User user, HttpSession httpSession) {

//        获取用户手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)){
//        生成一个四位的验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            System.out.println(code);

//        调用阿里云提供的短信服务API完成发送短信
//            SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            httpSession.setAttribute("phone",code);

            return true;
        }
        return false;
    }

    @Override
    public User login(Map map, HttpSession httpSession) {

        String phone = map.get("phone").toString();

        String code = map.get("code").toString();

        Object codeInSession = httpSession.getAttribute("phone");

        if (codeInSession.equals(code)){
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getPhone,phone);
            User user = this.getOne(userLambdaQueryWrapper);

            if (user == null){
                user = new User();
                user.setPhone(phone);
                this.save(user);
            }
            httpSession.setAttribute("user",user.getId());
            return user;
        }
        return null;
    }
}
