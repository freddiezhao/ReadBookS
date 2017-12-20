package com.sina.book.ui;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.AuthorPageResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.AuthorPageParser;
import com.sina.book.parser.SimpleParser;
import com.sina.book.ui.adapter.CommonListAdapter;
import com.sina.book.ui.adapter.ListAdapter;
import com.sina.book.ui.widget.XScrollView;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.Util;

/**
 * 作者主页
 * 
 * @author MarkMjw
 * @date 13-9-25.
 */
public class AuthorActivity extends CustomTitleActivity implements ITaskFinishListener,
		AdapterView.OnItemClickListener, View.OnClickListener, XScrollView.IXScrollViewListener {
	/**
	 * 作者类型.
	 */
	public static final int TYPE_AUTHOR = 0;
	/**
	 * 员工类型.
	 */
	public static final int TYPE_STAFF = 1;
	/**
	 * 普通用户类型
	 */
	public static final int TYPE_USER = 2;

	/**
	 * 用户信息请求类型.
	 */
	private static final String REQ_TYPE_USER_INFO = "userInfo";
	/**
	 * 关注请求类型.
	 */
	private static final String REQ_TYPE_ATTENTION = "attention";

	/**
	 * 每页书籍数.
	 */
	private static final int PAGE_NUM = 10;

	private View mProgressView;
	private View mErrorView;

	private XScrollView mScrollView;

	private ImageView mAvatarIv;
	private TextView mNameTv;
	private TextView mPostTv;
	private TextView mBookNumTv;
	private TextView mIntroTv;
	private TextView mAttentionTv;

	private ListView mListView;
	private ListAdapter<Book> mAdapter;

	private String mUid;
	private String mTitle;
	private int mType = TYPE_AUTHOR;

	// private int mCurPage = 1;

	/**
	 * 启动
	 * 
	 * @param context
	 * @param uid
	 */
	public static void launch(Context context, String title, String uid, int type) {
		Bundle bundle = new Bundle();
		bundle.putString("title", title);
		bundle.putString("uid", uid);
		bundle.putInt("type", type);

		Intent intent = new Intent();
		intent.setClass(context, AuthorActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		intent.putExtras(bundle);

		context.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_author);

		parseIntent();
		initTitle();
		initView();

		requestData(1);
	}

	@Override
	protected void retry() {
		requestData(1);
	}

	@Override
	public void onClickLeft() {
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position -= mListView.getHeaderViewsCount();

		if (position < mAdapter.getDataSize() && position >= 0) {
			// 进入详情
			String eventKey = "";
			String eventExtra = mUid;
			if (mType == TYPE_AUTHOR) {
				eventKey = "作者详情_01_" + Util.formatNumber(position + 1);
			} else if (mType == TYPE_STAFF) {
				eventKey = "员工推荐_01_" + Util.formatNumber(position + 1);
			} else if (mType == TYPE_USER) {
				eventKey = "用户推荐_01_" + Util.formatNumber(position + 1);
			}
			BookDetailActivity.launch(this, ((Book) mAdapter.getItem(position)), eventKey, eventExtra);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.attention_user:
			addAttention();
			break;

		default:
			break;
		}
	}

	private void parseIntent() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (null != bundle) {
			mTitle = bundle.getString("title");
			mUid = bundle.getString("uid");
			mType = bundle.getInt("type");
		}
	}

	private void initTitle() {
		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);

		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		if (null != middle) {
			middle.setEllipsize(TextUtils.TruncateAt.END);
			middle.setText(mTitle);
			setTitleMiddle(middle);
		}
	}

	protected void initView() {
		mProgressView = findViewById(R.id.progress_view);
		mErrorView = findViewById(R.id.error_view);

		mScrollView = (XScrollView) findViewById(R.id.scroll_view);
		mScrollView.setPullRefreshEnable(false);
		mScrollView.setPullLoadEnable(true);
		mScrollView.setIXScrollViewListener(this);

		View content = LayoutInflater.from(this).inflate(R.layout.vw_author_detail_layout, null);

		if (null != content) {
			ImageView headerImg = (ImageView) content.findViewById(R.id.background_img);
			mAvatarIv = (ImageView) content.findViewById(R.id.user_avatar);
			mNameTv = (TextView) content.findViewById(R.id.user_name);
			mPostTv = (TextView) content.findViewById(R.id.user_post);
			mBookNumTv = (TextView) content.findViewById(R.id.user_book);
			mIntroTv = (TextView) content.findViewById(R.id.user_des);
			mAttentionTv = (TextView) content.findViewById(R.id.attention_user);
			TextView userBooksTip = (TextView) content.findViewById(R.id.user_books_title);
			mListView = (ListView) content.findViewById(R.id.user_books);

			mAttentionTv.setOnClickListener(this);

			mAdapter = new CommonListAdapter(this, CommonListAdapter.TYPE_AUTHOR);
			mListView.setFocusable(false);
			mListView.setFocusableInTouchMode(false);
			mListView.setAdapter(mAdapter);
			mListView.setOnItemClickListener(this);

			if (TYPE_STAFF == mType || TYPE_USER == mType) {
				headerImg.setImageResource(R.drawable.author_title_bg0);
				userBooksTip.setText(R.string.user_recommend_tip);
			} else {
				headerImg.setImageResource(R.drawable.author_title_bg1);
				userBooksTip.setText(R.string.user_book_tip);
			}
		}

		mScrollView.setView(content);
	}

	@Override
	public void onRefresh() {
		// ignore
	}

	@Override
	public void onLoadMore() {
		if (!HttpUtil.isConnected(this)) {
			shortToast(R.string.network_unconnected);
			mScrollView.stopLoadMore();
			return;
		}

		if (mAdapter.IsAdding()) {
			return;
		}

		if (mAdapter.hasMore()) {
			requestData(mAdapter.getCurrentPage() + 1);
		}
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		RequestTask task = (RequestTask) taskResult.task;
		String type = task.getType();
		int code = taskResult.stateCode;
		Object result = taskResult.retObj;

		if (REQ_TYPE_ATTENTION.equals(type)) {
			if (code == HttpStatus.SC_OK && result != null) {
				if (ConstantData.CODE_SUCCESS.equals(String.valueOf(taskResult.retObj))) {
					mAttentionTv.setText("已关注");
					mAttentionTv.setEnabled(false);
					return;
				}
			}
			shortToast("关注失败");

		} else if (REQ_TYPE_USER_INFO.equals(type)) {
			// 停止加载更多
			mScrollView.stopLoadMore();

			int currPage = (Integer) task.getExtra();
			if (code == HttpStatus.SC_OK && result != null) {
				if (taskResult.retObj instanceof AuthorPageResult) {
					AuthorPageResult data = (AuthorPageResult) result;
					// 数据有效的情况下才显示
					if (data != null && data.getBooks().size() != 0) {
						mAdapter.setCurrentPage(currPage);
						updateData(data);
					} else {
						// 方案1
						// 加载更多时按照数据异常处理
						// if (currPage != 1) {
						// visibleDataErrorState();
						// } else {
						// // 首次获取数据时按照网络问题处理
						// visibleErrorState();
						// }
						// 方案2
						// 竟然网络是正常的(code == HttpStatus.SC_OK)
						// 那么不应该执行visibleErrorState
						visibleDataErrorState();
					}
				} else {
					if (mAdapter.IsAdding()) {
						mAdapter.setAdding(false);
					}
				}

				visibleSuccessState();
			} else {
				// 网络请求正常，但是数据异常
				if (code == HttpStatus.SC_OK) {
					if (currPage != 1) {
						visibleDataErrorState();
					} else {
						visibleErrorState();
					}
				} else {
					visibleErrorState();
				}
			}
		}
	}

	private void visibleSuccessState() {
		mProgressView.setVisibility(View.GONE);
		mErrorView.setVisibility(View.GONE);
		mScrollView.setVisibility(View.VISIBLE);
	}

	private void visibleErrorState() {
		mProgressView.setVisibility(View.GONE);
		mErrorView.setVisibility(View.VISIBLE);
		mScrollView.setVisibility(View.GONE);
	}

	private void visibleDataErrorState() {
		shortToast(R.string.data_error);
		// 变更正在加载更多的属性
		if (mAdapter.IsAdding()) {
			mAdapter.setAdding(false);
		}
	}

	private void requestData(int page) {
		if (page == 1) {
			if (HttpUtil.isConnected(this)) {
				mScrollView.setVisibility(View.GONE);
				mErrorView.setVisibility(View.GONE);
				mProgressView.setVisibility(View.VISIBLE);
			} else {
				mScrollView.setVisibility(View.GONE);
				mErrorView.setVisibility(View.VISIBLE);
				mProgressView.setVisibility(View.GONE);
				return;
			}
		} else {
			mAdapter.setAdding(true);
			mAdapter.notifyDataSetChanged();
		}

		String url;
		if (TYPE_STAFF == mType || TYPE_USER == mType) {
			url = String.format(Locale.CHINA, ConstantData.URL_USER_DETAIL, mUid, 2, page, PAGE_NUM);
		} else {
			url = String.format(Locale.CHINA, ConstantData.URL_AUTHOR_INFO, mUid, page, PAGE_NUM);
		}

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, url);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

		RequestTask task = new RequestTask(new AuthorPageParser());
		task.setTaskFinishListener(this);
		task.setExtra(page);
		task.setType(REQ_TYPE_USER_INFO);
		task.execute(params);
	}

	private void updateData(AuthorPageResult result) {
		if (null == result) {
			return;
		}

		String imgUrl = result.getImgUrl();
		if (imgUrl != null && !imgUrl.contains("http://")) {
			imgUrl = null;
		}
		ImageLoader.getInstance().load(imgUrl, mAvatarIv, ImageLoader.getDefaultAvatar(),
				ImageLoader.getDefaultMainAvatar());

		mNameTv.setText(result.getName());
		mPostTv.setText("粉丝：" + result.getFansCount() + "人");

		String intro = result.getIntro();
		if (TextUtils.isEmpty(intro)) {
			intro = getString(R.string.no_intro);
		}
		mIntroTv.setText(intro);

		// if (TYPE_STAFF == mType) {
		// if (mUid.equals(LoginUtil.getLoginInfo().getUID())) {
		// mAttentionTv.setVisibility(View.GONE);
		// } else {
		// mAttentionTv.setVisibility(View.VISIBLE);
		// }
		// } else {
		// mAttentionTv.setVisibility(View.GONE);
		// }

		StringBuilder builder = new StringBuilder(TYPE_STAFF == mType || TYPE_USER == mType ? "书单：" : "著书：");
		int start = builder.length();
		builder.append(result.getBookCount());
		int end = builder.length();
		builder.append("本");
		int color = ResourceUtil.getColor(R.color.praise_num_color);
		Spanned spanned = Util.highLight(builder, color, start, end);
		mBookNumTv.setText(spanned);

		if (mAdapter.IsAdding()) {
			mAdapter.setAdding(false);
			mAdapter.addList(result.getBooks());
			mAdapter.setTotalAndPerpage(result.getBookCount(), PAGE_NUM);
		} else {
			mAdapter.setList(result.getBooks());
			mAdapter.setTotalAndPerpage(result.getBookCount(), PAGE_NUM);
		}

		mAdapter.notifyDataSetChanged();
		Util.measureListViewHeight(mListView);

		if (!mAdapter.hasMore()) {
			mScrollView.setPullLoadEnable(false);
		} else {
			mScrollView.setPullLoadEnable(true);
		}
	}

	private void addAttention() {
		if (LoginUtil.isValidAccessToken(this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS)
			return;

		RequestTask task = new RequestTask(new SimpleParser());

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, ConstantData.URL_ATTENTION_WEIBO);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

		ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
		strNParams.add(new BasicNameValuePair("source", ConstantData.AppKey));
		strNParams.add(new BasicNameValuePair("access_token", LoginUtil.getLoginInfo().getAccessToken()));
		strNParams.add(new BasicNameValuePair("uid", mUid));
		task.setPostParams(strNParams);
		task.setTaskFinishListener(this);
		task.setType(REQ_TYPE_ATTENTION);
		task.execute(params);
	}
}
