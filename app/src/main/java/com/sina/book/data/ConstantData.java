package com.sina.book.data;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;

import com.sina.book.SinaBookApplication;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.parser.SimpleParser;
import com.sina.book.util.ApplicationUtils;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.Util;

import org.htmlcleaner.Utils;

import java.util.Locale;

/**
 * 各种常量.
 */
public class ConstantData {
    /**
     * 默认的渠道
     */
    public static final int CHANNEL_SINA = 16;
    /**
     * 第一次：百度全站限时免费(活动时间设定的是2014年3月07日-3月13日)渠道<br>
     * 第二次：百度全站限时免费(活动时间设定的是2014年3月15日-3月23日)渠道<br>
     */
    public static final int CHANNEL_BAIDU_LIMITFREE = 66;
    /**
     * 360渠道
     */
    public static final int CHANNEL_360 = 4;
    /**
     * 任京京<br>
     * 1.愚人节活动<br>
     * 2.话费大回馈活动渠道号<br>
     */
    public static final int CHANNEL_FOOLSDAY = 81;
    /**
     * 送券版渠道号
     */
    public static final int CHANNEL_QUAN_GIFT = 50;
    public static final int CHANNEL_QUAN_GIFT_UCBROWSER = 57;                                                                // UC浏览器
    public static final int CHANNEL_QUAN_GIFT_360BROWSER = 58;                                                                // 360浏览器
    public static final int CHANNEL_QUAN_GIFT_XUEERSI = 61;                                                                // 学而思
    public static final int CHANNEL_QUAN_GIFT_SOUGOUBROWSER = 68;                                                                // 搜狗浏览器
    public static final int CHANNEL_QUAN_GIFT_OPERABROWSER = 70;                                                                // 欧朋浏览器
    public static final int CHANNEL_QUAN_GIFT_360 = CHANNEL_360;                                                        // 360

    // 百度Family
    public static final int CHANNEL_BAIDU_FAMILY_91 = 3;
    public static final int CHANNEL_BAIDU_FAMILY_BAIDU = 8;
    public static final int CHANNEL_BAIDU_FAMILY_ANZHUO = 2;

    public static final int ACCESSTOKEN_EXPIRED_CODE1 = 21327;
    public static final int ACCESSTOKEN_EXPIRED_CODE2 = 21332;
    /**
     * // 内容重复/repeat content
     */
    public static final int WEIBO_SDK_ERROR_CODE_20019 = 20019;

    /**
     * 渠道号.
     */
    private static int mAppChannel = -1;
    /**
     * 版本号.
     */
    public static final String VERSION = ApplicationUtils.getVersionName(SinaBookApplication.gContext);

    /**
     * 使用设备Imei号
     */
    public static String IMEI;

    public static String getDeviceId() {
        if (Util.isNullOrEmpty(IMEI)) {
            try {
                TelephonyManager mTelephonyMgr = (TelephonyManager) SinaBookApplication.gContext
                        .getSystemService(Context.TELEPHONY_SERVICE);
                IMEI = mTelephonyMgr.getDeviceId();
            } catch (Exception e) {
                IMEI = "";
            }
        }
        return IMEI;
    }

    /**
     * 提交渠道信息给服务器
     */
    public static void reqChannel() {
        try {
            int channel = getChannelCode(SinaBookApplication.gContext);
            String url = String.format(Locale.CHINA, ConstantData.URL_STATISTICS, channel, VERSION);
            RequestTask reqTask = new RequestTask(new SimpleParser());
            TaskParams params = new TaskParams();
            params.put(RequestTask.PARAM_URL, url);
            params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
            reqTask.execute(params);
        } catch (Exception e) {
            // 这里任何异常都会忽略
        }
    }

    public static boolean isSinaAppChannel(Context context) {
        return getChannelCode(context) == 6 || getChannelCode(context) == 38 || getChannelCode(context) == 51;
    }

