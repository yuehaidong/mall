package com.imooc.mall.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.imooc.mall.common.ApiRestResponse;
import com.imooc.mall.common.Constant;
import com.imooc.mall.config.ThreadPoolConfig;
import com.imooc.mall.filter.UserFilter;
import com.imooc.mall.model.pojo.User;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.service.EmailService;
import com.imooc.mall.service.UserService;
import com.imooc.mall.util.EmailUtil;
import com.mysql.cj.util.StringUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Controller
public class UserController {
    @Resource
    ExecutorService getThreadPool;
    @Resource
    private UserService userService;
    @Resource
    private EmailService emailService;
    @GetMapping("/test")
    @ResponseBody
    public User getUser(Integer id){
        User user = userService.getUser(id);
        System.out.println("热部署测试");
        return user;
    }
    @PostMapping("/register")
    @ResponseBody
    public ApiRestResponse register(@RequestParam("username") String username,
                                    @RequestParam("password")String password,
                                    @RequestParam("emailAddress")String emailAddress,
                                    @RequestParam("verificationCode") String verificationCode) throws ImoocMallException, NoSuchAlgorithmException {
        if(StringUtils.isNullOrEmpty(username)){
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_USER_NAME);
        }
        if(StringUtils.isNullOrEmpty(password)){
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_USER_PASSWORD);
        }
        //密码长度不能少于8位
        if(password.length()<8){
            System.out.println(ImoocMallExceptionEnum.PASSWORD_TOO_SHORT);
            return ApiRestResponse.error(ImoocMallExceptionEnum.PASSWORD_TOO_SHORT);
        }
        if(StringUtils.isNullOrEmpty(emailAddress)){
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_EMAIL_ADDRESS);
        }
        if(StringUtils.isNullOrEmpty(verificationCode)){
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_VERIFICATIONCODE);
        }
        //如果邮箱已经注册，则不允许在注册
        boolean b = userService.checkEmailRegistered(emailAddress);
        if(!b){
            return ApiRestResponse.error(ImoocMallExceptionEnum.EMAIL_ALREADY_BEEN_REGISTERED);
        }
        //校验邮箱和验证码是否匹配
        Boolean passEmailAndCode = emailService.checkEmailAndCode(emailAddress, verificationCode);
        if(!passEmailAndCode){
            throw new ImoocMallException(ImoocMallExceptionEnum.WRONG_VERIFICATIONCODE);
        }
        userService.register(username,password,emailAddress);
        return ApiRestResponse.success();
    }
    @GetMapping("/login")
    @ResponseBody
