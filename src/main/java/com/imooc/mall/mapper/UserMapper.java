package com.imooc.mall.mapper;

import com.imooc.mall.model.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.Map;


public interface UserMapper {
    public User getUser(Integer id);
    public User selectUser(String username);
    public int insertUser(User user);
    public User selectLogin(Map map);
   public int updateByPrimaryKeySelective(User user);
   User selectOneByEmail(String emailAddress);

}
