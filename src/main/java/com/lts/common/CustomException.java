package com.lts.common;

/**
 * 自定义异常
 */
public class CustomException extends RuntimeException{

    public CustomException(String message){
        super(message);
    }

    public CustomException(String message,Throwable dishName){
        super(message,dishName);
    }
}
