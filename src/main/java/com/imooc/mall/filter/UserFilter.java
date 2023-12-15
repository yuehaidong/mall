package com.imooc.mall.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.imooc.mall.common.Constant;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.model.pojo.User;
import com.imooc.mall.service.UserService;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class UserFilter implements Filter {
    //解决并发
    public static ThreadLocal<User> userThreadLocal=new ThreadLocal<>();
    public static User currentUser=new User();
    @Resource
    private UserService userService;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        if("OPTIONS".equals(request.getMethod())){
            filterChain.doFilter(servletRequest,servletResponse);
        }else {
            //因为jwt通过Header发送jwt文件，所以这里查找有没有JWT_TOKEN
            String token = request.getHeader(Constant.JWT_TOKEN);
            if (token == null) {
                PrintWriter writer = new HttpServletResponseWrapper((HttpServletResponse) servletResponse).getWriter();
                writer.write("{\n" +
                        "    \"status\": 10007,\n" +
                        "    \"msg\": \"NEED_LOGIN\",\n" +
                        "    \"data\": null\n" +
                        "}");
                writer.flush();
                writer.close();
                return;
            }
            //解析token
            Algorithm algorithm = Algorithm.HMAC256(Constant.JWT_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            try {
                DecodedJWT jwt = verifier.verify(token);
                System.out.println(jwt);
                currentUser.setId(jwt.getClaim(Constant.USER_ID).asInt());
                currentUser.setRole(jwt.getClaim(Constant.USER_ROLE).asInt());
                currentUser.setUsername(jwt.getClaim(Constant.USER_NAME).asString());
                filterChain.doFilter(servletRequest, servletResponse);
            } catch (TokenExpiredException e) {
                //token过期抛出异常
                throw new ImoocMallException(ImoocMallExceptionEnum.TOKEN_EXPIRED);
            } catch (JWTDecodeException e) {
                //解码失败
                throw new ImoocMallException(ImoocMallExceptionEnum.TOKEN_WRONG);
            }
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
