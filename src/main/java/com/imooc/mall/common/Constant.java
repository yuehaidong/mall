package com.imooc.mall.common;

import com.google.common.collect.Sets;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

//因为给静态变量赋值，需要把当前类交给spring
@Component
public class Constant {
    public static final String IMOOC_MALL_USER="imooc_mall_user";
    public static final String salt="sashhfb[?sda]";
    public static final String EMAIL_FROM="1451053372@qq.com";
    public static final String EMAIL_SUBJECT="您的验证码";
    public static final String WATER_MARK_JPG="watermark.jpg";
    public static final Integer IMAGE_SIZE=400;
    public static final Float MAGE_OPACITY=0.5f;


    public static String FILE_UPLOAD_DTR;
    @Value("${file.upload.dir}")
    //@Value配置静态变量，需要写在set上
    public void setFileUploadDtr(String fileUploadDtr){

        FILE_UPLOAD_DTR=fileUploadDtr;
    }
    public  interface ProductListOrder{
        //Sets.newHashSet给静态Set赋值
        Set<String> PRICE_ASC_Desc= Sets.newHashSet("price desc","price asc");

    }
    public interface SaleStatus{
        int NOT_SALE=0;//商品下架状态
        int SALE=1;//商品上架状态
    }
    public interface Cart{
        int UN_CHECKED=0;//购物车未选中状态
        int CHECKED=1;//购物车选中状态
    }

    public enum OrderStatuEnum{
        //枚举顺序必须一致，先写code，那么第一个参数就是code
        CANCELED(0,"用户已取消"),
        NOT_PAID(10,"未付款"),
        PAID(20,"已付款"),
        DELIVERED(30,"已发货"),
        FINISHED(40,"交易完成");


        private int code;
        private String value;

        OrderStatuEnum(int code,String value) {
            this.value = value;
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
        public static OrderStatuEnum codeOf(int code){
//            /values()返回枚举列表
            for (OrderStatuEnum orderStatuEnum:values()){
                if(orderStatuEnum.getCode()==code){
                    return orderStatuEnum;
                }
            }
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ENUM);
        }
    }
    public static final String JWT_KEY="imooc_mall";
    public static final String JWT_TOKEN="jwt_token";
    public static final String USER_ID="user_id";
    public static final String USER_NAME="user_name";
    public static final String USER_ROLE="user_role";
    public static final Long EXPIRE_TIME=60*1000*60*24*1000L;//单位是ms

}

