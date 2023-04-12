package kele.boot;

import java.lang.reflect.Field;
import java.net.URL;

public class Utils {
    public static URL getClassPath(){
        return ClassLoader.getSystemResource("");
    }
    public static String getPathFormPackageName(String s){
        URL classPath = getClassPath();
        return classPath.getPath()+s.replaceAll("[/.]","/");
    }
    /*
    * copy对象的属性值
    * target 目标对象
    * source 源对象
    * */
    public static void copy(Object target, Object source){
        for (Field field : source.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object o = field.get(source);
                field.set(target,o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
