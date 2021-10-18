package com.wx;

import com.spring.WXApplicationContext;
import com.wx.service.UserService;

public class Test {
    public static void main(String[] args) {

        //创建spring容器
        WXApplicationContext applicationContext = new WXApplicationContext(AppConfig.class);
        //map
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();
//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("userService"));

    }
}
