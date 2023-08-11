package com.flameking.middleware.db.router.support;

public class DataSourceContextHolder {
    private final static ThreadLocal<String> dbHolder = new ThreadLocal<>();
    private final static ThreadLocal<String> tbHolder = new ThreadLocal<>();

    public static String getDbKey(){
        return dbHolder.get();
    }

    public static void setDbKey(String key){
        dbHolder.set(key);
    }

    public static String getTbKey(){
        return tbHolder.get();
    }

    public static void setTbKey(String key){
        tbHolder.set(key);
    }

    public static void clear(){
        dbHolder.remove();
        tbHolder.remove();
    }
}