public ApiRestResponse login(@RequestParam("userName") String username,
                             @RequestParam("password")String password, HttpSession session) throws ImoocMallException {
        //判断不能为空
        if(StringUtils.isNullOrEmpty(username)){
            System.out.println(ImoocMallExceptionEnum.NEED_USER_NAME);
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_USER_NAME);
        }
        if(StringUtils.isNullOrEmpty(password)){
            System.out.println(ImoocMallExceptionEnum.NEED_USER_PASSWORD);
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_USER_PASSWORD);
        }
        User user = userService.selectLogin(username, password);
        //保存用户信息时不保存密码
        user.setPassword(null);
        session.setAttribute(Constant.IMOOC_MALL_USER,user);
        return ApiRestResponse.success(user);

    }
    @PostMapping("/user/update")
    @ResponseBody
    public ApiRestResponse updateUserInfo(String signature,HttpSession session) throws ImoocMallException {
//        User currentUser= (User)session.getAttribute(Constant.IMOOC_MALL_USER);
        User currentUser = UserFilter.currentUser;
        if(currentUser==null){
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_LOGIN);
        }
        User user=new User();
        user.setId(currentUser.getId());
        user.setPersonalizedSignature(signature);
        userService.updateInformation(user);
        return ApiRestResponse.success();
    }

    @PostMapping("/user/logout")
    @ResponseBody
    public ApiRestResponse logout(HttpSession session){
        session.removeAttribute(Constant.IMOOC_MALL_USER);
        return ApiRestResponse.success();
    }
    //注意书写规范
    @GetMapping("/adminLogin")
    @ResponseBody
    public ApiRestResponse adminLogin(@RequestParam("userName") String username,
                                 @RequestParam("password")String password, HttpSession session) throws ImoocMallException {
        //判断不能为空
        if(StringUtils.isNullOrEmpty(username)){
            System.out.println(ImoocMallExceptionEnum.NEED_USER_NAME);
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_USER_NAME);
        }
        if(StringUtils.isNullOrEmpty(password)){
            System.out.println(ImoocMallExceptionEnum.NEED_USER_PASSWORD);
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_USER_PASSWORD);
        }
        //校验是不是管理员
        User user = userService.selectLogin(username, password);
        if (userService.getRole(user)) {
            //保存用户信息时不保存密码
            user.setPassword(null);
            session.setAttribute(Constant.IMOOC_MALL_USER,user);
            return ApiRestResponse.success(user);
        }else{
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_ADMIN);
        }
    }
    //注意书写规范
    @PostMapping("/user/sendEmail")
    @ResponseBody
    public ApiRestResponse adminLogin(String emailAddress){
        //检查邮箱地址是否有效，检查是否已经注册
        boolean validEmailAddress = EmailUtil.isValidEmailAddress(emailAddress);
        if(validEmailAddress){
            boolean b = userService.checkEmailRegistered(emailAddress);
            if(!b){
                return ApiRestResponse.error(ImoocMallExceptionEnum.EMAIL_ALREADY_BEEN_REGISTERED);
            }else{
                String verificationCode = EmailUtil.genVerificationCode();
                //防止恶意发送，存储到redis（临时数据适合放在redis）
                Boolean aBoolean = emailService.saveEmailToRedis(emailAddress, verificationCode);
                if(aBoolean){
                    getThreadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            emailService.sendSimpleMessage(emailAddress,Constant.EMAIL_SUBJECT,"欢迎注册，您的验证码是"+verificationCode);
                        }
                    });
                 return ApiRestResponse.success();
                }else{
                    return ApiRestResponse.error(ImoocMallExceptionEnum.EMAIL_ALREADY_BEEN_SEND);
                }

            }
        }else{
            return ApiRestResponse.error(ImoocMallExceptionEnum.WRONG_EMAIL);
        }
    }
    @GetMapping("/loginWithJwt")
    @ResponseBody
    public ApiRestResponse loginWithJwt(@RequestParam("userName") String userName,
                                        @RequestParam("password")String password){
        //判断不能为空
        if(StringUtils.isNullOrEmpty(userName)){
            System.out.println(ImoocMallExceptionEnum.NEED_USER_NAME);
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_USER_NAME);
        }
        if(StringUtils.isNullOrEmpty(password)){
            System.out.println(ImoocMallExceptionEnum.NEED_USER_PASSWORD);
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_USER_PASSWORD);
        }
        User user = userService.selectLogin(userName, password);
        //保存用户信息时不保存密码
        user.setPassword(null);
//        生成jwt
        Algorithm algorithm = Algorithm.HMAC256(Constant.JWT_KEY);
        String token = JWT.create().withClaim(Constant.USER_NAME, user.getUsername())
                .withClaim(Constant.USER_ID, user.getId())
                .withClaim(Constant.USER_ROLE, user.getRole())
                //过期时间
                .withExpiresAt(new Date(System.currentTimeMillis() + Constant.EXPIRE_TIME))
                .sign(algorithm);
        return ApiRestResponse.success(token);


    }
    //注意书写规范
    @PostMapping("/adminLoginWithJwt")
    @ResponseBody
    public ApiRestResponse adminLoginWithJwt(@RequestParam("userName") String userName,
                                      @RequestParam("password")String password) throws ImoocMallException {
        //判断不能为空
        if(StringUtils.isNullOrEmpty(userName)){
            System.out.println(ImoocMallExceptionEnum.NEED_USER_NAME);
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_USER_NAME);
        }
        if(StringUtils.isNullOrEmpty(password)){
            System.out.println(ImoocMallExceptionEnum.NEED_USER_PASSWORD);
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_USER_PASSWORD);
        }
        //校验是不是管理员
        User user = userService.selectLogin(userName, password);
        if (userService.getRole(user)) {
            //保存用户信息时不保存密码
            user.setPassword(null);
            Algorithm algorithm = Algorithm.HMAC256(Constant.JWT_KEY);
            String token = JWT.create().withClaim(Constant.USER_NAME, user.getUsername())
                    .withClaim(Constant.USER_ID, user.getId())
                    .withClaim(Constant.USER_ROLE, user.getRole())
                    //过期时间
                    .withExpiresAt(new Date(System.currentTimeMillis() + Constant.EXPIRE_TIME))
                    .sign(algorithm);
            return ApiRestResponse.success(token);
        }else{
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_ADMIN);
        }
    }

}
