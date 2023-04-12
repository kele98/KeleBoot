package kele.boot;

import kele.boot.annotation.*;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static javafx.scene.input.KeyCode.A;

public class Context {
    //存放初始化完成的bean和aop的bean
    private static ConcurrentHashMap<String,Object> singleMap = new ConcurrentHashMap();
    //存放构造函数创建的bean
    private static ConcurrentHashMap<String,Object> instancesBeanMap = new ConcurrentHashMap();
    private static ArrayList<beanDefination> beanDefinedList = new ArrayList<beanDefination>();
    private static Set<String> creatSet = new HashSet<>();
    private static String scanPath ;
    private static String classPath;
    public Context(Class rootClass) {
        init(rootClass);
    }
    //根据名字在单例池中拿bean
    public Object getBean(String name) {
        return singleMap.get(name);
    }
    //根据类型在单例池中拿bean
    public Object getBean(Class  aclass) {
        for (Map.Entry<String, Object> entry : singleMap.entrySet()) {
            if(entry.getClass() == aclass)
                return entry;
        }
        return null;
    }
    //初始化
    //在这里获取启动类携带的相关参数
    private void init(Class rootClass){
        for (Annotation annotation : rootClass.getAnnotations()) {
            //是KeleBootApplication的注解类
            if(annotation.annotationType() == KeleBootApplication.class){
                ComponentScan componentScan = annotation.annotationType().getAnnotation(ComponentScan.class);
                String path = componentScan.path();
                //使用配置类的路径扫描包
                if("".equals(path)){
                    scanPath=rootClass.getName().substring(0,rootClass.getName().lastIndexOf("."));
                    scanPath = Utils.getPathFormPackageName(scanPath);
                    classPath=Utils.getClassPath().getPath();
                    //初始化beanDefinedList
                    scan(scanPath);
                    for (int i = 0; i < beanDefinedList.size(); i++) {
                        createBean(beanDefinedList.get(i));
                    }
                }else{ //使用注解路径扫描包

                }
                return;
            }
        }
        throw new RuntimeException("This class is not a KeleBoot Configuration Class.");
    }
    //扫描
    private void scan(String path){
        File file = new File(path);
        if (file.isDirectory()) {
            for (File listFile : file.listFiles()) {
                //是目录递归下去进行查询
                if(listFile.isDirectory()){
                    scan(listFile.getPath());
                }
                //走到这里证明是Class结尾的文件
                if(listFile.getName().contains(".class")){
                    String innerFile = listFile.getName();
                    String beanName = innerFile.substring(0, innerFile.indexOf(".class"));
                    String fullName = listFile.getPath().substring(classPath.length()-1,listFile.getPath().indexOf(".class")).replaceAll("\\\\",".");
                    beanDefinedList.add(new beanDefination(beanName, fullName,listFile.getPath()));
                }
            }
            return;
        }
        throw new RuntimeException("not path");
    }
    //生成bean
    private void createBean(beanDefination beanDefination){
            //单例池或者instance池中已经有了
            if (singleMap.get(beanDefination.getBeanName())!=null||instancesBeanMap.get(beanDefination.getBeanName())!=null)
             return;
            //生成bean
            creatSet.add(beanDefination.getBeanName());
            Class<?> aClass = null;
            Object creatObject = null;
            try {
                aClass = this.getClass().getClassLoader().loadClass(beanDefination.getFullName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            Component component = aClass.getAnnotation(Component.class);
            if(component!=null){
                String name = component.name();
                try {
                    creatObject = aClass.newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if("".equals(name)){
                    instancesBeanMap.put(beanDefination.getBeanName(),creatObject);
                }else {
                    instancesBeanMap.put(name,creatObject);
                }
            }else {
                return;
            }
            //注入属性
            for (Field field : aClass.getDeclaredFields()) {
                Autowired autowired = field.getAnnotation(Autowired.class);
                Class<?> fieldType = field.getType();
                //需要注入
                if(autowired!=null){
                    field.setAccessible(true);
                    String fieldName = autowired.name();
                    Object fieldObject = null;
                    //获取属性对应的beanDefined
                    beanDefination fieldBeanDefined = null;
                    for (kele.boot.beanDefination beanDefined : beanDefinedList) {
                        if(beanDefined.getBeanName().equals(fieldName)||beanDefined.getFullName().equals(fieldType.getName())){
                            fieldBeanDefined=beanDefined;
                            break;
                        }
                        }
                    if(fieldBeanDefined==null)
                        throw new RuntimeException("Kele inner dont have this class of field");
                    // 用名字在singleMap中找
                    fieldObject=singleMap.get(fieldName);
                    if(fieldObject==null){
                        //在用类型singleMap中找
                        for (Map.Entry<String, Object> entry : singleMap.entrySet()) {
                            if(fieldType.isAssignableFrom(entry.getValue().getClass())){
                                fieldObject=entry.getValue();
                                break;
                            }

                        }
                    }
                    //在二级缓存中找
                    if(fieldObject==null){
                        // 用名字在instancesBeanMap中找
                        fieldObject = instancesBeanMap.get(fieldBeanDefined.getBeanName());
                        //在用类型instancesBeanMap中找
                        if(fieldObject==null){
                            for (Map.Entry<String, Object> entry : instancesBeanMap.entrySet()) {
                                if(fieldType.isAssignableFrom(entry.getValue().getClass())){
                                    fieldObject=entry.getValue();
                                    break;
                                }

                            }
                        }
                        if(fieldObject==null){
                            //需要创建
                            createBean(fieldBeanDefined);
                        }else {
                            //证明对象正在创建中
                            if(!creatSet.add(fieldBeanDefined.getBeanName())){
                                //检测是否需要aop 需要的话就提前aop
                                Class<?> fieldObjectClass = fieldObject.getClass();
                                for (Method method : fieldObjectClass.getDeclaredMethods()) {
                                    NeedAop annotation = method.getAnnotation(NeedAop.class);
                                    if(annotation!=null){
                                        //提前进行aop
                                        Object proxyObject = creatProxyObject(fieldObjectClass,fieldObject);
                                        //将代理对象放入单例池
                                        singleMap.put(fieldBeanDefined.getBeanName(),proxyObject);
                                        instancesBeanMap.remove(fieldBeanDefined.getBeanName());
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    //将属性注入
                    try {
                        //如果能拿到代理对象就直接设置代理对象的属性值
                        Object proxyObject = singleMap.get(fieldBeanDefined.getBeanName());
                        if(proxyObject==null)
                        field.set(creatObject,fieldObject);
                        else
                            field.set(creatObject,proxyObject);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            //进行aop 拿到类 进行代理后 放入单例池
            //判断是否需要进行aop
        Object finaObject = singleMap.get(beanDefination.getBeanName());
        if(finaObject==null){
                boolean flag = false;
                for (Method method : aClass.getDeclaredMethods()) {
                    NeedAop annotation = method.getAnnotation(NeedAop.class);
                    if(annotation!=null){
                        //进行aop
                        Object proxyObject = creatProxyObject(aClass, creatObject);
                        //将代理对象放入单例池
                        singleMap.put(beanDefination.getBeanName(),proxyObject);
                        instancesBeanMap.remove(beanDefination.getBeanName());
                        flag=true;
                        break;
                    }
                }
                //不需要aop时才设置
                if(!flag){
                    singleMap.put(beanDefination.getBeanName(),creatObject);
                    instancesBeanMap.remove(beanDefination.getBeanName());
                }
            }else {//已经进行过aop了需要复制下属性
                Utils.copy(finaObject,creatObject);
            }
            creatSet.remove(beanDefination.getBeanName());
    }
    /*
    创建代理对象并copy属性值
    * */
    public Object creatProxyObject(Class targetClass,Object targetOfCopy){
        //进行aop
        Object proxyObj = Enhancer.create(targetClass, new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                Object result = null;
                System.out.println("CGLIB代理执行前");
                result = methodProxy.invokeSuper(o, objects);
                System.out.println("CGLIB代理执行前");
                return result;
            }
        });
        //这里需要复制属性
        Utils.copy(proxyObj,targetOfCopy);
        return proxyObj;
    }

}
