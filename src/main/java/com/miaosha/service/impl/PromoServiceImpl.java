package com.miaosha.service.impl;

import com.miaosha.dao.PromoDOMapper;
import com.miaosha.dataobject.PromoDO;
import com.miaosha.model.PromoModel;
import com.miaosha.service.PromoService;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        //获取对应商品的秒杀活动信息
        PromoDO promoDO = this.promoDOMapper.selectByItemId(itemId);
        PromoModel promoModel = this.convertPromoModelFromPromoDO(promoDO);
        if(promoModel==null){
            return null;
        }
        //判断当前时间是否秒杀活动即将开始或者即将进行
        DateTime now = new DateTime();
        if(promoModel.getStartDate().isAfter(now)){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBefore(now)){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    private PromoModel convertPromoModelFromPromoDO(PromoDO promoDO){
        if(promoDO==null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        return promoModel;
    }
}
