package com.sina.book.reader;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Scroller;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.reader.model.BookSummaryPostion;
import com.sina.book.reader.selector.SelectorPopMenu;
import com.sina.book.reader.selector.SelectorPopMenu.IMenuClickListener;
import com.sina.book.util.LogUtil;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.ThemeUtil;

import java.lang.reflect.Method;
import java.util.List;

import static com.sina.book.reader.PageGesture.TOUCH_TOOLBAR_AREA;
import static com.sina.book.reader.PageGesture.TOUCH_TYPE_DEF;
import static com.sina.book.reader.PageGesture.TOUCH_TYPE_MARK;
import static com.sina.book.reader.PageGesture.TOUCH_TYPE_NEXTPAGE;
import static com.sina.book.reader.PageGesture.TOUCH_TYPE_PREPAGE;
import static com.sina.book.reader.PageGesture.TOUCH_TYPE_SUMMARY;
import static com.sina.book.reader.PageGesture.TOUCH_TYPE_TOOLBAR;

/**
 * 翻页效果控件
 * 
 * used like: pageWidget.init(width, height, pagefactory);
 * pageWidget.setPageWidgetListener(new PageListener());
 * 
 * @author Tsimle
 * 
 */
public class PageWidget extends View {

	// private static final String TAG = "PageWidget";

	/**
	 * touch时间
	 */
	private static final long TOUCH_QUICKLY_TIME = 800;
	private static final long LONG_CLICK_TIME = 500;

	private static final long TOUCH_INTERVAL_TIME = 200;
	/**
	 * 阴影的默认宽度
	 */
	private static final int SHADOW_DEF_DP = 20;

	/**
	 * 翻页动画的触发值
	 */
	private float PAGE_FLIP_VALUE = 0.5f;

	private float MARK_CHANGE_HEIGHT;
	private float MARK_MAX_HEIGHT;

	/**
	 * 是否允许翻页的交互
	 */
	private boolean mTouchEnabled = true;

	private float mDefaultShadow;
	private int mDisplayWidth;
	private int mDisplayHeight;
	private float mDisplayMaxLength;

	/**
	 * 当前页及下一页内容的bitmap
	 */
	private PageBitmap mCurPage;
	private PageBitmap mNextPage;

	/**
	 * 拖拽点对应的页脚
	 */
	private int mCornerX = 0;
	private int mCornerY = 0;
	/**
	 * 拖拽点和页脚点的中点
	 */
	private float mMiddleX;
	private float mMiddleY;
	private float mDegrees;

	/**
	 * 拖拽点到页脚的距离
	 */
	private float mTouchToCornerDis;

	/**
	 * 当前页翻起背面及下一页看到部分的path
	 */
	private Path mPath0;
	/**
	 * 当前页翻起页背面的path
	 */
	private Path mPath1;

	/**
	 * 拖拽点
	 */
	private PointF mTouch = new PointF();
	/**
	 * 贝塞尔曲线起始点
	 */
	private PointF mBezierStart1 = new PointF();
	/**
	 * 贝塞尔曲线控制点
	 */
	private PointF mBezierControl1 = new PointF();
	/**
	 * 贝塞尔曲线顶点
	 */
	private PointF mBeziervertex1 = new PointF();
	/**
	 * 贝塞尔曲线结束点
	 */
	private PointF mBezierEnd1 = new PointF();

	private PointF mBezierStart2 = new PointF();
	private PointF mBezierControl2 = new PointF();
	private PointF mBeziervertex2 = new PointF();
	private PointF mBezierEnd2 = new PointF();

	// private ColorMatrixColorFilter mColorMatrixFilter;
	private Matrix mMatrix;
	private float[] mMatrixArray = { 0, 0, 0, 0, 0, 0, 0, 0, 1.0f };

	private int[] mBackShadowColors;
	private int[] mFrontShadowColors;
	private GradientDrawable mBackShadowDrawableLR;
	private GradientDrawable mBackShadowDrawableRL;
	private GradientDrawable mFolderShadowDrawableLR;
	private GradientDrawable mFolderShadowDrawableRL;

	private GradientDrawable mFrontShadowDrawableHBT;
	private GradientDrawable mFrontShadowDrawableHTB;
	private GradientDrawable mFrontShadowDrawableVLR;
	private GradientDrawable mFrontShadowDrawableVRL;

	private GradientDrawable mSilideShadowDrawable;

	private Paint mPaint;

	/**
	 * 通过scroller来绘制翻页动画
	 */
	private Scroller mScroller;

	/**
	 * 是否属于右上左下
	 */
	private boolean mIsRTandLB;
	/**
	 * 动画的方向，左边或右边
	 */
	private boolean mIsAnimToLeft;

	/**
	 * touch交互
	 */
	private PageWidgetListener mListener;

	private boolean mIsFirstOrLastPage = false;
	private boolean mIsCenterArea = false;
	private boolean mIsClickEvent;

	private PointF mDownTouch = new PointF();
	private boolean mIsInTouchHandle;
	private int mDownTouchArea;
	private int mTouchType;
	private long mTouchBeginTime;
	private long mTouchEndTime;
	private long mTouchTotalTime;

	private long mLastHandleTouchTime;
	/**
	 * 读书页的图片生成类
	 * 
	 * @param context
	 * @param attr
	 */
	private PageFactory mPageFactory;
	/**
	 * 阅读样式管理类
	 * 
	 * @param context
	 * @param attr
	 */
	private ReadStyleManager mReadStyleManager;
	/**
	 * 阅读手势控制
	 */
	private PageGesture mPageGesture;

	/**
	 * 当使用下拉书签时，保存的view的y轴位置
	 */
	private float mMarkY = 0;
	/**
	 * 上次移动点的y轴位置
	 */
	private float mLastMotionY;
	/**
	 * 上次移动点的x轴位置
	 */
	private float mLastMotionX;

	/**
	 * 翻页动画速度控制
	 */
	private Interpolator mInterpolator;

	/**
	 * 长按状态标志
	 */
	private boolean mLongTouchMode;

	/**
	 * 是否启用长按选择支持
	 */
	private boolean mLongTouchAvailable;

	private SelectorPopMenu mPopupWindow;

	private ILongTouchListener mLongTouchListener;

	/**
	 * 音量键控制是否有效
	 */
	private boolean mVolumeKeyAvailable;

	private Context mContext;

	private long mMainThreadId = -1;

	private Runnable mLongTouchRunnable = new Runnable() {
		@Override
		public void run() {
			mLongTouchMode = true;

			mPageFactory.findSelection(mDownTouch.x, mDownTouch.y, PageFactory.INDEX_DEFAULT);

			if (mPageFactory.isSelectionsLegal()) {
				// 震动70毫秒
				Vibrator vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
				vib.vibrate(70);

				forceInvalidate();

				if (mLongTouchListener != null) {
					mLongTouchListener.onLongTouch(mLongTouchMode);
				}
			} else {
				if (mPageFactory.isVipChapter()) {
					Toast.makeText(mContext, ResourceUtil.getString(R.string.vip_chapter_selector_note),
							Toast.LENGTH_SHORT).show();
				}
				endLongTouch();
			}
		}
	};

	/**
	 * 滑动翻页相关
	 */
	private int mAnimScrollY = 0;
	private int mTouchSlop;
	private int mMinimumVelocity;
	private int mMaximumVelocity;
	private int lastDrawWhat = 0;
	private boolean needLoadContent = false;
	private VelocityTracker mVelocityTracker;

