package com.miaosha.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.miaosha.dao.UserDOMapper;
import com.miaosha.dao.UserPasswordDOMapper;
import com.miaosha.dataobject.UserDO;
import com.miaosha.dataobject.UserPasswordDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBussinessError;
import com.miaosha.model.UserModel;
import com.miaosha.service.UserService;
import com.miaosha.validator.ValidationResult;
import com.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jws.soap.SOAPBinding;
import java.awt.image.BufferStrategy;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO==null){
            return null;
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);
        return userModel;
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel==null){
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"用户模型为空");
        }
//        if(StringUtils.isEmpty(userModel.getName())
//                ||StringUtils.isEmpty(userModel.getTelphone())
//                ||userModel.getGender()==null
//                ||userModel.getAge()==null){
//            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
//        }
        ValidationResult validationResult =  this.validator.validate(userModel);
        if(validationResult.isHasErrors()){
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,validationResult.getErrorMsg());
        }
        UserDO userDO = new UserDO();
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        //实现model -》dataobject的方法
        userDO = convertFromUserModel(userModel);
        try {
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException e){
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"手机号已存在");
        }

        userModel.setId(userDO.getId());
        userPasswordDO=convertPasswordFromUserModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return;

    }

    @Override
    public UserModel validLogin(String telphone, String encrptPassword) throws BusinessException {
       //通过用户手机 获取用户信息，
        UserDO userDO = userDOMapper.selectByTelPhone(telphone);
        if(userDO==null){
            throw new BusinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        if(userPasswordDO==null){
            throw new BusinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);

       //比对用户密码
        if(!StringUtils.equals(encrptPassword,userModel.getEncrptPassword())){
            throw new BusinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }





    //userModel 转成 UserPasswordDO
    private UserPasswordDO convertPasswordFromUserModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return  userPasswordDO;
    }

    //userModel 转成 UserDO
    private UserDO convertFromUserModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO==null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        if(userPasswordDO==null){
            return null;
        }
        userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        return userModel;
    }
}

