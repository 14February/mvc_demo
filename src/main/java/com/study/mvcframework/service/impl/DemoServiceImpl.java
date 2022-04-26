package com.study.mvcframework.service.impl;

import com.study.mvcframework.annotations.Service;
import com.study.mvcframework.pojo.User;
import com.study.mvcframework.service.IDemoService;

@Service
public class DemoServiceImpl implements IDemoService {
    @Override
    public User queryUserInfoById(Integer id) {
        return new User(id, "yurui", 18);
    }
}
