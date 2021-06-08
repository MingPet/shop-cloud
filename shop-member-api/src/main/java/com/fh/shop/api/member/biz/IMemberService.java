package com.fh.shop.api.member.biz;

import com.fh.shop.api.member.param.MemberParam;
import com.fh.shop.common.ServerResponse;

public interface IMemberService {

    ServerResponse login(String memberName, String password);

    ServerResponse checkMemberName(String memberName);

    ServerResponse checkPhone(String phone);

    ServerResponse checkMail(String mail);

    ServerResponse checkMaiRetrievePassword(String mail);



    int activeMember(String id);

}
