package org.geometerplus.android.fbreader;

import static com.sina.book.data.ConstantData.CODE_FAIL_KEY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.geometerplus.android.util.ZLog;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;
import org.geometerplus.fbreader.fbreader.options.PageTurningOptions;
import org.geometerplus.fbreader.fbreader.options.ViewOptions;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLView.PageIndex;
import org.geometerplus.zlibrary.text.view.ZLTextPage;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.BookDetailData;
import com.sina.book.data.Chapter;
import com.sina.book.data.ConstantData;
import com.sina.book.data.WeiboContent;
import com.sina.book.db.DBService;
import com.sina.book.image.ImageLoader;
import com.sina.book.image.ImageUtil;
import com.sina.book.parser.BookDetailParser;
import com.sina.book.parser.IParser;
import com.sina.book.parser.SimpleParser;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.BookTagActivity;
import com.sina.book.ui.CommentListActivity;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.widget.BorderImageView;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.ui.widget.SwitchButton;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.BrightnessUtil;
import com.sina.book.util.GlobalToast;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.ThemeUtil;

final class NavigationPopup extends PopupPanel implements OnItemClickListener, OnClickListener, OnCheckedChangeListener {
	final static String ID = "NavigationPopup";

	private final int ITEM_WIDTH = (int) ResourceUtil.getDimens(R.dimen.setting_theme_img_width);
	private final int ITEM_HEIGHT = (int) ResourceUtil.getDimens(R.dimen.setting_theme_img_height);

	/**
	 * 白天、夜间图标的高度、宽度
	 */
	private final int NIGHT_BOUND = PixelUtil.dp2px(37.33f);

	private final int PRAISE_BOUND = PixelUtil.dp2px(21.33f);

	private volatile boolean myIsInProgress;

	/**
	 * 工具条容器.
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
	private ImageView mToolbarMark; // 添加书签
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

	private ImageView mFontSizeInc;
	private ImageView mFontSizeDec;

	private TextView mFlipBtn; // 仿真
	private TextView mSlideBtn; // 左右
	private TextView mScrollBtn; // 上下

	private TextView mPraiseBtn;
	private TextView mCommentBtn;

	private SeekBar mSeekBar;

	private SwitchButton mAutoBright;

	private ImageAdapter mImageAdapter;

	private BitmapDrawable mDotReal;

	// private ISettingChangedListener mListener;

	// private FBReader activity;

	private boolean isNavPop = false;

	// private ReadStyleManager mReadStyleManager;

	private FBReaderApp myFBReaderApp;

	private PageTurningOptions pageTurningOptions;

	private FBReader mContext;
	public static final String KEY = "prikey";
	private int mDisplayWidth;
	private int mDisplayHeight;

	// private SinaBookPageFactory mPageFactory;

	NavigationPopup(FBReader context, FBReaderApp fbReader) {
		super(fbReader);
		mContext = context;
		this.myFBReaderApp = fbReader;
		ReadStyleManager mReadStyleManager = ReadStyleManager.getInstance(SinaBookApplication.getTopActivity());
		int animType = mReadStyleManager.getReadAnim();
		pageTurningOptions = new PageTurningOptions();
		org.geometerplus.zlibrary.core.view.ZLView.Animation type = animType == 0 ? org.geometerplus.zlibrary.core.view.ZLView.Animation.curl
				: org.geometerplus.zlibrary.core.view.ZLView.Animation.left2right;
		pageTurningOptions.Animation.setValue(type);
		DisplayMetrics metrics = new DisplayMetrics();
		mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mDisplayWidth = metrics.widthPixels;
		mDisplayHeight = metrics.heightPixels;
		// mPageFactory = SinaBookPageFactory.getInstance(mDisplayWidth,
		// mDisplayHeight);
		// reqBookInfo(false);

	}

	// 执行显示菜单
	public void runNavigation() {
		if (!isNavPop) {
			// if (myWindow == null || myWindow.getVisibility() == View.GONE) {
			myIsInProgress = false;
			initPosition();
			Application.showPopup(ID);
		}
	}

	// 设置全屏或取消
	private void makeFullScreen(boolean enable) {
		if (enable) {
			// 如果工具栏或者进度条未隐藏，则直接返回
			// if (mToolbar.isShown() || progressShowing()) {
			// return;
			// }

			WindowManager.LayoutParams lp = myActivity.getWindow().getAttributes();
			lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			myActivity.getWindow().setAttributes(lp);

			// View decorView = myActivity.getWindow().getDecorView();
			// int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			// | View.SYSTEM_UI_FLAG_FULLSCREEN;
			// decorView.setSystemUiVisibility(uiOptions);

		} else {
			WindowManager.LayoutParams attr = myActivity.getWindow().getAttributes();
			attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			myActivity.getWindow().setAttributes(attr);

			// View decorView = myActivity.getWindow().getDecorView();
			// int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
			// decorView.setSystemUiVisibility(uiOptions);
		}
	}

	public String getId() {
		return ID;
	}

	protected void show_() {
		super.show_();
		if (isNavPop) {
			// if (myWindow != null) {
			setupNavigation(true);
		}
	}

	protected void hide_() {
		super.hide_();
		dismiss();
		// myActivity.runOnUiThread(new Runnable() {
		// public void run() {
		// dismiss();
		// }
		// });
	}

	protected void update() {
		if (!myIsInProgress && isNavPop) {
			// if (!myIsInProgress && myWindow != null) {
			// setupNavigation();
		}
	}

	private ChapterSeekBarListener mChapterSeekBarListener;

	// 初始化视图
	public void createControlPanel(FBReader activity, RelativeLayout root) {
		myActivity = activity;
		isNavPop = true;

		// if (mNavigationRootView == null) {
		// myWindow = new PopupWindow(activity, root,
		// PopupWindow.Location.Full);
		initViews(activity);
		// myWindow.addView(layout);
		root.addView(mNavigationRootView);
		// 设置监听器
		mToolbarCatalog.setOnClickListener(this);
		mToolbarMark.setOnClickListener(((FBReader) myFBReaderApp.getActivity()).getNavigationPopupClickListener());
		mPreChapterBtn.setOnClickListener(this);
		mNextChapterBtn.setOnClickListener(this);
		if (mChapterSeekBarListener != null) {
			mChapterSeekBarListener = null;
		}
		mChapterSeekBarListener = new ChapterSeekBarListener(mToolbarSeekBar);
		mToolbarSeekBar.setOnSeekBarChangeListener(mChapterSeekBarListener);
		// }

		//
		// final View layout =
		// activity.getLayoutInflater().inflate(R.layout.navigate, myWindow,
		// false);
		// final SeekBar slider =
		// (SeekBar)layout.findViewById(R.id.navigation_slider);
		// final TextView text =
		// (TextView)layout.findViewById(R.id.navigation_text);
		//
		// slider.setOnSeekBarChangeListener(new
		// SeekBar.OnSeekBarChangeListener() {
		// private void gotoPage(int page) {
		// final ZLTextView view = getReader().getTextView();
		// if (page == 1) {
		// view.gotoHome();
		// } else {
		// view.gotoPage(page);
		// }
		// getReader().getViewWidget().reset();
		// getReader().getViewWidget().repaint();
		// }
		//
		// public void onStartTrackingTouch(SeekBar seekBar) {
		// myIsInProgress = true;
		// }
		//
		// public void onStopTrackingTouch(SeekBar seekBar) {
		// myIsInProgress = false;
		// }
		//
		// public void onProgressChanged(SeekBar seekBar, int progress, boolean
		// fromUser) {
		// if (fromUser) {
		// final int page = progress + 1;
		// final int pagesNumber = seekBar.getMax() + 1;
		// gotoPage(page);
		// text.setText(makeProgressText(page, pagesNumber));
		// }
		// }
		// });
		//
		// final Button btnOk =
		// (Button)layout.findViewById(android.R.id.button1);
		// final Button btnCancel =
		// (Button)layout.findViewById(android.R.id.button3);
		// View.OnClickListener listener = new View.OnClickListener() {
		// public void onClick(View v) {
		// final ZLTextWordCursor position = StartPosition;
		// if (v == btnCancel && position != null) {
		// getReader().getTextView().gotoPosition(position);
		// } else if (v == btnOk) {
		// storePosition();
		// }
		// StartPosition = null;
		// Application.hideActivePopup();
		// getReader().getViewWidget().reset();
		// getReader().getViewWidget().repaint();
		// }
		// };
		// btnOk.setOnClickListener(listener);
		// btnCancel.setOnClickListener(listener);
		// final ZLResource buttonResource =
		// ZLResource.resource("dialog").getResource("button");
		// btnOk.setText(buttonResource.getResource("ok").getValue());
		// btnCancel.setText(buttonResource.getResource("cancel").getValue());
		//
		// myWindow.addView(layout);
	}

	/**
	 * 章节跳转Change监听器
	 * 
	 * @author chenjl
	 * 
	 */
	private class ChapterSeekBarListener implements SeekBar.OnSeekBarChangeListener {

