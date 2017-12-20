package com.sina.book.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.sina.book.SinaBookApplication;
import com.sina.book.control.FakeX509TrustManager;
import com.sina.book.control.GenericTask;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.HtmlDownManager;
import com.sina.book.control.download.VDiskSyncManager;
import com.sina.book.data.ConstantData;
import com.sina.book.data.LoginInfo;
import com.sina.book.data.UserInfo;
import com.sina.book.data.UserInfoUb;
import com.sina.book.data.UserInfoUb.Activitys;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.data.util.CollectedBookList;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.data.util.PurchasedBookList;
import com.sina.book.parser.AutoLoginParser;
import com.sina.book.parser.BalanceParser;
import com.sina.book.ui.RecommendWebUrlActivity;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.useraction.DeviceInfo;

/**
 * @author Li Wen
 * @ClassName: LoginUtil
 * @Description: 登录相关处理
 * @date 2012-11-7
 */
public class LoginUtil {
    private static final String KEY_TAG = "LoginUtil";

    private LoginUtil() {

    }

    public static LoginUtil i;

    static {
        i = new LoginUtil();
    }

    /**
     * 用户信息更新通知事件
     */
    public static String ACTION_INFO_UPDATE = "com.sina.book.action.infoUpdate";

    /**
     * 保存用户登录信息文件名
     */
    public static final String LOGIN_INFO_NAME = "login_info";
    // 保存用户登录的Cookie信息
    public static final String COOKIES_NAME = "cookies";

    // 登录信息
    private static final String KEY_SINA_ID = "key_sina_id";
    private static final String KEY_NICK_NAME = "key_nick_name";
    private static final String KEY_PROFILE_IMAGE_URL = "key_profile_image_url";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_LASTPAY_INFO = "key_lastpay_info";
    // 有效时间戳
    private static final String KEY_EXPRIES = "expires";

    // 失效时间
    private static final String KEY_EXPRIES_TIME = "expires_time";

    private static final String KEY_ISSUED_AT = "issued_at";
    private static final String KEY_BALANCE = "balance";

    private static final String KEY_ROLE = "role";
    private static final String KEY_ROLE_NAME = "role_name";

    private static final String KEY_GSID = "key_gsid";
    private static final String KEY_GSID_VAILD = "key_gsid_vaild";

    private static final String KEY_MYCARD_URL = "key_mycard_url";
    private static final String KEY_MYCARD_NAME = "key_mycard_name";
    private static final String KEY_MYCARD_TIP = "key_mycard_tip";

    private static Context sContext = SinaBookApplication.gContext;

    private static LoginInfo sLoginInfo;

    private static RequestTask mBalanceTask;
    private static RequestTask mRefreshTask;
    private static RequestTask mRefreshForceTask;

