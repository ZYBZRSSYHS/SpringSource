package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WXApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();


    public WXApplicationContext(Class configClass){
        this.configClass = configClass;

        //扫描路径--创建beandefinition -- 》beanDefinitionMap
        scan(configClass);

        for(String beanName: beanDefinitionMap.keySet()){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName,beanDefinition);
                singletonObjects.put(beanName,bean);
            }
        }

        for(String beanName: beanDefinitionMap.keySet()){

            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            Class clazz = beanDefinition.getClazz();
            Object instance = null;

            try {
                instance = clazz.getDeclaredConstructor().newInstance();

                for(Field declaredField : clazz.getDeclaredFields()){
                    if(declaredField.isAnnotationPresent(Autowired.class)){
                        Object bean = getBean(declaredField.getName());
                        if(bean == null){
                            System.out.println("未找到对应Bean");
                        } else {
                            System.out.println(bean + "------");
                        }
                        declaredField.setAccessible(true);
                        declaredField.set(instance, bean);
                    }
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

    }

    public Object createBean(String beanName,BeanDefinition beanDefinition){

        Class clazz = beanDefinition.getClazz();

        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            //依赖注入

//            for(Field declaredField : clazz.getDeclaredFields()){
//                if(declaredField.isAnnotationPresent(Autowired.class)){
//                    Object bean = getBean(declaredField.getName());
//                    if(bean == null){
//                        throw new Exception();
//                    }
//                    declaredField.setAccessible(true);
//                    declaredField.set(instance, bean);
//                }
//            }

            // Aware回调
            if(instance instanceof BeanNameAware){
                ((BeanNameAware) instance).setBeanName(beanName);
            }


            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }


            // 初始化
            if(instance instanceof InitializingBean){
                ((InitializingBean) instance).afterPropertiesSet();
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }


            //Bean

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();
        path = path.replace(".","/");
        System.out.println(path);

        ClassLoader classLoader = WXApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource("com/wx/service");
        File file = new File(resource.getFile());
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                String fileName = f.getAbsolutePath();
                if(fileName.endsWith(".class")){
                    String className = fileName.substring(fileName.indexOf("com"),fileName.indexOf(".class"));
                    className = className.replace("\\",".");

                    try {
                        Class<?> aClass = classLoader.loadClass(className);
                        //拿到Bean
                        if(aClass.isAnnotationPresent(Component.class)){


                            if(BeanPostProcessor.class.isAssignableFrom(aClass)){
                                BeanPostProcessor instance = (BeanPostProcessor) aClass.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instance);
                            }

                            //判断Bean类型
                            Component componentAnnotation = aClass.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(aClass);

                            if(aClass.isAnnotationPresent(Scope.class)){
                                Scope scopeAnnotation = aClass.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                beanDefinition.setScope("singleton");
                            }

                            beanDefinitionMap.put(beanName,beanDefinition);
                            System.out.println(beanName);
                            System.out.println(beanDefinition);
                            System.out.println("***************");

                        }

                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

//                System.out.println(f);
            }
        }
    }

    public Object getBean(String beanName){
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")){
                Object o = singletonObjects.get(beanName);
//                System.out.println(o);
                return o;
            } else {
                Object bean = createBean(beanName,beanDefinition);
                return bean;
            }
        } else {
            System.out.println("未查找到" + beanName);
            //不存在对于bean
        }
        return null;
    }
}
