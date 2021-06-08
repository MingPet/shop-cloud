package com.fh.shop.api.member.controller;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.member.biz.IMemberService;
import com.fh.shop.api.member.vo.MemberVo;
import com.fh.shop.common.Constants;
import com.fh.shop.common.KeyUtil;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api")
public class MemberController {

    @Resource(name = "memberService")
    private IMemberService memberService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Value("${active.success}")
    private String success;

    @Value("${active.error}")
    private String error;


    @PostMapping("/member/login")
    public ServerResponse login(String memberName, String password) {
        return memberService.login(memberName, password);
    }

    //注销
    @SneakyThrows
    @GetMapping("/member/logout")
    public ServerResponse logout() {
        //MemberVo memberVo = (MemberVo) request.getAttribute(Constants.CURR_MEMBER);
        String memberVoJson = URLDecoder.decode(request.getHeader(Constants.CURR_MEMBER),"utf-8");
        MemberVo memberVo = JSON.parseObject(memberVoJson, MemberVo.class);
        RedisUtil.delete(KeyUtil.buildMemberKey(memberVo.getId()));
        return ServerResponse.success();
    }

    //验证会员名
    @GetMapping("/member/checkMemberName")
    public ServerResponse checkMemberName(String memberName) {
        return memberService.checkMemberName(memberName);
    }

    //验证手机号
    @GetMapping("/member/checkPhone")
    public ServerResponse checkPhone(String phone) {
        return memberService.checkPhone(phone);
    }

    //验证邮箱
    @GetMapping("/member/checkMail")
    public ServerResponse checkMail(String mail) {
        return memberService.checkMail(mail);
    }

    //获取用户信息
    @SneakyThrows
    @GetMapping("/member/findMember")
    public ServerResponse findMember() {
        //MemberVo memberVo = (MemberVo) request.getAttribute(Constants.CURR_MEMBER);

        String memberVoJson = URLDecoder.decode(request.getHeader(Constants.CURR_MEMBER),"utf-8");
        MemberVo memberVo = JSON.parseObject(memberVoJson, MemberVo.class);
        return ServerResponse.success(memberVo);
    }



    //验证邮箱
    @GetMapping("/member/checkMaiRetrievePassword")
    public ServerResponse checkMaiRetrievePassword(String mail) {

        return memberService.checkMaiRetrievePassword(mail);
    }





}
