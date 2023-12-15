package com.imooc.mall.config;


import com.imooc.mall.filter.UserFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class UserFilterConfig {
    //定义Filter
    @Bean
    public UserFilter userFilter(){

        return new UserFilter();
    }
    @Bean(name="userFilterConf")
    //将filter放到整个链路中去
    public FilterRegistrationBean userFilterConfig(){
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(userFilter());
        filterFilterRegistrationBean.addUrlPatterns("/order/*");
        filterFilterRegistrationBean.addUrlPatterns("/cart/*");
        filterFilterRegistrationBean.addUrlPatterns("/user/update");
        filterFilterRegistrationBean.setName("userFilterConfig");
        return filterFilterRegistrationBean;

    }}
