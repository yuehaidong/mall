package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.model.VO.CategoryVO;
import com.imooc.mall.model.pojo.Category;
import com.imooc.mall.model.request.AddCategoryReq;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface CategoryService {
    void add(AddCategoryReq addCategoryReq) throws ImoocMallException;

    void update(Category updateCategory);

    void delete(Integer id);

    PageInfo listFormAdmin(Integer pageNum, Integer pageSize);

    @Cacheable(value = "listCategoryForCustomer")
    List<CategoryVO> listCategoryForCustomer(Integer parentId);
}
