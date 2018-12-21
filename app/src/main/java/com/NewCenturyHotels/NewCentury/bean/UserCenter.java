package com.NewCenturyHotels.NewCentury.bean;

public class UserCenter {

    private MemberInfo memberInfoData;//会员信息
    private NoticeInfo[] noticeManagement;//通知数组
    private String isDataComplete;
    private String voucherCount;//优惠卷数量
    private String headImages;//头像

    public MemberInfo getMemberInfoData() {
        return memberInfoData;
    }

    public void setMemberInfoData(MemberInfo memberInfoData) {
        this.memberInfoData = memberInfoData;
    }

    public NoticeInfo[] getNoticeManagement() {
        return noticeManagement;
    }

    public void setNoticeManagement(NoticeInfo[] noticeManagement) {
        this.noticeManagement = noticeManagement;
    }

    public String getIsDataComplete() {
        return isDataComplete;
    }

    public void setIsDataComplete(String isDataComplete) {
        this.isDataComplete = isDataComplete;
    }

    public String getVoucherCount() {
        return voucherCount;
    }

    public void setVoucherCount(String voucherCount) {
        this.voucherCount = voucherCount;
    }

    public String getHeadImages() {
        return headImages;
    }

    public void setHeadImages(String headImages) {
        this.headImages = headImages;
    }

    public class MemberInfo{
        private String balance;//卡值
        private String birthday;//生日
        private String cardLevel;//卡级别
        private String cardNO;//卡号
        private String cardType;//卡类型
        private String deposit;
        private String enrollDate;
        private String expiryDate;
        private String mobile;//手机
        private String nameCN;//姓名
        private String points;//积分
        private String sex;//性别
        private String cardTypName;//卡类型名称
        private String cardLevelName;//卡级别名称

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getCardLevel() {
            return cardLevel;
        }

        public void setCardLevel(String cardLevel) {
            this.cardLevel = cardLevel;
        }

        public String getCardNO() {
            return cardNO;
        }

        public void setCardNO(String cardNO) {
            this.cardNO = cardNO;
        }

        public String getCardType() {
            return cardType;
        }

        public void setCardType(String cardType) {
            this.cardType = cardType;
        }

        public String getDeposit() {
            return deposit;
        }

        public void setDeposit(String deposit) {
            this.deposit = deposit;
        }

        public String getEnrollDate() {
            return enrollDate;
        }

        public void setEnrollDate(String enrollDate) {
            this.enrollDate = enrollDate;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(String expiryDate) {
            this.expiryDate = expiryDate;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getNameCN() {
            return nameCN;
        }

        public void setNameCN(String nameCN) {
            this.nameCN = nameCN;
        }

        public String getPoints() {
            return points;
        }

        public void setPoints(String points) {
            this.points = points;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public String getCardTypName() {
            return cardTypName;
        }

        public void setCardTypName(String cardTypName) {
            this.cardTypName = cardTypName;
        }

        public String getCardLevelName() {
            return cardLevelName;
        }

        public void setCardLevelName(String cardLevelName) {
            this.cardLevelName = cardLevelName;
        }
    }

    public class NoticeInfo{
        private String code;
        private String content;//通知内容
        private Integer id;
        private String name;//通知名称
        private String url;//链接

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
