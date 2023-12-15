package com.imooc.mall.service;

import com.imooc.mall.model.VO.CartVO;

import java.util.List;

public interface CartService {
    List<CartVO> list(Integer userId);

    //为什么用List，在添加商品时。返回的响应里将添加的一起商品进行返回，减少一次刷新
    List<CartVO> add(Integer userId, Integer productId, Integer count);

    //为什么用List，在添加商品时。返回的响应里将添加的一起商品进行返回，减少一次刷新
    List<CartVO> update(Integer userId, Integer productId, Integer count);

    //为什么用List，在添加商品时。返回的响应里将添加的一起商品进行返回，减少一次刷新
    List<CartVO> delete(Integer userId, Integer productId);

    List<CartVO> selectOrNot(Integer userId, Integer productId, Integer selected);

    List<CartVO> selectAllOrNot(Integer userId, Integer selected);
}
