package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.model.OrderModel;

public interface OrderService {

    //1.通过前端url传过来的秒杀活动id，然后下单接口内校验对应id是否属于对应商品且活动已经开始
    OrderModel createOrder(Integer userId,Integer itemId,Integer amount,Integer promoId) throws BusinessException;
    //2.直接在下单接口内判断对应的商品是否存在秒杀，若存在进行中的则以秒杀价格下单

    //
}
