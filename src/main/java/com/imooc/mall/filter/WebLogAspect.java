package com.imooc.mall.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect//声明该类为一个切面类
@Component
public class WebLogAspect {
    private  final Logger log=LoggerFactory.getLogger(WebLogAspect.class);
    //在该切点范围内使用通知
    @Pointcut("execution(public  * com.imooc.mall.controller.*.*(..))")
    public void webLog(){

    }
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint){
        //收到请求，纪录请求内容
        ServletRequestAttributes servletRequestAttributes=(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        log.info("URL:"+request.getRequestURI().toString());
        log.info("HTTP_METHOD:"+request.getMethod());
        log.info("IP:"+request.getRemoteAddr());
        log.info("CLASS_METHOD:"+joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName());
        log.info("ARGS:"+ Arrays.toString(joinPoint.getArgs()));
    }
    @AfterReturning(returning = "res",pointcut = "webLog()")
    public void doAfterReturning(Object res) throws JsonProcessingException {
        //处理完请求，返回内容,new ObjectMapper().writeValueAsString(res)将对象转为json
        log.info("RESPONSE:"+new ObjectMapper().writeValueAsString(res));
    }
}
