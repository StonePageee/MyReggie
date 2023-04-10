package com.lts.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 捕获全球异常
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
public class GlobalExceptionHandler {

    //    处理SQLIntegrityConstraintViolationException异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {

        if (ex.getMessage().contains("Duplicate entry")) {
            String[] splits = ex.getMessage().split(" ");
            String msg = "添加失败，" + splits[2] + "已存在！";
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    //    处理CustomException异常
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {

        return R.error(ex.getMessage());
    }
}
