package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.model.ItemModel;

import java.util.List;

public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;
    //商品列表   所有的商品信息
    List<ItemModel> listItem();
    //商品详情
    ItemModel getItemById(Integer id);
    //库存扣减
    boolean decreaseStock(Integer id,Integer amount);
    //商品销量增加
    void increaseSales(Integer itemId,Integer amount);

}
