package com.imooc.mall.service;

import com.imooc.mall.model.VO.CartVO;

import java.util.List;

public interface EmailService {
    //to：subject：主题，text:正文
  void sendSimpleMessage(String to,String subject,String text);

    //验证码以及邮箱保存到redis
    Boolean saveEmailToRedis(String emailAddress, String verificationCode);

    Boolean checkEmailAndCode(String emailAddress, String verificationCode);
}
