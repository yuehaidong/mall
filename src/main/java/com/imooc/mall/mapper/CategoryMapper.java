package com.imooc.mall.mapper;

import com.imooc.mall.model.VO.CategoryVO;
import com.imooc.mall.model.pojo.Category;

import java.util.List;

public interface CategoryMapper {
    public Category selectByName(String name);
    public int insertSelective(Category category);
    public int updateByPrimarySelective(Category updateCategory);
    public Category selectById(Integer id);
    public int deleteById(Integer id);
    List<Category> selectList();
    List<Category> slectCategoriedsByParentId(Integer parentId);
}
