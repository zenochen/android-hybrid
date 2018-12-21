package com.NewCenturyHotels.NewCentury.bean;

public class TradeList {
    private String arrDate;//入住日期
    private String cardNo;//会员卡号
    private String chineseName;//酒店名称
    private String commentWelfare;//评论福利
    private String connectFlag;//直连标记1直连、0非直连
    private String createDatetime;//创建日期
    private String crsNo;//CRS单号
    private String depDate;//离店日期
    private String guestMobile;//入住人手机
    private String guestName;//入住人
    private String points;//积分
    private String priceTotal;//总价
    private String roomNum;//房间数
    private String roomTypeName;//房型名称
    private String tradeNo;//订单号
    private String tradePayStateEnum;//支付状态
    private String tradePayWayEnum;//支付方式
    private String tradeStateEnum;//订单状态
    private String tradeStateName;//订单状态名称
    private OrderOperates[] orderOperates;//后续功能

    public class OrderOperates{
        private String api;//api地址
        private String desc;//功能描述
        private String name;//功能名称
        private String tradePayWayList;//支付方式列表

        public String getApi() {
            return api;
        }

        public void setApi(String api) {
            this.api = api;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTradePayWayList() {
            return tradePayWayList;
        }

        public void setTradePayWayList(String tradePayWayList) {
            this.tradePayWayList = tradePayWayList;
        }
    }

    public OrderOperates[] getOrderOperates() {
        return orderOperates;
    }

    public void setOrderOperates(OrderOperates[] orderOperates) {
        this.orderOperates = orderOperates;
    }

    public String getArrDate() {
        return arrDate;
    }

    public void setArrDate(String arrDate) {
        this.arrDate = arrDate;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getCommentWelfare() {
        return commentWelfare;
    }

    public void setCommentWelfare(String commentWelfare) {
        this.commentWelfare = commentWelfare;
    }

    public String getConnectFlag() {
        return connectFlag;
    }

    public void setConnectFlag(String connectFlag) {
        this.connectFlag = connectFlag;
    }

    public String getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(String createDatetime) {
        this.createDatetime = createDatetime;
    }

    public String getCrsNo() {
        return crsNo;
    }

    public void setCrsNo(String crsNo) {
        this.crsNo = crsNo;
    }

    public String getDepDate() {
        return depDate;
    }

    public void setDepDate(String depDate) {
        this.depDate = depDate;
    }

    public String getGuestMobile() {
        return guestMobile;
    }

    public void setGuestMobile(String guestMobile) {
        this.guestMobile = guestMobile;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getPriceTotal() {
        return priceTotal;
    }

    public void setPriceTotal(String priceTotal) {
        this.priceTotal = priceTotal;
    }

    public String getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(String roomNum) {
        this.roomNum = roomNum;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getTradePayStateEnum() {
        return tradePayStateEnum;
    }

    public void setTradePayStateEnum(String tradePayStateEnum) {
        this.tradePayStateEnum = tradePayStateEnum;
    }

    public String getTradePayWayEnum() {
        return tradePayWayEnum;
    }

    public void setTradePayWayEnum(String tradePayWayEnum) {
        this.tradePayWayEnum = tradePayWayEnum;
    }

    public String getTradeStateEnum() {
        return tradeStateEnum;
    }

    public void setTradeStateEnum(String tradeStateEnum) {
        this.tradeStateEnum = tradeStateEnum;
    }

    public String getTradeStateName() {
        return tradeStateName;
    }

    public void setTradeStateName(String tradeStateName) {
        this.tradeStateName = tradeStateName;
    }
}
