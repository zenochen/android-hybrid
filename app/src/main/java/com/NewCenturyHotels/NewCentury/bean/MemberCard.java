package com.NewCenturyHotels.NewCentury.bean;

public class MemberCard {
    private String cardLevelCode;//卡类型编码
    private String cardLevelName;//卡类型名称
    private String cardTypeCode;//卡级别编码
    private String cardTypeName;//卡级别名称
    private String cardNo;//会员卡
    private String expiryDate;//截止日期
    private Boolean isSelected = false;

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCardLevelCode() {
        return cardLevelCode;
    }

    public void setCardLevelCode(String cardLevelCode) {
        this.cardLevelCode = cardLevelCode;
    }

    public String getCardLevelName() {
        return cardLevelName;
    }

    public void setCardLevelName(String cardLevelName) {
        this.cardLevelName = cardLevelName;
    }

    public String getCardTypeCode() {
        return cardTypeCode;
    }

    public void setCardTypeCode(String cardTypeCode) {
        this.cardTypeCode = cardTypeCode;
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }
}
