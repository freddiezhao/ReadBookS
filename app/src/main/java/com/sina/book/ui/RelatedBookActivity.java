package com.sina.book.ui;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.BookRelatedData;
import com.sina.book.data.ConstantData;
import com.sina.book.parser.BookRelatedParser;
import com.sina.book.ui.adapter.CommonListAdapter;
import com.sina.book.util.HttpUtil;

/**
 * 相关书籍推荐页面
 * 
 * @author YangZhanDong
 * 
 */
public class RelatedBookActivity extends CustomTitleActivity implements OnItemClickListener, ITaskFinishListener,
        OnClickListener {

    private String mBookId;
    private String mBookSrc;
    private String mBookSid;

    private ListView mListView;
    private CommonListAdapter mAdapter;

    private View mProgressView;
    private View mErrorView;

    public static void launch(Context context, String bookId, String bookSid, String bookSrc) {
        Intent intent = new Intent();
        intent.setClass(context, RelatedBookActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        intent.putExtra("bid", bookId);
        intent.putExtra("sid", bookSid);
        intent.putExtra("src", bookSrc);
        context.startActivity(intent);

    }

    @Override
    protected void init(Bundle savedInstanceState) {
        setContentView(R.layout.act_related_book_recommend);

        initIntent();
        initTitle();
        initViews();
        initData();
    }

    private void initIntent() {
        Intent intent = getIntent();
        mBookId = intent.getStringExtra("bid");
        mBookSid = intent.getStringExtra("sid");
        mBookSrc = intent.getStringExtra("src");
    }

    private void initTitle() {
        View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
        setTitleLeft(left);

        TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
        middle.setText(R.string.related_book_title);
        setTitleMiddle(middle);
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.related_book_listview);
        mListView.setOnItemClickListener(this);
        mListView.setDivider(null);
        mAdapter = new CommonListAdapter(this, CommonListAdapter.TYPE_RECOMMEND);
        mListView.setAdapter(mAdapter);

        View emptyView = findViewById(R.id.to_bookstore);
        Button goStoreBtn = (Button) emptyView.findViewById(R.id.book_home_btn);
        emptyView.setOnClickListener(null);
        goStoreBtn.setOnClickListener(this);
        mListView.setEmptyView(emptyView);

        mProgressView = findViewById(R.id.progress_layout);
        mErrorView = findViewById(R.id.error_layout);
    }

    private void initData() {
        if (!HttpUtil.isConnected(this)) {
            mErrorView.setVisibility(View.VISIBLE);
        } else {
            mErrorView.setVisibility(View.GONE);
            mProgressView.setVisibility(View.VISIBLE);

            reqRelatedBooks();
        }
    }

    @Override
    public void onClickLeft() {
        finish();
    }

    @Override
    protected void retry() {
        mErrorView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
        reqRelatedBooks();
    }

    @Override
    public void onTaskFinished(TaskResult taskResult) {
        mProgressView.setVisibility(View.GONE);
        if (taskResult.retObj != null && taskResult.stateCode == HttpStatus.SC_OK) {
            BookRelatedData data = (BookRelatedData) taskResult.retObj;

            mAdapter.setList(data.getCateItems());
            mAdapter.notifyDataSetChanged();
        } else {
            mErrorView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= 0 && position < mAdapter.getDataSize()) {
            BookDetailActivity.launch(this, ((Book) mAdapter.getItem(position)));
        }
    }

    private void reqRelatedBooks() {
        String reqUrl = String.format(ConstantData.URL_BOOK_INFO_EXT, mBookId, mBookSid, mBookSrc);

        RequestTask reqTask = new RequestTask(new BookRelatedParser());
        reqTask.setTaskFinishListener(this);
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        reqTask.execute(params);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.book_home_btn:
            // 去书城
            MainTabActivity.launch(this);
            break;

        default:
            break;
        }
    }

}