    /**
     * 获取渠道号
     *
     * @param context
     * @return
     */
    public static int getChannelCode(Context context) {
        if (-1 != mAppChannel) {
            return mAppChannel;
        }

        try {
            String code = getMetaData(context, "CHANNEL");
            if (!Utils.isEmptyString(code)) {
                return mAppChannel = Integer.parseInt(code);
            }
        } catch (Exception e) {
            LogUtil.e(e);
        }
        return mAppChannel = CHANNEL_SINA;
    }

    /**
     * 获取版本号（浮点数）
     *
     * @param context
     * @return
     */
    public static float getVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        String version = ConstantData.VERSION;

        try {
            if (null != pm) {
                version = pm.getPackageInfo(context.getPackageName(), 0).versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            // ignore
        }

        // 这里将版本名称转换成浮点数
        int dotIndex = version.indexOf(".");
        if (dotIndex > 0 && dotIndex < version.length() - 1) {
            String versionBegin = version.substring(0, dotIndex + 1);
            String versionEnd = version.substring(dotIndex + 1);

            versionEnd = versionEnd.replaceAll("\\.", "");
            version = versionBegin + versionEnd;
        }

        return Float.valueOf(version);
    }

    /**
     * 获取AndroidManifest.xml中某个meta-data标签对应的信息
     *
     * @param context
     * @param key     meta-data标签名
     * @return
     * @throws NameNotFoundException
     */
    private static String getMetaData(Context context, String key) throws NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        if (null != pm) {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai.metaData) {
                Object value = ai.metaData.get(key);
                if (value != null) {
                    return value.toString();
                }
            }
        }
        return null;
    }

    /**
     * 为url添加access_token等登录信息
     *
     * @param url
     * @return
     */
    public static String addLoginInfoToUrl(String url) {
        if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
            // 旧方法
            // StringBuilder sb = new StringBuilder(url);
            // if (!url.contains("?")) {
            // sb.append("?");
            // } else {
            // sb.append("&");
            // }
            //
            // sb.append("access_token=");
            // sb.append(LoginUtil.getLoginInfo().getAccessToken());
            // return sb.toString();
            // 新方法
            url = HttpUtil.setURLParams(url, ConstantData.ACCESS_TOKEN_KEY, LoginUtil.getLoginInfo().getAccessToken());
        }
        return url;
    }

    public static String addDeviceIdToUrl(String url) {
        // 旧方法
        // StringBuilder sb = new StringBuilder(url);
        // if (!url.contains("?")) {
        // sb.append("?");
        // } else {
        // sb.append("&");
        // }
        //
        // sb.append("device_id=");
        // sb.append(getDeviceId());
        // return sb.toString();
        // 新方法
        url = HttpUtil.setURLParams(url, ConstantData.DEVICE_ID_KEY, getDeviceId());
        return url;
    }

    /***************************** 网络请求状态码 *************************************/
    /**
     * 失败.
     */
    public static final String CODE_FAIL = "-1";

    /**
     * 成功.
     */
    public static final String CODE_SUCCESS = "0";

    public static final String CODE_CANCEL = "1";

    /**
     * 请求无数据.
     */
    public static final String CODE_DATA_NULL = "11";

    /**
     * 账户余额不足.
     */
    public static final String CODE_RECHARGE = "4";

    /**
     * 未登录.
     */
    public static final String CODE_LOGIN = "5";

    /***************************** 状态码 *************************************/
    /**
     * 失败（程序内部错误码，比如sd卡空间不足等）.
     */
    public static final int CODE_FAIL_KEY = -1;

    /**
     * 成功.
     */
    public static final int CODE_SUCCESS_KEY = 0;

    /*****************************
     * AppKey
     *************************************/
    public static final String AppKey = "2551836002";
    public static final String AppSecret = "2d17a87f0f0d6f50c425d720bcc071fd";

    public static final String APP_KEY_VDISK = "3153122571";
    public static final String APP_SECRET_VDISK = "29970d158e107dc7378a9aeb964c292b";
    public static final String APP_REDIRECT_URL_VDISK = "http://com.sina.sinashucheng";

    // public static final String SCOPE = "all";
    // public static final String SCOPE =
    // "email,direct_messages_read,direct_messages_write,"
    // + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
    // + "follow_app_official_microblog," + "invitation_write";
    public static final String SCOPE = "";
    // public static final String SCOPE =
    // "direct_messages_read,direct_messages_write";

    /**
     * 行为统计 APP_KEY.
     */
    public static final String USER_ACTION_APP_KEY = "f8b8211b-677c-4e57-bd40-a492c7701881";

    /**
     * 接口签名Key.
     */
    public static final String AUTO_KEY = "d7612b498d9815f23cf1d5df43afd121";

    /**
     * 消费记录进入详情页传递key
     */
    public static final String BUYDETETAIL_KEY = "36db29ae34b939bad4146f0d9680a5fe";

    /**
     * html图文书籍下载传递key，可以直接下载收费内容
     */
    public static final String HTMLREAD_KEY = "43a2001016298d08fab35f936116b058";

    /**
     * 分页数据默认每页的数据.
     */
    public static final int PAGE_SIZE = 20;

    /**
     * 书城接口Auth code.
     */
    public static final String AUTH_CODE = "d6712b498d9815f23cf1d5df43afd242";

    /**
     * 书城接口Auth code (GET).
     */
    public static final String AUTH_CODE_GET = "authcode=" + AUTH_CODE;

    /**
     * 书城接口from client.
     */
    public static final String FROM_CLIENT = "from_client=android";

    /***************************** Server Interface *************************************/
    /**
     * 正式服务器地址new
     */
