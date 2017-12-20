package com.sina.book.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.image.ImageUtil;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.ReadActivity;
import com.sina.book.ui.widget.BorderImageView;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.ui.widget.SwitchButton;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.LogUtil;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.ThemeUtil;

import java.util.ArrayList;

/**
 * 阅读页工具栏，附属于【阅读页】
 * 
 * @author MarkMjw
 * @date 13-6-25.
 */
public class ReadToolbar extends RelativeLayout implements View.OnClickListener, AdapterView.OnItemClickListener,
		CompoundButton.OnCheckedChangeListener {

	private final int ITEM_WIDTH = (int) ResourceUtil.getDimens(R.dimen.setting_theme_img_width);
	private final int ITEM_HEIGHT = (int) ResourceUtil.getDimens(R.dimen.setting_theme_img_height);
	private final int MSG_HIDE_NAVIGATIONBAR = 0x1001;
	private final int MSG_SHOW_NAVIGATIONBAR = 0x1002;

	private Context mContext;

	/**
	 * 工具条按钮.
	 */
	private View mTopToolBar;
	private View mBottomToolBar;

	/**
	 * 顶部工具栏相关按钮.
	 */
	private ImageView mToolbarReturn;
	private ImageView mToolbarCatalog;
	private ImageView mUpdateFlag;
	private ImageView mToolbarDown;
	private ImageView mToolbarMark;
	private ImageView mToolbarBookInfo;

	/**
	 * 底部工具栏相关按钮.
	 */
	private TextView mToolbarProgress;
	private TextView mToolbarSetting;
	private TextView mToolbarComment;
	private TextView mToolbarNight;

	private SeekBar mToolbarSeekBar;
	private TextView mSeekInfo;
	private EllipsizeTextView mChapterTitle;
	private TextView mPreChapterBtn;
	private TextView mNextChapterBtn;

	private View mToolbarProgressView;
	private View mToolbarSettingView;
	private View mToolbarCommentView;

	private ReadStyleManager mStyleManager;

	private ImageView mFontSizeInc;
	private ImageView mFontSizeDec;

	private TextView mFlipBtn;
	private TextView mSlideBtn;
	private TextView mScrollBtn;

	private TextView mPraiseBtn;
	private TextView mCommentBtn;

	private SeekBar mSeekBar;

	// private RelativeLayout mUpdateLayout;
	// private SwitchButton mUpdateRemind;
	private SwitchButton mAutoBright;

	private ImageAdapter mImageAdapter;

	private BitmapDrawable mDotReal;

	private ISettingChangedListener mListener;

	private Handler mHandler = new Handler(Looper.getMainLooper());

	/**
	 * 推荐图标的高度、宽度
	 */
	private final int PRAISE_BOUND = PixelUtil.dp2px(21.33f);

	/**
	 * 白天、夜间图标的高度、宽度
	 */
	private final int NIGHT_BOUND = PixelUtil.dp2px(37.33f);

	public ReadToolbar(Context context) {
		this(context, null);
	}

	public ReadToolbar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ReadToolbar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews(context);
	}

	private void initViews(Context context) {
		if (isInEditMode()) {
			return;
		}

		mContext = context;

		LayoutInflater.from(mContext).inflate(R.layout.vw_toolbar_layout, this);

		// 初始化工具条按钮
		mTopToolBar = findViewById(R.id.top_toolbar);
		mBottomToolBar = findViewById(R.id.bottom_toolbar);
		mSeekInfo = (TextView) findViewById(R.id.seek_info);

		mToolbarReturn = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_home_btn);
		mToolbarCatalog = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_catalog_btn);
		mUpdateFlag = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_update_flag);
		mToolbarDown = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_down_btn);
		mToolbarMark = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_bookmark_btn);
		mToolbarBookInfo = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_bookinfo_btn);

		mToolbarProgress = (TextView) mBottomToolBar.findViewById(R.id.toolbar_progress_btn);
		mToolbarSetting = (TextView) mBottomToolBar.findViewById(R.id.toolbar_setting_btn);
		mToolbarComment = (TextView) mBottomToolBar.findViewById(R.id.toolbar_comment_btn);
		mToolbarNight = (TextView) mBottomToolBar.findViewById(R.id.toolbar_night_btn);

		mToolbarSeekBar = (SeekBar) mBottomToolBar.findViewById(R.id.read_seekbar);
		mChapterTitle = (EllipsizeTextView) mBottomToolBar.findViewById(R.id.read_progress_title);
		mPreChapterBtn = (TextView) mBottomToolBar.findViewById(R.id.pre_chapter_btn);
		mNextChapterBtn = (TextView) mBottomToolBar.findViewById(R.id.next_chapter_btn);

		mToolbarProgressView = mBottomToolBar.findViewById(R.id.toolbar_progress);
		mToolbarSettingView = mBottomToolBar.findViewById(R.id.toolbar_setting);
		mToolbarCommentView = mBottomToolBar.findViewById(R.id.toolbar_comment);

		mFontSizeDec = (ImageView) mBottomToolBar.findViewById(R.id.read_setting_font_dec);
		mFontSizeInc = (ImageView) mBottomToolBar.findViewById(R.id.read_setting_font_inc);
		
		mFlipBtn = (TextView) mBottomToolBar.findViewById(R.id.read_setting_flip);
		mSlideBtn = (TextView) mBottomToolBar.findViewById(R.id.read_setting_slide);
		mScrollBtn = (TextView) mBottomToolBar.findViewById(R.id.read_setting_scroll);
		
		mPraiseBtn = (TextView) mBottomToolBar.findViewById(R.id.read_praise_btn);
		mCommentBtn = (TextView) mBottomToolBar.findViewById(R.id.read_comment_btn);

		mSeekBar = (SeekBar) mBottomToolBar.findViewById(R.id.read_setting_seekbar);

		// mUpdateLayout = (RelativeLayout)
		// mBottomToolBar.findViewById(R.id.read_update_layout);
		// mUpdateRemind = (SwitchButton)
		// mBottomToolBar.findViewById(R.id.read_update_remind);
		mAutoBright = (SwitchButton) mBottomToolBar.findViewById(R.id.read_setting_auto);

		initReadThemeView();
		initListener();

		decodeDividerDot();
		initDivider();

		setToolbarTouchListener();
	}

	private void initReadThemeView() {
		GridView gridView = (GridView) mBottomToolBar.findViewById(R.id.read_setting_theme_grid);
		mImageAdapter = new ImageAdapter(mContext);

		int horizontalSpacing = PixelUtil.dp2px(10.67f);
		int w = mImageAdapter.getCount() * (ITEM_WIDTH + horizontalSpacing) - horizontalSpacing;
		gridView.setLayoutParams(new LinearLayout.LayoutParams(w, LinearLayout.LayoutParams.WRAP_CONTENT));

		gridView.setNumColumns(mImageAdapter.getCount());
		gridView.setHorizontalSpacing(horizontalSpacing);
		gridView.setVerticalSpacing(0);

		gridView.setAdapter(mImageAdapter);
		gridView.setOnItemClickListener(this);
	}

	private void initListener() {
		mToolbarProgress.setOnClickListener(this);
		mToolbarSetting.setOnClickListener(this);
		mToolbarComment.setOnClickListener(this);
		mToolbarNight.setOnClickListener(this);

		mFlipBtn.setOnClickListener(this);
		mSlideBtn.setOnClickListener(this);
		mScrollBtn.setOnClickListener(this);

		mFontSizeInc.setOnClickListener(this);
		mFontSizeDec.setOnClickListener(this);

		// mUpdateRemind.setOnCheckedChangeListener(this);
		mAutoBright.setOnCheckedChangeListener(this);
	}

	private void decodeDividerDot() {
		Bitmap dotReal = BitmapFactory.decodeResource(getResources(), R.drawable.read_setting_divider_real);
		mDotReal = new BitmapDrawable(getResources(), dotReal);
		mDotReal.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mDotReal.setDither(true);
	}

	private void initDivider() {
		((ImageView) mToolbarProgressView.findViewById(R.id.read_progress_divider0)).setImageDrawable(mDotReal);
		((ImageView) mToolbarProgressView.findViewById(R.id.read_progress_divider1)).setImageDrawable(mDotReal);
		((ImageView) mToolbarSettingView.findViewById(R.id.read_setting_divider0)).setImageDrawable(mDotReal);
		((ImageView) mToolbarSettingView.findViewById(R.id.read_setting_divider1)).setImageDrawable(mDotReal);
		((ImageView) mToolbarSettingView.findViewById(R.id.read_setting_divider2)).setImageDrawable(mDotReal);
		((ImageView) mToolbarSettingView.findViewById(R.id.read_setting_divider3)).setImageDrawable(mDotReal);
	}

	private void setToolbarTouchListener() {
		// 拦截顶部工具栏背景touch事件
		mTopToolBar.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// 拦截底部工具栏背景touch事件
		mBottomToolBar.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}

	public void init(ReadStyleManager styleManager) {
		mStyleManager = styleManager;
	}

	public void setListener(ISettingChangedListener settingChangeListener, View.OnClickListener clickListener,
			SeekBar.OnSeekBarChangeListener seekListener) {
		mListener = settingChangeListener;

		mToolbarReturn.setOnClickListener(clickListener);
		mToolbarCatalog.setOnClickListener(clickListener);
		mToolbarDown.setOnClickListener(clickListener);
		mToolbarMark.setOnClickListener(clickListener);
		mToolbarBookInfo.setOnClickListener(clickListener);

		mPraiseBtn.setOnClickListener(clickListener);
		mCommentBtn.setOnClickListener(clickListener);

		mPreChapterBtn.setOnClickListener(clickListener);
		mNextChapterBtn.setOnClickListener(clickListener);

		mToolbarSeekBar.setOnSeekBarChangeListener(seekListener);
		mToolbarSeekBar.setProgress(0);
	}

	/**
	 * 显示工具栏
	 * 
	 * @param progress
	 *            阅读进度
	 * @param showUpdateFlag
	 *            是否显示更新提示标志
	 * @param showWeiboBtn
	 *            是否显示分享微博的按钮
	 * @param hasMark
	 *            是否有书签
	 * @param showRemind
	 *            是否显示订阅的按钮
	 * @param chapterTitle
	 *            章节进度
	 */
	public void show(int progress, boolean showUpdateFlag, boolean showWeiboBtn, boolean hasMark, boolean showRemind,
			String chapterTitle, String log) {
		// LogUtil.d("ReadChapterCount_SeekBar",
		// "ReadToolbar >> show >> progress=" + progress);
		// 初始化SeekBar
		progress--;
		if (progress < 0) {
			progress = 0;
		}
		mToolbarSeekBar.setProgress(progress);

		mSeekInfo.setVisibility(GONE);

		// 设置更新提醒布局的显示状态
		int visible = showRemind ? VISIBLE : GONE;
		// mUpdateRemind.setCheckedWithOutListener(ReadActivity.gBook.isRemind());
		// mUpdateLayout.setVisibility(visible);
		mBottomToolBar.findViewById(R.id.read_progress_divider1).setVisibility(visible);

		// 设置评论按钮、书籍详情按钮的状态
		int visible1 = showWeiboBtn ? VISIBLE : GONE;
        // 隐藏点评
		//mToolbarComment.setVisibility(visible1);
		mBottomToolBar.findViewById(R.id.toolbar_comment_divider).setVisibility(visible1);
		mToolbarBookInfo.setVisibility(visible1);
		
		// 设置更新提示按钮的状态
		mUpdateFlag.setVisibility(showUpdateFlag ? VISIBLE : GONE);

		// 设置书签按钮状态
		mToolbarMark.setImageResource(hasMark ? R.drawable.toolbar_mark_del_bg : R.drawable.toolbar_mark_add_bg);

		// 初始化各个按钮的状态
		updateFontSizeBtn();
		updateReadModeBtn();
		updateCheckboxAndSeekBar();
		updateAnimBtn();
		updatePraiseStatus();
		setChapterTitle(chapterTitle, log);

		// 初始化显示TAB按钮
		mToolbarProgressView.setVisibility(VISIBLE);
		mToolbarSettingView.setVisibility(GONE);
		mToolbarCommentView.setVisibility(GONE);

		mToolbarProgress.setEnabled(false);
		mToolbarSetting.setEnabled(true);
		mToolbarComment.setEnabled(true);

		Animation topIn = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_from_top);
		mTopToolBar.clearAnimation();
		mTopToolBar.setAnimation(topIn);

		Animation bottomIn = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_from_bottom);
		mBottomToolBar.clearAnimation();
		mBottomToolBar.setAnimation(bottomIn);

		setVisibility(View.VISIBLE);
	}

	/**
	 * 隐藏工具栏
	 */
	public void dismiss() {
		Animation topOut = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_from_top);
		mTopToolBar.clearAnimation();
		mTopToolBar.setAnimation(topOut);

		Animation bottomOut = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_from_bottom);
		mBottomToolBar.clearAnimation();
		if (null != bottomOut) {
			bottomOut.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					setVisibility(View.GONE);
				}
			});
			mBottomToolBar.startAnimation(bottomOut);
		} else {
			setVisibility(View.GONE);
		}
	}

	/**
	 * 得到工具栏上的下载按钮 TODO 这样写不好，但是又没啥好的办法
	 * 
	 * @return
	 */
	public ImageView getToolbarDownBtn() {
		return mToolbarDown;
	}

	/**
	 * 设置进度条的提示文字
	 * 
	 * @param text
	 */
	public void setSeekInfo(String text) {
		mSeekInfo.setText(text);

		if (GONE == mSeekInfo.getVisibility()) {
			mSeekInfo.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 设置章节信息
	 * 
	 * @param text
	 */

	public void setChapterTitle(String text, String log) {
		LogUtil.d("BugBugFuckU", "ReadToolbar >> setChapterTitle >> log=" + log + ", text=" + text);
		mChapterTitle.setText(text);
	}

	// public void setSeekBarProgress(int progress) {
	// setSeekBarProgress(progress, false, 100);
	// }

	private boolean isInitBarData = true;

	public void setIsInitBarData(boolean isInitBarData) {
		this.isInitBarData = isInitBarData;
	}

	/**
	 * 设置进度条
	 * 
	 * @param progress
	 */
	public void setSeekBarProgress(int progress, boolean ignoreSetProgress, int max) {
		int preMax = mToolbarSeekBar.getMax();
		int preProgress = mToolbarSeekBar.getProgress();
		if (preMax != max || max == 100) {
			max--;
			mToolbarSeekBar.setMax(max);
		}
		// LogUtil.d("ReadChapterCount_SeekBar",
		// "ReadToolbar >> setSeekBarProgress >> preProgress=" + preProgress
		// + ", progress=" + progress + ", preMax=" + preMax + ", max=" + max +
		// ", mContext=" + mContext);

		progress--;
		if (progress < 0) {
			progress = 0;
		}

		if (preProgress != progress || isInitBarData) {
			isInitBarData = false;
			mToolbarSeekBar.setProgress(progress);
			if (!ignoreSetProgress) {
				// LogUtil.e("ReadChapterCount_SeekBar",
				// "ReadToolbar >> setSeekBarProgress >> 设置进度");
				if (mContext != null && mContext instanceof ReadActivity) {
					// 一次章节阅读行为
					ReadActivity activity = (ReadActivity) mContext;
					activity.record1ChapterReadCount();
				}
			}
		} else {
			// 如何解决第一次进入阅读时进入的是第一章节呢？

		}
		mSeekInfo.setVisibility(View.GONE);
	}

	/**
	 * 移除更新提示标志
	 */
	public void removeUpdateFlag() {
		mUpdateFlag.setVisibility(GONE);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.toolbar_progress_btn:// 进度条
			mToolbarProgressView.setVisibility(View.VISIBLE);

			mToolbarSettingView.setVisibility(View.GONE);
			mToolbarCommentView.setVisibility(View.GONE);

			mSeekInfo.setVisibility(GONE);

			mToolbarProgress.setEnabled(false);
			mToolbarSetting.setEnabled(true);
			mToolbarComment.setEnabled(true);
			break;

		case R.id.toolbar_setting_btn:// 设置
			mToolbarSettingView.setVisibility(View.VISIBLE);

			mToolbarProgressView.setVisibility(View.GONE);
			mToolbarCommentView.setVisibility(View.GONE);

			mSeekInfo.setVisibility(GONE);

			mToolbarProgress.setEnabled(true);
			mToolbarSetting.setEnabled(false);
			mToolbarComment.setEnabled(true);
			break;

		case R.id.toolbar_comment_btn:// 评论
			mToolbarCommentView.setVisibility(View.VISIBLE);

			mToolbarProgressView.setVisibility(View.GONE);
			mToolbarSettingView.setVisibility(View.GONE);

			mSeekInfo.setVisibility(GONE);

			mToolbarProgress.setEnabled(true);
			mToolbarSetting.setEnabled(true);
			mToolbarComment.setEnabled(false);
			break;

		case R.id.toolbar_night_btn:// 夜间模式
			mToolbarProgressView.setVisibility(View.GONE);
			mToolbarCommentView.setVisibility(View.GONE);
			mToolbarSettingView.setVisibility(View.GONE);

			mSeekInfo.setVisibility(GONE);

			mToolbarProgress.setEnabled(true);
			mToolbarSetting.setEnabled(true);
			mToolbarComment.setEnabled(true);
			
			if (ReadStyleManager.READ_MODE_NORMAL == mStyleManager.getReadMode()) {
				mStyleManager.changeReadMode(ReadStyleManager.READ_MODE_NIGHT);
				if (null != mListener) {
					mListener.readModeChanged(ReadStyleManager.READ_MODE_NIGHT);
				}

				UserActionManager.getInstance().recordEventValue(Constants.KEY_READ_MODE + "night");
			} else {
				mStyleManager.changeReadMode(ReadStyleManager.READ_MODE_NORMAL);

				if (null != mListener) {
					mListener.readModeChanged(ReadStyleManager.READ_MODE_NORMAL);
				}

				UserActionManager.getInstance().recordEventValue(Constants.KEY_READ_MODE + "day");
			}
			updateReadModeBtn();
			break;

		case R.id.read_setting_font_dec:// 字体-
			if (mStyleManager.decreaseReadFontSize()) {
				updateFontSizeBtn();

				if (null != mListener) {
					mListener.fontSizeChanged(mStyleManager.getCurReadFontSizeInSp());
				}

				UserActionManager.getInstance().recordEventValue(
						Constants.KEY_FONTSIZE + mStyleManager.getCurReadFontSizeInSp());
			}
			break;

		case R.id.read_setting_font_inc:// 字体+
			if (mStyleManager.increaseReadFontSize()) {
				updateFontSizeBtn();

				if (null != mListener) {
					mListener.fontSizeChanged(mStyleManager.getCurReadFontSizeInSp());
				}

				UserActionManager.getInstance().recordEventValue(
						Constants.KEY_FONTSIZE + mStyleManager.getCurReadFontSizeInSp());
			}
			break;

		case R.id.read_setting_flip:// 3D翻页
			mStyleManager.changeReadAnim(ReadStyleManager.ANIMATION_TYPE_FLIP);
			updateAnimBtn();

			if (null != mListener) {
				mListener.animModeChanged(mStyleManager.getReadAnim());
			}

			UserActionManager.getInstance().recordEventValue(Constants.KEY_ANIMATION + "flip");
			break;

		case R.id.read_setting_slide:// 平滑翻页
			mStyleManager.changeReadAnim(ReadStyleManager.ANIMATION_TYPE_SILIDE);
			updateAnimBtn();

			if (null != mListener) {
				mListener.animModeChanged(mStyleManager.getReadAnim());
			}

			UserActionManager.getInstance().recordEventValue(Constants.KEY_ANIMATION + "slide");
			break;

		case R.id.read_setting_scroll:// 上下翻页
			mStyleManager.changeReadAnim(ReadStyleManager.ANIMATION_TYPE_SCROLL);
			updateAnimBtn();

			if (null != mListener) {
				mListener.animModeChanged(mStyleManager.getReadAnim());
			}

			UserActionManager.getInstance().recordEventValue(Constants.KEY_ANIMATION + "scroll");
			break;

		default:
			// ignore
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position >= 0 && position < parent.getAdapter().getCount()) {
			// 如果不是白天模式点击之后改变阅读模式
			if (ReadStyleManager.READ_MODE_NORMAL != mStyleManager.getReadMode()) {
				mStyleManager.changeReadMode(ReadStyleManager.READ_MODE_NORMAL);
				updateReadModeBtn();
			}

			int resId = (Integer) parent.getAdapter().getItem(position);
			mStyleManager.setReadBgResId(resId);
			mImageAdapter.notifyDataSetChanged();

			// 这里仅仅为了刷新
			if (null != mListener) {
				mListener.readModeChanged(mStyleManager.getReadMode());
			}

			UserActionManager.getInstance().recordEventValue(
					Constants.KEY_READ_BACKGROUND + ResourceUtil.getNameById(resId));
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
		switch (compoundButton.getId()) {
		// case R.id.read_update_remind:
		// ReadActivity.gBook.setRemind(b);
		//
		// LogUtil.e("mjw", "**********************************************");
		// // PushHelper.getInstance().updateRemindBooks();
		// break;

		case R.id.read_setting_auto:
			if (null != mListener) {
				mListener.brightModeChanged(b);
			}

			updateCheckboxAndSeekBar();

			UserActionManager.getInstance().recordEventValue(Constants.IS_AUTO_BRIGHTNESS + b);
			break;
		}
	}

	private void setSeekBarListener() {
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				StorageUtil.putBrightness(seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (null != mListener) {
					mListener.brightValueChanged(progress);
				}
			}
		});
	}

	/**
	 * 设置
	 * */
	public void setShowNavigationHeight(){
	}

	private void updateFontSizeBtn() {
		mFontSizeInc.setEnabled(!mStyleManager.isBiggestReadFontSize());
		mFontSizeDec.setEnabled(!mStyleManager.isSmallestReadFontSize());
	}

	private void updateReadModeBtn() {
		if (ReadStyleManager.READ_MODE_NORMAL == mStyleManager.getReadMode()) {
			Drawable drawable = ResourceUtil.getDrawable(R.drawable.toolbar_night_bg);
			drawable.setBounds(0, 0, NIGHT_BOUND, NIGHT_BOUND);
			mToolbarNight.setCompoundDrawables(null, drawable, null, null);
			mToolbarNight.setText(ResourceUtil.getString(R.string.toolbar_night));
		} else {
			Drawable drawable = ResourceUtil.getDrawable(R.drawable.toolbar_bright_bg);
			drawable.setBounds(0, 0, NIGHT_BOUND, NIGHT_BOUND);
			mToolbarNight.setCompoundDrawables(null, drawable, null, null);
			mToolbarNight.setText(ResourceUtil.getString(R.string.toolbar_bright));
		}
	}

	private void updateCheckboxAndSeekBar() {
		if (StorageUtil.getBoolean(StorageUtil.KEY_AUTO_BRIGHTNESS, true)) {
			mAutoBright.setCheckedWithOutListener(true);

			mSeekBar.setEnabled(false);
			mSeekBar.setOnSeekBarChangeListener(null);
		} else {
			mAutoBright.setCheckedWithOutListener(false);

			mSeekBar.setEnabled(true);
			setSeekBarListener();
		}

		mSeekBar.setProgress(StorageUtil.getBrightness());
	}

	private void updateAnimBtn() {
		int animType = mStyleManager.getReadAnim();
		switch (animType) {
		case ReadStyleManager.ANIMATION_TYPE_FLIP:
			mFlipBtn.setEnabled(false);
			mSlideBtn.setEnabled(true);
			mScrollBtn.setEnabled(true);
			break;

		case ReadStyleManager.ANIMATION_TYPE_SILIDE:
			mSlideBtn.setEnabled(false);
			mFlipBtn.setEnabled(true);
			mScrollBtn.setEnabled(true);
			break;

		case ReadStyleManager.ANIMATION_TYPE_SCROLL:
			mScrollBtn.setEnabled(false);
			mFlipBtn.setEnabled(true);
			mSlideBtn.setEnabled(true);
			break;

		default:
			break;
		}
	}

	public void updatePraiseStatus() {
		if (ReadActivity.gBook.hasPraised()) {
			mPraiseBtn.setText(ResourceUtil.getString(R.string.has_praised));
			mPraiseBtn.setTextColor(ResourceUtil.getColor(R.color.setting_text_color_pressed));

			Drawable drawable = ResourceUtil.getDrawable(R.drawable.like_pressed);
			drawable.setBounds(0, 0, PRAISE_BOUND, PRAISE_BOUND);
			mPraiseBtn.setCompoundDrawables(drawable, null, null, null);

			mPraiseBtn.setEnabled(false);
		} else {
			mPraiseBtn.setText(ResourceUtil.getString(R.string.praise));
			mPraiseBtn.setTextColor(ResourceUtil.getColorStateList(R.drawable.toolbar_text_color));

			Drawable drawable = ResourceUtil.getDrawable(R.drawable.toolbar_praise_btn);
			drawable.setBounds(0, 0, PRAISE_BOUND, PRAISE_BOUND);
			mPraiseBtn.setCompoundDrawables(drawable, null, null, null);

			mPraiseBtn.setEnabled(true);
		}
	}

	/**
	 * 图片适配器
	 * 
	 * @author MarkMjw
	 */
	public class ImageAdapter extends BaseAdapter {
		private final Context mContext;

		private ArrayList<Integer> mIdList = ThemeUtil.getBgResIdList();
		private SparseIntArray mIdThumbnailList = ThemeUtil.getBgThumbnailResIdList();

		public ImageAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return mIdList.size();
		}

		@Override
		public Object getItem(int position) {
			return mIdList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BorderImageView imageView;
			if (convertView == null) {
				imageView = (BorderImageView) LayoutInflater.from(mContext).inflate(R.layout.vw_read_bg_item, null);
				if (null != imageView) {
					imageView.setBackgroundDrawable(null);

					GridView.LayoutParams lp = new GridView.LayoutParams(ITEM_WIDTH, ITEM_HEIGHT);
					imageView.setLayoutParams(lp);
					imageView.setScaleType(ImageView.ScaleType.CENTER);

					convertView = imageView;
				}
			} else {
				imageView = (BorderImageView) convertView;
			}

			int resId = mIdList.get(position);
			int thumbnailResId = ThemeUtil.getThumbnail(resId);
			Drawable drawable = mContext.getResources().getDrawable(thumbnailResId);

			if (drawable instanceof ColorDrawable) {
				// 如果是颜色值，则生成一个Bitmap
				int width = PixelUtil.dp2px(32.66667f);
				Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);

				int color = mContext.getResources().getColor(resId);
				canvas.drawColor(color);

				float roundPx = ITEM_WIDTH / 6.5f;
				bitmap = ImageUtil.getRoundedCornerBitmap(bitmap, roundPx);

				if (null != imageView) {
					imageView.setImageBitmap(bitmap);
				}

			} else {
				// 如果是图片，则直接设置缩略图
				// int thumbnailResId = ThemeUtil.getThumbnail(resId);
				if (ThemeUtil.DEFAULT_VALUE != thumbnailResId) {
					if (null != imageView) {
						// imageView.setImageResource(thumbnailResId);
						imageView.setImageDrawable(drawable);
					}
				}
			}

			if (null != imageView) {
				imageView.setSelect(mStyleManager.getReadBgResId() == resId);
			}

			return convertView;
		}
	}

	/**
	 * 设置信息改变监听器
	 * 
	 * @author MarkMjw
	 */
	public interface ISettingChangedListener {
		/**
		 * 阅读模式改变
		 * 
		 * @param readMode
		 *            阅读模式
		 */
		public void readModeChanged(int readMode);

		/**
		 * 动画模式改变
		 * 
		 * @param animMode
		 *            动画模式
		 */
		public void animModeChanged(int animMode);

		/**
		 * 阅读字体大小变化
		 * 
		 * @param fontSize
		 *            字体大小
		 */
		public void fontSizeChanged(float fontSize);

		/**
		 * 亮度模式改变
		 * 
		 * @param isAuto
		 *            是否自动调节
		 */
		public void brightModeChanged(boolean isAuto);

		/**
		 * 亮度变化
		 * 
		 * @param value
		 *            亮度值
		 */
		public void brightValueChanged(int value);
	}
}
