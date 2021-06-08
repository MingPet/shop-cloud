package com.fh.shop.api.member.biz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fh.shop.api.member.mapper.IMemberMapper;
import com.fh.shop.api.member.po.Member;
import com.fh.shop.api.member.vo.MemberVo;
import com.fh.shop.common.Constants;
import com.fh.shop.common.KeyUtil;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.Md5Util;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("memberService")
@Transactional(rollbackFor = Exception.class)
public class IMemberServiceImpl implements IMemberService {

    @Autowired
    private IMemberMapper memberMapper;


    //登录
    @Override
    public ServerResponse login(String memberName, String password) {

        //非空验证
        if(StringUtils.isEmpty(memberName) || StringUtils.isEmpty(password)){
            return ServerResponse.error(ResponseEnum.MEMBER_LOGIN_INFO_IS_EXIST);
        }


        //验证用户名是否正确
        QueryWrapper<Member> memberQueryWrapper = new QueryWrapper<>();
        memberQueryWrapper.eq("memberName",memberName);
        Member member = memberMapper.selectOne(memberQueryWrapper);
        if(member == null){
            return ServerResponse.error(ResponseEnum.MEMBER_LOGIN_MEMBER_NAME_NOT_EXIST);
        }

        //验证密码是否正确
        if(!Md5Util.md5(password).equals(member.getPassword())){
            return ServerResponse.error(ResponseEnum.MEMBER_LOGIN_PASSWORD_IS_ERROR);
        }

        //
        String status = member.getStatus();
        //未激活
        if(status.equals(Constants.INACTIVE_STATUS)){
            //获取mail 和 生成uuid 作为错误信息传递
            String mail = member.getMail();

            Map<String,String> result = new HashMap<>();
            result.put("mail",mail);
            result.put("id",member.getId()+"");
            return ServerResponse.error(ResponseEnum.MEMBER_OLD_INACTIVE,result);
        }

        //创建MemberVo
        MemberVo memberVo = new MemberVo();
        Long id = member.getId();
        memberVo.setId(id);
        memberVo.setMemberName(member.getMemberName());
        memberVo.setNickName(member.getNickName());
        //将用户信息转为json字符串
        String memberVoJson = com.alibaba.fastjson.JSON.toJSONString(memberVo);
        //生成签名
        String sign = Md5Util.sign(memberVoJson,Constants.SECRET);

        //将用户信息进行base64编码
        String memberVoJsonBase64 = Base64.getEncoder().encodeToString(memberVoJson.getBytes());
        //将签名进行base64编码
        String signBase64 = Base64.getEncoder().encodeToString(sign.getBytes());

        //将信息存入redis中
        RedisUtil.setEx(KeyUtil.buildMemberKey(id),"",Constants.TOKEN_EXPIRE);

        //把用户信息和签名相应给前台 相应给前台的信息中不能有密码
        //将用户信息和签名通过","分割 组成一个字符串传给前台
        return ServerResponse.success(memberVoJsonBase64 +"," +signBase64);
    }

    @Override
    public ServerResponse checkMemberName(String memberName) {
        //会员名是否为空
        if(StringUtils.isEmpty(memberName)){
            return ServerResponse.error(ResponseEnum.MEMBER_MEMBER_NAME_IS_NULL);
        }
        //会员名是否已存在 唯一
        QueryWrapper<Member> memberNameQueryWrapper = new QueryWrapper<>();
        memberNameQueryWrapper.eq("memberName", memberName);
        Member member = memberMapper.selectOne(memberNameQueryWrapper);
        if(member != null){
            return ServerResponse.error(ResponseEnum.MEMBER_INFO_IS_EXIST);
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse checkPhone(String phone) {
        //判断手机格式
        Pattern pattern = Pattern.compile("^[1]\\d{10}$");
        boolean matches = pattern.matcher(phone).matches();
        if(matches != true){
            return ServerResponse.error(ResponseEnum.MEMBER_PHONE_IS_ERROR);
        }
        //手机非空
        if(StringUtils.isEmpty(phone)){
            return ServerResponse.error(ResponseEnum.MEMBER_PHONE_IS_NULL);
        }
        //手机号是否已存在  唯一
        QueryWrapper<Member> phoneQueryWrapper = new QueryWrapper<>();
        phoneQueryWrapper.eq("phone", phone);
        Member member = memberMapper.selectOne(phoneQueryWrapper);
        if(member != null){
            return ServerResponse.error(ResponseEnum.MEMBER_PHONE_IS_EXIST);
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse checkMail(String mail) {
        //邮箱非空
        if (StringUtils.isEmpty(mail)) {
            return ServerResponse.error(ResponseEnum.MEMBER_MAIL_IS_NULL);
        }
        //邮箱是否已存在  唯一
        QueryWrapper<Member> mailQueryWrapper = new QueryWrapper<>();
        mailQueryWrapper.eq("mail", mail);
        Member member = memberMapper.selectOne(mailQueryWrapper);
        if (member != null) {
            return ServerResponse.error(ResponseEnum.MEMBER_MAIL_IS_EXIST);
        }

        //验证邮箱格式
        String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(mail);
        if(!matcher.matches()){
            //邮箱格式不正确
            return ServerResponse.error(ResponseEnum.MEMBER_MAIL_FORMAT_ERROR);
        }

        return ServerResponse.success();
    }

    @Override
    public ServerResponse checkMaiRetrievePassword(String mail) {
        //邮箱非空
        if (StringUtils.isEmpty(mail)) {
            return ServerResponse.error(ResponseEnum.MEMBER_MAIL_IS_NULL);
        }


        //验证邮箱格式
        String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(mail);
        if(!matcher.matches()){
            //邮箱格式不正确
            return ServerResponse.error(ResponseEnum.MEMBER_MAIL_FORMAT_ERROR);
        }

        //验证邮箱是否存在
        QueryWrapper<Member> memberQueryWrapper = new QueryWrapper<>();
        memberQueryWrapper.eq("mail",mail);
        Member member = memberMapper.selectOne(memberQueryWrapper);
        if(member == null){
            return ServerResponse.error(ResponseEnum.MEMBER_FIND_PASSWORD_MAIL_ERROR);
        }

       // String code = RandomStringUtils.randomAlphanumeric(4);
        //发送邮件
        //mailUtil.sendMailHtml(mail,"飞狐乐购平台通知","您的验证码为："+code+",请登录之后尽快修改！");


        return ServerResponse.success();
    }



    @Override
    public int activeMember(String id) {
        //判断redis中是否有该key
        String result = RedisUtil.get(KeyUtil.buildActiveMemberkey(id));
        if(StringUtils.isEmpty(result)){
            //错误：提示错误信息 请求错误
            return Constants.REQUEST_ERROR;

        }
        //更新数据库
        Member member = new Member();
        member.setId(Long.parseLong(result));
        member.setStatus(Constants.ACTIVE_STATUS);
        memberMapper.updateById(member);
        //删除redis
        RedisUtil.delete(KeyUtil.buildActiveMemberkey(id));
        return Constants.REQUEST_SUCCESS;


    }


}
