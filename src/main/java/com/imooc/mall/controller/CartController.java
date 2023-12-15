package com.imooc.mall.controller;

import com.imooc.mall.common.ApiRestResponse;
import com.imooc.mall.filter.UserFilter;
import com.imooc.mall.model.VO.CartVO;
import com.imooc.mall.service.CartService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Resource
    CartService cartService;
    @PostMapping("/add")
    @ApiOperation("添加商品到购物车去")
    @ResponseBody
    public ApiRestResponse add(Integer productId,Integer count){
        Integer id = UserFilter.currentUser.getId();
        List<CartVO> add = cartService.add(id, productId, count);
        return ApiRestResponse.success(add);
    }
    @GetMapping("/list")
    @ApiOperation("购物车列表")
    @ResponseBody
    public ApiRestResponse list(){
        //内部获取userId，防止横向越权（）
        List<CartVO> cartVOList = cartService.list(UserFilter.currentUser.getId());
        return ApiRestResponse.success(cartVOList);
    }
    @PostMapping("/update")
    @ApiOperation("更新购物车")
    @ResponseBody
    public ApiRestResponse update(Integer productId,Integer count){
        //内部获取userId，防止横向越权（）
        List<CartVO> cartVOList = cartService.update(UserFilter.currentUser.getId(),productId,count);
        return ApiRestResponse.success(cartVOList);
    }
    @PostMapping("/delete")
    @ApiOperation("删除购物车")
    @ResponseBody
    public ApiRestResponse delete(Integer productId){
        //不能传入userId、cartId,否则可以删除别人购物车
        List<CartVO> cartVOList = cartService.delete(UserFilter.currentUser.getId(),productId);
        return ApiRestResponse.success(cartVOList);
    }
    @PostMapping("/select")
    @ApiOperation("选择/不选择购物车的某商品")
    @ResponseBody
    public ApiRestResponse select(Integer productId,Integer selected){
        //不能传入userId、cartId,否则可以删除别人购物车
        List<CartVO> cartVOList = cartService.selectOrNot(UserFilter.currentUser.getId(),productId,selected);
        return ApiRestResponse.success(cartVOList);
    }
    @PostMapping("/selectAll")
    @ApiOperation("全选/全不选购物车的某商品")
    @ResponseBody
    public ApiRestResponse selectAll(Integer selected){
        //不能传入userId、cartId,否则可以删除别人购物车
        List<CartVO> cartVOList = cartService.selectAllOrNot(UserFilter.currentUser.getId(),selected);
        return ApiRestResponse.success(cartVOList);
    }


}
