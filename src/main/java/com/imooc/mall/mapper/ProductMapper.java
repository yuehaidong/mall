package com.imooc.mall.mapper;

import com.imooc.mall.model.pojo.Product;
import com.imooc.mall.model.query.ProductListQuery;
import com.imooc.mall.model.request.AddProductReq;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    Product selectByName(String name);
    Integer insertSelective(Product product);
     int updateByPrimaryKeySelective(Product product);
     Product selectById(Integer id);
     int deleteById(Integer id);
     int batchUpdateSellStatus(@Param("ids") Integer[] ids,@Param("sellStatus") Integer sellStatus);
     List<Product> selectListForAdmin();
     List<Product> selectList(ProductListQuery query);
}
