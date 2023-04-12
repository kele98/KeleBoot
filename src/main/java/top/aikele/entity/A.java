package top.aikele.entity;

import kele.boot.annotation.Autowired;
import kele.boot.annotation.Component;
import kele.boot.annotation.NeedAop;

@Component
public class A{
    private int a= 10;
    @Autowired
    private B b;
    @Autowired
    private C c;
    public A() {
    }
    public void eat() {
        System.out.println("A吃东西"+a);
    }
}
