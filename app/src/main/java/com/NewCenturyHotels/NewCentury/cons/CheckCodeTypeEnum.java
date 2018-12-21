package com.NewCenturyHotels.NewCentury.cons;

public enum CheckCodeTypeEnum {
    API_LOGIN_CODE_KEY("登录验证码"),
    API_REGISTER_CODE_KEY("注册验证码"),
    API_UPDATE_PASSWORD_CODE_KEY("修改密码验证码"),
    API_MODIFY_EMAIL_CODE_KEY("修改邮箱验证码"),
    API_MODIFY_MOBILE_CODE_KEY("修改手机号验证码"),
    API_NEW_MODIFY_EMAIL_CODE_KEY("新邮箱验证码"),
    API_NEW_MODIFY_MOBILE_CODE_KEY("新手机号验证码");

    private String name;

    private CheckCodeTypeEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
