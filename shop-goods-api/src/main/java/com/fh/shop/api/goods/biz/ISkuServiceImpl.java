package com.fh.shop.api.goods.biz;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.goods.mapper.ISkuMapper;
import com.fh.shop.api.goods.po.Sku;
import com.fh.shop.api.goods.vo.SkuVo;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("skuService")
@Transactional(rollbackFor = Exception.class)
public class ISkuServiceImpl implements ISkuService {

    @Autowired
    private ISkuMapper skuMapper;


    @Override
    public ServerResponse findList() {

        //先从缓存中找  如果有直接返回  没有再从数据库找 放到缓存中  返回
        String skuListJson = RedisUtil.get("skuList");
        if(!StringUtils.isEmpty(skuListJson)){
            List<SkuVo> skuVoList = JSON.parseArray(skuListJson, SkuVo.class);
            return ServerResponse.success(skuVoList);
        }
        List<Sku> skuList = skuMapper.findList();
        List<SkuVo> skuVoList = skuList.stream().map(x -> {
            SkuVo skuVo = new SkuVo();
            skuVo.setId(x.getId());
            skuVo.setPrice(x.getPrice().toString());
            skuVo.setSkuName(x.getSkuName());
            skuVo.setImage(x.getImage());
            return skuVo;
        }).collect(Collectors.toList());
        //转换
        String SkuVoListJson = JSON.toJSONString(skuVoList);
        //放到redis缓存中
        RedisUtil.setEx("skuList",SkuVoListJson,30);
        return ServerResponse.success(skuVoList);
    }
}
