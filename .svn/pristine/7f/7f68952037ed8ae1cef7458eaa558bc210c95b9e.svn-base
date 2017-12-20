package com.sina.book.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import com.sina.book.R;
import com.sina.book.util.StorageUtil;

import java.util.Calendar;

/**
 * 评分对话框
 *
 * @author MarkMjw
 * @date 2013-10-11
 */
public class CommentDialog extends Dialog implements View.OnClickListener{
    private Context mContext;

    public CommentDialog(Context context) {
        super(context, R.style.CommentDialog);

        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vw_comment_dialog);

        View yes = findViewById(R.id.comment_yes);
        View no = findViewById(R.id.comment_no);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

        setCancelable(false);
    }

    @Override
    public void show() {
        getWindow().setWindowAnimations(R.style.PopWindowAnimation);
        super.show();
    }

    @Override
    public void onClick(View v) {
        dismiss();

        switch (v.getId()) {
            case R.id.comment_yes:
                String packetName = mContext.getPackageName();
                Uri uri = Uri.parse("market://details?id=" + packetName);
                Intent callIntent = new Intent(Intent.ACTION_VIEW, uri);
                mContext.startActivity(Intent.createChooser(callIntent, "完成动作的方式"));

                StorageUtil.saveBoolean(StorageUtil.KEY_NEED_SHOW_COMMENT_APP, false);
                saveCurrentDate();
                break;

            case R.id.comment_no:
                saveCurrentDate();
                break;
        }
    }

    /**
     * 保存当前日期的开始时间
     */
    private void saveCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        StorageUtil.saveLong(StorageUtil.KEY_SHOW_COMMENT_APP_DATE, calendar.getTimeInMillis());
    }
}
