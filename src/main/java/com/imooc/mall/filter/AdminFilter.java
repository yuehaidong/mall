package com.imooc.mall.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.imooc.mall.common.ApiRestResponse;
import com.imooc.mall.common.Constant;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.model.pojo.User;
import com.imooc.mall.service.UserService;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
//这边没写bean的配置是因为在adminFiltrConfig中声明它是一个bean
public class AdminFilter implements Filter {
    @Resource
    private UserService userService;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
//        HttpSession session = request.getSession();
        if("OPTIONS".equals(request.getMethod())){
            filterChain.doFilter(servletRequest,servletResponse);
        }else {
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
            //查看是否是管理员
            Algorithm algorithm = Algorithm.HMAC256(Constant.JWT_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            //
            filterChain.doFilter(servletRequest, servletResponse);
            try {
                DecodedJWT jwt = verifier.verify(token);
                UserFilter.currentUser = new User();
                UserFilter.currentUser.setId(jwt.getClaim(Constant.USER_ID).asInt());
                UserFilter.currentUser.setRole(jwt.getClaim(Constant.USER_ROLE).asInt());
                UserFilter.currentUser.setUsername(jwt.getClaim(Constant.USER_NAME).asString());
            } catch (TokenExpiredException e) {
                //token过期抛出异常
                throw new ImoocMallException(ImoocMallExceptionEnum.TOKEN_EXPIRED);
            } catch (JWTDecodeException e) {
                //解码失败
                throw new ImoocMallException(ImoocMallExceptionEnum.TOKEN_WRONG);
            }
            boolean adminRole = userService.getRole(UserFilter.currentUser);
            if (adminRole) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                PrintWriter writer = new HttpServletResponseWrapper((HttpServletResponse) servletResponse).getWriter();
                writer.write("{\n" +
                        "    \"status\": 10009,\n" +
                        "    \"msg\": \"NEED_ADMIN\",\n" +
                        "    \"data\": null\n" +
                        "}");
                writer.flush();
                writer.close();

            }
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