//	 public static final String FICTION_HOST_NEW =
//	 "http://221.179.190.191/prog/wapsite/read/interface/c/";
    public static final String FICTION_HOST_NEW = "http://read.sina.cn/interface/c/";

    /**
     * 测试服务器地址
     */
    // public static final String FICTION_HOST_NEW =
    // "http://221.179.190.191/prog/wapsite/read/interface/c/";
    // public static final String FICTION_HOST_TEST =
    // "http://221.179.190.191/prog/wapsite/read/interface/c/";

    public final static String URL_BANNER_SHOW = FICTION_HOST_NEW + "singlepacket_check.php?bid=%s";

    /**
     * 微博OpenApi地址
     */
    public static final String WEIBO_API_HOST = "https://api.weibo.com/2/";

    /**
     * 重定向url
     */
    public static final String URL_REDIRECT = "http://www.sina.com";

    /**
     * 获取gsid
     */
    public static final String URL_GET_GSID = FICTION_HOST_NEW + "user_login.php?s_type=bookRecommend&access_token=%s";

    /**
     * 行为统计服务器地址.
     */
    public static final String USER_ACTION_URL = FICTION_HOST_NEW + "statistics.php?" + AUTH_CODE_GET;

    /**
     * 行为统计服务器地址(old).
     */
    public static final String USER_ACTION_URL_OLD = "http://appstat.sinaapp.com/i";

    /**
     * 书城推荐页编辑推荐、新书上架、免费专区数据接口
     */
    public static final String URL_INDEX = FICTION_HOST_NEW + "index.php?type=%s&page=%s&perpage=%s";

    /**
     * 书城推荐首页数据接口
     */
    public static final String URL_RECOMMEND = FICTION_HOST_NEW + "recommend_list.php?version=" + VERSION;

    /**
     * 出版接口
     */
    public static final String URL_RECOMMEND_CMS = FICTION_HOST_NEW + "recommend_bookcms.php?" + "index_type=%s";
    /**
     * 男生女生接口
     */
    public static final String URL_RECOMMEND_BoyAndGirl = FICTION_HOST_NEW + "recommend_bookcms.php?" + "index_type=%s&page=%s&perpage=%s";

    /**
     * 图书分类数据接口,返回分类图片
     */
    public static final String URL_NEW_PARTITION = FICTION_HOST_NEW + "cate_list_new.php?version=" + VERSION;

    /**
     * 图书分类数据接口详情
     */
    public static final String URL_PARTITION_DATA = FICTION_HOST_NEW
            + "category.php?cate=%s&page=%s&perpage=%s&type=%s";

    /**
     * 图书分类top数据接口
     */
    public static final String URL_PARTITION_TOP = FICTION_HOST_NEW + "category.php?cate=%s&type=all";

    /**
     * 免费书籍多章节下载
     */
    public static final String URL_COMMON_DOWN = FICTION_HOST_NEW + "freechapters.php";

    /**
     * 图书在线章节阅读/下载接口.
     */
    public static final String URL_READ_ONLINE = FICTION_HOST_NEW + "chapter.php";

    /**
     * 图书章节列表接口.
     */
    public static final String URL_GET_CHAPTERS = FICTION_HOST_NEW + "chapterlist"
            + ".php?bid=%s&sid=%s&src=%s&page=1&perpage=1";

    /**
     * 分页获取图书章节列表接口.
     */
    public static final String URL_GET_CHAPTERS_BY_PAGE = FICTION_HOST_NEW
            + "chapterlist.php?bid=%s&sid=%s&src=%s&page=%s&perpage=%s";

    /**
     * 多本书籍同时检查章节更新.
     */
    public static final String URL_UPDATE_BOOKS = FICTION_HOST_NEW + "books_chapterlist.php?bstring=%s";

    /**
     * 获取单章章节价格.
     */
    public static final String URL_GET_SINGLE_CHAPTER_PRICE = FICTION_HOST_NEW
            + "chaptersingle.php?bid=%s&sid=%s&src=%s&c_id=%s&version=" + VERSION;

    /**
     * 搜索好书接口.
     */
    public static final String URL_SEARCH_INDEX = FICTION_HOST_NEW + "search_index.php";

    /**
     * 搜索数据接口. Checked
     */
    public static final String URL_SEARCH_KEY = FICTION_HOST_NEW + "search.php?keys=%s&page=%s&pagesize=%s&kind=suite";

    /**
     * 图书详情接口.
     */
    public static final String URL_BOOK_INFO = FICTION_HOST_NEW + "bookinfo.php?bid=%s&sid=%s&src=%s&version="
            + VERSION;

    /**
     * 图书详情的扩展接口.
     */
    public static final String URL_BOOK_INFO_EXT = FICTION_HOST_NEW + "bookinfo_ext.php?bid=%s&sid=%s&src=%s&version="
            + VERSION;

    /**
     * 获取图书评论列表.
     */
    public static final String URL_COMMENTS = FICTION_HOST_NEW + "comment.php?bid=%s&sid=%s&src=%s&page=%s&perpage=%s";

    /**
     * 发送图书评论接口.
     */
    public static final String URL_COMMENTS_POST = FICTION_HOST_NEW + "comment_post.php?";

    /**
     * 获取用户uid.
     */
    public final static String WEIBO_GET_UID = WEIBO_API_HOST + "account/get_uid.json";

    /**
     * 购买记录.
     */
    public final static String URL_PURCHASED_BOOKS = FICTION_HOST_NEW + "paylist.php?page=%s&perpage=%s";

    /**
     * 赞.
     */
    public final static String URL_PRAISE_POST = FICTION_HOST_NEW + "praise_post.php?version=" + VERSION;

    /**
     * 分享到微博
     */
    public static final String URL_SHARE_WEIBO = FICTION_HOST_NEW + "share_weibo.php";

    /**
     * 获取用户信息通过我们服务器
     */
    public static final String URL_GET_USER_INFO_OUR = FICTION_HOST_NEW + "getuserinfo.php?access_token=%s&uid=%s";

    /**
     * openApi发送微博
     */
    public static final String URL_SEND_WEIBO = WEIBO_API_HOST + "statuses/update.json";
    public static final String URL_SEND_WEIBO_PIC = WEIBO_API_HOST + "statuses/upload.json";

    /**
     * openApi关注
     */
    public static final String URL_ATTENTION_WEIBO = WEIBO_API_HOST + "friendships/create.json";

    /*
     * 新浪读书账号登陆页面
     */
    public static final String URL_SINABOOK_LOGIN = "http://book1.sina.cn/dpool/booklogin/login.php?ftype=%s";

    /**
     * 收藏图书列表
     */
    public final static String URL_COLLECTED_BOOKS = FICTION_HOST_NEW + "collectlist.php?page=%s&perpage=%s";

    /**
     * 添加收藏
     */
    public final static String URL_COLLECTE_BOOK = FICTION_HOST_NEW + "collect"
            + ".php?access_token=%s&bid=%s&sid=%s&src=%s";

    /**
     * 删除收藏
     */
    public final static String URL_DELETE_COLLECTE_BOOK = FICTION_HOST_NEW
            + "del_collect.php?access_token=%s&bid=%s&sid=%s&src=%s&bag_id=%s";

    /**
     * 获取用户账户信息 服务器将调整
     */
    public final static String URL_ACCOUNT = FICTION_HOST_NEW
            + "userinfo.php?authcode=d6712b498d9815f23cf1d5df43afd242&access_token=%s";
    public final static String URL_GSID = FICTION_HOST_NEW + "userinfo.php?gsid=%s";

    /**
     * 名人推荐
     */
    public final static String URL_FAMOUS_RECOMMEND = FICTION_HOST_NEW + "timeline.php";

    /**
     * 热词
     */
    public final static String URL_HOT_WORD = FICTION_HOST_NEW + "hotword_list.php?page=%d&perpage=%d";

    /**
     * 用户详情接口
     */
    public final static String URL_USER_DETAIL = FICTION_HOST_NEW
            + "user_timeline.php?uid=%s&rec_type=%s&page=%s&perpage=%s";

    /**
     * 大家在看
     */
    public final static String URL_ALL_READ = FICTION_HOST_NEW + "recommend.php?page=%d&perpage=%d";

    /**
     * 统计数据接口.
     */
    public final static String URL_STATISTICS = FICTION_HOST_NEW + "statistics.php?from=%d&version=%s";

    /**
     * 包月套餐列表接口.
     */
    public final static String URL_SUITE_LIST = FICTION_HOST_NEW + "suite_list.php";

    /**
     * 包月套餐对应的书籍列表接口.
     */
    public final static String URL_SUITE_BOOK_LIST = FICTION_HOST_NEW + "suitel_booklist"
            + ".php?suite_id=%s&page=%s&perpage=%s";

    /**
     * 我的包月套餐信息接口，需要传递登陆信息.
     */
    public final static String URL_SUITE_MY = FICTION_HOST_NEW + "suite_my.php?version=" + VERSION;

    /**
     * 包月购买接口，需要传递登陆信息.
     */
    public final static String URL_SUITE_BUY = FICTION_HOST_NEW + "suite_buy.php?suite_id=%s";

    /**
     * APP版本更新.
     */
    public final static String URL_UPDATE_VERSION = /*"http://221.179.193.164/prog/wapsite/read/interface/c/" +*/ "getversion.php?client=android&version=%s";

    /**
     * 获取充值相关参数.
     */
    public final static String URL_RECHARGE_CHOOSE = FICTION_HOST_NEW + "recharge_choose.php?version=" + VERSION;

    /**
     * TODO:跳转充值页面
     */
    public final static String URL_RECHARGE_SUB = FICTION_HOST_NEW + "recharge_sub.php?amount=%s&type=%s&version="
            + VERSION;

    /**
     * 跳转充值页面2
     */
    public final static String URL_RECHARGE_SUB2 = FICTION_HOST_NEW + "recharge_sub.php?amount=%s&version=" + VERSION;

    /**
     * 微币转阅读币
     */
    public final static String URL_RECHARGE_EXCHANGE = FICTION_HOST_NEW + "recharge_excharge.php";

    /**
     * 主界面获取数据接口
     */
    public final static String URL_MAIN_INFO = FICTION_HOST_NEW + "index_list.php?version=" + VERSION;

    /**
     * 榜单页获取数据接口
     */
    public final static String URL_SELL_FAST_NEW = FICTION_HOST_NEW + "toplist_list_new" + ".php?version=" + VERSION;

    /**
     * 子榜单页获取数据接口(new)
     */
    public final static String URL_CHILD_SELL_FAST_NEW = FICTION_HOST_NEW + "toplist_new"
            + ".php?top_type=%s&page=%s&perpage=%s&version=" + VERSION;

    /**
     * 图书分类（包含登录用户喜欢的分类状态）接口
     */
    public final static String URL_LIKED_PARTITION = FICTION_HOST_NEW + "cate/cate_list.php?version=" + VERSION;

    /**
     * 更新喜欢的图书分类接口
     */
    public final static String URL_UPDATE_LIKED_PARTITION = FICTION_HOST_NEW + "cate/favorite_update.php?cate_id=%s"
            + "&version=" + VERSION;

    /**
     * 增量同步接口
     */
    public final static String URL_SYNC_FROM = FICTION_HOST_NEW + "collectlist_sync.php?etag=%s&books=%s";

    /**
     * 批量收藏接口
     */
    public final static String URL_COLLECT_BATCH = FICTION_HOST_NEW + "collect_batch.php?books=%s";

    /**
     * 全本付费书籍获取价格接口
     */
    public final static String URL_BOOK_INFO_CHECK = FICTION_HOST_NEW
            + "bookinfo_check.php?bid=%s&sid=%s&src=%s&version=" + VERSION;

    /**
     * 订阅书籍更新push接口
     */
    public final static String URL_REMIND_UPDATE_PUSH = FICTION_HOST_NEW + "push_post"
            + ".php?s_type=bookScan&act=%s&books=%s&gdid=%s&version=" + VERSION + "&device_id=" + getDeviceId();

    /**
     * 推荐流接口
     */
    public final static String URL_REC_COMMENDLIST = FICTION_HOST_NEW + "rec_commendlist"
            + ".php?rec_type=%s&page=%s&perpage=%s&version=" + VERSION;

    /**
     * 作者主页
     */
    public final static String URL_AUTHOR_INFO = FICTION_HOST_NEW + "authorinfo" + ".php?uid=%s&page=%s&perpage=%s";

    /**
     * 作家推荐
     */
    public final static String URL_AUTHOR_HOME = FICTION_HOST_NEW + "author_timeline" + ".php?page=%s&perpage=%s";

    /**
     * 书籍相关的作者信息接口
     */
    public final static String URL_AUTHOR_CHECK = FICTION_HOST_NEW + "authorinfo_check" + ".php?bid=%s";

    /**
     * 好书推荐书单
     */
    public final static String URL_BOOK_RECOMMEND_LIST = FICTION_HOST_NEW + "push_recommendlist"
            + ".php?gdid=%s&version=" + VERSION + "&device_id=" + getDeviceId();

    /**
     * 通过uid获取用户accessToken等用户信息
     */
    public final static String URL_GET_ACCESSTOKEN = FICTION_HOST_NEW + "getusertoken.php?uid=%s&sign=%s";

    /**
     * page下载客户端第一次启动获取书籍id
     */
    public final static String URL_GET_DOWN_BID = FICTION_HOST_NEW + "getpagecollect.php";

    /**
     * 消费记录
     */
    public final static String URL_CONSUME = FICTION_HOST_NEW + "consumelog.php?page=%d&perpage=10&type=%s";
    public final static String URL_CONSUME_DETAIL = FICTION_HOST_NEW
            + "consumelog.php?page=%d&perpage=10&type=chapters_list&bid=%s";

    /**
     * 获取专题相关信息
     */
    public final static String URL_TOPIC = FICTION_HOST_NEW + "topic.php?tid=%s";

    /**
     * 获取专题列表
     */
    public final static String URL_TOPIC_LIST = FICTION_HOST_NEW + "topic_list.php?tid=%s&page=%s&perpage=%s";

    /**
     * 书籍购买接口
     */
    public static final String URL_BOOK_BUY = FICTION_HOST_NEW + "book_buy.php";

    /**
     * 礼包活动接口
     */
    public static final String URL_GIFT = FICTION_HOST_NEW + "getpackage.php?";

    /**
     * 用户行为提交接口
     */
    public static final String URL_CHANNEL_ACTIVITY = FICTION_HOST_NEW + "getuseract.php?act=1";

    /**
     * 前往充值
     */
    public static final String RECHARGE_URL = FICTION_HOST_NEW + "recharge_customize.php";

    /**
     * 通用活动接口
     */
    public static final String GENERAL_ACTIVITY = FICTION_HOST_NEW + "getuseract.php?act=3";

    /**
     * 卸载统计页面地址
     */
    public static final String URL_UNISTALL_OBSERVER = "http://book1.sina.cn/dpool/newbook/uninstallreport/index.php?";

    /**
     * 明星作家列表接口(2014年9月5日发布)
     */
    public static final String RECOMMEND_AUTHORS = FICTION_HOST_NEW + "recommend_author.php?vt=1&page=%s&perpage=%s";

    /**
     * 作者作品列表接口
     */
    public static final String RECOMMEND_AUTHORBOOKS = FICTION_HOST_NEW + "recommend_authorbooks.php?vt=1&author_id=%s&page=%s&perpage=%s";


    /**
     * epub书籍下载接口
     * http://221.179.190.191/prog/wapsite/read/interface/c/epub_download.php?bid=243473&version=2.0.2&authcode=d6712b498d9815f23cf1d5df43afd242<br>
     * bid 必传<br>
     * access_token 可选,vip作品必传<br>
     * authcode,version等统计参数<br>
     * 错误码：<br>
     * 37 未购买<br>
     * 10 书籍信息有误<br>
     * 5 未登录<br>
     */
    public static final String EPUB_DOWNLOAD = FICTION_HOST_NEW + "epub_download.php";//

    /**
     * 根据bid检测该书是否存在epub资源(用于page页调起客户端后请求检测)
     */
    public static final String PAGE_EPUB_CEHCK = FICTION_HOST_NEW + "epub_check.php?bid=%s";

    public static final String TICKET_TO_USED = "http://open.book.weibo.com/ticket/user/setTicketToUsed?user_uid=%s&book_id=%s&channel=%s&key=%s";


    /*****************************
     * URL params keys and values(add by chenjianli)
     *************************************/
    public static final String AUTH_CODE_KEY = "authcode";
    public static final String FROM_CLIENT_KEY = "from_client";
    public static final String APP_CHANNEL_KEY = "app_channel";
    public static final String APP_VERSION_KEY = "version";                                                                                                    // APP版本号
    public static final String ACCESS_TOKEN_KEY = "access_token";                                                                                                // 新浪授权accessToken
    public static final String PHONE_IMEI_KEY = "phone_imei";                                                                                                // 手机IMEI
    public static final String DEVICE_ID_KEY = "device_id";                                                                                                    // 设备ID
    public static final String OPERATORS_NAME_KEY = "carrier";                                                                                                    // 运营商名称
    public static final String APN_ACCESS_KEY = "apn_access";                                                                                                // APN接入点
    public static final String TIME_STAMP_KEY = "timestamp";                                                                                                    // 时间戳

    public static final String AUTH_CODE_VALUE = AUTH_CODE;
    public static final String FROM_CLIENT_VALUE = "android";
    public static final String APP_VERSION_VALUE = ApplicationUtils.getVersionName(SinaBookApplication.gContext);

    // 注册微博h5地址
//    public static final String WEIBO_REGIST_URL = "http://m.weibo.cn/reg/index?jp=1";
    public static final String WEIBO_REGIST_URL = "http://weibo.cn/dpool/ttt/h5/reg.php";

}
