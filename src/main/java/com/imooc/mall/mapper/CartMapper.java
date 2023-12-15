package com.imooc.mall.mapper;

import com.imooc.mall.model.VO.CartVO;
import com.imooc.mall.model.pojo.Cart;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    //多参数可以不用注解但是必须要保证传参顺序和sql语句顺序一样，否则会出错
    Cart selectByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
    int insertSlective(Cart cart);
    int updateById(Cart cart);
    List<CartVO> selectList(@Param("userId") Integer userId);
    void deleteById(Integer cartId);
    Integer selectOrNot(@Param("userId") Integer userId,
                        @Param("productId") Integer productId,
                        @Param("selected") Integer selected);

}
