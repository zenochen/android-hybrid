package com.NewCenturyHotels.NewCentury.req;

public class ChargeReq {
    private String rechargeAmount;
    private String rechargePayFrom = "APP";
    private String rechargePayWay;

    public String getRechargeAmount() {
        return rechargeAmount;
    }

    public void setRechargeAmount(String rechargeAmount) {
        this.rechargeAmount = rechargeAmount;
    }

    public String getRechargePayFrom() {
        return rechargePayFrom;
    }

    public void setRechargePayFrom(String rechargePayFrom) {
        this.rechargePayFrom = rechargePayFrom;
    }

    public String getRechargePayWay() {
        return rechargePayWay;
    }

    public void setRechargePayWay(String rechargePayWay) {
        this.rechargePayWay = rechargePayWay;
    }
}
