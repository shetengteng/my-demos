package com.stt.yygh.common.handler;

import com.stt.yygh.common.exception.YyghException;
import com.stt.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e) {
        e.printStackTrace();
        return Result.fail(e.getMessage());
    }

    // 自定义异常处理方法
    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public Result error(YyghException e) {
        return Result.build(e.getCode(), e.getMessage());
    }

}
