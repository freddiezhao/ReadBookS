package com.sina.book.ui;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.FamousRecommend;
import com.sina.book.data.ListResult;
import com.sina.book.parser.FamousRecommendParser;
import com.sina.book.ui.adapter.FamousRecommendAdapter;
import com.sina.book.ui.widget.XListView;
import com.sina.book.util.HttpUtil;

/**
 * 【书城】-推荐页[名人推荐页面]
 * 
 * @author MarkMjw
 * @date 2013-4-9
 */
public class FamousRecommendActivity extends CustomTitleActivity implements OnItemClickListener,
        ITaskFinishListener {
    // private static final String TAG = "FamousRecommendActivity";

    private XListView mListView;
    private View mProgressView;
    private View mErrorView;

    private FamousRecommendAdapter mAdapter;
    
    public static void launch(Context c) {
        Intent intent = new Intent();
        intent.setClass(c, FamousRecommendActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        c.startActivity(intent);
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        setContentView(R.layout.act_common_book_list);
        
        initTitle();
        initViews();
        requsetData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position -= mListView.getHeaderViewsCount();

        if (position >= 0 && position < mAdapter.getDataSize()) {
            // 进入详情
            FamousRecommend item = (FamousRecommend) mAdapter.getItem(position);
            FamousDetailActivity.launch(this, item.getUid(), item.getScreenName());
        }
    }

    @Override
    protected void retry() {
        requsetData();
    }

    @Override
    public void onClickLeft() {
        finish();
        super.onClickLeft();
    }

    @Override
    public void onTaskFinished(TaskResult taskResult) {
        mProgressView.setVisibility(View.GONE);

        if (taskResult.retObj != null && taskResult.stateCode == HttpStatus.SC_OK) {
            if (taskResult.retObj instanceof ListResult<?>) {
                @SuppressWarnings("unchecked")
                ListResult<FamousRecommend> famousResult = (ListResult<FamousRecommend>) taskResult.retObj;

                if (famousResult.isSucc()) {
                    mAdapter.setList(famousResult.getList());
                    mAdapter.notifyDataSetChanged();
                }

                mErrorView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
                return;
            }
        }
        mListView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
    }

    private void initTitle() {
        TextView middleTv = (TextView) LayoutInflater.from(this).inflate(
                R.layout.vw_title_textview, null);
        middleTv.setText(R.string.recommend_famous);
        setTitleMiddle(middleTv);

        View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back,
                null);
        setTitleLeft(left);
    }

    private void initViews() {
        mListView = (XListView) findViewById(R.id.lv_books);
        mListView.setOnItemClickListener(this);
        mListView.setPullRefreshEnable(false);
        mListView.setPullLoadEnable(false);
        mListView.setDivider(null);

        mProgressView = findViewById(R.id.rl_progress);
        mErrorView = findViewById(R.id.error_layout);

        mAdapter = new FamousRecommendAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    private void requsetData() {
        if (!HttpUtil.isConnected(this)) {
            mProgressView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            return;
        } else {
            mProgressView.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
        }

        String url = ConstantData.URL_FAMOUS_RECOMMEND;
        RequestTask reqTask = new RequestTask(new FamousRecommendParser());
        reqTask.setTaskFinishListener(this);
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, url);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        reqTask.execute(params);

        mAdapter.setAdding(true);
        mAdapter.notifyDataSetChanged();
    }
}
