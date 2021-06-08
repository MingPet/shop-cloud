package com.fh.shop.api.filter;

import com.alibaba.fastjson.JSON;
import com.fh.shop.common.Constants;
import com.fh.shop.common.KeyUtil;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.Md5Util;
import com.fh.shop.util.RedisUtil;
import com.fh.shop.vo.MemberVo;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JwtFilter extends ZuulFilter {

    @Value("${fh.shop.checkUrls}")
    private List<String> checkUrls;


    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @SneakyThrows
    @Override
    public Object run() throws ZuulException {

        log.info("----------{}",checkUrls);
        //获取当前访问的url
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        StringBuffer requestURL = request.getRequestURL();
        log.info("---==========---{}",requestURL);
        //包含
        boolean isCheck = false;//声明一个变量 是否检查
        for (String checkUrl : checkUrls) {
            if(requestURL.indexOf(checkUrl) > 0){
                //包含
                isCheck = true;
                break;
            }
        }

        if (!isCheck){
            return null; //如果不包含就放行
        }
        //进行验证
        //验证 x-auth:fygfd23...
        //判断是否有头信息
        String header = request.getHeader("x-auth");
        if(StringUtils.isEmpty(header)){
            //不仅拦截  还给前台提示信息
            //头信息丢失
            return buildResponse(ResponseEnum.MEMBER_LOGIN_TOKEN_IS_MISS);
        }
        //验证头信息是否完整
        String[] headerArr = header.split(",");
        if(headerArr.length != 2){
            return buildResponse(ResponseEnum.MEMBER_LOGIN_TOKEN_IS_NOT_FULL);
        }
        //验签
        String memberVoJsonBase64 = headerArr[0];
        String signBase64 = headerArr[1];
        //解码
        String memberVoJson = new String(Base64.getDecoder().decode(memberVoJsonBase64),"utf-8");
        String sign = new String(Base64.getDecoder().decode(signBase64),"utf-8");

        String newSign = Md5Util.sign(memberVoJson, Constants.SECRET);
        if(!newSign.equals(sign)){
            //验签失败
            return buildResponse(ResponseEnum.MEMBER_LOGIN_TOKEN_IS_FALL);
        }
        //将json转为java对象
        MemberVo memberVo = com.alibaba.fastjson.JSON.parseObject(memberVoJson, MemberVo.class);
        Long id = memberVo.getId();

        //判断是否过期
        if(!RedisUtil.exist(KeyUtil.buildMemberKey(id))){
            return buildResponse(ResponseEnum.MEMBER_LOGIN_TOKEN_IS_TIME_OUT);
        }
        //续命
        RedisUtil.expire(KeyUtil.buildMemberKey(id),Constants.TOKEN_EXPIRE);
        //将memberVo存入requent中
        //request.setAttribute(Constants.CURR_MEMBER,memberVo);
        //将要传给后台微服务的信息放到请求头中
        currentContext.addZuulRequestHeader(Constants.CURR_MEMBER, URLEncoder.encode(memberVoJson,"utf-8"));

        return null;
    }

    private Object buildResponse(ResponseEnum responseEnum) {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletResponse response = currentContext.getResponse();
        response.setContentType("application/json;charset=utf-8");
        currentContext.setSendZuulResponse(false);//拦截
        ServerResponse error = ServerResponse.error(responseEnum);

        String res = JSON.toJSONString(error);
        currentContext.setResponseBody(res);
        return null;
    }
}
