package com.wx.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class WXBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

//        if(beanName.equals("userService")){
//            System.out.println("初始化前");
//            ((UserService)bean).setName("WenXiao");
//        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        if(beanName.equals("userService")){
            Object proxyInstance = Proxy.newProxyInstance(WXBeanPostProcessor.class.getClassLoader(),
                    bean.getClass().getInterfaces(), new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            System.out.println("代理逻辑");
                            return method.invoke(bean, args);
                        }
                    });
            //返回代理对象
            return proxyInstance;
        }
        return bean;
    }
}