	public PageWidget(Context context, AttributeSet attr) {
		super(context, attr);

		if (isInEditMode()) {
			return;
		}

		mContext = context;

		MARK_CHANGE_HEIGHT = ResourceUtil.getDimens(R.dimen.mark_pull_change);
		MARK_MAX_HEIGHT = ResourceUtil.getDimens(R.dimen.mark_pull_height);

		mPopupWindow = new SelectorPopMenu(context);

		final ViewConfiguration configuration = ViewConfiguration.get(mContext);
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		setDrawingCacheEnabled(false);
	}

	public SelectorPopMenu initTextSelectorMenu(List<String> menus, ReadStyleManager styleManager) {
		return mPopupWindow.init(getRootView(), menus, styleManager);
	}

	/**
	 * 初始化翻页控件
	 * 
	 * @param displayWidth
	 *            阅读内容显示的宽度
	 * @param displayHeight
	 *            阅读内容显示的高度
	 * @param pageFactory
	 *            阅读页生成器
	 */
	public void init(int displayWidth, int displayHeight, PageFactory pageFactory) {
		mDisplayWidth = displayWidth;
		mDisplayHeight = displayHeight;
		mDisplayMaxLength = (float) Math.hypot(mDisplayWidth, mDisplayHeight);
		mDefaultShadow = dp2px(SHADOW_DEF_DP, getContext());

		mPath0 = new Path();
		mPath1 = new Path();
		createDrawable();

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAlpha(40);

		// ColorMatrix cm = new ColorMatrix();
		// float array[] = { 0.55f, 0, 0, 0, 80.0f, 0, 0.55f, 0, 0, 80.0f, 0, 0,
		// 0.55f, 0, 80.0f, 0,
		// 0, 0, 0.2f, 0 };
		// cm.set(array);
		// mColorMatrixFilter = new ColorMatrixColorFilter(cm);
		mMatrix = new Matrix();

		mInterpolator = new DefinedInterception();
		mScroller = new Scroller(getContext(), mInterpolator);
		mTouch.x = 0.01f;
		mTouch.y = 0.01f;
		mPageFactory = pageFactory;
		PageBitmap curPage = mPageFactory.drawCurPage();
		setBitmaps(curPage, curPage);

		mReadStyleManager = ReadStyleManager.getInstance(displayWidth, displayHeight);
		mPageGesture = new PageGesture(displayWidth, displayHeight);

		// 3.0后关闭硬件加速
		if (Build.VERSION.SDK_INT >= 11) {
			try {
				Method layerMethod = View.class.getMethod("setLayerType", int.class, Paint.class);
				int type = Integer.parseInt(View.class.getField("LAYER_TYPE_SOFTWARE").get(null).toString());
				if (layerMethod != null) {
					layerMethod.invoke(this, type, null);
				}
			} catch (Exception e) {
				// no problem
			}
		}

		if (mDisplayWidth > 800) {
			PAGE_FLIP_VALUE = 1f;
		} else {
			PAGE_FLIP_VALUE = 0.5f;
		}

		setVolumeKeyAvailable(true);
	}

	/**
	 * 强制刷新翻页控件，控件会重新加载当前展示页的内容
	 */
	public void forceInvalidate() {
		forceInvalidate(true);
	}

	public void forceInvalidate(boolean contentChanged) {
		if (!contentChanged && mReadStyleManager.getReadAnim() == ReadStyleManager.ANIMATION_TYPE_SCROLL) {
			if (mCurPage != null) {
				mCurPage.reDraw();
			}
			if (mNextPage != null) {
				mNextPage.reDraw();
			}

			LogUtil.d("FileMissingLog", "PageWidget >> forceInvalidate >> 1");
			threadInvalidate();
			return;
		}
		// 当正在交互过程中，不去强制刷新
		if (mScroller.isFinished() && !mIsInTouchHandle || mLongTouchMode) {
			mPageFactory.reDrawBattery();
			PageBitmap curPage = mPageFactory.drawCurPage();
			setBitmaps(curPage, curPage);
			LogUtil.d("FileMissingLog", "PageWidget >> forceInvalidate >> 2");
			threadInvalidate();
		}
	}

	public void setVolumeKeyAvailable(boolean available) {
		mVolumeKeyAvailable = available;
	}

	public void nextPage() {
		if (!mVolumeKeyAvailable) {
			return;
		}
		if (mReadStyleManager.getReadAnim() == ReadStyleManager.ANIMATION_TYPE_SCROLL) {
			return;
		}
		if (mListener != null && mListener.isToolBarVisible()) {
			mListener.onToolBar();
			return;
		}
		if (mListener != null && mListener.isBookSummaryPopVisible()) {
			mListener.onBookSummaryPop(null);
			return;
		}
		float x = mDisplayWidth - 0.1f;
		float y = 0.1f;
		if (!prepareNextPage()) {
			abortAnimation();
			resetCornerAndTouch();
			mIsFirstOrLastPage = true;
			return;
		}
		abortAnimation();
		caculateCornerXY((int) x, (int) y, TOUCH_TYPE_NEXTPAGE);
		mTouch.x = x;
		mTouch.y = y;
		startPageTurnAnimation();
		if (mListener != null) {
			mListener.onPageTurned();
		}
	}

	public void prePage() {
		if (!mVolumeKeyAvailable) {
			return;
		}
		if (mReadStyleManager.getReadAnim() == ReadStyleManager.ANIMATION_TYPE_SCROLL) {
			return;
		}
		if (mListener != null && mListener.isToolBarVisible()) {
			mListener.onToolBar();
			return;
		}
		if (mListener != null && mListener.isBookSummaryPopVisible()) {
			mListener.onBookSummaryPop(null);
			return;
		}

		float x = 0.1f;
		float y = 0.1f;
		if (!preparePrePage()) {
			abortAnimation();
			resetCornerAndTouch();
			mIsFirstOrLastPage = true;
			return;
		}
		abortAnimation();
		caculateCornerXY((int) x, (int) y, TOUCH_TYPE_PREPAGE);
		mTouch.x = x;
		mTouch.y = y;
		startPageTurnAnimation();
		if (mListener != null) {
			mListener.onPageTurned();
		}
	}

	/**
	 * 设置翻页监听器
	 * 
	 * @param listener
	 */
	public void setPageWidgetListener(PageWidgetListener listener) {
		mListener = listener;
	}

	/**
	 * 开启或关闭翻页控件对触摸事件的监听
	 * 
	 * @param touchEnabled
	 */
	public void setTouchEnabled(boolean touchEnabled) {
		this.mTouchEnabled = touchEnabled;
	}

	/**
	 * 设置长按是否有效
	 * 
	 * @param longTouchEnable
	 */
	public void setLongTouchEnable(boolean longTouchEnable) {
		this.mLongTouchAvailable = longTouchEnable;
	}

	/**
	 * 设置长按事件监听器
	 * 
	 * @param longTouchListener
	 */
	public void setLongTouchListener(ILongTouchListener longTouchListener) {
		this.mLongTouchListener = longTouchListener;
	}

	/**
	 * 获取高度
	 * 
	 * @return
	 */
	public int getPopWindowHeight() {
		return mPopupWindow.getHeight();
	}

	/**
	 * 设置菜单点击事件监听器
	 * 
	 * @param clickListener
	 */
	public void setMenuClickListener(IMenuClickListener clickListener) {
		mPopupWindow.setMenuClickListener(clickListener);
	}

