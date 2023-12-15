package com.imooc.mall.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.mapper.CategoryMapper;
import com.imooc.mall.model.VO.CategoryVO;
import com.imooc.mall.model.pojo.Category;
import com.imooc.mall.model.request.AddCategoryReq;
import com.imooc.mall.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Resource
    private CategoryMapper categoryMapper;
    @Override
    public void add(AddCategoryReq addCategoryReq) throws ImoocMallException {
        Category category=new Category();
        //把addCategoryReq中同类型同名字的属性拷贝到category
        BeanUtils.copyProperties(addCategoryReq,category);
        Category categoryOld = categoryMapper.selectByName(addCategoryReq.getName());
        if(categoryOld!=null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NAME_EXISTED);
        }
        int i = categoryMapper.insertSelective(category);
        if(i==0){
            throw new ImoocMallException(ImoocMallExceptionEnum.CREATE_FAILED);
        }
    }
    @Override
    public void update(Category updateCategory){
        //查找有没有这个名字
        Category categoryOld = categoryMapper.selectByName(updateCategory.getName());
        if(categoryOld != null && !categoryOld.getId().equals(updateCategory.getId())){
            throw new ImoocMallException(ImoocMallExceptionEnum.NAME_EXISTED);
        }
        int count = categoryMapper.updateByPrimarySelective(updateCategory);
        if(count==0){
            throw new ImoocMallException(ImoocMallExceptionEnum.UPDATE_FAILED);
        }
    }
    @Override
    public void delete(Integer id){
        Category categoryOld=categoryMapper.selectById(id);
        //查不到记录，删除失败
        if(categoryOld==null){
            throw new ImoocMallException(ImoocMallExceptionEnum.DELETE_FAILED);
        }
        int count=categoryMapper.deleteById(id);
        if(count==0){
            throw new ImoocMallException(ImoocMallExceptionEnum.DELETE_FAILED);
        }
    }
    @Override
    public PageInfo listFormAdmin(Integer pageNum, Integer pageSize){
        //排序规则，先用type排序，在相同type下对order_num排序
        PageHelper.startPage(pageNum,pageSize,"type,order_num");
        List<Category> categories = categoryMapper.selectList();
        PageInfo<Category> categoryPageInfo = new PageInfo<>(categories);
        return categoryPageInfo;
    }
    @Override
    /**
     * cacheNames/value,指定缓存组的名字
     * key:缓存数据使用的key；可以用它来指定，默认使用参数值，缓存内容是方法返回值例如key为1缓存内容是1-categoryVOList
     * key支持spel，#parentId，将parentId的值传进去，#result可以是方法返回值结果
     * keyGenerator()：key的生成器，可以自己制定key的生成器的组件id
     * key和keyGenerator二选一
     * cacheManager()：指定缓存管理器，或者cacheResolver指定获取解析器，二选一
     * condition()：指定符合条件的情况下，才缓存内容
     * condition ="#parentId>0":parentId>0时才将内容进行缓存
     * unless()：否定缓存，当unless指定条件为true时，方法返回值就不会被缓存
     * unless="#result==null"
     * sync()是否进行异步
     *  @CachePut(value = "listCategoryForCustomer",key = "#parentId")即带哦用方法，也跟新数据，如果在使用 @Cacheable
     *  有关系的话，需要将两个value和key一样
     *  @CacheEvict(value = "listCategoryForCustomer",key = "#parentId"，allEntries=true):缓存清除，当执行删除操作使用,allEntries=true
     *  删除所有缓存
     *  @Caching()复杂的缓存规则
     *  @CacheConfig:放在类上，指定所有的value
     *  默认是用的是ConcurrentCacheMapCache;将数据保存在ConcurrentMap
     *  开发中使用缓存中间件，redis
     *
     * */


    @Cacheable(value = "listCategoryForCustomer",key = "#parentIdy",condition ="#parentId>0")
    public List<CategoryVO> listCategoryForCustomer(Integer parentId){
        List<CategoryVO> categoryVOList = new ArrayList<>();
        recursivelyFindCategories(categoryVOList,parentId);
        return categoryVOList;
    }
    private void recursivelyFindCategories(List<CategoryVO> categoryVOList,Integer parentId){
        //递归获取所有子类别并组合成为一个“目录树”
        List<Category> categories = categoryMapper.slectCategoriedsByParentId(parentId);
        if(!CollectionUtils.isEmpty(categories)){
            for (Category c:categories
                 ) {
                CategoryVO categoryVO=new CategoryVO();
                BeanUtils.copyProperties(c,categoryVO);
                categoryVOList.add(categoryVO);
                recursivelyFindCategories(categoryVO.getChildCategory(),categoryVO.getId());

            }
        }

    }
}
