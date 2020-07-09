package com.miaosha.service;


import com.miaosha.error.BusinessException;
import com.miaosha.model.UserModel;

public interface UserService {

    /**
     * 通过id查找用户
     * */
    UserModel getUserById(Integer id);

    /**
     * 用户注册
     * */
    void register(UserModel userModel) throws BusinessException;

    /**
     * 用户校验登录
     * telphone 用户注册手机
     * password  用户加密后的密码
     * */

    UserModel validLogin(String telphone,String encrptPassword) throws BusinessException;
}
