package com.miaosha.service;

//封装本地缓存操作类
public interface CacheService {

    //存 方法
    void setCommonCache(String key,Object value);

    //取方法
    Object getCommonCache(String key);


}
