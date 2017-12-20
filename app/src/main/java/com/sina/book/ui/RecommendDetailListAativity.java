package com.sina.book.ui;

import java.util.ArrayList;

import org.htmlcleaner.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.ui.adapter.BookAdapter;

/**
 * 当推荐内容为书籍列表时，都可以通过它来显示
 * 
 * @author Tsimle
 * 
 */
public class RecommendDetailListAativity extends CustomTitleActivity implements
        OnItemClickListener {

    private ListView mListLv;
    private BookAdapter mAdapter;
    private ArrayList<Book> mList;
    private String mTitle;

    public static boolean launch(Context c, ArrayList<Book> books, String title) {
        if (books == null || books.size() == 0) {
            return false;
        }
        Intent intent = new Intent();
        intent.setClass(c, RecommendDetailListAativity.class);
        intent.putExtra("lists", books);
        intent.putExtra("title", title);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        c.startActivity(intent);
        return true;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        setContentView(R.layout.act_recommond_detail);
        initIntent();
        initTitle();
        initViews();
    }

    @Override
    public void onClickLeft() {
        finish();
        super.onClickLeft();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // 进入详情
        BookDetailActivity.launch(this, ((Book) mAdapter.getItem(arg2)));
    }

    private void initTitle() {
        View left = LayoutInflater.from(this).inflate(
                R.layout.vw_generic_title_back, null);
        setTitleLeft(left);

        TextView middleTv = (TextView) LayoutInflater.from(this).inflate(
                R.layout.vw_title_textview, null);
        middleTv.setText(mTitle);
        setTitleMiddle(middleTv);
    }

    private void initViews() {
        mListLv = (ListView) findViewById(R.id.recommond_detail_lv);

        mAdapter = new BookAdapter(this);
        mAdapter.addList(mList);
        mListLv.setAdapter(mAdapter);
        mListLv.setOnItemClickListener(this);
    }

    @SuppressWarnings("unchecked")
    private void initIntent() {
        Intent intent = getIntent();
        mList = (ArrayList<Book>) intent.getSerializableExtra("lists");
        mTitle = intent.getStringExtra("title");
        if (Utils.isEmptyString(mTitle)) {
            mTitle = getString(R.string.recomond);
        }
    }
}
