package com.lts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lts.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
