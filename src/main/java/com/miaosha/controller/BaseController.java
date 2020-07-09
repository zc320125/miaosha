package com.miaosha.controller;

import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBussinessError;
import com.miaosha.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public class BaseController {

    public static final String CONTENT_TYPE_FORMED  ="application/x-www-form-urlencoded";

    //定义exceptionhandle解决未被controller层吸收的exception，设置前端异常
    //ExceptionHandler 统一处理方法抛出的异常
    //ResponseStatus 设置返回结果HttpStatus.OK 200
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handleException(HttpServletRequest request, Exception e){
        HashMap<String,Object> responseMap= new HashMap<String,Object>();
        if(e instanceof BusinessException){
            BusinessException businessException = (BusinessException)e;
            responseMap.put("errCode",businessException.getErrCode());
            responseMap.put("errMsg",businessException.getErrMsg());
        }else{
            responseMap.put("errCode", EmBussinessError.PARAMETER_UNKNOWN_ERROR.getErrCode());
            responseMap.put("errMsg",EmBussinessError.PARAMETER_UNKNOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.create(responseMap,"fail");
    }
}
