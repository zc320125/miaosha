package com.miaosha.service.impl;

import com.miaosha.dao.OrderDOMapper;
import com.miaosha.dao.SequenceDOMapper;
import com.miaosha.dataobject.OrderDO;
import com.miaosha.dataobject.SequenceDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBussinessError;
import com.miaosha.model.ItemModel;
import com.miaosha.model.OrderModel;
import com.miaosha.model.UserModel;
import com.miaosha.service.ItemService;
import com.miaosha.service.OrderService;
import com.miaosha.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount,Integer promoId) throws BusinessException {
        //检验下单状态,用户是否合法，购买数量是否正确，商品是否存在
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel==null){
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        UserModel userModel = userService.getUserById(userId);
        if(userModel==null){
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"用户不存在");
        }
        if(amount<=0 || amount>99){
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"数量信息不正确");
        }

        //校验活动信息
        if(promoId!=null){
            //1.校验对应活动是否存在该商品
            if(promoId.intValue() != itemModel.getPromoModel().getId()){
                throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
            }else if(itemModel.getPromoModel().getStatus() != 2){ //2.校验活动是否正在进行
                throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"活动还未开始");
            }
        }


        //落单 减少库存
        boolean result = this.itemService.decreaseStock(itemId,amount);
        if(!result){
            throw new BusinessException(EmBussinessError.STOCK_NOT_ENOUGH);
        }

        //订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if(promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        //生产交易流水号
        orderModel.setId(generateOrderNo());
        OrderDO orderDO = this.convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);
        //加上商品的销量
        this.itemService.increaseSales(itemId,amount);
        //返回前端
        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo(){
        StringBuilder stringBuilder = new StringBuilder();
        //订单号16位
        //前8位为实践信息 年月日
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(date);
        //中间6位为自增序列
        //获取当前seq
        int seq = 0;
        SequenceDO sequenceDO = this.sequenceDOMapper.getSequenceByName("order_info");
        seq = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        //更新seq
        String sequence = String.valueOf(seq);
        for(int i=0;i<6-sequence.length();i++){
            stringBuilder.append(0);
        }
        stringBuilder.append(sequence);


        //后2位为分库分表
        stringBuilder.append("00");
        return stringBuilder.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel==null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }

}