    private static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);

    public static void cancelValidAccessToken2Refresh() {
        if (mRefreshForceTask != null) {
            mRefreshForceTask.cancel(true);
            mRefreshForceTask = null;
        }
    }

    /**
     * 获取登录信息
     *
     * @return
     */
    public static LoginInfo getLoginInfo() {
        if (null == sLoginInfo) {
            sLoginInfo = getLoginInfoFromFile();
        }

        return sLoginInfo;
    }

    public static String syncRequestGsidHttpConnection_old() {
        LoginInfo info = LoginUtil.getLoginInfo();
        if (info == null) {
            return null;
        }
        String token = info.getAccessToken();

        final String gsidUrl = "https://m.weibo.cn/login?backurl=book.sina.cn&ns=1&vt=4&access_token=" + token;
        HttpURLConnection con = null;

        try {
            URL url = new URL(gsidUrl);
            // 无视SSL证书是否有效问题
            FakeX509TrustManager.allowAllSSL();
            con = HttpUtil.getHttpUrlConnection(url, SinaBookApplication.gContext, false);
            con.connect();
            String key = null;
            for (int i = 1; (key = con.getHeaderFieldKey(i)) != null; i++) {
                // LogUtil.d("cx", "key:" + key);
                // LogUtil.d("cx", "value:" + con.getHeaderField(i));
                if (key.equalsIgnoreCase("Set-Cookie")) {
                    String[] cookies = con.getHeaderField(i).split(";");
                    if (cookies != null) {
                        for (String cookie : cookies) {
                            String[] kv = cookie.split("=");
                            if (kv != null && kv.length == 2) {
                                String cookieKey = kv[0];
                                String cookieValue = kv[1];
                                if (cookieKey.startsWith("gsid")) {
                                    return cookieValue;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (con != null) {
                con.disconnect();
            }
            // wrong
        }
        return null;
    }

    public static String syncRequestGsidHttpConnection() {
        LoginInfo info = LoginUtil.getLoginInfo();
        if (info == null) {
            return null;
        }

        String carrier = DeviceInfo.getCarrier(SinaBookApplication.gContext);
        String apn = HttpUtil.getNetworkType(SinaBookApplication.gContext);
        String imei = ConstantData.getDeviceId();
        String deviceId = DeviceInfo.getUDID();
        String appChannel = String.valueOf(ConstantData.getChannelCode(SinaBookApplication.gContext));

        String token = info.getAccessToken();
        HttpClient client = null;
        HttpResponse response = null;
        String gsid = null;
        String gsidUrl = String.format(ConstantData.URL_GET_GSID, token);
        gsidUrl = HttpUtil.setURLParams(gsidUrl, ConstantData.AUTH_CODE_KEY, ConstantData.AUTH_CODE_VALUE);
        gsidUrl = HttpUtil.setURLParams(gsidUrl, ConstantData.APP_VERSION_KEY, ConstantData.VERSION);
        gsidUrl = HttpUtil.setURLParams(gsidUrl, ConstantData.OPERATORS_NAME_KEY, carrier);
        gsidUrl = HttpUtil.setURLParams(gsidUrl, ConstantData.APN_ACCESS_KEY, apn);
        gsidUrl = HttpUtil.setURLParams(gsidUrl, ConstantData.PHONE_IMEI_KEY, imei);
        gsidUrl = HttpUtil.setURLParams(gsidUrl, ConstantData.DEVICE_ID_KEY, deviceId);
        gsidUrl = HttpUtil.setURLParams(gsidUrl, ConstantData.APP_CHANNEL_KEY, appChannel);

        try {
            client = HttpUtil.createHttpClient();
            response = HttpUtil.doGetRequest(client, gsidUrl);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String json = EntityUtils.toString(response.getEntity());
                if (json == null)
                    return gsid;
                JSONObject object = new JSONObject(json);
                gsid = object.optString("gsid_CTandWM");
                return gsid;
            }
        } catch (Exception e) {
            if (client != null) {
                client.getConnectionManager().shutdown();
                client = null;
            }
        }
        return gsid;
    }

//	private Map<String, String> myHttpsWeiboCnLoginCookie = new HashMap<String, String>();

//	public synchronized Map<String, String> getHttpsWeiboCnLoginCookie() {
//		return myHttpsWeiboCnLoginCookie;
//	}

//	public synchronized String getHttpsWeiboCnLoginCookieValue(String key) {
//		if(myHttpsWeiboCnLoginCookie != null && key != null) {
//			return myHttpsWeiboCnLoginCookie.get(key);
//		}
//		return null;
//	}

    public synchronized String getGsid() {
//		return getCookieValue(" gsid_CTandWM");
        Map<String, ?> cookieMap = getAllCookies();
        if (cookieMap != null && cookieMap.size() > 0) {
            for (String key : cookieMap.keySet()) {
                if(key.endsWith("SUB")){
                    return (String)cookieMap.get(key);
                }
            }
        }
        return "";
//        return getCookieValue(" SUB");
    }

    /**
     * 同步请求gsid
     */
    public synchronized LoginUtil syncRequestGsidHttpClient() {
        final String gsidUrl = "https://m.weibo.cn/login?backurl=book.sina.cn&ns=1&vt=4&access_token="
                + LoginUtil.getLoginInfo().getAccessToken();
        HttpResponse response = null;
        HttpClient httpClient = null;
        try {
            httpClient = HttpUtil.getHttpClient(SinaBookApplication.gContext);
            //将HttpContext对象作为参数传给execute()方法,则HttpClient会把请求响应交互过程中的状态信息存储在HttpContext中
            HttpContext httpContext = new BasicHttpContext();
            response = HttpUtil.doGetRequestWithHasRedirectURL(httpClient, gsidUrl, httpContext);
            LogUtil.d("Cookie", "/////////////////////////////////////");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try {
//					myHttpsWeiboCnLoginCookie.clear();
                    clearCookies();
                    //获取重定向之后的主机地址信息
                    HttpHost targetHost = (HttpHost) httpContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                    //获取实际的请求对象的URI,即重定向之后的URL
                    HttpUriRequest realRequest = (HttpUriRequest) httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
                    LogUtil.d("Cookie", "主机地址:" + targetHost);
                    if (realRequest != null) {
                        LogUtil.d("Cookie", "URI信息:" + realRequest.getURI());
                        Header[] headers = realRequest.getAllHeaders();
                        if (null != headers) {
                            for (int i = 0; i < headers.length; i++) {
                                Header header = headers[i];
                                if (null != header) {
                                    String key = header.getName();
                                    LogUtil.d("Cookie", "key:" + key);
                                    LogUtil.d("Cookie", "value:" + header.getValue());
                                    if (key != null && key.equalsIgnoreCase("Cookie")) {
                                        String value = header.getValue();
                                        String[] cookies = value.split(";");
                                        if (cookies != null) {
                                            for (String cookie : cookies) {
                                                String[] kv = cookie.split("=");
                                                if (kv != null && kv.length == 2) {
                                                    String cookieKey = kv[0];
                                                    String cookieValue = kv[1];

//													myHttpsWeiboCnLoginCookie.put(cookieKey, cookieValue);
                                                    LogUtil.d("Cookie", "///// cookieKey:" + cookieKey + ", cookieValue=" + cookieValue);
                                                    saveCookie(cookieKey, cookieValue);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
                httpClient = null;
            }
        }
        return i;
    }

    /**
     * 判断登录信息是否有效，无效时异步获取<br>
     * 初次获取登录信息使用，并尽可能等待结果返回
     *
     * @param listener
     */
    public static void isValidAccessToken2Refresh(final LoginStatusListener listener) {
        boolean isValidToken = false;
        String uid = null;

        LoginInfo loginInfo = getLoginInfo();
        if (loginInfo != null) {
            uid = loginInfo.getUID();
            Long expiresTime = loginInfo.getExpires_time();
            if (expiresTime != null) {
                String date = sFormat.format(new java.util.Date(expiresTime));

                LogUtil.d(KEY_TAG, "isValidAccessToken2Refresh 有效期：" + date + "---" + expiresTime);
                if (!Util.isNullOrEmpty(loginInfo.getAccessToken())
                        && (expiresTime == 0 || System.currentTimeMillis() < expiresTime)) {
                    sLoginInfo = loginInfo;
                    isValidToken = true;

                    // 从preference中读取登录信息，还有6天过期时，触发一个换新的动作
                    if (expiresTime > 0 && expiresTime - System.currentTimeMillis() < 518400000L) {
                        refreshAccessToken(sLoginInfo.getUID());
                    }
                }
            }
        }

        if (!isValidToken) {
            if (uid != null) {
                refreshAccessToken(loginInfo, listener);
            } else {
                listener.onFail();
            }
        } else {
            listener.onSuccess();
        }
    }

    /**
     * 已登录并且token未失效
     */
    public static final int TOKEN_TYPE_LOGIN_SUCCESS = 0;
    /**
     * 未登录
     */
    public static final int TOKEN_TYPE_NO_LOGININFO = 1;
    /**
     * token过期
     */
    public static final int TOKEN_TYPE_TOKEN_EXPIRES = 2;

    public static int isValidAccessToken(Context context) {
        return isValidAccessToken(context, true);
    }

    /**
     * 判断用户Oauth2AccessToken是否有效
     *
     * @param context
     * @return boolean
     */
    public static int isValidAccessToken(Context context, boolean refreshToken) {
        boolean isValidToken = false;
        if (sLoginInfo != null) {
            Long expiresTime = sLoginInfo.getExpires_time();
            if (expiresTime != null) {
                LogUtil.d(KEY_TAG, "isValidAccessToken 1 有效期：" + expiresTime + " context:" + context);
                if (!Util.isNullOrEmpty(sLoginInfo.getAccessToken())
                        && (expiresTime == 0 || System.currentTimeMillis() < expiresTime)) {
                    isValidToken = true;
                }
            } else {
                // 无法取到expiresTime也算入token过期吧
                isValidToken = true;
                return TOKEN_TYPE_TOKEN_EXPIRES;
            }
        } else {
            LoginInfo loginInfo = getLoginInfoFromFile();
            if (loginInfo != null) {
                Long expiresTime = loginInfo.getExpires_time();
                if (expiresTime != null) {
                    String date = sFormat.format(new Date(expiresTime));

                    LogUtil.d(KEY_TAG, "isValidAccessToken 2 有效期：" + date + "---" + expiresTime);
                    if (!Util.isNullOrEmpty(loginInfo.getAccessToken())
                            && (expiresTime == 0 || System.currentTimeMillis() < expiresTime)) {
                        sLoginInfo = loginInfo;
                        isValidToken = true;

                        // 从preference中读取登录信息，还有6天过期时，触发一个换新的动作
                        if (refreshToken && expiresTime > 0 && expiresTime - System.currentTimeMillis() < 518400000L) {
                            refreshAccessToken(sLoginInfo.getUID());
                        }
                    } else {
                        return TOKEN_TYPE_TOKEN_EXPIRES;
                    }
                } else {
                    // 无法取到expiresTime也算入token过期吧
                    return TOKEN_TYPE_TOKEN_EXPIRES;
                }
            } else {
                return TOKEN_TYPE_NO_LOGININFO;
            }
        }

        // if (!isValidToken) {
        // mRefreshForceTask == null 说明未取到uid
        // if (null == mRefreshForceTask) {
        // TODO：这里的清除行为引发了诸多问题
        // 清除登录信息
        // LoginUtil.clearLoginInfo(context, "LoginUtil");
        // }
        // }
        LogUtil.d(KEY_TAG, "isValidToken:" + isValidToken + " clear:" + (mRefreshForceTask == null));
        LogUtil.d("ReadInfoLeft", "isValidToken:" + isValidToken + " clear:" + (mRefreshForceTask == null));
        return TOKEN_TYPE_LOGIN_SUCCESS;
    }

    /**
     * 更新余额,用户信息
     *
     *  isWeiboLogin true:微博登陆流程 false：读书账号登陆流程
     */
    public static void reqBalance(final Context context) {
        if (mBalanceTask != null || isValidAccessToken(context) != TOKEN_TYPE_LOGIN_SUCCESS) {
            return;
        }

        String url = String.format(ConstantData.URL_ACCOUNT, sLoginInfo.getAccessToken());
        mBalanceTask = new RequestTask(new BalanceParser());
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, url);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        mBalanceTask.setTaskFinishListener(new ITaskFinishListener() {

            public void onTaskFinished(TaskResult taskResult) {
                if (taskResult.retObj instanceof UserInfoUb) {
                    LogUtil.d(KEY_TAG, "获取balance!");
                    UserInfoUb ub = (UserInfoUb) taskResult.retObj;
                    if (sLoginInfo != null) {
                        sLoginInfo.setBalance(ub.getBalance());
                        sLoginInfo.getUserInfoRole().setRoleName(ub.getRoleName());
                        sLoginInfo.getUserInfoRole().setRole(ub.getRole());
                        sLoginInfo.setUID(ub.getUid());
                        sLoginInfo.getUserInfo().setuName(ub.getName());

                        // 活动信息即时生效，不存到本地
                        // sLoginInfo.setActivity(ub.getActivityTip(),
                        // ub.getActivityName(), ub.getActivityUrl(),
                        // ub.getActivityEndTime());
                        sLoginInfo.setActivitys(ub.getActivitys());

                        // 本地保存balance
                        saveBalance(ub.getBalance());
                        // 本地保存role
                        saveRole(ub.getRoleName(), ub.getRole());
                        // 请求用户信息时，将赠书卡信息存储，用于进入个人中心时快速显示赠书卡
                        saveCardInfo(ub.getActivitys());

                        // 发送用户信息更新通知
                        Intent intent = new Intent();
                        intent.setAction(ACTION_INFO_UPDATE);
                        context.sendBroadcast(intent);
                    }
                }
                mBalanceTask = null;
            }
        });
        mBalanceTask.execute(params);
    }

    public static boolean syncReqBalance(final Context context) {
        if (mBalanceTask != null) {
            mBalanceTask = null;
        }

        String url = String.format(ConstantData.URL_ACCOUNT, sLoginInfo.getAccessToken());
        mBalanceTask = new RequestTask(new BalanceParser());
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, url);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

        TaskResult taskResult = mBalanceTask.syncExecute(params);
        if (taskResult.retObj instanceof UserInfoUb) {
            UserInfoUb ub = (UserInfoUb) taskResult.retObj;
            if (sLoginInfo != null) {
                sLoginInfo.setBalance(ub.getBalance());
                sLoginInfo.getUserInfoRole().setRoleName(ub.getRoleName());
                sLoginInfo.getUserInfoRole().setRole(ub.getRole());
                sLoginInfo.setUID(ub.getUid());
                sLoginInfo.getUserInfo().setuName(ub.getName());
                sLoginInfo.setActivitys(ub.getActivitys());

                // 本地保存balance
                saveBalance(ub.getBalance());
                // 本地保存role
                saveRole(ub.getRoleName(), ub.getRole());
                // 请求用户信息时，将赠书卡信息存储，用于进入个人中心时快速显示赠书卡
                boolean result = saveCardInfo(ub.getActivitys());
                return result;
            }
        }
        return false;
    }

    /**
     * 保存用户登录信息到preferences
     * <p/>
     * ingoreActivitys: 忽略掉微博呼气自动登陆时，赠书卡的逻辑状态存储(因为自动登陆没有下载到赠书卡信息)
     */
    public static int saveLoginInfo(LoginInfo loginInfo, boolean ingoreActivitys) {
        int result = 1;
        sLoginInfo = loginInfo;
        if (sLoginInfo != null) {
            SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
            Editor editor = preferences.edit();

            editor.putString(KEY_SINA_ID, sLoginInfo.getUID());
            editor.putString(KEY_ACCESS_TOKEN, sLoginInfo.getAccessToken());
            if (sLoginInfo.getRemindIn() != null && sLoginInfo.getRemindIn().length() > 0) {
                editor.putString(KEY_ISSUED_AT, sLoginInfo.getRemindIn());
            }
            editor.putString(KEY_EXPRIES, sLoginInfo.getExpires());
            editor.putLong(KEY_EXPRIES_TIME, sLoginInfo.getExpires_time());
            editor.putString(KEY_BALANCE, sLoginInfo.getBalance());
            editor.putString(KEY_ROLE_NAME, sLoginInfo.getUserInfoRole().getRoleName());
            editor.putInt(KEY_ROLE, sLoginInfo.getUserInfoRole().getRole());
            editor.putString(KEY_PROFILE_IMAGE_URL, sLoginInfo.getUserInfo().getUserProfileUrl());
            editor.putString(KEY_NICK_NAME, sLoginInfo.getUserInfo().getuName());
            editor.putString(KEY_GSID, sLoginInfo.getUserInfo().getGsid());

            ArrayList<Activitys> activityList = sLoginInfo.getActivitys();
            if (activityList != null && activityList.size() > 0) {
                for (int i = 0; i < activityList.size(); ++i) {
                    Activitys activitys = activityList.get(i);
                    int activityType = activitys.getActivityType();
                    if (activityType == Activitys.TYPE_CARD) {
                        // 保存赠书卡
                        result = 2;
                        editor.putString(KEY_MYCARD_NAME, activitys.getActivityName());
                        editor.putString(KEY_MYCARD_TIP, activitys.getActivityTip());
                        editor.putString(KEY_MYCARD_URL, activitys.getActivityUrl());
                        break;
                    }
                }
            }
            editor.commit();
            // if (!ingoreActivitys) {
            // StorageUtil.saveBoolean("showbookcard", result);
            // }
        } else {
            result = -1;
        }
        return result;
    }

    public static boolean saveCardInfo(ArrayList<Activitys> list) {
        boolean result = false;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); ++i) {
                Activitys activitys = list.get(i);
                int activityType = activitys.getActivityType();
                if (activityType == Activitys.TYPE_CARD) {
                    // 保存赠书卡
                    result = true;
                    SharedPreferences preferences = sContext
                            .getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
                    Editor editor = preferences.edit();

                    editor.putString(KEY_MYCARD_NAME, activitys.getActivityName());
                    editor.putString(KEY_MYCARD_TIP, activitys.getActivityTip());
                    editor.putString(KEY_MYCARD_URL, activitys.getActivityUrl());

                    editor.commit();
                    break;
                }
            }
            StorageUtil.saveBoolean("showbookcard", result);
        }
        return result;
    }

    /**
     * 清除用户登录信息到preferences
     *
     * @param context
     */
    public void clearLoginInfo(Context context, String logTag) {
        LogUtil.e("ReadInfoLeft", "LoginUtil >> clearLoginInfo >> logTag=" + logTag);

//		DownBookManager.getInstance().stopAllJob();
        HtmlDownManager.getInstance().stopAllJob();

        clearCookies();

        sLoginInfo = null;
        SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        PurchasedBookList.getInstance().clean();
        CollectedBookList.getInstance().clean();
        // clearCookies(context);

        /**
         * 同时清除包月信息
         */
        PaymentMonthMineUtil.getInstance().clear();

        /**
         * 清除微盘缓存
         */
        VDiskSyncManager.recycle();

        /**
         * 云端书架登出处理
         */
        CloudSyncUtil.getInstance().logout("LoginUtil >> clearLoginInfo");
    }

    /**
     * 保存用户上次支付方式及金额
     */
    public static void saveLastPayInfo(String info) {
        SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(KEY_LASTPAY_INFO, info);
        editor.commit();
    }

    /**
     * 读取preferences中用户上次支付的方式
     */
    public static String getLatPayInfo() {
        SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LASTPAY_INFO, "");
    }

    public static void saveLoginGsid(String gSid) {
        LogUtil.d(KEY_TAG, "saveLoginGsid!");
        SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        if (!Util.isNullOrEmpty(gSid)) {
            editor.putString(KEY_GSID, gSid);
            // 3天
            editor.putLong(KEY_GSID_VAILD, System.currentTimeMillis() + 259200000);
        }
        editor.commit();
    }

    private static void saveNewAccessToken(String accessToken, String expires_in, Long expires_time) {
        LogUtil.d(KEY_TAG, "saveNewAccessToken!");
        if (accessToken != null && (expires_time == 0 || System.currentTimeMillis() < expires_time)) {
            SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putString(KEY_ACCESS_TOKEN, accessToken);
            editor.putString(KEY_EXPRIES, expires_in);
            editor.putLong(KEY_EXPRIES_TIME, expires_time);
            editor.commit();
        }
    }

    /**
     * 保存用户余额
     *
     * @param balance
     */
    public static void saveBalance(String balance) {
        SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        if (balance != null) {
            LogUtil.d(KEY_TAG, "saveBalance：" + balance);
            editor.putString(KEY_BALANCE, balance);
        }
        editor.commit();
    }

    /**
     * 保存用户权限信息
     *
     * @param roleName
     * @param role
     */
    public static void saveRole(String roleName, int role) {
        SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        if (roleName != null) {
            editor.putString(KEY_ROLE_NAME, roleName);
            editor.putInt(KEY_ROLE, role);
        }
        editor.commit();
    }

    private static void saveUid(String uid) {
        SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        if (uid != null) {
            editor.putString(KEY_SINA_ID, uid);
        }
        editor.commit();
    }

    private static void saveName(String name) {
        SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        if (name != null) {
            editor.putString(KEY_NICK_NAME, name);
        }
        editor.commit();
    }

    /**
     * @param context
     * @return void 返回类型
     * @Description: 清楚cookies缓存
     */
    private void clearCookies(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    private void clearCookies() {
        SharedPreferences preferences = sContext.getSharedPreferences(COOKIES_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }

    private void saveCookie(String key, String value) {
        SharedPreferences preferences = sContext.getSharedPreferences(COOKIES_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public Map<String, ?> getAllCookies() {
        if (sContext != null) {
            SharedPreferences preferences = sContext.getSharedPreferences(COOKIES_NAME, Context.MODE_PRIVATE);
            Map<String, ?> map = preferences.getAll();
            return map;
        }
        return null;
    }

    public String getCookieValue(String key) {
        return getCookieValue(key, "");
    }

    public String getCookieValue(String key, String defValue) {
        SharedPreferences preferences = sContext.getSharedPreferences(COOKIES_NAME, Context.MODE_PRIVATE);
        return preferences.getString(key, defValue);
    }

    /**
     * 从preferences中读取用户登录信息
     *
     * @return 用户登录信息
     */
    private static LoginInfo getLoginInfoFromFile() {
        SharedPreferences preferences = sContext.getSharedPreferences(LOGIN_INFO_NAME, Context.MODE_PRIVATE);

        try {
            String accessToken = preferences.getString(KEY_ACCESS_TOKEN, "");
            if (Util.isNullOrEmpty(accessToken)) {
                return null;
            }
            LoginInfo info = new LoginInfo();
            info.setAccessToken(accessToken);
            info.setUID(preferences.getString(KEY_SINA_ID, ""));
            info.setExpires(preferences.getString(KEY_EXPRIES, ""));

            info.setExpires_time(preferences.getLong(KEY_EXPRIES_TIME, -1L));

            info.setRemindIn(preferences.getString(KEY_ISSUED_AT, ""));
            info.setBalance(preferences.getString(KEY_BALANCE, "0.00"));
            info.getUserInfoRole().setRoleName(preferences.getString(KEY_ROLE_NAME, "普通用户"));
            info.getUserInfoRole().setRole(preferences.getInt(KEY_ROLE, 0));

            UserInfo user = new UserInfo();
            user.setuName(preferences.getString(KEY_NICK_NAME, ""));
            user.setUserProfileUrl(preferences.getString(KEY_PROFILE_IMAGE_URL, ""));
            long gsidVaild = preferences.getLong(KEY_GSID_VAILD, 0);
            // TODO:
            // if (gsidVaild > System.currentTimeMillis()) {
            user.setGsid(preferences.getString(KEY_GSID, ""));
            // }
            info.setUserInfo(user);

            // 读取赠书卡
            String name = preferences.getString(KEY_MYCARD_NAME, "");
            String tip = preferences.getString(KEY_MYCARD_TIP, "");
            String url = preferences.getString(KEY_MYCARD_URL, "");
            if (url.length() > 0 && (name.length() > 0 || tip.length() > 0)) {
                Activitys activitys = new Activitys();
                activitys.setActivityType(Activitys.TYPE_CARD);
                activitys.setActivityName(name);
                activitys.setActivityTip(tip);
                activitys.setActivityUrl(url);

                ArrayList<UserInfoUb.Activitys> list = new ArrayList<UserInfoUb.Activitys>();
                list.add(activitys);
                info.setActivitys(list);
            }

            return info;
        } catch (Exception e) {
            LogUtil.e("comic", "", e);
        }
        return null;
    }

    private static void refreshAccessToken(final LoginInfo loginInfo, final LoginStatusListener listener) {
        if (loginInfo == null) {
            listener.onFail();
            return;
        }
        if (mRefreshForceTask != null) {
            mRefreshForceTask.cancel(true);
        }
        mRefreshForceTask = new RequestTask(new AutoLoginParser());
        TaskParams task1Params = new TaskParams();
        String key = "1aeef8acf149e0ba78c19eb225e1e533";
        String content = loginInfo.getUID() + "|" + key;
        String task1url = String.format(ConstantData.URL_GET_ACCESSTOKEN, loginInfo.getUID(), Util.genMD5Code(content));
        task1Params.put(RequestTask.PARAM_URL, task1url);
        task1Params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        mRefreshForceTask.setTaskFinishListener(new ITaskFinishListener() {

            @Override
            public void onTaskFinished(TaskResult taskResult) {
                if (taskResult.retObj instanceof LoginInfo) {
                    LoginInfo retLoginInfo = (LoginInfo) taskResult.retObj;
                    if (retLoginInfo.getAccessToken() != null
                            && (retLoginInfo.getExpires_time() == 0 || System.currentTimeMillis() < retLoginInfo
                            .getExpires_time())) {
                        sLoginInfo = loginInfo;
                        // 1 更新内存中的登录信息
                        sLoginInfo.setAccessToken(retLoginInfo.getAccessToken());
                        sLoginInfo.setExpires_time(retLoginInfo.getExpires_time());
                        sLoginInfo.setExpires(retLoginInfo.getExpires());
                        // 2 更新preference
                        saveNewAccessToken(retLoginInfo.getAccessToken(), retLoginInfo.getExpires(),
                                retLoginInfo.getExpires_time());
                    }
                }
                if (listener != null) {
                    listener.onSuccess();
                }
                mRefreshForceTask = null;
            }
        });
        mRefreshForceTask.execute(task1Params);
    }

    private static void refreshAccessToken(final String uid) {
        if (mRefreshTask != null) {
            return;
        }
        mRefreshTask = new RequestTask(new AutoLoginParser());
        TaskParams task1Params = new TaskParams();
        String key = "1aeef8acf149e0ba78c19eb225e1e533";
        String content = uid + "|" + key;
        String task1url = String.format(ConstantData.URL_GET_ACCESSTOKEN, uid, Util.genMD5Code(content));
        task1Params.put(RequestTask.PARAM_URL, task1url);
        task1Params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        mRefreshTask.setTaskFinishListener(new ITaskFinishListener() {

            @Override
            public void onTaskFinished(TaskResult taskResult) {
                if (taskResult.retObj instanceof LoginInfo) {
                    LoginInfo retLoginInfo = (LoginInfo) taskResult.retObj;
                    if (retLoginInfo.getAccessToken() != null
                            && (retLoginInfo.getExpires_time() == 0 || System.currentTimeMillis() < retLoginInfo
                            .getExpires_time())) {
                        // 1 更新内存中的登录信息
                        if (null != sLoginInfo) {
                            sLoginInfo.setAccessToken(retLoginInfo.getAccessToken());
                            sLoginInfo.setExpires_time(retLoginInfo.getExpires_time());
                            sLoginInfo.setExpires(retLoginInfo.getExpires());
                        }
                        // 2 更新preference
                        saveNewAccessToken(retLoginInfo.getAccessToken(), retLoginInfo.getExpires(),
                                retLoginInfo.getExpires_time());
                    }
                }
                mRefreshTask = null;
            }
        });
        mRefreshTask.execute(task1Params);
    }

    private static GenericTask mReqGsidTask;

    public static void reqGsidAndEnterWebView(Activity activity, String url, final String title) {
        final String finalURL = Util.getUnInstallObserverUrl(url);
        String gsid = LoginUtil.getLoginInfo().getUserInfo().getGsid();
        if (gsid != null && gsid.length() > 0) {
            String endUrl = HttpUtil.setURLParams(finalURL, "gsid", gsid);
            endUrl = HttpUtil.addAuthCode2Url(endUrl);
            RecommendWebUrlActivity.launch(activity, endUrl, title);
        } else {
            // 请求gsid
            DialogUtils.showProgressDialog(activity, "正在加载...", true, false, new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                }
            }, new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (mReqGsidTask != null) {
                        mReqGsidTask.cancel(true);
                        mReqGsidTask = null;
                    }
                }
            });

            mReqGsidTask = new GenericTask() {
                protected TaskResult doInBackground(TaskParams... params) {
                    // 1 如果无gsid再去取一次gsid
                    if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
                            && Util.isNullOrEmpty(LoginUtil.getLoginInfo().getUserInfo().getGsid())) {
                        String gsid = LoginUtil.i.syncRequestGsidHttpClient().getGsid();
                        LoginUtil.getLoginInfo().getUserInfo().setGsid(gsid);
                        LoginUtil.saveLoginGsid(gsid);
                    }
                    String endUrl = HttpUtil.setURLParams(finalURL, "gsid", LoginUtil.getLoginInfo().getUserInfo()
                            .getGsid());
                    endUrl = HttpUtil.addAuthCode2Url(endUrl);
                    RecommendWebUrlActivity.launch(SinaBookApplication.gContext, endUrl, title);
                    return null;
                }
            };
            mReqGsidTask.setTaskFinishListener(new ITaskFinishListener() {
                public void onTaskFinished(TaskResult taskResult) {
                    DialogUtils.dismissProgressDialog();
                    mReqGsidTask = null;
                }
            });
            mReqGsidTask.execute();

        }
    }
}
