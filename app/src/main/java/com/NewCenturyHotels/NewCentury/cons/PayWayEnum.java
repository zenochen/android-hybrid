package com.NewCenturyHotels.NewCentury.cons;

public enum PayWayEnum {
    ALIPAY("支付宝"),WEIXIN("微信支付"),UNIONPAY("银联支付"),CARD("卡值支付");

    private String name;

    PayWayEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