		private SeekBar mySeekBar;
		private ZLTree<?> myTree;
		private ZLTree<?>[] myItems;

		// private FBReaderApp myFbReader;

		public ChapterSeekBarListener(SeekBar seekBar) {
			mySeekBar = seekBar;
			myTree = myFBReaderApp.Model.TOCTree;
			myItems = new ZLTree[myTree.getSize() - 1];
		}

		private final int indexByPosition(int position, ZLTree<?> tree) {
			if (position == 0) {
				return 0;
			}
			--position;
			int index = 1;
			for (ZLTree<?> subtree : tree.subtrees()) {
				int count = getCount(subtree);
				if (count <= position) {
					position -= count;
					index += subtree.getSize();
				} else {
					return index + indexByPosition(position, subtree);
				}
			}
			throw new RuntimeException("That's impossible!!!");
		}

		private int getCount(ZLTree<?> tree) {
			int count = 1;
			// if (isOpen(tree)) {
			for (ZLTree<?> subtree : tree.subtrees()) {
				count += getCount(subtree);
			}
			// }
			return count;
		}

		void openBookText(TOCTree tree, boolean notifyViewChanged) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference != null) {
				final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
				fbreader.addInvisibleBookmark();
				fbreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0, 0);
				fbreader.showBookTextView(notifyViewChanged);
				fbreader.storePosition();
			}
		}

		public final ZLTree<?> getItem(int position) {
			final int index = indexByPosition(position + 1, myTree) - 1;
			ZLTree<?> item = myItems[index];
			if (item == null) {
				item = myTree.getTreeByParagraphNumber(index + 1);
				myItems[index] = item;
			}
			return item;
		}

		// 上一章
		public void previou() {
			int progress = mySeekBar.getProgress();
			if (progress > 0) {
				progress--;
				openBookText((TOCTree) getItem(progress), false);
				setupNavigation(false);
			} else {
				GlobalToast.i.toast("已是第一章");
			}

		}

		// 下一章
		public void next() {
			int max = mySeekBar.getMax();
			int progress = mySeekBar.getProgress();
			if (progress < max) {
				progress++;
				openBookText((TOCTree) getItem(progress), false);
				setupNavigation(false);
			} else {
				GlobalToast.i.toast("已是最后一章");
			}

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (!onlyOneChapter) {
				int progress = seekBar.getProgress();
				ZLog.d(ZLog.NavigationPopup, "onStopTrackingTouch >> progress = " + progress);
				openBookText((TOCTree) getItem(progress), true);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (!onlyOneChapter) {
				// 显示章节名称
				TOCTree currTree = (TOCTree) getItem(progress);
				if (currTree != null) {
					String chapterText = currTree.getText();
					mSeekInfo.setText(chapterText);
					mSeekInfo.setVisibility(View.VISIBLE);
				}

				// 进度
				int max = mToolbarSeekBar.getMax();
				max++;
				progress++;
				String progressChapterText = String.format(myActivity.getString(R.string.chapter_information), progress, max);
				setChapterTitle(progressChapterText, "progressChapterText");
			}
		}

	}

	private View mNavigationRootView; // 菜单父视图
	private View mCenterEmptyRectView; // 全屏容器，其中间区域空白，点击它控制菜单消失

	private void initViews(FBReader activity) {
		mNavigationRootView = activity.getLayoutInflater().inflate(R.layout.vw_toolbar_layout, null);

		mCenterEmptyRectView = mNavigationRootView.findViewById(R.id.toolbar_parent_c);

		// 初始化工具条按钮
		mTopToolBar = mNavigationRootView.findViewById(R.id.top_toolbar);
		mBottomToolBar = mNavigationRootView.findViewById(R.id.bottom_toolbar);
		mSeekInfo = (TextView) mNavigationRootView.findViewById(R.id.seek_info);
		mSeekInfo.setVisibility(View.GONE);

		// 顶部工具栏
		mToolbarReturn = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_home_btn);
		mToolbarCatalog = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_catalog_btn);
		mUpdateFlag = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_update_flag);
		// 下载
		mToolbarDown = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_down_btn);
		mToolbarDown.setVisibility(View.GONE);
		// 书签
		mToolbarMark = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_bookmark_btn);
		// 摘要
		mToolbarBookInfo = (ImageView) mTopToolBar.findViewById(R.id.reading_toolbar_bookinfo_btn);

		// 底部工具栏
		mToolbarProgress = (TextView) mBottomToolBar.findViewById(R.id.toolbar_progress_btn);
		mToolbarSetting = (TextView) mBottomToolBar.findViewById(R.id.toolbar_setting_btn);
		mToolbarComment = (TextView) mBottomToolBar.findViewById(R.id.toolbar_comment_btn);
		mToolbarNight = (TextView) mBottomToolBar.findViewById(R.id.toolbar_night_btn);
		mToolbarNight.setVisibility(View.GONE);

		// 阅读进度条
		mToolbarSeekBar = (SeekBar) mBottomToolBar.findViewById(R.id.read_seekbar);
		// 中间章节名
		mChapterTitle = (EllipsizeTextView) mBottomToolBar.findViewById(R.id.read_progress_title);
		// 上一章
		mPreChapterBtn = (TextView) mBottomToolBar.findViewById(R.id.pre_chapter_btn);
		// 下一章
		mNextChapterBtn = (TextView) mBottomToolBar.findViewById(R.id.next_chapter_btn);

		// 进度容器
		mToolbarProgressView = mBottomToolBar.findViewById(R.id.toolbar_progress);
		// 设置容器
		mToolbarSettingView = mBottomToolBar.findViewById(R.id.toolbar_setting);
		// 点评容器
		mToolbarCommentView = mBottomToolBar.findViewById(R.id.toolbar_comment);

		// 字体大小
		mFontSizeDec = (ImageView) mBottomToolBar.findViewById(R.id.read_setting_font_dec);
		mFontSizeInc = (ImageView) mBottomToolBar.findViewById(R.id.read_setting_font_inc);

		// 翻页动画(仿真、左右、上下)
		mFlipBtn = (TextView) mBottomToolBar.findViewById(R.id.read_setting_flip);
		mSlideBtn = (TextView) mBottomToolBar.findViewById(R.id.read_setting_slide);
		mScrollBtn = (TextView) mBottomToolBar.findViewById(R.id.read_setting_scroll);
		mScrollBtn.setVisibility(View.GONE);
		mBottomToolBar.findViewById(R.id.read_setting_anim_divider1).setVisibility(View.GONE);

		// 亮度调节
		mSeekBar = (SeekBar) mBottomToolBar.findViewById(R.id.read_setting_seekbar);
		// 系统自动亮度
		mAutoBright = (SwitchButton) mBottomToolBar.findViewById(R.id.read_setting_auto);

		// 推荐
		mPraiseBtn = (TextView) mBottomToolBar.findViewById(R.id.read_praise_btn);
		// 评论
		mCommentBtn = (TextView) mBottomToolBar.findViewById(R.id.read_comment_btn);

		View matireal = mBottomToolBar.findViewById(R.id.read_setting_model_matireal);
		matireal.setVisibility(View.GONE);

		// 初始化材质图列表
		initReadThemeView(activity);
		initListener();

		// 设置分割线
		decodeDividerDot(activity);
		initDivider();

		setToolbarTouchListener();
	}

	// 初始化材质列表
	private void initReadThemeView(FBReader activity) {
		GridView gridView = (GridView) mBottomToolBar.findViewById(R.id.read_setting_theme_grid);
		mImageAdapter = new ImageAdapter(activity);

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
		mToolbarReturn.setOnClickListener(this);
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

		mPraiseBtn.setOnClickListener(this);
		mCommentBtn.setOnClickListener(this);
	}

	private void decodeDividerDot(FBReader activity) {
		Bitmap dotReal = BitmapFactory.decodeResource(activity.getResources(), R.drawable.read_setting_divider_real);
		mDotReal = new BitmapDrawable(activity.getResources(), dotReal);
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
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// 拦截底部工具栏背景touch事件
		mBottomToolBar.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}

	/**
	 * 图片适配器
	 */
	public class ImageAdapter extends BaseAdapter {
		private final Context mContext;
		private ArrayList<Integer> mIdList = ThemeUtil.getBgResIdList();

		// private SparseIntArray mIdThumbnailList =
		// ThemeUtil.getBgThumbnailResIdList();

		public ImageAdapter(Context context) {
			mContext = context;
		}

		public int getCount() {
			return mIdList.size();
		}

		public Object getItem(int position) {
			return mIdList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

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
				ReadStyleManager mReadStyleManager = ReadStyleManager.getInstance(SinaBookApplication.getTopActivity());
				imageView.setSelect(mReadStyleManager.getReadBgResId() == resId);
			}

			return convertView;
		}
	}

	// 更新亮度UI
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

	// 亮度调节
	private void setSeekBarListener() {
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				StorageUtil.putBrightness(seekBar.getProgress());
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				BrightnessUtil.setCurrentScreenBrightness(myActivity, progress);
			}
		});
	}

	// New Add Start
	private final HashSet<ZLTree<?>> tempItems = new HashSet<ZLTree<?>>();

	private void openTree(ZLTree<?> tree) {
		if (tree == null) {
			return;
		}
		while (!tempItems.contains(tree)) {
			tempItems.add(tree);
			tree = tree.Parent;
		}
	}

	// private boolean isOpen(ZLTree<?> tree) {
	// return tempItems.contains(tree);
	// }

	public final int childAt(ZLTree<?> tree) {
		if (tree == null) {
			return 0;
		}
		openTree(tree.Parent);
		int index = 0;
		while (true) {
			ZLTree<?> parent = tree.Parent;
			if (parent == null) {
				break;
			}
			for (ZLTree<?> sibling : parent.subtrees()) {
				if (sibling == tree) {
					break;
				}
				index += getCount(sibling);
			}
			tree = parent;
			++index;
		}
		if (index > 0) {
			// myParent.setSelection(index - 1);
			return index - 1;
		}
		return 0;
	}

	private int getCount(ZLTree<?> tree) {
		int count = 1;
		// if (isOpen(tree)) {
		for (ZLTree<?> subtree : tree.subtrees()) {
			count += getCount(subtree);
		}
		// }
		return count;
	}

	// New Add End

	private boolean hasBookmark() {
		// FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		boolean hasMark = false;
		ZLTextView text = myFBReaderApp.getTextView();
		ZLTextPage textPage = text.getPage(PageIndex.current);
		if (textPage != null) {
			hasMark = textPage.hasBookmark();
		}
		return hasMark;
	}

	/**
	 * 只有一个章节
	 */
	private boolean onlyOneChapter = false;

	private void setupNavigation(boolean init) {
		// 显示菜单之前，需要获取当前阅读的章节信息
		// FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		// 目录信息
		TOCTree tree = myFBReaderApp.Model.TOCTree;
		tempItems.clear();
		tempItems.add(tree);
		Book book = myFBReaderApp.Model.Book;
		int max = tree.getSize() - 1;

		TOCTree treeCurrRead = myFBReaderApp.getCurrentTOCElement();

		int index = childAt(treeCurrRead);

		ZLog.d(ZLog.NavigationPopup, "当前阅读书籍《" + book.getTitle() + "》，全部章节数 >> tree >> size = " + max + "，当前阅读章节所在索引 >> index = " + index);

		int progress = index + 1;

		// fb2文件没有目录，艹
		if (max == 0 || max == 1 && index == 0) {
			onlyOneChapter = true;
			mToolbarSeekBar.setMax(0);
			mToolbarSeekBar.setProgress(0);
			progress = 1;
			max = 1;

			// 禁用前一章/后一章
			mPreChapterBtn.setEnabled(false);
			mNextChapterBtn.setEnabled(false);

		} else {
			onlyOneChapter = false;
			int preMax = mToolbarSeekBar.getMax();
			int preProgress = mToolbarSeekBar.getProgress();
			// ZLog.d(ZLog.NavigationPopup, "setupNavigation >> preMax = "
			// + preMax + ", preProgress = " + preProgress);
			if (preMax != max - 1 || preProgress != progress - 1) {
				mToolbarSeekBar.setMax(max - 1);
				mToolbarSeekBar.setProgress(progress - 1);
			}
			// 启用前一章/后一章
			mPreChapterBtn.setEnabled(true);
			mNextChapterBtn.setEnabled(true);
		}

		String currChapterTipText = String.format(myActivity.getString(R.string.chapter_information), progress, max);

		boolean showBookDetail = false;
		if (!TextUtils.isEmpty(myFBReaderApp.bookId)) {
			showBookDetail = true;
		}
		show(progress, false, true, hasBookmark(), false, showBookDetail, currChapterTipText, "NavigationPopup", init);

		// toolBar.show(10, true, true, true, true, "hehe", "log");

		// final SeekBar slider =
		// (SeekBar)myWindow.findViewById(R.id.navigation_slider);
		// final TextView text =
		// (TextView)myWindow.findViewById(R.id.navigation_text);
		//
		// final ZLTextView textView = getReader().getTextView();
		// final ZLTextView.PagePosition pagePosition = textView.pagePosition();
		//
		// if (slider.getMax() != pagePosition.Total - 1 || slider.getProgress()
		// != pagePosition.Current - 1) {
		// slider.setMax(pagePosition.Total - 1);
		// slider.setProgress(pagePosition.Current - 1);
		// text.setText(makeProgressText(pagePosition.Current,
		// pagePosition.Total));
		// }
	}

	public void show(int progress, boolean showUpdateFlag, boolean showWeiboBtn, boolean hasMark, boolean showRemind, boolean showBookDetail, String chapterTitle, String log, boolean init) {

		if (init) {
			makeFullScreen(false);
		}

		// 初始化SeekBar
		progress--;
		if (progress < 0) {
			progress = 0;
		}
		mToolbarSeekBar.setProgress(progress);

		mSeekInfo.setVisibility(View.GONE);

		// 设置更新提醒布局的显示状态
		int visible = showRemind ? View.VISIBLE : View.GONE;
		// mUpdateRemind.setCheckedWithOutListener(ReadActivity.gBook.isRemind());
		// mUpdateLayout.setVisibility(visible);
		mBottomToolBar.findViewById(R.id.read_progress_divider1).setVisibility(visible);

		// 设置评论按钮、书籍详情按钮的状态
		int visible1 = showWeiboBtn ? View.VISIBLE : View.GONE;
		// 点评
		mToolbarComment.setVisibility(visible1);
		mBottomToolBar.findViewById(R.id.toolbar_comment_divider).setVisibility(visible1);

		// 摘要
		int visible2 = showBookDetail ? View.VISIBLE : View.GONE;
		mToolbarBookInfo.setVisibility(visible2);
		mToolbarBookInfo.setOnClickListener(this);

		// 设置更新提示按钮的状态
		mUpdateFlag.setVisibility(showUpdateFlag ? View.VISIBLE : View.GONE);

		// 设置书签按钮状态
		mToolbarMark.setImageResource(hasMark ? R.drawable.toolbar_mark_del_bg : R.drawable.toolbar_mark_add_bg);

		// 初始化各个按钮的状态
		updateFontSizeBtn();
		updateReadModeBtn();
		updateCheckboxAndSeekBar();
		updatePraiseStatus();
		reqBookInfo(false);
		// org.geometerplus.zlibrary.core.view.ZLView.Animation animType =
		// pageTurningOptions.Animation
		// .getValue();

		ReadStyleManager mReadStyleManager = ReadStyleManager.getInstance(SinaBookApplication.getTopActivity());
		int animType = mReadStyleManager.getReadAnim();
		updateAnimBtn(animType);
		// updatePraiseStatus();
		setChapterTitle(chapterTitle, log);

		// 初始化显示TAB按钮
		mToolbarProgressView.setVisibility(View.VISIBLE);
		mToolbarSettingView.setVisibility(View.GONE);
		mToolbarCommentView.setVisibility(View.GONE);

		mToolbarProgress.setEnabled(false);
		mToolbarSetting.setEnabled(true);
		mToolbarComment.setEnabled(true);

		if (init) {
			Animation topIn = AnimationUtils.loadAnimation(myActivity, R.anim.slide_in_from_top);
			mTopToolBar.clearAnimation();
			mTopToolBar.setAnimation(topIn);

			Animation bottomIn = AnimationUtils.loadAnimation(myActivity, R.anim.slide_in_from_bottom);
			mBottomToolBar.clearAnimation();
			mBottomToolBar.setAnimation(bottomIn);
		}

		mNavigationRootView.setVisibility(View.VISIBLE);
		mCenterEmptyRectView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 隐藏菜单
				Application.hideActivePopup();
				// hide_();

				// final ZLTextWordCursor position = StartPosition;
				// if (v == btnCancel && position != null) {
				// getReader().getTextView().gotoPosition(position);
				// } else if (v == btnOk) {
				// storePosition();
				// }
				StartPosition = null;
				// Application.hideActivePopup();
				getReader().getViewWidget().reset();
				getReader().getViewWidget().repaint();
			}
		});
	}

	public void dismiss() {
		isNavPop = false;
		makeFullScreen(true);
		Animation topOut = AnimationUtils.loadAnimation(myActivity, R.anim.slide_out_from_top);
		mTopToolBar.clearAnimation();
		mTopToolBar.setAnimation(topOut);

		Animation bottomOut = AnimationUtils.loadAnimation(myActivity, R.anim.slide_out_from_bottom);
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
					ViewParent parent = mNavigationRootView.getParent();
					if (parent != null) {
						((ViewGroup) parent).removeView(mNavigationRootView);
					}
					// mNavigationRootView.setVisibility(View.GONE);
				}
			});
			mBottomToolBar.startAnimation(bottomOut);
		} else {
			ViewParent parent = mNavigationRootView.getParent();
			if (parent != null) {
				((ViewGroup) parent).removeView(mNavigationRootView);
			}
			// mNavigationRootView.setVisibility(View.GONE);
		}
	}

	// 更新字体按钮显示
	private int updateFontSizeBtn() {
		final ZLIntegerRangeOption option = myFBReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption;
		int fontSize = option.getValue();
		int minValue = (int) (ReadStyleManager.MIN_FONT_SIZE_SP);
		int maxvalue = (int) (ReadStyleManager.MAX_FONT_SIZE_SP);
		// int minValue = (int) (ReadStyleManager.MIN_FONT_SIZE_SP +
		// ReadStyleManager.INS_FONT_SIZE_SP);
		// int maxvalue = (int) (ReadStyleManager.MAX_FONT_SIZE_SP +
		// ReadStyleManager.INS_FONT_SIZE_SP);

		if (fontSize >= maxvalue) {
			mFontSizeInc.setEnabled(false);
			mFontSizeDec.setEnabled(true);
		} else if (fontSize <= minValue) {
			mFontSizeInc.setEnabled(true);
			mFontSizeDec.setEnabled(false);
		} else {
			mFontSizeInc.setEnabled(true);
			mFontSizeDec.setEnabled(true);
		}
		return fontSize;
	}

	public void updatePraiseStatus() {
		if (mContext.sinaBook != null && mContext.sinaBook.hasPraised()) {
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

	// 更新阅读样式
	private void updateReadModeBtn() {
		ReadStyleManager mReadStyleManager = ReadStyleManager.getInstance(SinaBookApplication.getTopActivity());
		// Log.i("ouyang",
		// "NavigationPopup--updateReadModeBtn------1-----mReadStyleManager:"+mReadStyleManager);

		if (ReadStyleManager.READ_MODE_NORMAL == mReadStyleManager.getReadMode()) {
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

	public void setChapterTitle(String text, String log) {
		mChapterTitle.setText(text);
	}

	// 更新动画效果
	private void updateAnimBtn(int animType) {
		// int animType = ReadStyleManager.ANIMATION_TYPE_FLIP;
		// int animType = mReadStyleManager.getReadAnim();
		switch (animType) {
		case 0:// 仿真
			mFlipBtn.setEnabled(false);
			mSlideBtn.setEnabled(true);
			mScrollBtn.setEnabled(true);
			pageTurningOptions.Animation.setValue(org.geometerplus.zlibrary.core.view.ZLView.Animation.curl);
			break;

		case 1:// 左右
		case 2:// 上下
			mSlideBtn.setEnabled(false);
			mFlipBtn.setEnabled(true);
			mScrollBtn.setEnabled(true);
			pageTurningOptions.Animation.setValue(org.geometerplus.zlibrary.core.view.ZLView.Animation.left2right);
			break;

		// case 2:
		// mScrollBtn.setEnabled(false);
		// mFlipBtn.setEnabled(true);
		// mSlideBtn.setEnabled(true);
		// break;

		default:
			break;
		}
	}

	// private String makeProgressText(int page, int pagesNumber) {
	// final StringBuilder builder = new StringBuilder();
	// builder.append(page);
	// builder.append("/");
	// builder.append(pagesNumber);
	// final TOCTree tocElement = getReader().getCurrentTOCElement();
	// if (tocElement != null) {
	// builder.append("  ");
	// builder.append(tocElement.getText());
	// }
	// return builder.toString();
	// }

	// 材质列表监听
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// 更改背景
		if (position >= 0 && position < parent.getAdapter().getCount()) {
			ReadStyleManager mReadStyleManager = ReadStyleManager.getInstance(SinaBookApplication.getTopActivity());
			// Log.i("ouyang",
			// "NavigationPopup--onItemClick------1-----mReadStyleManager:"+mReadStyleManager);

			if (ReadStyleManager.READ_MODE_NORMAL != mReadStyleManager.getReadMode()) {
				// 如果不是白天模式点击之后改变阅读模式
				mReadStyleManager.changeReadMode(ReadStyleManager.READ_MODE_NORMAL);
				updateReadModeBtn();
				myFBReaderApp.ViewOptions.ColorProfileName.setValue("defaultLight");
			}

			// 微博读书存储主题记录
			int resId = (Integer) parent.getAdapter().getItem(position);
			mReadStyleManager.setReadBgResId(resId);
			mImageAdapter.notifyDataSetChanged();

			// 存储FBReader颜色记录
			ViewOptions viewOptions = new ViewOptions();
			ColorProfile profile = viewOptions.getColorProfile();
			profile.RegularTextOption.setValue(new ZLColor(mReadStyleManager.mReadTextColor));

			// 这里仅仅为了刷新
			myFBReaderApp.getViewWidget().reset();
			myFBReaderApp.getViewWidget().repaint();

			UserActionManager.getInstance().recordEventValue(Constants.KEY_READ_BACKGROUND + ResourceUtil.getNameById(resId));
		}
	}

	public void onClick(View v) {
		ReadStyleManager mReadStyleManager = ReadStyleManager.getInstance(SinaBookApplication.getTopActivity());

		switch (v.getId()) {
		case R.id.reading_toolbar_home_btn:
			// 返回
			myActivity.onQuit();
			// myFBReaderApp.hideActivePopup();
			// myFBReaderApp.closeWindow();
			break;

		case R.id.reading_toolbar_bookinfo_btn:
			// 摘要
			com.sina.book.data.Book book = new com.sina.book.data.Book();
			book.setBookId(myFBReaderApp.bookId);
			BookDetailActivity.launch(myActivity, book, null, "阅读器_工具栏_01", null);

			UserActionManager.getInstance().recordEvent(Constants.CLICK_READ_DETAIL);
			break;

		case R.id.reading_toolbar_bookmark_btn:// 书签
			// 判断是删除还是添加
			if (hasBookmark()) {
				// 删除
				ZLAndroidWidget widget = (ZLAndroidWidget) myFBReaderApp.getWindow().getViewWidget();
				widget.deleteCurrPageAllBookmark();
			} else {
				// 添加
				Bookmark bookmark = myFBReaderApp.createBookmark(20, true);
				myFBReaderApp.Collection.saveBookmark(bookmark);
			}
			// 隐藏工具栏
			hide_();
			break;
		case R.id.pre_chapter_btn:// 上一章节
			if (!onlyOneChapter) {
				mChapterSeekBarListener.previou();
				// hide_();
			} else {
				// Toast提示：该本书只有一个章节
				GlobalToast.i.toast("该本书只有一个章节");
			}

			break;

		case R.id.next_chapter_btn:// 下一章节
			if (!onlyOneChapter) {
				mChapterSeekBarListener.next();
				// hide_();
			} else {
				// Toast提示：该本书只有一个章节
				GlobalToast.i.toast("该本书只有一个章节");
			}
			break;

		case R.id.reading_toolbar_catalog_btn:// 目录
			Intent dirIntent = new Intent();
			dirIntent.setClass(myActivity.getApplicationContext(), BookTagActivity.class);
			// dirIntent.setClass(myActivity.getApplicationContext(),
			// EpubChapterActivity.class);
			// dirIntent.putExtra("curpos",
			// mPageFactory.getCurrentChapterIndex());
			// DEBUG CJL
			// 假定这里通过判定，得到书籍类型信息为epub。
			dirIntent.putExtra(BookTagActivity.EXTRA_BOOK_TYPE, BookTagActivity.BOOKTYPE_ZXIN_EPUB);
			dirIntent.putExtra("book", myActivity.sinaBook);
			if (!TextUtils.isEmpty(myFBReaderApp.bookId)) {
				dirIntent.putExtra("bookId", myFBReaderApp.bookId);
			}

			dirIntent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			// startActivityForResult(dirIntent, MARK_CHAPTER_CODE);
			OrientationUtil.startActivity(myActivity, dirIntent);
			myActivity.overridePendingTransition(R.anim.push_left_in, R.anim.keep_loaction);
			// disToolBarWithoutFull();
			// hide_();

			UserActionManager.getInstance().recordEvent(Constants.CLICK_READ_CATALOG);
			break;
		case R.id.toolbar_progress_btn:// 进度条
			mToolbarProgressView.setVisibility(View.VISIBLE);

			mToolbarSettingView.setVisibility(View.GONE);
			mToolbarCommentView.setVisibility(View.GONE);

			mSeekInfo.setVisibility(View.GONE);

			mToolbarProgress.setEnabled(false);
			mToolbarSetting.setEnabled(true);
			mToolbarComment.setEnabled(true);
			break;

		case R.id.toolbar_setting_btn:// 设置
			mToolbarSettingView.setVisibility(View.VISIBLE);

			mToolbarProgressView.setVisibility(View.GONE);
			mToolbarCommentView.setVisibility(View.GONE);

			mSeekInfo.setVisibility(View.GONE);

			mToolbarProgress.setEnabled(true);
			mToolbarSetting.setEnabled(false);
			mToolbarComment.setEnabled(true);
			break;

		case R.id.toolbar_comment_btn:// 评论
			mToolbarCommentView.setVisibility(View.VISIBLE);

			mToolbarProgressView.setVisibility(View.GONE);
			mToolbarSettingView.setVisibility(View.GONE);

			mSeekInfo.setVisibility(View.GONE);

			mToolbarProgress.setEnabled(true);
			mToolbarSetting.setEnabled(true);
			mToolbarComment.setEnabled(false);
			break;

		case R.id.toolbar_night_btn:
			// 夜间模式
			mToolbarProgressView.setVisibility(View.GONE);
			mToolbarCommentView.setVisibility(View.GONE);
			mToolbarSettingView.setVisibility(View.GONE);

			mSeekInfo.setVisibility(View.GONE);

			mToolbarProgress.setEnabled(true);
			mToolbarSetting.setEnabled(true);
			mToolbarComment.setEnabled(true);

			// Log.i("ouyang",
			// "NavigationPopup--onCLick------1-----mReadStyleManager:"+mReadStyleManager);

			if (ReadStyleManager.READ_MODE_NORMAL == mReadStyleManager.getReadMode()) {
				mReadStyleManager.changeReadMode(ReadStyleManager.READ_MODE_NIGHT);
				myFBReaderApp.ViewOptions.ColorProfileName.setValue("defaultDark");
				// if (null != mListener) {
				// mListener.readModeChanged(ReadStyleManager.READ_MODE_NIGHT);
				// }
				UserActionManager.getInstance().recordEventValue(Constants.KEY_READ_MODE + "night");
			} else {
				mReadStyleManager.changeReadMode(ReadStyleManager.READ_MODE_NORMAL);
				myFBReaderApp.ViewOptions.ColorProfileName.setValue("defaultLight");
				// if (null != mListener) {
				// mListener
				// .readModeChanged(ReadStyleManager.READ_MODE_NORMAL);
				// }
				UserActionManager.getInstance().recordEventValue(Constants.KEY_READ_MODE + "day");
			}

			myFBReaderApp.getViewWidget().reset();
			myFBReaderApp.getViewWidget().repaint();
			updateReadModeBtn();
			break;

		case R.id.read_setting_font_dec: {// 字体-
			myFBReaderApp.runAction(ActionCode.DECREASE_FONT);
			int fontSize = updateFontSizeBtn();
			GlobalToast.i.toast("当前字体" + fontSize + ".0号");
			UserActionManager.getInstance().recordEventValue(Constants.KEY_FONTSIZE + fontSize);
			break;
		}

		case R.id.read_setting_font_inc: {// 字体+
			myFBReaderApp.runAction(ActionCode.INCREASE_FONT);
			int fontSize = updateFontSizeBtn();
			GlobalToast.i.toast("当前字体" + fontSize + ".0号");
			UserActionManager.getInstance().recordEventValue(Constants.KEY_FONTSIZE + fontSize);
			break;
		}

		case R.id.read_setting_flip:// 3D翻页
			mReadStyleManager.changeReadAnim(ReadStyleManager.ANIMATION_TYPE_FLIP);
			updateAnimBtn(ReadStyleManager.ANIMATION_TYPE_FLIP);

			// if (null != mListener) {
			// mListener.animModeChanged(mReadStyleManager.getReadAnim());
			// }
			UserActionManager.getInstance().recordEventValue(Constants.KEY_ANIMATION + "flip");
			break;

		case R.id.read_setting_slide:// 左右翻页
			mReadStyleManager.changeReadAnim(ReadStyleManager.ANIMATION_TYPE_SILIDE);
			updateAnimBtn(ReadStyleManager.ANIMATION_TYPE_SILIDE);

			// if (null != mListener) {
			// mListener.animModeChanged(mReadStyleManager.getReadAnim());
			// }

			UserActionManager.getInstance().recordEventValue(Constants.KEY_ANIMATION + "slide");
			break;

		case R.id.read_setting_scroll:// 上下翻页
			pageTurningOptions.Animation.setValue(org.geometerplus.zlibrary.core.view.ZLView.Animation.slide);
			mReadStyleManager.changeReadAnim(ReadStyleManager.ANIMATION_TYPE_SCROLL);
			updateAnimBtn(ReadStyleManager.ANIMATION_TYPE_SCROLL);

			// if (null != mListener) {
			// mListener.animModeChanged(mReadStyleManager.getReadAnim());
			// }

			UserActionManager.getInstance().recordEventValue(Constants.KEY_ANIMATION + "scroll");
			break;
		case R.id.read_praise_btn:// 推荐
			if (!mContext.sinaBook.isForbidden()) {
				praise();
			}
			break;

		case R.id.read_comment_btn:// 去评论列表页面
			if (!mContext.sinaBook.isForbidden()) {
				enterCommentListActivity();
			}
			break;
		default:
			// ignore
			break;
		}
	}

	// 系统亮度调节
	public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
		switch (compoundButton.getId()) {
		case R.id.read_setting_auto:
			brightModeChanged(isChecked);
			updateCheckboxAndSeekBar();
			UserActionManager.getInstance().recordEventValue(Constants.IS_AUTO_BRIGHTNESS + isChecked);
			break;
		}
	}

	// 系统亮度自动调节
	public void brightModeChanged(boolean isAuto) {
		if (isAuto) {
			// 设置系统亮度自动调节
			BrightnessUtil.setScreenBrightnessMode(myActivity, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
			StorageUtil.saveBoolean(StorageUtil.KEY_AUTO_BRIGHTNESS, true);
			// 设置为默认值
			BrightnessUtil.setCurrentScreenDefault(myActivity);
		} else {
			// 设置系统亮度手动调节
			BrightnessUtil.setScreenBrightnessMode(myActivity, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			StorageUtil.saveBoolean(StorageUtil.KEY_AUTO_BRIGHTNESS, false);

			BrightnessUtil.setCurrentScreenBrightness(myActivity, StorageUtil.getBrightness());
		}
	}

	// 推荐
	private void praise() {
		if (!HttpUtil.isConnected(mContext)) {
			shortToast(mContext.getString(R.string.network_error));
			return;
		}

		// 第一次进行“赞”的操作：
		// （1）.没有登录微博的情况：弹出登陆对话框，若登陆成功后，进行“赞”，并判断是否开启“微博自动转发”，
		// 若开启，进行赞操作并发送一条微博；若未开启，只进行赞操作，并Toast提示赞成功/失败；
		// （2）.已经登录微博的情况：点击“赞”，需要判断是否开启“微博自动转发”的按钮；
		// 若开启，进行赞操作并发送一条微博；若未开启，只进行赞操作，并Toast提示赞成功/失败；
		if (LoginUtil.isValidAccessToken(mContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			reqPostPraise();
			// 请求的同时更改赞的状态
			updatePraiseBtnStatus();
			if (StorageUtil.getBoolean(StorageUtil.KEY_AUTO_WEIBO)) {
				autoShareWeibo();
			}
		} else {
			LoginDialog.launch(mContext, new LoginStatusListener() {

				@Override
				public void onSuccess() {
					reqBookInfo(true);
					// reqBookPrice();
					reqPostPraise();
				}

				@Override
				public void onFail() {

				}
			});
		}
	}

	private void enterCommentListActivity() {
		Intent intent = new Intent();
		intent.setClass(mContext, CommentListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		intent.putExtra("book", mContext.sinaBook);
		mContext.startActivity(intent);

		UserActionManager.getInstance().recordEvent(Constants.CLICK_READ_COMMENT);
	}

	private Toast mToast;

	protected void shortToast(String content) {
		if (mToast != null) {
			mToast.setText(content);
			mToast.setDuration(Toast.LENGTH_SHORT);
		} else {
			mToast = Toast.makeText(mContext, content, Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	/**
	 * 赞一个
	 */
	private void reqPostPraise() {
		TaskParams params = new TaskParams();

		String url = ConstantData.addLoginInfoToUrl(ConstantData.URL_PRAISE_POST);
		params.put(RequestTask.PARAM_URL, url);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

		ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
		strNParams.add(new BasicNameValuePair("bid", mContext.sinaBook.getBookId()));
		strNParams.add(new BasicNameValuePair("src", mContext.sinaBook.getBookSrc()));
		strNParams.add(new BasicNameValuePair("sid", mContext.sinaBook.getSid()));
		// if (mBookKey != null) {
		// strNParams.add(new BasicNameValuePair(KEY, mBookKey));
		// }

		RequestTask task = new RequestTask(new SimpleParser());
		task.bindActivity(mContext);
		task.setPostParams(strNParams);
		task.setTaskFinishListener(new ITaskFinishListener() {
			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (taskResult.stateCode == HttpStatus.SC_OK && null != taskResult.retObj) {
					if (ConstantData.CODE_SUCCESS.equals(String.valueOf(taskResult.retObj))) {
						mContext.sinaBook.setPraiseType("Y");

						Intent intent = new Intent(DownBookManager.ACTION_INTENT_RECOMMANDSTATE);
						intent.putExtra("bookid", mContext.sinaBook.getBookId());
						intent.putExtra("praise", true);
						SinaBookApplication.gContext.sendBroadcast(intent);
						return;
					}
				}

				mContext.sinaBook.setPraiseType("N");
				reqPostPraise();
			}
		});
		task.execute(params);

		UserActionManager.getInstance().recordEvent(Constants.CLICK_READ_RECOMMEND);
	}

	/**
	 * 更新赞的状态，保证显示已赞
	 */
	private void updatePraiseBtnStatus() {
		shortToast(mContext.getString(R.string.parise_success));
		mContext.sinaBook.setPraiseType("Y");
		updatePraiseStatus();// 更新点赞按钮状态
	}

	/**
	 * 自动分享微博
	 */
	private void autoShareWeibo() {
		Bitmap cover = ImageLoader.getInstance().syncLoadBitmap(mContext.sinaBook.getDownloadInfo().getImageUrl());
		cover = (null == cover) ? ImageLoader.getDefaultPic() : cover;

		Bitmap coverImage = ImageUtil.zoom(cover, mDisplayWidth, mDisplayHeight);

		// Chapter curChapter = mPageFactory.getCurrentChapter();
		com.sina.book.data.Book book = null;
		if (mContext.sinaBook != null) {
			book = mContext.sinaBook;
		} else {
			book = new com.sina.book.data.Book();
			book.setBookId(myFBReaderApp.bookId);
		}
		List<Chapter> chapters = DBService.getAllChapter(book);
		if (chapters == null || chapters.size() <= 0) {
			chapters = book.getChapters();
		}
		TOCTree treeCurrRead = myFBReaderApp.getCurrentTOCElement();
		int index = childAt(treeCurrRead);
		try {
			Chapter chapter = chapters.get(index);
			if (null != chapter) {
				WeiboContent content = new WeiboContent(mContext.sinaBook, WeiboContent.TYPE_PRAISE);
				content.setMsg(mContext.sinaBook.getIntro());
				content.setChapterId(chapter.getGlobalId());
				content.setChapterOffset(0);
				String text = content.getMsg();

				reqPostWeibo(chapter, text);
			} else {
				shortToast(mContext.getString(R.string.share_weibo_failed));
			}
		} catch (Exception e) {
			shortToast(mContext.getString(R.string.share_weibo_failed));
		}
		if (coverImage != null) {
			coverImage.recycle();
		}
	}

	/**
	 * 发送微博
	 * 
	 * @param curChapter
	 *            当前章节
	 * @param msg
	 *            信息
	 */
	private void reqPostWeibo(Chapter curChapter, String msg) {
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, ConstantData.URL_SHARE_WEIBO);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

		ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
		strNParams.add(new BasicNameValuePair("authcode", ConstantData.AUTH_CODE));
		strNParams.add(new BasicNameValuePair("u_id", LoginUtil.getLoginInfo().getUID()));
		strNParams.add(new BasicNameValuePair("access_token", LoginUtil.getLoginInfo().getAccessToken()));
		strNParams.add(new BasicNameValuePair("sid", mContext.sinaBook.getSid()));
		strNParams.add(new BasicNameValuePair("b_id", mContext.sinaBook.getBookId()));
		strNParams.add(new BasicNameValuePair("b_src", mContext.sinaBook.getBookSrc()));
		strNParams.add(new BasicNameValuePair("c_id", "" + curChapter.getGlobalId()));
		strNParams.add(new BasicNameValuePair("c_offset", ""));
		strNParams.add(new BasicNameValuePair("u_comment", msg));
		// if (mBookKey != null) {
		// strNParams.add(new BasicNameValuePair(KEY, mBookKey));
		// }

		RequestTask task = new RequestTask(new SimpleParser());
		task.bindActivity(mContext);
		task.setPostParams(strNParams);
		task.setTaskFinishListener(new PostWeiboFinishListener());
		task.execute(params);

		UserActionManager.getInstance().recordEvent(Constants.ACTION_SHARE_WEIBO);
	}

	/**
	 * 发送微博任务事件监听器
	 */
	private class PostWeiboFinishListener implements ITaskFinishListener {

		@Override
		public void onTaskFinished(TaskResult taskResult) {
			if (taskResult.stateCode == HttpStatus.SC_OK) {
				IParser parser = ((RequestTask) taskResult.task).getParser();
				String stateCode = CODE_FAIL_KEY + "";
				String msg = "请求失败";

				if (null != parser) {
					stateCode = parser.getCode();
					String msgStr = parser.getMsg();

					// 分享微博直接返回了微博API的信息，需要特殊处理（这里只过滤了分享失败的情况）
					if (!TextUtils.isEmpty(msgStr)) {
						boolean error = msgStr.contains("error") || msgStr.contains("ERROR") || msgStr.contains("error_code") || msgStr.contains("ERROR_CODE");
						if (error) {
							msg = mContext.getString(R.string.share_weibo_failed);
						} else {
							msg = msgStr;
						}
					}
				}

				if (stateCode.equals(ConstantData.CODE_SUCCESS) && taskResult.retObj != null) {
					if (taskResult.retObj instanceof String) {
						if (ConstantData.CODE_SUCCESS.equals(String.valueOf(taskResult.retObj))) {
							shortToast(mContext.getString(R.string.share_weibo_success));
						} else {
							shortToast(mContext.getString(R.string.share_weibo_failed));
						}
					}
				} else {
					shortToast(msg);
				}
			}
		}
	}

	/**
	 * 后台请求书籍详情,及时更新部分属性值(是否赞、赞数量、包月信息以及购买信息等)
	 */
	private void reqBookInfo(boolean showToast) {
		final boolean isShowToast = showToast;
		String reqUrl = String.format(ConstantData.URL_BOOK_INFO, mContext.sinaBook.getBookId(), mContext.sinaBook.getSid(), mContext.sinaBook.getBookSrc());
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		// if (mBookKey != null) {
		// reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
		// }

		RequestTask reqTask = new RequestTask(new BookDetailParser());
		reqTask.bindActivity(mContext);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

		reqTask.setTaskFinishListener(new ITaskFinishListener() {
			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (null != taskResult && taskResult.stateCode == HttpStatus.SC_OK) {
					if (taskResult.retObj instanceof BookDetailData) {
						BookDetailData data = (BookDetailData) taskResult.retObj;
						if (null != mContext.sinaBook) {
							mContext.sinaBook.update(data.getBook());
							updatePraiseStatus();

							if (isShowToast) {
								if (mContext.sinaBook.hasPraised()) {
									shortToast(mContext.getString(R.string.book_detail_has_praised_note));
								} else {
									praise();
								}
							}
						}
					}
				}
			}
		});
		reqTask.execute(params);
	}
}
