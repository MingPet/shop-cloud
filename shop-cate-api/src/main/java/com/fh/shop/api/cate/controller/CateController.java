package com.fh.shop.api.cate.controller;

import com.fh.shop.api.cate.biz.ICateService;
import com.fh.shop.common.ServerResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
@Api(tags = "商品分类接口")

@Slf4j
public class CateController {

    @Resource(name = "cateService")
    private ICateService cateService;


    @Value("${server.port}")
    private String port;



    @GetMapping("/cates")

    public ServerResponse findList(){
        log.info("端口信息：{}",port);
        return  cateService.findList();
    }
}
