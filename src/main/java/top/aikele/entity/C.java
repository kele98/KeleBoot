package top.aikele.entity;

import kele.boot.annotation.Autowired;
import kele.boot.annotation.Component;
import kele.boot.annotation.NeedAop;

/**
 * @projectName: KeleBoot
 * @package: top.aikele.entity
 * @className: C
 * @author: Kele
 * @description: TODO
 * @date: 2023/4/13 0:57
 * @version: 1.0
 */
@Component
public class C {
    @Autowired
    private A a;
    public C() {
    }
    public void eat() {
        System.out.println("C吃东西");
    }
}
