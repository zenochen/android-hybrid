package com.NewCenturyHotels.NewCentury.cons;

public enum ChannelEnum {
    XIAOMI("小米"),HUAWEI("华为"),GW("官网");

    private String name;

    private ChannelEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
