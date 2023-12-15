package com.imooc.mall.service.Impl;

import com.imooc.mall.model.pojo.User;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.mapper.UserMapper;
import com.imooc.mall.service.UserService;
import com.imooc.mall.util.MD5Utils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Override
    public User getUser(Integer id) {
        return userMapper.getUser(id);
    }

    @Override
    public void register(String userName, String password,String emailAddress) throws ImoocMallException, NoSuchAlgorithmException {
        //查询用户名是否存在
        User result = userMapper.selectUser(userName);
        if(result!=null){
            //运行时异常不需要进行throws处理
            throw new ImoocMallException(ImoocMallExceptionEnum.NAME_EXISTED);
        }
        //写到数据库
        User user=new User();
        user.setUsername(userName);
        user.setPassword(MD5Utils.getMD5Str(password));
        user.setEmailAddress(emailAddress);
        int count = userMapper.insertUser(user);
        if(count==0){
            throw new ImoocMallException(ImoocMallExceptionEnum.INSERT_FAILED);
        }
    }

    @Override
    public User selectLogin(String username, String password) throws ImoocMallException {
        try {
            password=MD5Utils.getMD5Str(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Map map=new HashMap<>();
        map.put("username",username);
        map.put("password",password);
        User user = userMapper.selectLogin(map);
        if(user==null){
            throw new ImoocMallException(ImoocMallExceptionEnum.WRONG_PASSWORD);
        }
        return user;

    }

    @Override
    public void updateInformation(User user) throws ImoocMallException {
        //更新个性签名
        int updateCount=userMapper.updateByPrimaryKeySelective(user);
        if(updateCount>1){
            throw new ImoocMallException(ImoocMallExceptionEnum.UPDATE_FAILED);
        }
    }

    @Override
    public boolean getRole(User user) throws ImoocMallException {
        return user.getRole().equals(2);
    }
    @Override
    public boolean checkEmailRegistered(String emailAddress){
        User user = userMapper.selectOneByEmail(emailAddress);
        if(user!=null){
            return false;
        }
        return true;
    }
}
