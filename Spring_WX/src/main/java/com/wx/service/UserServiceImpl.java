package com.wx.service;

import com.spring.*;
import com.wx.service.OrderService;
import com.wx.service.UserService;

@Component("userService")
public class UserServiceImpl implements UserService, InitializingBean, BeanNameAware {


    @Autowired
    private OrderService orderService;

    private String name;

    private String beanName;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void test() {
        System.out.println(orderService);
        System.out.println(name);
    }

//    @Override
//    public void setBeanName(String name) {
//        this.name = name;
//    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("初始化");
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
