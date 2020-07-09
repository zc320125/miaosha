package com.miaosha.controller;

import com.miaosha.controller.viewobject.ItemVO;
import com.miaosha.error.BusinessException;
import com.miaosha.model.ItemModel;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.CacheService;
import com.miaosha.service.ItemService;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Controller("item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")    //跨域请求
public class ItemController extends BaseController {
    @Autowired
    private ItemService itemService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    //创建商品的controller
    @RequestMapping(value="/create" ,method = RequestMethod.POST,consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name="title")String title,
                                       @RequestParam(name="description")String description,
                                       @RequestParam(name="stock")Integer stock,
                                       @RequestParam(name="price") BigDecimal price,
                                       @RequestParam(name="imgUrl")String imgUrl
                                       ) throws BusinessException {
        //封装service请求创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);
        itemModel.setDescription(description);

        ItemModel itemModelForReturn = this.itemService.createItem(itemModel);

        ItemVO itemVO = this.convertItemVOFromItemModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);
    }


    //商品详情页浏览
    @RequestMapping(value="/get" ,method = RequestMethod.GET)
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id){
        ItemModel itemModel = null;

        //先查本地缓存
        itemModel = (ItemModel)cacheService.getCommonCache("item_"+id);
        if(itemModel == null){
            //根据商品ID到redis内获取
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_"+id);
            //若redis内不存在对应的itemmodel，则访问service
            if(itemModel == null){
                itemModel = this.itemService.getItemById(id);
                //设置itemmodel到redis内
                redisTemplate.opsForValue().set("item_"+id,itemModel);
                redisTemplate.expire("item_"+id,10, TimeUnit.MINUTES);
            }
            //填充本地缓存
            cacheService.setCommonCache("item_"+id,itemModel);
        }
        ItemVO itemVO = this.convertItemVOFromItemModel(itemModel);

        return CommonReturnType.create(itemVO);
    }

    //商品列表页面浏览
    @RequestMapping(value="/list" ,method = RequestMethod.GET)
    @ResponseBody
    public CommonReturnType getItemList(){
        List<ItemModel> itemModels = this.itemService.listItem();
        List<ItemVO> itemVOS = itemModels.stream().map(itemModel -> {
            ItemVO itemVO = this.convertItemVOFromItemModel(itemModel);
            return  itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOS);
    }


    private ItemVO convertItemVOFromItemModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        if(itemModel.getPromoModel() != null){
            //说明有正在进行或即将进行的活动
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }
}
