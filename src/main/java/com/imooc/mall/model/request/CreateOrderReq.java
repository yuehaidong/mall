package com.imooc.mall.model.request;

import javax.validation.constraints.NotNull;

public class CreateOrderReq {
    @NotNull(message = "收件人不能为空")
    private String receiverName;
    @NotNull(message = "手机号不能为空")
    private String receiverMobile;

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    @NotNull(message = "地址不能为空")
    private String receiverAddress;
    private Integer postage=0;//包邮
    private Integer paymentType=1;//扫码支付

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile;
    }

    public Integer getPostage() {
        return postage;
    }

    public void setPostage(Integer postage) {
        this.postage = postage;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }
}
