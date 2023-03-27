package com.lts.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lts.entity.Employee;
import com.lts.mapper.EmployeeMapper;
import com.lts.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {


}
