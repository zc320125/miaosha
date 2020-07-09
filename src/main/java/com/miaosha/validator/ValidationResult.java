package com.miaosha.validator;

import org.apache.tomcat.util.buf.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
    //校验结果
    private boolean hasErrors = false;

    //存放错误信息的map
    private Map<String,String > errorMsgMap= new HashMap<>();

    //实现公用的通过格式化字符串信息获取错误结果的msg方法
    public String getErrorMsg(){
        return StringUtils.join(errorMsgMap.values());
    }




    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Map<String, String> getErrorMsgMap() {
        return errorMsgMap;
    }

    public void setErrorMsgMap(Map<String, String> errorMsgMap) {
        this.errorMsgMap = errorMsgMap;
    }
}
