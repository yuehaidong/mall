package com.imooc.mall.exception;

public enum ImoocMallExceptionEnum {
    //枚举实例必须写在第一行
    NEED_USER_NAME(10001,"用户名不能为空"),
    NEED_USER_PASSWORD(10002,"密码不能为空"),
    PASSWORD_TOO_SHORT(10003,"密码不能少于8位"),
    NAME_EXISTED(10004,"不允许重名"),
    INSERT_FAILED(10005,"插入失败，请重试"),
    WRONG_PASSWORD(10006,"密码错误"),
    NEED_LOGIN(10007,"用户未登录"),
    UPDATE_FAILED(10008,"更新失败"),
    NEED_ADMIN(10009,"没有管理员权限"),
    NAME_NOT_NULL(10010,"参数不能为空"),
    CREATE_FAILED(10011,"新增失败"),
    REQUEST_PARAM_ERROR(10012,"参数错误"),
    DELETE_FAILED(10013,"删除失败"),
    MKDIR_FAILED(10014,"文件夹创建失败"),
    UPLOAD_FAILED(10015,"图片上传失败"),
    NOT_SALE(10016,"商品状态不可售"),
    NOT_ENOUGH(10017,"商品库存不足"),
    CART_EMPTY(10018,"购物车已勾选商品为空"),
    NO_ENUM(10019,"未找到对应枚举类"),
    NO_ORDER(10020,"订单不存在"),
    NOT_YOUR_ORDER(10021,"订单不属于你"),
    WRONG_ORDER_STATUS(10022,"订单状态不符"),
    WRONG_EMAIL(10023,"非法的邮件地址"),
    EMAIL_ALREADY_BEEN_REGISTERED(10024,"email地址已经被注册"),
    EMAIL_ALREADY_BEEN_SEND(10025,"email已经发送，若无法收到，请稍后再试"),
    NEED_EMAIL_ADDRESS(10026,"邮箱不能为空"),
    NEED_VERIFICATIONCODE(10027,"验证码不能为空"),
    WRONG_VERIFICATIONCODE(10028,"验证码错误"),
    TOKEN_EXPIRED(10029,"Token过期"),
    TOKEN_WRONG(10030,"Token解析失败"),
    CANCEL_WRONG_ORDER_STATUS(10031,"订单状态有误，付款后暂不支持取消订单"),
    PAY_WRONG_ORDER_STATUS(10032,"订单状态有误，仅能在未付款时付款"),
    DELIVER_WRONG_ORDER_STATUS(10033,"订单状态有误，仅能在付款后发货"),
    FINISH_WRONG_ORDER_STATUS(10034,"订单状态有误，仅能在发货后完单"),
    SYSTEM_ERROR(20000,"系统异常");

    //异常码
    Integer code;
    //异常信息
    String msg;


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }


    //必须写构造器
    ImoocMallExceptionEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
