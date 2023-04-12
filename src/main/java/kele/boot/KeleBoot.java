package kele.boot;

public class KeleBoot {
    public static Context run(Class rootClass) {
        return new Context(rootClass);
    }
}
