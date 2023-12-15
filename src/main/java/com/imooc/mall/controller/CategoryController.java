package com.imooc.mall.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.mall.common.ApiRestResponse;
import com.imooc.mall.common.Constant;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.filter.UserFilter;
import com.imooc.mall.model.VO.CategoryVO;
import com.imooc.mall.model.pojo.Category;
import com.imooc.mall.model.pojo.User;
import com.imooc.mall.model.request.AddCategoryReq;
import com.imooc.mall.model.request.UpdateCategoryReq;
import com.imooc.mall.service.CategoryService;
import com.imooc.mall.service.UserService;
import io.swagger.models.auth.In;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.cert.X509Certificate;
import java.util.List;

@Controller
public class CategoryController {
    @Resource
    private UserService userService;
    @Resource
    private CategoryService categoryService;
    @ApiIgnore("后台添加目录")
    @PostMapping("/admin/category/add")
    @ResponseBody
    //@RequestBody如果参属是json格式，需要用该注解
    public ApiRestResponse addCategory(HttpSession session,
                                       @Valid @RequestBody AddCategoryReq categoryReq) throws ImoocMallException {
        //查看用户是否登录
       // User currentUser = (User)session.getAttribute(Constant.IMOOC_MALL_USER);
        User currentUser = UserFilter.currentUser;
        if(currentUser==null){
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_LOGIN);
        }
        //查看是否是管理员
        boolean adminRole= userService.getRole(currentUser);
        if(adminRole){
            //是管理员执行程序
            categoryService.add(categoryReq);
            return ApiRestResponse.success();
        }else{
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_ADMIN);
        }
    }
    @PostMapping("/admin/category/update")
    @ResponseBody
    //@RequestBody如果参属是json格式，需要用该注解
    public ApiRestResponse updateCategory(HttpSession session,@Valid @RequestBody UpdateCategoryReq updateCategoryReq) throws ImoocMallException {
        //查看用户是否登录
        //User currentUser = (User)session.getAttribute(Constant.IMOOC_MALL_USER);
        User currentUser = UserFilter.currentUser;
        if(currentUser==null){
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_LOGIN);
        }
        //查看是否是管理员
        boolean adminRole= userService.getRole(currentUser);
        if(adminRole){
            //是管理员执行程序
            Category category=new Category();
            BeanUtils.copyProperties(updateCategoryReq,category);
            categoryService.update(category);
            return ApiRestResponse.success();
        }else{
            return ApiRestResponse.error(ImoocMallExceptionEnum.NEED_ADMIN);
        }
    }
    @ApiIgnore("后台删除目录")
    @PostMapping("admin/category/delete")
    @ResponseBody
    public ApiRestResponse deletCategory(Integer id){
        categoryService.delete(id);
        return ApiRestResponse.success();
    }

    @ApiIgnore("后台目录列表")
    @PostMapping("admin/category/list")
    @ResponseBody
    public ApiRestResponse listCategoryFormAdin(Integer pageNum,Integer pageSize){
        PageInfo pageInfo = categoryService.listFormAdmin(pageNum, pageSize);
        return ApiRestResponse.success(pageInfo);
    }
    @ApiIgnore("前台目录列表")
    @PostMapping("category/list")
    @ResponseBody
    public ApiRestResponse listCategoryForCustomer(){
        List<CategoryVO> categoryVOS = categoryService.listCategoryForCustomer(0);
        return ApiRestResponse.success(categoryVOS);
    }

}
