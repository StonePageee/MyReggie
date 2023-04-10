package com.lts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lts.entity.User;
import com.lts.mapper.UserMapper;
import com.lts.service.UserService;
import com.lts.utils.ValidateCodeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Boolean getCode(User user, HttpSession httpSession) {

//        获取用户手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)){
//        生成一个四位的验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

//        调用阿里云提供的短信服务API完成发送短信
//            SMSUtils.sendMessage("瑞吉外卖","",phone,code);

//            httpSession.setAttribute("phone",code);
//            使用redis保存验证码
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return true;
        }
        return false;
    }

    @Override
    public User login(Map map, HttpSession httpSession) {

        String phone = map.get("phone").toString();

        String code = map.get("code").toString();

//        Object codeInSession = httpSession.getAttribute("phone");

//        使用redis保存验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

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
//            登录成功后从redis中删除
            redisTemplate.delete(phone);
            return user;
        }
        return null;
    }
}
