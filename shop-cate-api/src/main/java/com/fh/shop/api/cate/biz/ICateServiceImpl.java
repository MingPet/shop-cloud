package com.fh.shop.api.cate.biz;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.cate.mapper.ICateMapper;
import com.fh.shop.api.cate.po.Cate;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service("cateService")
@Transactional(rollbackFor = Exception.class)
public class ICateServiceImpl implements ICateService {
    @Autowired
    private ICateMapper cateMapper;
    @Override
    public ServerResponse findList() {
        //先从缓存中找
        String cateListInfo = RedisUtil.get("cateList");
        //如果缓存中有的话  直接返回
        if(!StringUtils.isEmpty(cateListInfo)){
            //json字符串转为java对象
            List<Cate> cates = JSON.parseArray(cateListInfo, Cate.class);
            //返回
            return ServerResponse.success(cates);
        }

        //先从数据库找
        List<Cate> cateList = cateMapper.selectList(null);
        //要把java对象转为json格式的字符串
        String cateJsonString = JSON.toJSONString(cateList);
        //放到缓存中
        RedisUtil.set("cateList",cateJsonString);
        //返回
        return ServerResponse.success(cateList);
    }
}
