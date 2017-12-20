package com.sina.book.ui.view;

import java.util.ArrayList;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.ReadActivity;
import com.sina.book.ui.adapter.BookImageAdapter2;
import com.sina.book.ui.widget.HorizontalListView;
import com.sina.book.useraction.Constants;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ReadPageDimenUtil;
import com.sina.book.util.ResourceUtil;

/**
 * 阅读完成View，附属于【阅读页】
 * 
 * @author MarkMjw
 * @date 2012-12-27
 */
public class ReadCompleteView extends RelativeLayout implements OnClickListener, ITaskFinishListener {
	// private static final String TAG = "ReadCompleteView";

	/** 阅读完整本书籍 */
	public static final int TYPE_ALL_BOOK = 0x00;
	/** 阅读完试读章节 */
	public static final int TYPE_READ_CHAPTER = 0x01;

	/** 赞、评论图标的高度、宽度 */
	private final int PRAISE_COMMENT_BOUND = PixelUtil.dp2px(21.33f);

	private TextView mTitleTv;
	private TextView mReadCompleteTv;

	private TextView mPraiseBtn;
//	private TextView mCommentBtn;

	private Button mContinueBtn;

	private View mRecommendView;

	private int mType = TYPE_READ_CHAPTER;
	private String mBookId;

	private IViewOnClickListener mListener;

	private ReadStyleManager mStyleManager;

	private HorizontalListView mRelatedPersonListView;
	private BookImageAdapter2 mRelatedPersonAdapter;

	// private CheckBox mSubscriptionCheckbox;

	private RequestTask mTask;

	public ReadCompleteView(Context context) {
		this(context, null);
	}

	public ReadCompleteView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ReadCompleteView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	@Override
	public void onClick(View v) {
		// 这里的点击事件统一交给实现接口的类处理
		if (null != mListener) {
			mListener.viewOnClick(v);
		}
	}

	private void initView(Context context) {
		if (isInEditMode()) {
			return;
		}

		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.list_divide_dot);
		BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
		drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		drawable.setDither(true);

		mStyleManager = ReadStyleManager.getInstance(context);
		float fontSize = ReadPageDimenUtil.getPixel(mStyleManager.getCurReadFontSizeInSp(),
				ReadPageDimenUtil.TITILE_FONT_SIZE);

		LayoutInflater.from(context).inflate(R.layout.vw_read_complete_view, this);
		mTitleTv = (TextView) findViewById(R.id.reading_complete_title);
		mTitleTv.setTextSize(PixelUtil.px2sp(fontSize));
		mReadCompleteTv = (TextView) findViewById(R.id.reading_complete_tip);

		mPraiseBtn = (TextView) findViewById(R.id.reading_complete_praise_txt);
//		mCommentBtn = (TextView) findViewById(R.id.reading_complete_comment_txt);

		// 继续阅读
		mContinueBtn = (Button) findViewById(R.id.reading_continue_button);

		mRecommendView = findViewById(R.id.reading_complete_recommended);
		ImageView divider = (ImageView) findViewById(R.id.reading_complete_recommended_divider);
		divider.setBackgroundDrawable(drawable);

		// mSubscriptionCheckbox = (CheckBox)
		// findViewById(R.id.reading_subscription_checkbox);

		mPraiseBtn.setOnClickListener(this);
//		mCommentBtn.setOnClickListener(this);
		mContinueBtn.setOnClickListener(this);
		// mSubscriptionCheckbox.setOnCheckedChangeListener(new CompoundButton
		// .OnCheckedChangeListener() {
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView, boolean
		// isChecked) {
		// ReadActivity.gBook.setRemind(isChecked);
		// PushHelper.getInstance().updateRemindBooks();
		// }
		// });

