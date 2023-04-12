package kele.boot;

public class beanDefination {
    //在单例池中的名字
    private String beanName;
    //带包名的名字
    private String fullName;
    //路径
    private String beanDir;

    public beanDefination(String beanName, String FullName, String beanDir) {
        this.beanName = beanName;
        this.fullName = FullName;
        this.beanDir = beanDir;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanDir() {
        return beanDir;
    }

    public void setBeanDir(String beanFullName) {
        this.beanDir = beanFullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String FullName) {
        this.fullName = FullName;
    }

    @Override
    public String toString() {
        return "beanDefination{" +
                "beanName='" + beanName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", beanDir='" + beanDir + '\'' +
                '}';
    }
}
