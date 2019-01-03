package com.NewCenturyHotels.NewCentury.cons;

/**
 * 网络请求URL及常量参数
 */
public class Const {

    public static final String ROOT_SERVER = "http://api-center.kaiyuanhotels.com";
    public static final String _ROOT_SERVER = "http://test-server-api-gateway.shands.cn";
    //用户密码登录
    public static final String SIGN_IN = ROOT_SERVER + "/gw-rest/api/member/login";
    //验证码登录
    public static final String SIGN_BY_CODE = ROOT_SERVER + "/gw-rest/api/member/mobileLogin";
    //注册
    public static final String SIGN_UP = ROOT_SERVER + "/gw-rest/api/member/memberRegister";
    //忘记密码
    public static final String MODIFY_PWD = ROOT_SERVER + "/gw-rest/api/member/memberModifyPwd";
    //发送手机验证码
    public static final String SEND_MOBILE_CHECK = ROOT_SERVER + "/gw-rest/api/message/sendMobileCheckCode";
    //发送邮箱验证码
    public static final String SEND_EMAIL_CHECK = ROOT_SERVER + "/gw-rest/api/message/sendEmailCheckCode";
    //发送登录的手机验证码
    public static final String SEND_LOGIN_MOBILE_CHECK = ROOT_SERVER + "/gw-rest/api/message/sendLoginMobileCheckCode";
    //发送登录的邮箱验证码
    public static final String SEND_LOGIN_EMAIL_CHECK = ROOT_SERVER + "/gw-rest/api/message/sendLoginEmailCheckCode";
    //未登录校验验证码
    public static final String VERIFY_CHECK_CODE = ROOT_SERVER + "/gw-rest/api/message/verifyCheckCode";
    //已登录校验验证码
    public static final String VERIFY_CHECK_CODE_LOGINED = ROOT_SERVER + "/gw-rest/api/message/verifyLoginCheckCode";
    //校验是否是会员
    public static final String CHECK_MEMBER = ROOT_SERVER + "/gw-rest/api/member/checkMember";
    //修改手机/邮箱
    public static final String CHANGE_MOBIL_EMAIL = ROOT_SERVER + "/gw-rest/api/vipMember/changeMobileOrEmail";
    //个人基本信息
    public static final String VIP_MEMBER = ROOT_SERVER + "/gw-rest/api/vipMember/toVipMemberCenter";
    //会员卡列表
    public static final String MEMEBER_CARDS = ROOT_SERVER + "/gw-rest/api/member/memberCardList";
    //切换卡号
    public static final String CHANGE_CARD = ROOT_SERVER + "/gw-rest/api/member/changeCardNo";
    //消息列表
    public static final String MSG_LIST = ROOT_SERVER + "/gw-rest/api/message/messageList";
    //消息详情
    public static final String MSG_DETAIL = ROOT_SERVER + "/gw-rest/api/message/messageDetail";
    //我的订单
    public static final String TRADE_LIST = ROOT_SERVER + "/gw-rest/api/trade/list";
    //编辑基本信息
    public static final String UPDATE_USER_INFO = ROOT_SERVER + "/gw-rest/api/vipMember/changeUserInfo";
    //个人中心
    public static final String USER_CENTER = ROOT_SERVER + "/gw-rest/api/index/memberInfo";
    //字典按组查询
    public static final String DICT_LIST = ROOT_SERVER + "/gw-rest/api/common/dictItemList";
    //应用初始化-获取版本号
    public static final String APP_VERSION = ROOT_SERVER + "/gw-rest/api/version/getVersion";
    //首页图标
    public static final String HOME_PAGE_ICON = ROOT_SERVER +  "/gw-rest/api/homePage/findHomePageList";
    //订单支付
    public static final String ORDER_PAY = ROOT_SERVER +  "/gw-rest/api/trade/pay";
    //订单详情
    public static final String TRADE_DETAIL = ROOT_SERVER +  "/gw-rest/api/trade/detail";
    //取消订单
    public static final String TRADE_CANCEL = ROOT_SERVER +  "/gw-rest/api/trade/cancel";
    //删除订单
    public static final String TRADE_DEL = ROOT_SERVER +  "/gw-rest/api/trade/del";
    //创建卡值支付订单
    public static final String CHARGE_TRADE = ROOT_SERVER + "/gw-rest/api/rechargeLog/buildReChargeTrade";
    //获取版本号的参数
    public static final String SOURCE_TYPE = "ANDROID";
    //手机参数
    public static final String MESSAGE_CONTENT_TYPE_PHONE = "SMS_1";
    //邮箱参数
    public static final String MESSAGE_CONTENT_TYPE_EMAIL = "EMAIL_EDIT_EMAIL";

    //-----------------HTML5跳转链接--------------------------
    public static final String APP_ROOT = "http://app.kaiyuanhotels.com";
    public static final String _APP_ROOT = "http://test-app-xgw.shands.cn";
    //地址拼接
    public static final String FREEMARK_PATH = "http://api-center.kaiyuanhotels.com/gw-freemarket/api/free/redirectUrl?";//test-server-api-gateway.shands.cn
    //中间页
    public static final String MIDDLE = "/middle";
    //搜索酒店
    public static final String SEARCH_HOTEL = "/searchhome";
    //种草
    public static final String SOCIAL_PLANT = "/community";
    //会员特权
    public static final String MEMBER = "/member";
    //我的余额
    public static final String MY_MONEY = "/money";
    //我的积分
    public static final String MY_POINTS = "/pointsdetail";
    //我的优惠券
    public static final String MY_COUPONS = "/coupons";
    //每日签到
    public static final String DAILY_SIGNIN = "/points";
    //积分抽奖
    public static final String LUCKY_DRAW = "/luckyDraw";
    //领取礼包
    public static final String GIFT_RECEIVE = "/giftReceive";
    //我的订单
    public static final String MY_ORDER = "/order";
    //我的评论
    public static final String MY_COMMENT = "/personageComment";
    //我的收藏
    public static final String MY_COLLECTION = "/collection";
    //我的消费
    public static final String MY_CONSUME = "/conlist";
    //积分商城
    public static final String POINT_STORE = "http://store.kaiyuanhotels.com/home";
    //联名卡
    public static final String UNION_CARD = "http://static.shands.cn/gw/activity/member/home.html";
    //礼品卡
    public static final String GIFT_CARD = "http://www.kaiyuanhotels.com.cn/giftCard/index.htm";
    //常客信息
    public static final String PASSAGE_INFO = "/user";
    //我的发票
    public static final String MY_INVOICE = "/invoice";
    //关于我们
    public static final String ABOUT_US = "/aboutky";
    //商祺会章程
    public static final String CLUB_RULES = "/kyrule";
    //订单详情
    public static final String ORDER_DETAIL = "/orderdetail?id=";
    //支付
    public static final String PAY = "/pay?id=";
    //评论
    public static final String PUBLISH_COMMENT = "/publishComment?id=";
    //查看评论
    public static final String SHOW_COMMENT = "/personageComment";

}