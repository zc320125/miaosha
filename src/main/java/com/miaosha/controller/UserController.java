package com.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaosha.controller.viewobject.UserVO;
import com.miaosha.dataobject.UserPasswordDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBussinessError;
import com.miaosha.model.UserModel;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.UserService;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")    //跨域请求
public class UserController extends  BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    //用户登陆接口
    @RequestMapping(value="/login",method = RequestMethod.POST,consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        //入参校验
        if(StringUtils.isEmpty(telphone)||StringUtils.isEmpty(password)){
            throw  new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"手机号或密码为空");
        }
        //用户登录服务，校验用户手机号和密码是否合法

        UserModel userModel = userService.validLogin(telphone,this.EncodeByMD5(password));

        //将登录凭证加入到用户登录成功的session中
//        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
//        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        //修改成若用户登录验证成功后将对应的登录信息和登录凭证一起存入redis中
        //生成登录凭证 token  uuid
        String uuidToken = UUID.randomUUID().toString();
        uuidToken = uuidToken.replace("-","");
        //建立token和用户登陆台之间的联系
        redisTemplate.opsForValue().set(uuidToken,userModel);
        redisTemplate.expire(uuidToken,1, TimeUnit.HOURS);
        //下发token
        return CommonReturnType.create(uuidToken);
    }



    //用户注册接口
    @RequestMapping(value="/register",method = RequestMethod.POST,consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone")String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "password") String password,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Integer gender,
                                     @RequestParam(name = "age")Integer age) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        //验证手机号和对应的otpCode
        String inSessionOtpCode  =(String)this.httpServletRequest.getSession().getAttribute(telphone);
        if(!StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BusinessException(EmBussinessError.PARAMETER_UNKNOWN_ERROR,"短信验证码不符合");
        }

        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setGender(gender.byteValue());
        userModel.setRegister("byphone");
        userModel.setEncrptPassword(this.EncodeByMD5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }


    public String EncodeByMD5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String newstr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }


    //用户获取otp短信接口
    @RequestMapping(value = "/getotp",method = RequestMethod.POST,consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone")String telphone){
        //需要按照一定规则 生产otp验证码
        Random random = new Random();
        int r = random.nextInt(99999);
        r+=100000;
        String otpCode = String.valueOf(r);
        //将otp验证码 同对应用户的手机号 关联 ,使用httpsession 方式绑定手机号和otpcode
        httpServletRequest.getSession().setAttribute(telphone,otpCode);
        //将otp验证码通过短信发送给用户，（省略）
        System.out.println("tel:"+telphone+" &otpCode:"+otpCode);
        return CommonReturnType.create(null);
    }




    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id")Integer id) throws BusinessException{
        //调用service服务获得对应id的user返回给前端
        UserModel userModel = userService.getUserById(id);
        //若用户信息不存在
        if(userModel==null){
            throw new BusinessException(EmBussinessError.USER_NOT_EXIST);
        }
        //将核心模型对象转换为ui使用的viewobject
        UserVO userVO = convertFromModel(userModel);
        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

}
