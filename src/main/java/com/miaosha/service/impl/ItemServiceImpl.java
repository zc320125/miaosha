package com.miaosha.service.impl;

import com.miaosha.dao.ItemDOMapper;
import com.miaosha.dao.ItemStockDOMapper;
import com.miaosha.dataobject.ItemDO;
import com.miaosha.dataobject.ItemStockDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBussinessError;
import com.miaosha.model.ItemModel;
import com.miaosha.model.PromoModel;
import com.miaosha.service.ItemService;
import com.miaosha.service.PromoService;
import com.miaosha.validator.ValidationResult;
import com.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.ls.LSInput;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult validationResult = validator.validate(itemModel);
        if(validationResult.isHasErrors()){
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,validationResult.getErrorMsg());
        }
        //转化itemmodel 为dataobject
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);
        //写入数据库
        itemDOMapper.insertSelective(itemDO);

        itemModel.setId(itemDO.getId());
        ItemStockDO itemStockDO = this.convertItemStockFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);
        //返回对象
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel>itemModels =itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = this.itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertItemModelFromItem(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModels;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO==null){
            return null;
        }
        //拿到库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        if(itemStockDO==null){
            return null;
        }
        ItemModel itemModel = this.convertItemModelFromItem(itemDO,itemStockDO);
        //获取活动商品信息
        PromoModel promoModel = this.promoService.getPromoByItemId(itemModel.getId());
        if(promoModel != null && promoModel.getStatus().intValue() != 3){
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        int affectedRow = itemStockDOMapper.decreaseStock(itemId,amount);
        if(affectedRow>0){
            return true;
        }else{
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) {
        this.itemDOMapper.increaseSales(itemId,amount);
    }


    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    private ItemStockDO convertItemStockFromItemModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    private ItemModel convertItemModelFromItem(ItemDO itemDO,ItemStockDO itemStockDO){
        if(itemDO==null || itemStockDO==null){
            return null;
        }

        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;

    }


}
