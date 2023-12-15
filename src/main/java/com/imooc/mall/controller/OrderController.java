package com.imooc.mall.controller;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.common.ApiRestResponse;
import com.imooc.mall.model.VO.OrderVO;
import com.imooc.mall.model.request.CreateOrderReq;
import com.imooc.mall.service.OrderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
public class OrderController {
    @Resource
    OrderService orderService;
    @PostMapping("order/create")
    @ApiOperation("创建订单")
    public ApiRestResponse create(@Valid @RequestBody CreateOrderReq createOrder){
        System.out.println(createOrder);
        String orderNo = orderService.create(createOrder);
        return ApiRestResponse.success(orderNo);
    }
    @GetMapping("order/detail")
    @ApiOperation("前台订单详情")
    public ApiRestResponse detail(String orderNo){

        OrderVO detail = orderService.detail(orderNo);
        return ApiRestResponse.success(detail);
    }
    @GetMapping("order/list")
    @ApiOperation("前台列表")
    public ApiRestResponse list(Integer pageNum,Integer pageSize){
        PageInfo pageInfo = orderService.listForCustpmer(pageNum, pageSize);
        return ApiRestResponse.success(pageInfo);

    }
    @PostMapping("order/cancel")
    @ApiOperation("前台取消订单")
    public ApiRestResponse cancel(String orderNo){
        orderService.cancel(orderNo);
        return ApiRestResponse.success();
    }
    @GetMapping("order/qrcode")
    @ApiOperation("生成支付二维码")
    public ApiRestResponse qrcode(String orderNo){
        String qrcode = orderService.qrcode(orderNo);
        return ApiRestResponse.success(qrcode);
    }
    @GetMapping("order/pay")
    @ApiOperation("支付接口")
    public ApiRestResponse pay(String orderNo){
       orderService.pay(orderNo);
        return ApiRestResponse.success();
    }


}
