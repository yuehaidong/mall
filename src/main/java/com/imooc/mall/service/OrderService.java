package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.model.VO.OrderStatisticsVO;
import com.imooc.mall.model.VO.OrderVO;
import com.imooc.mall.model.request.CreateOrderReq;

import java.util.Date;
import java.util.List;

public interface OrderService {
    String create(CreateOrderReq createOrderReq);

    OrderVO detail(String oderNo);

    PageInfo listForCustpmer(Integer pageNum, Integer pageSize);

    void cancel(String orderNo);

    //生成二维码
    String qrcode(String orderNo);

    //管理员列表
    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    void pay(String orderNo);

    //支付接口
    void deliver(String orderNo);

    //完结订单
    void finish(String orderNo);

    List<OrderStatisticsVO> statistics(Date startDate, Date endDate);
}
