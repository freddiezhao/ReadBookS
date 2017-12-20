package com.sina.book.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.sina.book.R;

public class AboutActivity extends CustomTitleActivity {

    private TextView mHomePage;
    private TextView mWeiboPage;

    @Override
    protected void init(Bundle savedInstanceState) {
        setContentView(R.layout.act_about);
        initTitle();
        mHomePage = (TextView) findViewById(R.id.home_page);
        mWeiboPage = (TextView) findViewById(R.id.weibo_page);
        String homeurl = "http://book.sina.cn/prog/wapsite/books/h5/sinaread_download.php";
        SpannableStringBuilder homeUrlSb = new SpannableStringBuilder(homeurl);
        homeUrlSb.setSpan(new DefinedUrlSpan(
                "http://book.sina.cn/prog/wapsite/books/h5/sinaread_download.php"), 0,
                homeurl.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mHomePage.setMovementMethod(LinkMovementMethod.getInstance());
        mHomePage.setText(homeUrlSb);

        String weibourl = "http://m.weibo.cn/58331";
        SpannableStringBuilder weiboUrlSb = new SpannableStringBuilder(weibourl);
        weiboUrlSb.setSpan(new DefinedUrlSpan("http://m.weibo.cn/58331"), 0,
                weibourl.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mWeiboPage.setMovementMethod(LinkMovementMethod.getInstance());
        mWeiboPage.setText(weiboUrlSb);
    }

    private void initTitle() {
        TextView middleV = (TextView) LayoutInflater.from(this).inflate(
                R.layout.vw_title_textview, null);
        middleV.setText(R.string.about_title);
        setTitleMiddle(middleV);

        View leftV = LayoutInflater.from(this).inflate(
                R.layout.vw_generic_title_back, null);
        setTitleLeft(leftV);
    }

    @Override
    public void onClickLeft() {
        this.finish();
    }

    public class DefinedUrlSpan extends ClickableSpan {

        private String mUrl;

        public DefinedUrlSpan(String url) {
            mUrl = url;
        }

        @Override
        public void onClick(View widget) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri uri = Uri.parse(mUrl);
            intent.setData(uri);
            mContext.startActivity(intent);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(true);
        }
    }
}
