package top.aikele.entity;

import kele.boot.annotation.Autowired;
import kele.boot.annotation.Component;
import kele.boot.annotation.NeedAop;

@Component
public class B{

    @Autowired
    private A a;

    public B() {
    }
    @NeedAop
    public void eat() {
        System.out.println("B吃东西");
    }
}
