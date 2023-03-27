package com.lts.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

//通用结果集
@Data
public class R<T> {

    private Integer code;//1表示成功 0或其它数字表示失败

    private String msg;//错误信息

    private T data;//响应数据

    private Map map = new HashMap<>(16);//动态数据

    public static <T> R<T> success(T object){
        R<T> r = new R<>();
        r.code = 1;
        r.data = object;
        return  r;
    }

    public static <T> R<T> error(String msg){
        R<T> r = new R<>();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value){
        this.map.put(key,value);
        return this;
    }
}
