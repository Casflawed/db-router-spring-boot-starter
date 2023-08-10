package com.flameking.middleware.db.router.support;

public class DataSourceContextHolder {
    private final static ThreadLocal<String> holder = new ThreadLocal<>();

    public static String get(){
        return holder.get();
    }

    public static void set(String key){
        holder.set(key);
    }

    public static void clear(){
        holder.remove();
    }
}
