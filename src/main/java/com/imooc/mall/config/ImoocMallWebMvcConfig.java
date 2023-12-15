package com.imooc.mall.config;

import com.imooc.mall.common.Constant;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class ImoocMallWebMvcConfig implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("file:"+ Constant.
                FILE_UPLOAD_DTR);
        //访问地址->真实地址举例：访问"http://127.0.0.1:8083/images/2022070610482617148.png这个文件，就会去(127.0.0.1，不同的ip不同的机子)这台机子上Constant.
        //                        FILE_UPLOAD_DTR目录下找2022070610482617148.png这个文件,zhe这file必须写
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:"+ Constant.
                        FILE_UPLOAD_DTR);
        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
                .maxAge(3600)
                .allowCredentials(true);
    }

}
