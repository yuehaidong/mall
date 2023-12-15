package com.imooc.mall.mapper;

import com.imooc.mall.model.VO.OrderStatisticsVO;
import com.imooc.mall.model.pojo.Order;
import com.imooc.mall.model.query.OrderStatisticsQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    void insertOrder(Order order);
    Order selectByOrderNo(String orderNo);
    List<Order> selectForCustomer(Integer userId);
    void updateByOrderNo(Order order);
    List<Order> selectForAdmin();
    List<OrderStatisticsVO> selectOrderStatistics(@Param("query")OrderStatisticsQuery orderStatisticsQuery);

}
