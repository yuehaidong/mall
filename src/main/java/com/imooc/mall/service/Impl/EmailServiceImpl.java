package com.imooc.mall.service.Impl;

import com.imooc.mall.common.Constant;
import com.imooc.mall.service.EmailService;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class EmailServiceImpl implements EmailService {
    @Resource
    private JavaMailSender mailSender;//用于发邮件
    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
        simpleMailMessage.setFrom(Constant.EMAIL_FROM);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(text);
        mailSender.send(simpleMailMessage);
    }
    @Override
    //验证码以及邮箱保存到redis
    public Boolean saveEmailToRedis(String emailAddress, String verificationCode){
        RedissonClient client = Redisson.create();
        //传入key
        RBucket<String> bucket = client.getBucket(emailAddress);
        boolean exists = bucket.isExists();
        if(!exists){
            bucket.set(verificationCode,60, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }
    @Override
    public Boolean checkEmailAndCode(String emailAddress,String verificationCode){
        RedissonClient client = Redisson.create();
        //传入key
        RBucket<String> bucket = client.getBucket(emailAddress);
        boolean exists = bucket.isExists();
        if(exists){
            String s = bucket.get();
            if(s.equals(verificationCode)){
                return true;
            }
        }
        return false;
    }
}
