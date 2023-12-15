package com.imooc.mall.service.Impl;

import com.imooc.mall.common.Constant;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.mapper.CartMapper;
import com.imooc.mall.mapper.ProductMapper;
import com.imooc.mall.model.VO.CartVO;
import com.imooc.mall.model.pojo.Cart;
import com.imooc.mall.model.pojo.Product;
import com.imooc.mall.service.CartService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Resource
    ProductMapper productMapper;
    @Resource
    CartMapper cartMapper;
    @Override
    public List<CartVO> list(Integer userId){
        List<CartVO> cartVOS = cartMapper.selectList(userId);
        for (int i = 0; i < cartVOS.size(); i++) {
            CartVO cartVO = cartVOS.get(i);
            cartVO.setTotalPrice(cartVO.getPrice()*cartVO.getQuantity());
        }
        return cartVOS;
    }


    @Override
    //为什么用List，在添加商品时。返回的响应里将添加的一起商品进行返回，减少一次刷新
    public List<CartVO> add(Integer userId, Integer productId, Integer count){
        validProduct(productId,count);
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if(cart==null){
            //这个商品之前不在购物车里，需要新增记录
            cart = new Cart();
            cart.setProductId(productId);
            cart.setUserId(userId);
            cart.setQuantity(count);
            cart.setSelected(Constant.Cart.CHECKED);
            int i = cartMapper.insertSlective(cart);
            if (i==0){
                throw new ImoocMallException(ImoocMallExceptionEnum.INSERT_FAILED);
            }
        }else {
                //这个商品已经在购物车里了，商品数量相加
            count=cart.getQuantity()+count;
            Cart cartNew= new Cart();
            cartNew.setQuantity(count);
            cartNew.setId(cart.getId());
            cartNew.setUserId(cart.getUserId());
            cartNew.setProductId(cart.getProductId());
            cartNew.setSelected(Constant.Cart.CHECKED);
            cartMapper.updateById(cartNew);
        }
        return this.list(userId);


    }
    private void validProduct(Integer productId,Integer count){
        Product product = productMapper.selectById(productId);
        //判断商品是否存在，是否上架
        if(product==null|| product.getStatus().equals(Constant.SaleStatus.NOT_SALE)){
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_SALE);
        }
        //判断商品库存
        if(count>product.getStock()){
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_ENOUGH);
        }
    }
    @Override
    //为什么用List，在添加商品时。返回的响应里将添加的一起商品进行返回，减少一次刷新
    public List<CartVO> update(Integer userId, Integer productId, Integer count){
        validProduct(productId,count);
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if(cart==null){
            //这个商品之前不在购物车里，无法更新
          throw new ImoocMallException(ImoocMallExceptionEnum.UPDATE_FAILED);
        }else {
            //这个商品已经在购物车里了，更新数量
            Cart cartNew= new Cart();
            cartNew.setQuantity(count);
            cartNew.setId(cart.getId());
            cartNew.setUserId(cart.getUserId());
            cartNew.setProductId(cart.getProductId());
            cartNew.setSelected(Constant.Cart.CHECKED);
            cartMapper.updateById(cartNew);
        }
        return this.list(userId);
    }
    @Override
    //为什么用List，在添加商品时。返回的响应里将添加的一起商品进行返回，减少一次刷新
    public List<CartVO> delete(Integer userId, Integer productId){

        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if(cart==null){
            //这个商品之前不在购物车里，无法删除
            throw new ImoocMallException(ImoocMallExceptionEnum.DELETE_FAILED);
        }else {
            //这个商品已经在购物车里了，可以删除
            cartMapper.deleteById(cart.getId());
        }
        return this.list(userId);
    }
    @Override
    public List<CartVO> selectOrNot(Integer userId, Integer productId, Integer selected) {
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);

        if (cart == null) {
            //这个商品之前不在购物车里，无法选中
            throw new ImoocMallException(ImoocMallExceptionEnum.UPDATE_FAILED);
        } else {
            //这个商品已经在购物车里了，可以选中/不选中
            cartMapper.selectOrNot(userId,productId,selected);
        }
        return this.list(userId);
    }
    @Override
    public List<CartVO> selectAllOrNot(Integer userId, Integer selected) {
        //改变选中状态
        cartMapper.selectOrNot(userId,null,selected);
        return this.list(userId);
    }

}