		mRelatedPersonAdapter = new BookImageAdapter2(context, "阅读器", "喜欢本书的人还喜欢", null);
		mRelatedPersonAdapter.setActionType(Constants.CLICK_READ_RELATED_BOOK);
		mRelatedPersonListView = (HorizontalListView) findViewById(R.id.read_complete_related_book_listview);
		mRelatedPersonListView.setAdapter(mRelatedPersonAdapter);
	}

	private void updateView() {
		if (mType == TYPE_ALL_BOOK) {
			if (ReadActivity.gBook.isSeriesBook()) {
				mReadCompleteTv.setText(R.string.reading_complete_book);

				// mSubscriptionCheckbox.setChecked(ReadActivity.gBook.isRemind());
				// mSubscriptionCheckbox.setVisibility(View.VISIBLE);

			} else {
				mReadCompleteTv.setText(R.string.reading_complete_book_all);
				mReadCompleteTv.setVisibility(View.VISIBLE);

				// mSubscriptionCheckbox.setVisibility(View.GONE);
			}

			mContinueBtn.setVisibility(View.GONE);

		} else {
			mReadCompleteTv.setText(R.string.reading_complete_chapter);

			// if (ReadActivity.gBook.isSeriesBook()) {
			// mSubscriptionCheckbox.setChecked(ReadActivity.gBook.isRemind());
			// mSubscriptionCheckbox.setVisibility(View.VISIBLE);
			// } else {
			// mSubscriptionCheckbox.setVisibility(View.GONE);
			// }

			mContinueBtn.setVisibility(View.VISIBLE);
		}

		updatePraiseBtnStatus();
	}

	/**
	 * 更新赞按钮状态
	 */
	public void updatePraiseBtnStatus() {
		if (ReadActivity.gBook.hasPraised()) {
			mPraiseBtn.setText(ResourceUtil.getString(R.string.book_detail_has_praised));
			mPraiseBtn.setTextColor(ResourceUtil.getColor(R.color.book_detail_checked_font_color));

			Drawable drawable = ResourceUtil.getDrawable(R.drawable.book_detail_praise_pressed);
			drawable.setBounds(0, 0, PRAISE_COMMENT_BOUND, PRAISE_COMMENT_BOUND);
			mPraiseBtn.setCompoundDrawables(drawable, null, null, null);

			mPraiseBtn.setEnabled(false);
		} else {
			mPraiseBtn.setText(ResourceUtil.getString(R.string.book_detail_praise_text));
			mPraiseBtn
					.setTextColor(ResourceUtil.getColorStateList(R.drawable.selector_book_detail_praise_comment_font));

			Drawable drawable = ResourceUtil.getDrawable(R.drawable.selector_book_detail_praise);
			drawable.setBounds(0, 0, PRAISE_COMMENT_BOUND, PRAISE_COMMENT_BOUND);
			mPraiseBtn.setCompoundDrawables(drawable, null, null, null);

			mPraiseBtn.setEnabled(true);
		}
	}

	/**
	 * 显示
	 * 
	 * @param type
	 *            类型<整本书籍、在线试读>
	 * @param title
	 *            显示标题
	 */
	public void show(int type, String title, String bookId) {
		mBookId = bookId;
		mType = type;
		mTitleTv.setText(title);

		updateView();
		reqBookExt();

		setVisibility(VISIBLE);
	}

	/**
	 * 是否正显示
	 */
	public boolean isShowing() {
		return View.VISIBLE == getVisibility();
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		if (taskResult.retObj != null && taskResult.stateCode == HttpStatus.SC_OK) {
			if (taskResult.retObj instanceof BookRelatedData) {
				BookRelatedData data = (BookRelatedData) taskResult.retObj;

				if (mType == TYPE_ALL_BOOK) {
					mRelatedPersonAdapter.setBookId(mBookId);
					mRelatedPersonAdapter.setList(data.getCateItems());
					mRelatedPersonAdapter.notifyDataSetChanged();

					setHListLayout(mRelatedPersonListView);
					mRecommendView.setVisibility(VISIBLE);
				}
			}
		}
	}

	/**
	 * 请求书籍详情的扩展内容，这里只是为了获取相关书籍推荐
	 */
	private void reqBookExt() {
		if (mType != TYPE_ALL_BOOK) {
			mRecommendView.setVisibility(GONE);
			return;
		}

		if (mRelatedPersonAdapter != null) {
			String preBookId = mRelatedPersonAdapter.getBookId();
			if (preBookId != null && !preBookId.equals(mBookId)) {
				// 置空数据源
				mRelatedPersonAdapter.setBookId(mBookId);
				// 滚动到起始位置
				mRelatedPersonListView.scrollTo(0);
				mRelatedPersonAdapter.setList(new ArrayList<Book>());
			}
		}

		String reqUrl = String.format(ConstantData.URL_BOOK_INFO_EXT, ReadActivity.gBook.getBookId(),
				ReadActivity.gBook.getSid(), ReadActivity.gBook.getBookSrc());

		// 书籍列表不为空则返回
		if (mRelatedPersonAdapter.getDataSize() > 0) {
			mRelatedPersonListView.setAdapter(mRelatedPersonAdapter);
			mRecommendView.setVisibility(VISIBLE);
			return;
		}

		// task否存在且url未变
		if (null != mTask && reqUrl.equalsIgnoreCase(mTask.getType())) {
			return;
		}

		mRecommendView.setVisibility(GONE);
		mTask = new RequestTask(new BookRelatedParser());
		mTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		mTask.setType(reqUrl);
		mTask.execute(params);
	}

	/**
	 * 设置水平ListView的高度
	 * 
	 * @param listView
	 */
	private void setHListLayout(HorizontalListView listView) {
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		if (null != params) {
			params.height = BookImageAdapter2.ITEM_HEIGHT;
			listView.setLayoutParams(params);
		}

		listView.setVisibility(View.VISIBLE);
		float width = mStyleManager.getScreenWidth() - 2 * PixelUtil.dp2px(13.33f);
		double dividerWidth = (width - 3.5 * BookImageAdapter2.ITEM_WIDTH) / 3;
		listView.setDividerWidth((int) dividerWidth);
	}

	/**
	 * 设置子View的点击事件监听器
	 * 
	 * @param listener
	 */
	public void setViewClickListener(IViewOnClickListener listener) {
		this.mListener = listener;
	}

	/**
	 * View的点击事件监听接口
	 * 
	 * @author MarkMjw
	 */
	public interface IViewOnClickListener {

		/**
		 * View的点击事件
		 * 
		 * @param view
		 */
		public void viewOnClick(View view);
	}
}
