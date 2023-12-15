package com.imooc.mall.service;

import com.imooc.mall.model.pojo.User;
import com.imooc.mall.exception.ImoocMallException;

import java.security.NoSuchAlgorithmException;

public interface UserService {
    public User getUser(Integer id);
    public void register(String userName,String password,String emailAddress) throws ImoocMallException, NoSuchAlgorithmException;
    public User selectLogin(String username,String password) throws ImoocMallException;
    public void updateInformation(User user) throws ImoocMallException;
    public boolean getRole(User user) throws ImoocMallException;

    boolean checkEmailRegistered(String emailAddress);
}


