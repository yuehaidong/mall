package com.imooc.mall.exception;

import com.imooc.mall.common.ApiRestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice//拦截异常
public class GlobalExceptionHandler {
    private final Logger log= LoggerFactory.getLogger
            (GlobalExceptionHandler.class);
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public  Object handleException(Exception e){
        log.error("Default Exception",e);
        return ApiRestResponse.error(ImoocMallExceptionEnum.SYSTEM_ERROR);
    }
    @ExceptionHandler(ImoocMallException.class)
    @ResponseBody
    public  Object handleImoocMallException(ImoocMallException e){
        log.error("ImoocMallException",e);
        return ApiRestResponse.error(e.getCode(),e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)//参数校验错误
    @ResponseBody
    public ApiRestResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error("MethodArgumentNotValidException",e);
        return handleBindingResult(e.getBindingResult());
    }
    private ApiRestResponse handleBindingResult(BindingResult result){
        //把异常处理为对外暴露的提示
        List<String> list=new ArrayList<>();
       // result.hasErrors() 判断是否包含错误
       if (result.hasErrors()){
           List<ObjectError> allErrors = result.getAllErrors();
           for (int i = 0; i < allErrors.size(); i++) {
               ObjectError objectError = allErrors.get(i);
               String message = objectError.getDefaultMessage();
               list.add(message);
           }
       }
        if(list.size()==0){
            return ApiRestResponse.error(ImoocMallExceptionEnum.REQUEST_PARAM_ERROR);
        }
        return ApiRestResponse.error(ImoocMallExceptionEnum.REQUEST_PARAM_ERROR.getCode(),list.toString());
    }

}
