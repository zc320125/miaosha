package com.miaosha.service;

import com.miaosha.model.PromoModel;

public interface PromoService {
    //根据itemid 获取即将进行和正在进行的活动
    PromoModel getPromoByItemId(Integer itemId);
}
