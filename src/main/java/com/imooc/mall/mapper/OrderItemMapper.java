package com.imooc.mall.mapper;

import com.imooc.mall.model.pojo.OrderItem;

import java.util.List;

public interface OrderItemMapper {
    void insert(OrderItem orderItem);
    //因为一个orderNo有多件商品，所以需要使用集合
    List<OrderItem> selectByOrderNo(String orderNo);
}
