package com.imooc.mall.config;

import com.imooc.mall.filter.AdminFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class AdminFilterConfig {
    //定义Filter
    @Bean
    public AdminFilter adminFilter(){

        return new AdminFilter();
    }
    @Bean(name="adminFilterConf")
    //将filter放到整个链路中去
    public FilterRegistrationBean adminFilterConfig(){
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(adminFilter());
        filterFilterRegistrationBean.addUrlPatterns("/admin/category/*");
        filterFilterRegistrationBean.addUrlPatterns("/admin/product/*");
//        filterFilterRegistrationBean.addUrlPatterns("/admin/order/*");
        filterFilterRegistrationBean.setName("adminFilterConfig");
        return filterFilterRegistrationBean;

    }}
