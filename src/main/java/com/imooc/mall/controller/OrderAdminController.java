package com.imooc.mall.controller;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.common.ApiRestResponse;
import com.imooc.mall.model.VO.OrderStatisticsVO;
import com.imooc.mall.service.OrderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RestController
public class OrderAdminController {
    @Resource
    OrderService orderService;
    @GetMapping("admin/order/list")
    @ApiOperation("管理员订单")
    public ApiRestResponse listForAdmin(Integer pageNum,Integer pageSize){
        PageInfo pageInfo = orderService.listForAdmin(pageNum,pageSize);
        return ApiRestResponse.success(pageInfo);
    }
    @PostMapping("admin/order/delivered")
    @ApiOperation("管理员发货")
    public ApiRestResponse delivered(String orderNo){
       orderService.deliver(orderNo);
        return ApiRestResponse.success();
    }
    //管理员和用户都可以使用
    @PostMapping("order/finish")
    @ApiOperation("订单完结")
    public ApiRestResponse finish(String orderNo){
        orderService.finish(orderNo);
        return ApiRestResponse.success();
    }
    @GetMapping("admin/order/statistics")
    @ApiOperation("每日订单量统计")
    public ApiRestResponse statistics(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,@DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate){
        List<OrderStatisticsVO> statistics = orderService.statistics(startDate, endDate);
        return ApiRestResponse.success(statistics);
    }
}
