package com.NewCenturyHotels.NewCentury.req;

public class OrderPayReq {
    private String tradeNo;
    private String payWay;
    private String source = "app";

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getPayWay() {
        return payWay;
    }

    public void setPayWay(String payWay) {
        this.payWay = payWay;
    }
}
