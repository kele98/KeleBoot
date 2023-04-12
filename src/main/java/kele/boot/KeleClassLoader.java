package kele.boot;

import java.io.*;

public class KeleClassLoader extends ClassLoader{
    protected Class<?> findClass(beanDefination beanDefination)  {
        try{
            byte[] classBytes = getClassBytes(beanDefination.getBeanDir());
            return defineClass(beanDefination.getFullName(),classBytes,0,classBytes.length);
        }catch (Throwable e){
            throw new RuntimeException("load class error："+beanDefination.getBeanName());
        }
    }
    private byte[] getClassBytes(String path){
        try(InputStream fis = new FileInputStream(path); ByteArrayOutputStream classBytes = new ByteArrayOutputStream()){
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                classBytes.write(buffer, 0, len);
            }
            return classBytes.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