	/**
	 * 显示弹出菜单
	 * 
	 * @param x
	 * @param y
	 * @param arrowUp
	 */
	public void showPopupWindow(int x, int y, boolean arrowUp) {
		mPopupWindow.show(x, y, arrowUp);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		long touchHandleInterval = System.currentTimeMillis() - mLastHandleTouchTime;
		
		if (touchHandleInterval < TOUCH_INTERVAL_TIME && !mScroller.isFinished()
				&& e.getAction() == MotionEvent.ACTION_DOWN
				&& mReadStyleManager.getReadAnim() != ReadStyleManager.ANIMATION_TYPE_SCROLL) {
			return false;
		}
		
		if (mTouchEnabled) {
			if (mListener != null && mListener.isToolBarVisible()) {
				mListener.onToolBar();
				return false;
			}
			
			if (mListener != null && mListener.isBookSummaryPopVisible()) {
				mListener.onBookSummaryPop(null);
				return false;
			}
			
			if (mReadStyleManager.getReadAnim() == ReadStyleManager.ANIMATION_TYPE_SCROLL) {
				if (mVelocityTracker == null) {
					mVelocityTracker = VelocityTracker.obtain();
				}
				mVelocityTracker.addMovement(e);
				
				switch (e.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					abortAnimation();

					// 标识touch处理开始
					setIsInTouchHandle(true);
					mIsFirstOrLastPage = false;
					mIsClickEvent = true;
					mTouchBeginTime = System.currentTimeMillis();
					
					// 记录上次事件的点
					mLastMotionY = e.getY();
					
					// 判断touch的区域
					mDownTouchArea = mPageGesture.judgeTouchArea(e);
					break;
					
				case MotionEvent.ACTION_MOVE:
					if (mIsFirstOrLastPage) {
						return false;
					}
					final float moveY = e.getY();
					int deltaY = (int) (mLastMotionY - moveY);
					if (mIsClickEvent) {
						if (Math.abs(deltaY) > mTouchSlop) {
							mIsClickEvent = false;
							doScrollEvent(deltaY);
							mLastMotionY = e.getY();
						}
					} else {
						doScrollEvent(deltaY);
						mLastMotionY = e.getY();
					}

					break;
				case MotionEvent.ACTION_UP:
					// 标识touch处理结束
					setIsInTouchHandle(false);
					mTouchEndTime = System.currentTimeMillis();
					mTouchTotalTime = mTouchEndTime - mTouchBeginTime;
					mLastMotionY = e.getY();

					if (mIsClickEvent) {
						// 长按点击，不做任何处理
						if (mTouchTotalTime >= LONG_CLICK_TIME) {
							Toast.makeText(mContext, "滑动翻页暂不支持长按功能", Toast.LENGTH_SHORT).show();
							break;
						}
						if (mDownTouchArea == TOUCH_TOOLBAR_AREA) {
							if (mListener != null) {
								mListener.onToolBar();
							}
						}
					} else {
						final VelocityTracker velocityTracker = mVelocityTracker;
						velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
						int initialVelocity = (int) velocityTracker.getYVelocity();
						if (Math.abs(initialVelocity) > mMinimumVelocity && !mIsFirstOrLastPage) {
							fling(initialVelocity);
						}
						if (mVelocityTracker != null) {
							mVelocityTracker.recycle();
							mVelocityTracker = null;
						}
					}
					break;
				default:
					break;
				}
				return true;

			} else {
				mPageGesture.formatTouchPoint(e);
				switch (e.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					// 标识touch处理开始
					setIsInTouchHandle(true);
					
					mTouchType = TOUCH_TYPE_DEF;
					mIsClickEvent = false;
					mIsCenterArea = mPageGesture.isCenterArea(e);
					mTouchBeginTime = System.currentTimeMillis();

					// 清理mark移动的相关
					mMarkY = 0;
					mLastMotionX = 0;
					mLastMotionY = 0;

					// 记录down touch点
					mDownTouch.x = e.getX();
					mDownTouch.y = e.getY();
					mIsFirstOrLastPage = false;

					// 判断touch的区域
					mDownTouchArea = mPageGesture.judgeTouchArea(e);

					if (!mLongTouchMode) {
						if (mLongTouchAvailable) {
							/*
							 * 将mLongPressRunnable放进任务队列中，到达设定时间后开始执行
							 * 这里的长按时间采用系统标准长按时间
							 */
							postDelayed(mLongTouchRunnable, ViewConfiguration.getLongPressTimeout());
						}
					} else {
						// 结束长按状态
						endLongTouch();
						return false;
					}
					// Log.d(TAG, "ACTION_DOWN mDownTouchArea:" + mDownTouchArea
					// + " x:" + mDownTouch.x + " y:" + mDownTouch.y);
					break;
				case MotionEvent.ACTION_MOVE:
					if (mLongTouchMode) {
						return false;
					}
					// 1 判断是否是有效的移动
					if (!mPageGesture.isaVaildMove(e, mDownTouch)) {
						return false;
					}
					removeCallbacks(mLongTouchRunnable);

					// 2 当已经判断出是最后一页和第一页屏蔽继续的交互
					if (mPageGesture.isTurnPage(mTouchType)) {
						if (mIsFirstOrLastPage) {
							return false;
						}
					}

					// 3 当未判断移动类型，去判断
					if (mTouchType == TOUCH_TYPE_DEF) {
						mTouchType = mPageGesture.judgeTouchType(e, mDownTouch, mDownTouchArea);

						// 3.1当移动类型为翻页时，准备翻页的内容
						if (mPageGesture.isTurnPage(mTouchType)) {
							if (!preparePage(e)) {
								return false;
							}
						} else if (mTouchType == TOUCH_TYPE_MARK) {
							if (mListener != null) {
								mListener.onPullStart();
							}
						}
					}

					// 4 当向前翻页、向后翻页中间区域触发时改变touch点的位置，让翻页动画显示为平移
					if (mTouchType == TOUCH_TYPE_PREPAGE || (mTouchType == TOUCH_TYPE_NEXTPAGE && mIsCenterArea)) {
						mPageGesture.transPageAnimFormatPoint(e);
					}
					break;
				case MotionEvent.ACTION_UP:
					// Log.d(TAG, "ACTION_UP: x:" + e.getX() + " y:" +
					// e.getY());
					// 标识touch处理结束
					setIsInTouchHandle(false);

					if (mLongTouchMode) {
						mLongTouchListener.onTouchUp(e);
						return false;
					}
					removeCallbacks(mLongTouchRunnable);

					// 1 当已经判断出是最后一页和第一页屏蔽继续的交互
					if (mPageGesture.isTurnPage(mTouchType)) {
						if (mIsFirstOrLastPage) {
							return false;
						}
					}

					// 2 计算整个touch过程花的时间
					mTouchEndTime = System.currentTimeMillis();
					mTouchTotalTime = mTouchEndTime - mTouchBeginTime;
					// Log.d(TAG, "touchTotalTime:" + mTouchTotalTime);

					// 3 未移动，点击事件，需要重新判断触摸类型
					if (mTouchType == TOUCH_TYPE_DEF) {
						mIsClickEvent = true;
						// 先给pageFactory看它是否需要处理点击事件
						BookSummaryPostion bookSummaryPostion = mPageFactory.onTouchBookSummary(e);
						if (null != bookSummaryPostion) {
							mTouchType = TOUCH_TYPE_SUMMARY;
							doBookSummaryTouchEvent(e, bookSummaryPostion);
						} else {
							mTouchType = mPageGesture.judgeTouchType(e, mDownTouch, mDownTouchArea);
							// 长按点击，不做任何处理
							if (mTouchTotalTime >= LONG_CLICK_TIME) {
								return false;
							}
							if (mPageGesture.isTurnPage(mTouchType)) {
								if (!preparePage(e)) {
									return false;
								}
							}
						}
					}

					// 4 当向前翻页、向后翻页中间区域触发时改变touch点的位置，让翻页动画显示为平移
					if (mTouchType == TOUCH_TYPE_PREPAGE || (mTouchType == TOUCH_TYPE_NEXTPAGE && mIsCenterArea)) {
						mPageGesture.transPageAnimFormatPoint(e);
					}

					// Log.d(TAG, "ACTION_UP mTouchType:" + mTouchType);
					// 5 根据touch类型，做按起的后期处理
					switch (mTouchType) {
					case TOUCH_TYPE_TOOLBAR:
						if (mListener != null) {
							mListener.onToolBar();
						}
						break;
					case TOUCH_TYPE_PREPAGE:
					case TOUCH_TYPE_NEXTPAGE:
						caculateTouchToDis();
						if (mTouchTotalTime <= TOUCH_QUICKLY_TIME) {
							mIsClickEvent = true;
						}

						// 如果这次的触发事件不能够翻页，回滚
						if (!canDragOver(e.getX(), e.getY(), mDownTouch, mTouchType, mIsClickEvent)) {
							if (mListener != null) {
								if (mTouchType == TOUCH_TYPE_PREPAGE) {
									mPageFactory.nextPage();
								} else {
									mPageFactory.prePage();
								}
							}
						}
						break;
					default:
						break;
					}

					break;
				default:
					break;
				}

				if (mPageGesture.isTurnPage(mTouchType)) {
					return doTouchEvent(e, mTouchType, mIsClickEvent, mDownTouch);
				} else if (mTouchType == TOUCH_TYPE_MARK) {
					return doMarkTouchEvent(e, mTouchType, mIsClickEvent, mDownTouch);
				} else {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 结束长按状态
	 */
	public void endLongTouch() {
		mPageFactory.clearSelection();
		forceInvalidate();
		mLongTouchMode = false;
		if (mLongTouchListener != null) {
			mLongTouchListener.onLongTouch(mLongTouchMode);
		}

		// 标识touch处理结束
		setIsInTouchHandle(false);
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			float x = mScroller.getCurrX();
			float y = mScroller.getCurrY();
			if (mReadStyleManager.getReadAnim() == ReadStyleManager.ANIMATION_TYPE_SCROLL) {
				final float moveY = y;
				int deltaY = (int) (mLastMotionY - moveY);
				doScrollEvent(deltaY);
				mLastMotionY = moveY;
			} else {
				if (y < 0.1f) {
					y = 0.1f;
				} else if (y > mDisplayHeight - 0.1f) {
					y = mDisplayHeight - 0.1f;
				}
				mTouch.x = x;
				mTouch.y = y;
				ViewCompat.postInvalidateOnAnimation(this);
			}
		}
	}

	private void setBitmaps(PageBitmap curBitmap, PageBitmap nextBitmap) {
		mCurPage = curBitmap;
		mNextPage = nextBitmap;
		if (mCurPage == mNextPage) {
			mAnimScrollY = 0;
			lastDrawWhat = 0;
			needLoadContent = true;
		} else {
			needLoadContent = false;
		}
		if (mCurPage != null) {
			mCurPage.setReadPosScroll(-1);
		}
		if (mNextPage != null) {
			mNextPage.setReadPosScroll(-1);
		}
	}

	private boolean doScrollEvent(int distanceY) {
		// 限制每次scroll事件移动范围一定在一页高度以内
		if (distanceY < -mDisplayHeight) {
			distanceY = -mDisplayHeight;
		} else if (distanceY > mDisplayHeight) {
			distanceY = mDisplayHeight;
		}

		final int originScrollY = mAnimScrollY;
		final int nowScrollY = mAnimScrollY + distanceY;

		// 触发上一页加载
		if (originScrollY >= 0 && nowScrollY < 0) {
			if (!prepareScrollPrePage()) {
				mAnimScrollY = 0;
			} else {
				mAnimScrollY = nowScrollY + mDisplayHeight;
			}
			if (mListener != null) {
				mListener.onPageTurned();
			}
		} else if (originScrollY <= mDisplayHeight && nowScrollY > mDisplayHeight) {
			if (!prepareScrollNextPage()) {
				mAnimScrollY = mDisplayHeight;
			} else {
				mAnimScrollY = nowScrollY - mDisplayHeight;
			}
			if (mListener != null) {
				mListener.onPageTurned();
			}
		} else if (needLoadContent) {
			if (nowScrollY > originScrollY) {
				if (!prepareScrollNextPage()) {
					mAnimScrollY = mDisplayHeight;
				} else {
					mAnimScrollY = nowScrollY;
				}
			} else if (nowScrollY < originScrollY) {
				if (!prepareScrollPrePage()) {
					mAnimScrollY = 0;
				} else {
					mAnimScrollY = nowScrollY;
				}
			} else {
				mAnimScrollY = nowScrollY;
			}
			if (mListener != null) {
				mListener.onPageTurned();
			}
		} else {
			mAnimScrollY = nowScrollY;
		}

		ViewCompat.postInvalidateOnAnimation(this);
		return true;
	}

	/**
	 * 处理touch事件
	 * 
	 * @param event
	 * @return
	 */
	private boolean doTouchEvent(MotionEvent event, int touchType, boolean isClickEvent, PointF downTouch) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (touchType != TOUCH_TYPE_PREPAGE) {
				mTouch.x = event.getX() + (mDisplayWidth - downTouch.x);
			} else {
				mTouch.x = event.getX() - downTouch.x;
			}
			if (mTouch.x < 0.1f) {
				mTouch.x = 0.1f;
			} else if (mTouch.x >= mDisplayWidth - 0.1f) {
				mTouch.x = mDisplayWidth - 0.1f;
			}
			mTouch.y = event.getY();

		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (touchType != TOUCH_TYPE_PREPAGE) {
				mTouch.x = event.getX() + (mDisplayWidth - downTouch.x);
			} else {
				mTouch.x = event.getX() - downTouch.x;
			}
			if (mTouch.x < 0.1f) {
				mTouch.x = 0.1f;
			} else if (mTouch.x >= mDisplayWidth - 0.1f) {
				mTouch.x = mDisplayWidth - 0.1f;
			}
			mTouch.y = event.getY();

			// 第一次进入，初始化上次触摸的点
			if (mLastMotionY == 0 && mLastMotionX == 0) {
				mLastMotionX = downTouch.x;
				mLastMotionY = downTouch.y;
			}

			// 通过上次触摸点做简单的过滤，减少重画的次数
			final float scrollX = Math.abs(event.getX() - mLastMotionX);
			final float scrollY = Math.abs(event.getY() - mLastMotionY);
			if (scrollX > PAGE_FLIP_VALUE || scrollY > PAGE_FLIP_VALUE) {
				threadInvalidate();
			}
			mLastMotionX = event.getX();
			mLastMotionY = event.getY();
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (touchType != TOUCH_TYPE_PREPAGE) {
				mTouch.x = event.getX() + (mDisplayWidth - downTouch.x);
			} else {
				mTouch.x = event.getX() - downTouch.x;
			}
			if (mTouch.x < 0.1f) {
				mTouch.x = 0.1f;
			} else if (mTouch.x >= mDisplayWidth - 0.1f) {
				mTouch.x = mDisplayWidth - 0.1f;
			}
			mTouch.y = event.getY();

			if (canDragOver(event.getX(), event.getY(), downTouch, touchType, isClickEvent)) {
				startPageTurnAnimation();
			} else {
				startPageRollbackAnimation();
			}
			if (mListener != null) {
				mListener.onPageTurned();
			}
			mLastHandleTouchTime = System.currentTimeMillis();
			threadInvalidate();
		}
		return true;
	}

	private void doBookSummaryTouchEvent(MotionEvent event, BookSummaryPostion bookSummaryPostion) {
		if (mListener != null) {
			mListener.onBookSummaryPop(bookSummaryPostion);
		}
	}

	/**
	 * 处理书签类的下拉事件
	 */
	private boolean doMarkTouchEvent(MotionEvent event, int touchType, boolean isClickEvent, PointF downTouch) {
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// 第一次进入，初始化上次触摸的点
			if (mLastMotionY == 0) {
				mLastMotionY = downTouch.y;
			}
			final float scrollY = event.getY() - mLastMotionY;
			if ((Math.abs(scrollY) >= 1)) {
				float lastMarkY = mMarkY;
				mMarkY = mMarkY + scrollY / 2;
				if (mMarkY > MARK_MAX_HEIGHT) {
					mMarkY = MARK_MAX_HEIGHT;
				} else if (mMarkY < 0) {
					mMarkY = 0;
				}

				if (mMarkY != lastMarkY) {
					translateView(0, 0, lastMarkY, mMarkY, 0, null);
				}

				if (mMarkY >= MARK_CHANGE_HEIGHT && lastMarkY < MARK_CHANGE_HEIGHT && mMarkY > lastMarkY) {
					if (mListener != null) {
						mListener.onPullStateDown();
					}
				} else if (mMarkY <= MARK_CHANGE_HEIGHT && lastMarkY > MARK_CHANGE_HEIGHT && mMarkY < lastMarkY) {
					if (mListener != null) {
						mListener.onPullStateUp();
					}
				}

				mLastMotionY = event.getY();
			}
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			final boolean isMarkStatusChanged;
			if (mMarkY >= MARK_CHANGE_HEIGHT) {
				isMarkStatusChanged = true;
			} else {
				isMarkStatusChanged = false;
			}

			long duration = (long) (600.0F * Math.abs(mMarkY / MARK_MAX_HEIGHT));
			translateView(0, 0, mMarkY, 0, duration, new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (isMarkStatusChanged) {
						if (mListener != null) {
							mListener.onPullDown();
						}
					}
					if (mListener != null) {
						mListener.onPullEnd();
					}
				}
			});
		}
		return true;
	}

	private void translateView(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, long duration,
			AnimationListener listener) {
		TranslateAnimation transAnimation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
		transAnimation.setDuration(duration);
		transAnimation.setFillAfter(true);
		transAnimation.setFillEnabled(true);
		transAnimation.setAnimationListener(listener);
		this.startAnimation(transAnimation);
	}

	private void abortAnimation() {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
	}

	private void resetCornerAndTouch() {
		mTouch.x = 0.01f;
		mTouch.y = 0.01f;
		mCornerX = 0;
		mCornerY = 0;
	}

	/**
	 * 计算页脚点
	 * 
	 * @param touchType
	 *            1:触发上一页 2:触发下一页
	 */
	private void caculateCornerXY(float x, float y, int touchType) {
		// 上一页动画往右边
		if (touchType == 1) {
			mIsAnimToLeft = false;
		} else {
			mIsAnimToLeft = true;
		}
		mCornerX = mDisplayWidth;

		if (y <= mDisplayHeight / 2) {
			mCornerY = 0;
		} else {
			mCornerY = mDisplayHeight;
		}

		if ((mCornerX == 0 && mCornerY == mDisplayHeight) || (mCornerX == mDisplayWidth && mCornerY == 0)) {
			mIsRTandLB = true;
		} else {
			mIsRTandLB = false;
		}
		// Log.d(TAG, "caculateCornerXY: Cornerx" + mCornerX + "Cornery:"
		// + mCornerY + " x:" + x + " y:" + y);
	}

	/**
	 * 计算拖拽点到页脚点的距离
	 */
	private void caculateTouchToDis() {
		mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX), (mTouch.y - mCornerY));
	}

	/**
	 * 是触发翻页还是回滚动作
	 * 
	 * @param touchType
	 *            1:触发上一页 2:触发下一页
	 */
	private boolean canDragOver(float x, float y, PointF downTouch, int touchType, boolean isClickEvent) {
		if (isClickEvent) {
			return true;
		}
		if (touchType != TOUCH_TYPE_PREPAGE) {
			x = x + (mDisplayWidth - downTouch.x);
		} else {
			x = x - downTouch.x;
		}
		if (mTouchType == TOUCH_TYPE_NEXTPAGE) {
			if (mTouchToCornerDis > mDisplayMaxLength / 6)
				return true;
		} else {
			if (x > mDisplayWidth / 6) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 准备翻页用到的图片，上一页或下一页 如果第一页或最后一页，返回false
	 * 
	 * @param e
	 * @return
	 */
	private boolean preparePage(MotionEvent e) {
		if (mTouchType == TOUCH_TYPE_PREPAGE) {
			if (!preparePrePage()) {
				abortAnimation();
				resetCornerAndTouch();
				mIsFirstOrLastPage = true;
				return false;
			}
			abortAnimation();
			mPageGesture.transPageAnimFormatPoint(e);
		} else {
			if (!prepareNextPage()) {
				abortAnimation();
				resetCornerAndTouch();
				mIsFirstOrLastPage = true;
				return false;
			}
			abortAnimation();
			if (mIsCenterArea) {
				mPageGesture.transPageAnimFormatPoint(e);
			}
		}

		caculateCornerXY((int) e.getX(), (int) e.getY(), mTouchType);
		return true;
	}

	private boolean preparePrePage() {
		PageBitmap curPage = mPageFactory.drawCurPage();
		PageBitmap prePage = mPageFactory.drawPrePage();
		if (prePage == null) {
			if (mListener != null) {
				mListener.onFirstOrLastPage(true);
			}
			return false;
		}
		setBitmaps(prePage, curPage);
		// 预加载上一章节的时候，如果上一章节未预加载成功，便断开了网络
		// ，此时一直翻页，翻到本章节第一页时，prePage为空了
		// 为了解决BugID=21420问题，将mListener.onFirstOrLastPage(true);
		// 放置在mListener.onPrePage();的前面，这样就能解决翻到本章节第一页时
		// 去下载上一章节出现错误时弹出“加载失败”的错误界面
		if (mListener != null) {
			mListener.onPrePage();
		}
		return true;
	}

	private boolean prepareNextPage() {
		PageBitmap curPage = mPageFactory.drawCurPage();
		PageBitmap nextPage = mPageFactory.drawNextPage();
		if (nextPage == null) {
			if (mListener != null) {
				mListener.onFirstOrLastPage(false);
			}
			return false;
		}
		setBitmaps(curPage, nextPage);
		// 预加载下一章节的时候，如果下一章节未预加载成功，便断开了网络
		// ，此时一直翻页，翻到本章节最后一页时，nextPage为空了
		// 为了解决BugID=21420问题，将mListener.onFirstOrLastPage(false);
		// 放置在mListener.onNextPage();的前面，这样就能解决翻到本章节最后一页时
		// 去下载下一章节出现错误时弹出“加载失败”的错误界面
		if (mListener != null) {
			mListener.onNextPage();
		}
		return true;
	}

	private boolean prepareScrollPrePage() {
		if (mListener != null) {
			mListener.onPrePage();
		}
		if (lastDrawWhat == 2) {
			mPageFactory.prePage();
		}
		PageBitmap curPage = mPageFactory.drawCurPage();
		PageBitmap prePage = mPageFactory.drawPrePage();
		if (prePage == null) {
			if (mListener != null) {
				mIsFirstOrLastPage = true;
				abortAnimation();
				mListener.onFirstOrLastPage(true);
			}
			needLoadContent = true;
			lastDrawWhat = 0;
			return false;
		} else {
			lastDrawWhat = 1;
			setBitmaps(prePage, curPage);
			return true;
		}
	}

	private boolean prepareScrollNextPage() {
		if (mListener != null) {
			mListener.onNextPage();
		}
		if (lastDrawWhat == 1) {
			mPageFactory.nextPage();
		}
		PageBitmap curPage = mPageFactory.drawCurPage();
		PageBitmap nextPage = mPageFactory.drawNextPage();
		if (nextPage == null) {
			if (mListener != null) {
				mIsFirstOrLastPage = true;
				abortAnimation();
				mListener.onFirstOrLastPage(false);
			}
			needLoadContent = true;
			lastDrawWhat = 0;
			return false;
		} else {
			lastDrawWhat = 2;
			setBitmaps(curPage, nextPage);
			return true;
		}
	}

	private void startPageTurnAnimation() {
		int dx, dy;
		if (!mIsAnimToLeft) {
			dx = mDisplayWidth - (int) mTouch.x;
		} else {
			dx = -(mDisplayWidth + (int) mTouch.x);
		}

		if (mCornerY > 0) {
			dy = mDisplayHeight - (int) mTouch.y;
		} else {
			dy = (int) -mTouch.y;
		}

		scroll((int) mTouch.x, (int) mTouch.y, dx, dy);
	}

	private void startPageRollbackAnimation() {
		int dx, dy;
		if (!mIsAnimToLeft) {
			dx = -((int) mTouch.x + mDisplayWidth);
		} else {
			dx = mDisplayWidth - (int) mTouch.x;
		}

		if (mCornerY > 0) {
			dy = mDisplayHeight - (int) mTouch.y;
		} else {
			dy = (int) -mTouch.y;
		}

		scroll((int) mTouch.x, (int) mTouch.y, dx, dy);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// 保留两位精度
		mTouch.x = (int) (mTouch.x * 100) / 100f;
		mTouch.y = (int) (mTouch.y * 100) / 100f;

		if (mCurPage == null || mNextPage == null) {
			return;
		}

		final Bitmap curBitmap = mCurPage.getBitmap();
		final Bitmap nextBitmap = mNextPage.getBitmap();

		// reset scroll pos
		if (mCurPage != null) {
			mCurPage.setReadPosScroll(-1);
		}
		if (mNextPage != null) {
			mNextPage.setReadPosScroll(-1);
		}

		int animType = mReadStyleManager.getReadAnim();
		if (animType != ReadStyleManager.ANIMATION_TYPE_SILIDE || curBitmap == nextBitmap) {
			drawContentBg(canvas);
		}
		switch (animType) {
		case ReadStyleManager.ANIMATION_TYPE_FLIP:// 仿真翻页
			if (isToolBarVisible()) {
				if (!mScroller.isFinished()) {
					mTouch.x = mScroller.getFinalX();
					mTouch.y = mScroller.getFinalY();
					if (mTouch.y < 0.1f) {
						mTouch.y = 0.1f;
					}
					if (mTouch.y > mDisplayHeight - 0.1f) {
						mTouch.y = mDisplayHeight - 0.1f;
					}
					abortAnimation();
				}
				drawCurrentPage(canvas, curBitmap);
				drawNextPage(canvas, nextBitmap);
				drawCurPageShadow(canvas);
				break;
			}
			caculatePoints();
			drawCurrentPageArea(canvas, curBitmap);
			if (mTouch.x > mReadStyleManager.getRightX() && mTouchType == TOUCH_TYPE_PREPAGE) {
				break;
			}
			drawNextPageAreaAndShadow(canvas, nextBitmap);
			drawCurrentPageShadow(canvas);
			drawCurrentBackArea(canvas, curBitmap);
			break;

		case ReadStyleManager.ANIMATION_TYPE_SILIDE:// 覆盖动画
			drawCurrentPage(canvas, curBitmap);
			drawNextPage(canvas, nextBitmap);
			drawCurPageShadow(canvas);
			break;
		case ReadStyleManager.ANIMATION_TYPE_SCROLL:// 滑动动画

			int startY = mReadStyleManager.getScrollTop();
			int endY = mReadStyleManager.getScrollBottom();
			int contentH = mReadStyleManager.getScrollContent();

			int scrollYReal = (int) (((float) mAnimScrollY / mDisplayHeight) * contentH);
			if (scrollYReal > contentH) {
				scrollYReal = contentH;
			}
			if (scrollYReal < 0) {
				scrollYReal = 0;
			}

			canvas.save();
			canvas.clipRect(0, 0, mDisplayWidth, startY);
			canvas.drawBitmap(curBitmap, 0, 0, null);
			canvas.restore();

			canvas.save();
			canvas.clipRect(0, startY, mDisplayWidth, endY - scrollYReal);
			canvas.drawBitmap(curBitmap, 0, -scrollYReal, null);
			canvas.restore();
			// record scroll pos
			if (mCurPage != null) {
				mCurPage.calculateReadPosScroll(scrollYReal);
			}

			canvas.save();
			canvas.clipRect(0, endY - scrollYReal, mDisplayWidth, endY);
			canvas.drawBitmap(nextBitmap, 0, contentH - scrollYReal, null);
			canvas.restore();

			canvas.save();
			canvas.clipRect(0, endY, mDisplayWidth, mDisplayHeight);
			canvas.drawBitmap(curBitmap, 0, 0, null);
			canvas.restore();
			break;
		}
	}

	/**
	 * 画阅读背景<br>
	 * 因为这里的画背景99%的情况是会被阅读内容覆盖<br>
	 * 只有异常情况需要这底色，所以不按阅读主题画，耗费时间<br>
	 * 
	 * @param canvas
	 */
	private void drawContentBg(Canvas canvas) {
		if (ReadStyleManager.READ_MODE_NIGHT == mReadStyleManager.getReadMode()) {
			// 如果是夜间模式，则直接绘制夜间模式背景
			canvas.drawColor(ResourceUtil.getColor(R.color.reading_bg_night));
		} else {
			int resId = mReadStyleManager.getReadBgResId();
			canvas.drawColor(ThemeUtil.getBackAreaColor(resId));
		}
	}

	/**
	 * 覆盖动画--画出当前页
	 * 
	 * @param canvas
	 * @param bitmap
	 */
	private void drawCurrentPage(Canvas canvas, Bitmap bitmap) {
		if (canvas == null || bitmap == null || bitmap.isRecycled())
			return;

		float dx = mTouch.x;
		canvas.save();
		canvas.clipRect(0, 0, mTouch.x, mDisplayHeight);
		canvas.drawBitmap(bitmap, -mDisplayWidth + dx, 0, null);
		canvas.restore();
	}

	/**
	 * 覆盖动画--画出当前页阴影
	 * 
	 * @param canvas
	 */
	private void drawCurPageShadow(Canvas canvas) {
		if (mTouch.x <= 0.01f && mTouch.y <= 0.01f) {
			return;
		}
		GradientDrawable mCurrentPageShadow = mSilideShadowDrawable;

		mCurrentPageShadow.setBounds((int) mTouch.x, 0, (int) (mTouch.x + mDefaultShadow), mDisplayHeight);
		canvas.save();
		mCurrentPageShadow.draw(canvas);
		canvas.restore();
	}

	/**
	 * 覆盖动画--画出下一页
	 * 
	 * @param canvas
	 * @param bitmap
	 */
	private void drawNextPage(Canvas canvas, Bitmap bitmap) {
		if (canvas == null || bitmap == null || bitmap.isRecycled())
			return;

		canvas.save();
		canvas.clipRect(mTouch.x, 0, mDisplayWidth, mDisplayHeight);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();
	}

	private void createDrawable() {
		int[] color = { 0x222222, 0x60222222 };
		mFolderShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, color);
		mFolderShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFolderShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color);
		mFolderShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowColors = new int[] { 0x80111111, 0x111111 };
		mBackShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
		mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
		mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowColors = new int[] { 0x60222222, 0x222222 };
		mFrontShadowDrawableVLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
		mFrontShadowDrawableVLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableVRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
		mFrontShadowDrawableVRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHTB = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
		mFrontShadowDrawableHTB.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHBT = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
		mFrontShadowDrawableHBT.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		int[] silideColors = new int[] { 0x60222222, 0x222222 };
		mSilideShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, silideColors);
		mSilideShadowDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
	}

	/**
	 * 计算贝塞尔曲线的各点的值
	 */
	private void caculatePoints() {
		// 尽量防止触摸点在x轴和y轴上
		if (mTouch.x == 0) {
			mTouch.x = 0.01f;
		} else if (mTouch.x == mDisplayWidth) {
			mTouch.x = mDisplayWidth - 0.01f;
		}
		if (mTouch.y == 0) {
			mTouch.y = 0.01f;
		} else if (mTouch.y == mDisplayHeight) {
			mTouch.y = mDisplayHeight - 0.01f;
		}

		mMiddleX = (mTouch.x + mCornerX) / 2;
		mMiddleY = (mTouch.y + mCornerY) / 2;
		mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
		mBezierControl1.y = mCornerY;
		mBezierControl2.x = mCornerX;
		mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

		mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2;
		mBezierStart1.y = mCornerY;

		if (mTouch.x > 0 && mTouch.x < mDisplayWidth) {
			if (mBezierStart1.x < 0 || mBezierStart1.x > mDisplayWidth) {
				if (mBezierStart1.x < 0)
					mBezierStart1.x = mDisplayWidth - mBezierStart1.x;

				float f1 = Math.abs(mCornerX - mTouch.x);
				float f2 = mDisplayWidth * f1 / mBezierStart1.x;
				mTouch.x = Math.abs(mCornerX - f2);

				float f3 = Math.abs(mCornerX - mTouch.x) * Math.abs(mCornerY - mTouch.y) / f1;
				mTouch.y = Math.abs(mCornerY - f3);

				mMiddleX = (mTouch.x + mCornerX) / 2;
				mMiddleY = (mTouch.y + mCornerY) / 2;

				mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
				mBezierControl1.y = mCornerY;

				mBezierControl2.x = mCornerX;
				mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
				mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2;
			}
		}
		mBezierStart2.x = mCornerX;
		mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y) / 2;

		mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX), (mTouch.y - mCornerY));

		mBezierEnd1 = getCross(mTouch, mBezierControl1, mBezierStart1, mBezierStart2);
		mBezierEnd2 = getCross(mTouch, mBezierControl2, mBezierStart1, mBezierStart2);
		mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
		mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
		mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
		mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;
	}

	/**
	 * 仿真动画--画出当前页
	 * 
	 * @param canvas
	 * @param bitmap
	 */
	private void drawCurrentPageArea(Canvas canvas, Bitmap bitmap) {
		mPath0.reset();
		mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x, mBezierEnd1.y);
		mPath0.lineTo(mTouch.x, mTouch.y);
		mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x, mBezierStart2.y);
		mPath0.lineTo(mCornerX, mCornerY);
		mPath0.close();

		canvas.save();
		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();
	}

	/**
	 * 仿真动画--画出下一页阴影
	 * 
	 * @param canvas
	 * @param bitmap
	 */
	private void drawNextPageAreaAndShadow(Canvas canvas, Bitmap bitmap) {
		mPath1.reset();
		mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.lineTo(mCornerX, mCornerY);
		mPath1.close();

		mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x - mCornerX, mBezierControl2.y - mCornerY));
		int leftx;
		int rightx;
		GradientDrawable mBackShadowDrawable;
		if (mIsRTandLB) {
			leftx = (int) (mBezierStart1.x);
			rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
			mBackShadowDrawable = mBackShadowDrawableLR;
		} else {
			leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
			rightx = (int) mBezierStart1.x;
			mBackShadowDrawable = mBackShadowDrawableRL;
		}
		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
		mBackShadowDrawable
				.setBounds(leftx, (int) mBezierStart1.y, rightx, (int) (mDisplayMaxLength + mBezierStart1.y));
		mBackShadowDrawable.draw(canvas);
		canvas.restore();
	}

	/**
	 * 仿真动画--画出当前页阴影
	 * 
	 * @param canvas
	 */
	private void drawCurrentPageShadow(Canvas canvas) {
		if (mTouch.x <= 0.01f && mTouch.y <= 0.01f) {
			return;
		}
		double degree;
		if (mIsRTandLB) {
			degree = Math.PI / 4 - Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x - mBezierControl1.x);
		} else {
			degree = Math.PI / 4 - Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x - mBezierControl1.x);
		}
		int shadow = (int) (mDefaultShadow * (Math.abs(mTouch.x - mCornerX) / mCornerX));
		double d1 = (float) shadow * 1.414 * Math.cos(degree);
		double d2 = (float) shadow * 1.414 * Math.sin(degree);
		float x = (float) (mTouch.x + d1);
		float y;
		if (mIsRTandLB) {
			y = (float) (mTouch.y + d2);
		} else {
			y = (float) (mTouch.y - d2);
		}
		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
		mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.close();
		float rotateDegrees;
		canvas.save();

		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		int leftx;
		int rightx;
		GradientDrawable mCurrentPageShadow;
		if (mIsRTandLB) {
			leftx = (int) (mBezierControl1.x);
			rightx = (int) mBezierControl1.x + shadow;
			mCurrentPageShadow = mFrontShadowDrawableVLR;
		} else {
			leftx = (int) (mBezierControl1.x - shadow);
			rightx = (int) mBezierControl1.x + 1;
			mCurrentPageShadow = mFrontShadowDrawableVRL;
		}

		rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouch.x - mBezierControl1.x, mBezierControl1.y - mTouch.y));
		canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
		mCurrentPageShadow.setBounds(leftx, (int) (mBezierControl1.y - mDisplayMaxLength), rightx,
				(int) (mBezierControl1.y));
		mCurrentPageShadow.draw(canvas);
		canvas.restore();

		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.close();
		canvas.save();
		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		if (mIsRTandLB) {
			leftx = (int) (mBezierControl2.y);
			rightx = (int) (mBezierControl2.y + shadow);
			mCurrentPageShadow = mFrontShadowDrawableHTB;
		} else {
			leftx = (int) (mBezierControl2.y - shadow);
			rightx = (int) (mBezierControl2.y + 1);
			mCurrentPageShadow = mFrontShadowDrawableHBT;
		}
		rotateDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl2.y - mTouch.y, mBezierControl2.x - mTouch.x));
		canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y);
		float temp;
		if (mBezierControl2.y < 0)
			temp = mBezierControl2.y - mDisplayHeight;
		else
			temp = mBezierControl2.y;

		int hmg = (int) Math.hypot(mBezierControl2.x, temp);
		if (hmg > mDisplayMaxLength)
			mCurrentPageShadow.setBounds((int) (mBezierControl2.x - shadow) - hmg, leftx,
					(int) (mBezierControl2.x + mDisplayMaxLength) - hmg, rightx);
		else
			mCurrentPageShadow.setBounds((int) (mBezierControl2.x - mDisplayMaxLength), leftx,
					(int) (mBezierControl2.x), rightx);
		mCurrentPageShadow.draw(canvas);
		canvas.restore();
	}

	/**
	 * 仿真动画--画出当前页后面部分
	 * 
	 * @param canvas
	 * @param bitmap
	 */
	private void drawCurrentBackArea(Canvas canvas, Bitmap bitmap) {
		int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
		float f1 = Math.abs(i - mBezierControl1.x);
		int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
		float f2 = Math.abs(i1 - mBezierControl2.y);
		float f3 = Math.min(f1, f2);
		mPath1.reset();
		mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath1.close();
		GradientDrawable mFolderShadowDrawable;
		int left;
		int right;
		if (mIsRTandLB) {
			left = (int) (mBezierStart1.x - 1);
			right = (int) (mBezierStart1.x + f3 + 1);
			mFolderShadowDrawable = mFolderShadowDrawableLR;
		} else {
			left = (int) (mBezierStart1.x - f3 - 1);
			right = (int) (mBezierStart1.x + 1);
			mFolderShadowDrawable = mFolderShadowDrawableRL;
		}
		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);

		// mPaint.setColorFilter(mColorMatrixFilter);

		float dis = (float) Math.hypot(mCornerX - mBezierControl1.x, mBezierControl2.y - mCornerY);
		float f8 = (mCornerX - mBezierControl1.x) / dis;
		float f9 = (mBezierControl2.y - mCornerY) / dis;
		mMatrixArray[0] = 1 - 2 * f9 * f9;
		mMatrixArray[1] = 2 * f8 * f9;
		mMatrixArray[3] = mMatrixArray[1];
		mMatrixArray[4] = 1 - 2 * f8 * f8;
		mMatrix.reset();
		mMatrix.setValues(mMatrixArray);
		mMatrix.preTranslate(-mBezierControl1.x, -mBezierControl1.y);
		mMatrix.postTranslate(mBezierControl1.x, mBezierControl1.y);
		canvas.drawBitmap(bitmap, mMatrix, mPaint);
		// mPaint.setColorFilter(null);
		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
		mFolderShadowDrawable
				.setBounds(left, (int) mBezierStart1.y, right, (int) (mBezierStart1.y + mDisplayMaxLength));
		mFolderShadowDrawable.draw(canvas);
		canvas.restore();
	}

	private PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
		PointF CrossP = new PointF();
		float a1 = (P2.y - P1.y) / (P2.x - P1.x);
		float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

		float a2 = (P4.y - P3.y) / (P4.x - P3.x);
		float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
		CrossP.x = (b2 - b1) / (a1 - a2);
		CrossP.y = a1 * CrossP.x + b1;
		return CrossP;
	}

	private boolean isToolBarVisible() {
		if (mListener != null && mListener.isToolBarVisible()) {
			return true;
		} else {
			return false;
		}
	}

	private float dp2px(float value, Context context) {
		return PixelUtil.dp2px(value, context);
	}

	private void setIsInTouchHandle(boolean isInTouchHandle) {
		this.mIsInTouchHandle = isInTouchHandle;
	}

	// 滑动翻页fling
	public void fling(int velocityY) {
		velocityY = velocityY * 3 / 4;
		if (velocityY > 4 * mDisplayHeight) {
			velocityY = 4 * mDisplayHeight;
		} else if (velocityY < -4 * mDisplayHeight) {
			velocityY = -4 * mDisplayHeight;
		}

		int curY = (int) mLastMotionY;
		int minY = Math.min(curY + velocityY, 0);
		int maxY = Math.max(curY + velocityY, mDisplayHeight);
		mScroller.fling(0, curY, 0, velocityY, 0, 0, minY, maxY);
		ViewCompat.postInvalidateOnAnimation(this);
	}

	public void scroll(int sx, int sy, int dx, int dy) {
		int distance = (int) Math.sqrt((dx - sx) * (dx - sx) + (dy - sy) * (dy - sy));
		int delay;
		// 回翻
		if (dx > sx) {
			delay = (int) (1.2f * PixelUtil.px2dp(distance));
		} else {
			delay = (int) (0.5f * PixelUtil.px2dp(distance));
		}
		if (mReadStyleManager.getReadAnim() == ReadStyleManager.ANIMATION_TYPE_SCROLL) {

		} else {
			mScroller.startScroll(sx, sy, dx, dy, delay);
		}
		ViewCompat.postInvalidateOnAnimation(this);
	}

	private void threadInvalidate() {
		try {
			if (mMainThreadId < 0) {
				mMainThreadId = Looper.getMainLooper().getThread().getId();
			}
			if (mMainThreadId == Thread.currentThread().getId()) {
				LogUtil.d("FileMissingLog", "PageWidget >> forceInvalidate >> 2");
				invalidate();
			} else {
				LogUtil.d("FileMissingLog", "PageWidget >> forceInvalidate >> 3");
				postInvalidate();
			}
		} catch (Exception e) {
			LogUtil.d("FileMissingLog", "PageWidget >> forceInvalidate >> 4");
			postInvalidate();
		}
	}

	private class DefinedInterception implements Interpolator {

		private AccelerateDecelerateInterpolator accelerateInterpolator;
		private float sViscousFluidScale;
		private float sViscousFluidNormalize;

		public DefinedInterception() {
			accelerateInterpolator = new AccelerateDecelerateInterpolator();
			if (mDisplayHeight <= 480) {
				sViscousFluidScale = 3.0f;
			} else {
				sViscousFluidScale = 2.0f;
			}
			sViscousFluidNormalize = 1.0f;
			sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
		}

		@Override
		public float getInterpolation(float input) {
			if (mReadStyleManager.getReadAnim() != ReadStyleManager.ANIMATION_TYPE_FLIP) {
				return viscousFluid(input);
			} else {
				return accelerateInterpolator.getInterpolation(input);
			}
		}

		private float viscousFluid(float x) {
			x *= sViscousFluidScale;
			if (x < 1.0f) {
				x -= (1.0f - (float) Math.exp(-x));
			} else {
				float start = 0.36787944117f; // 1/e == exp(-1)
				x = 1.0f - (float) Math.exp(1.0f - x);
				x = start + x * (1.0f - start);
			}
			x *= sViscousFluidNormalize;
			return x;
		}
	}
}
