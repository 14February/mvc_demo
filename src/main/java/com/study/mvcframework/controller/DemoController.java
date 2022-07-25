package com.study.mvcframework.controller;

import com.study.mvcframework.annotations.AutoWired;
import com.study.mvcframework.annotations.Controller;
import com.study.mvcframework.annotations.RequestMapping;
import com.study.mvcframework.annotations.Security;
import com.study.mvcframework.pojo.User;
import com.study.mvcframework.service.IDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("api/")
public class DemoController {

    @AutoWired
    private IDemoService demoService;

    @Security({"1", "2"})
    @RequestMapping("user")
    public void queryUserInfo(HttpServletRequest req, Integer id, HttpServletResponse resp) throws IOException {
        System.out.println("test git conflict");
        System.out.println("what how why?");
        System.out.println("test");
        resp.getWriter().print(demoService.queryUserInfoById(id).toString());
        System.out.println("test");

    }

}
