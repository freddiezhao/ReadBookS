package com.sina.book.useraction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.sina.basicfunc.App;
import com.sina.basicfunc.inst.Inst;
import com.sina.basicfunc.sendsuggestion.SendSuggestionActivity;
import com.sina.basicfunc.sendsuggestion.SendSuggestionActivity.GetAddition;
import com.sina.basicfunc.sendsuggestion.SendSuggestionActivity.GetCfg;
import com.sina.basicfunc.utility.LogUtil;
import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.ConstantData;
import com.sina.book.util.StorageUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 通用SDK调用处理(统计日活和意见反馈)
 *
 * @author Li Wen
 * @date 2012-11-20
 */
public class BasicFuncUtil {
    private static final String KEY_ADDITION = "#SinaReader#";

    private static BasicFuncUtil sInstance;
    private Context mContext;

    /**
     * 产品渠道号
     */
    public static final String APP_PID = "free";
    /**
     * 产品代号
     */
    public static final String APP_PDSTR = "ds";

    /**
     * 0是release版
     */
    public static final String APP_ABR_RELEASE = "0";
    /**
     * 1是alpha版
     */
    public static final String APP_ABR_ALPHA = "1";
    /**
     * 2是beta版
     */
    public static final String APP_ABR_BETA = "2";

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);

    public static BasicFuncUtil getInstance() {
        if (sInstance == null) {
            sInstance = new BasicFuncUtil();
        }
        return sInstance;
    }

    private BasicFuncUtil() {
        init();
    }

    private void init() {
        mContext = SinaBookApplication.gContext;

        App.setContext(mContext, new Handler(mContext.getMainLooper()));
        float version = ConstantData.getVersion(mContext);
        App.setPdPidAbrVer(APP_PDSTR, APP_PID, APP_ABR_RELEASE, version);

        // 打log用
        LogUtil.gDebug2File = false;
        LogUtil.gDebug2Log = SinaBookApplication.isDebug();

        initSendMessage();
    }

    /**
     * 初始化意见反馈消息
     */
    private void initSendMessage() {
        SendSuggestionActivity.init(R.color.public_bg, R.drawable.shelves_bar_bg,
                com.sina.book.R.drawable.return_button_bg, mContext.getString(R.string
                .address_weibo), true);

        SendSuggestionActivity.setGetCfg(new GetCfg() {

            @Override
            public String getCfgStr() {
                StringBuilder sb = new StringBuilder();
                ArrayList<DownBookJob> jobs = DownBookManager.getInstance().getAllJobs();
                int n = jobs.size();
                // 最多上传9本书的书名
                for (int i = 0; i < Math.min(9, n); i++) {
                    String bookTitle = jobs.get(i).getBook().getTitle();
                    sb.append("《");
                    sb.append(bookTitle);
                    sb.append("》");
                }
                return sb.toString();
            }
        });

        SendSuggestionActivity.setGetAddition(new GetAddition() {

            @Override
            public String getAddition() {
                return KEY_ADDITION;
            }
        });
    }

    /**
     * 安装统计
     */
    public void recordInstall() {
        String today = mDateFormat.format(new Date());
        String prefDay = StorageUtil.getString(StorageUtil.KEY_INSTALL_DATE);
        if (!today.equals(prefDay)) {
            Inst.setProductName("SinaBook");
            Inst.sendInst(mContext);
            StorageUtil.saveString(StorageUtil.KEY_INSTALL_DATE, today);
        }
    }

    /**
     * 意见反馈
     *
     * @param activity 当前界面
     */
    public void sendSuggestion(Activity activity) {
        Intent intent = new Intent(activity, SendSuggestionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        activity.startActivity(intent);
    }
}
