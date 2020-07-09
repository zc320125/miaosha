package com.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBussinessError;
import com.miaosha.model.OrderModel;
import com.miaosha.model.UserModel;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")    //跨域请求
public class OrderController extends  BaseController{
    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    //封装下单请求
    @RequestMapping(value="/createOrder",method = RequestMethod.POST,consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name = "amount")Integer amount,
                                        @RequestParam(name = "promoId",required = false)Integer promoId) throws BusinessException {
        //获取用户登录信息//
//        Boolean isLogin = (Boolean) this.httpServletRequest.getSession().getAttribute("IS_LOGIN");
//        if(isLogin==null || isLogin==false){
//            throw new BusinessException(EmBussinessError.USER_NOT_LOGIN,"用户未登录不能下单");
//        }
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBussinessError.USER_NOT_LOGIN,"用户未登录不能下单");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null){
            throw new BusinessException(EmBussinessError.USER_NOT_LOGIN,"用户已过期，请重新登陆");

        }


 //       UserModel userModel = (UserModel) this.httpServletRequest.getSession().getAttribute("LOGIN_USER");
        if(userModel==null){
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"无用户登录信息");
        }

        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,amount,promoId);

        return CommonReturnType.create(null);
    }

}
