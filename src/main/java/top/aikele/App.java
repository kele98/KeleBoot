package top.aikele;

import kele.boot.Context;
import kele.boot.KeleBoot;
import kele.boot.annotation.KeleBootApplication;
import top.aikele.entity.A;
import top.aikele.entity.B;
import top.aikele.entity.C;

import java.util.HashSet;
import java.util.Set;

/**
 * Hello world!
 *
 */
@KeleBootApplication
public class App
{
    public static void main( String[] args )
    {
        Context context = KeleBoot.run(App.class);
        A a = (A) context.getBean("A");
        a.eat();
        B b = (B) context.getBean("B");
        b.eat();
        C c = (C) context.getBean("C");
        c.eat();
        System.out.println();
    }
}
