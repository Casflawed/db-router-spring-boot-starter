package com.flameking.middleware.db.router.support;

public class DataSourceContextHolder {
    private final static ThreadLocal<Integer> dbHolder = new ThreadLocal<>();
    private final static ThreadLocal<Integer> tbHolder = new ThreadLocal<>();

    public static Integer getDbKey(){
        return dbHolder.get();
    }

    public static void setDbKey(Integer key){
        dbHolder.set(key);
    }

    public static Integer getTbKey(){
        return tbHolder.get();
    }

    public static void setTbKey(Integer key){
        tbHolder.set(key);
    }

    public static void clear(){
        dbHolder.remove();
        tbHolder.remove();
    }
}
