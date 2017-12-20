package com.sina.book.reader;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ReadPageDimenUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.ThemeUtil;

/**
 * 将所有阅读样式的转变统一放这里
 * 
 * @author Tsimle
 * 
 */
public class ReadStyleManager
{
	// private static final String TAG = "ReadStyleManager";
	// TODO:
	// public static final float INS_FONT_SIZE_SP = 14;

	/** 最大字体大小（30sp） */
	public static final float		MAX_FONT_SIZE_SP		= 30;
	/** 最小字体大小（12sp） */
	public static final float		MIN_FONT_SIZE_SP		= 12;
	/** 默认字体大小（20sp） */
	public static final float		DEF_FONT_SIZE_SP		= 20;

	/** 白天阅读模式 */
	public static final int			READ_MODE_NORMAL		= 0;
	/** 夜间阅读模式 */
	public static final int			READ_MODE_NIGHT			= 1;

	/** 翻页动画 */
	public static final int			ANIMATION_TYPE_FLIP		= 0;
	/** 平滑动画 */
	public static final int			ANIMATION_TYPE_SILIDE	= 1;
	/** 滑动动画 */
	public static final int			ANIMATION_TYPE_SCROLL	= 2;

	private static final String		NIGHT_SUFFIX			= "_night";

	/** 一页最多有多少字节 */
	private int						mMaxBytesApage			= 1500;
	/** 一行最大长度 */
	private float[]					mMeasuredWidths			= new float[1];
	/** 一页的行数 */
	private int						mLineCount;
	/** 当前字体大小（sp） */
	private float					mCurrentFontSize;
	/** 行间距（px） */
	private float					mLineSpaceHeight;
	/** 标题字体大小（px） */
	private int						mHeaderFontSize;
	/** 底部文字字体大小（px） */
	public int						mFooterFontSize;

	/** 屏幕的宽高 */
	private int						mDisplayWidth;
	private int						mDisplayHeight;

	/** 绘制内容的宽高 */
	private float					mVisibleHeight;
	private float					mVisibleWidth;

	/** 绘制的内容左右边距 **/
	private float					mMarginHorizontal;
	/** 绘制的内容上边距 **/
	private float					mTopMargin;
	/** 绘制的内容下边距 **/
	public float					mBottomMargin;

	/** 绘制的阅读内容距离上标题栏 */
	private float					mContentMarginTop;
	/** 绘制的阅读内容距离下标题栏 */
	private float					mContentMarginBottom;

	/** 当前阅读模式 */
	private int						mReadMode;

	/** 阅读页字体颜色 */
	public int						mReadTextColor;
	private int						mHeaderTextColor;
	private int						mFooterTextColor;

	/** 选中的文字的颜色 */
	private int						mSelectionTextColor;

	/** 下划线颜色 */
	private int						mUnderLineColor;

	/** 阅读页字体 */
	private Typeface				mTextTypeFace;

	/** 阅读页翻页动画 */
	private int						mReadAnimType;

	/** 缓存起来头部和底部的画笔 */
	private Paint					mHeaderPaint;
	private Paint					mFooterPaint;

	/** 阅读背景图片（当选择主题为图片类型时缓存此图片提高翻页速度） */
	private Bitmap					mReadBackground;
	private Canvas					mReadBgCavas;

	/** 显示电量的矩形区域背景颜色 */
	private String					mBatteryRectBgColor		= "#A0736d5a";

	private static ReadStyleManager	mInstance;

	private ReadStyleManager(int displayWidth, int displayHeight)
	{
		mDisplayWidth = displayWidth;
		mDisplayHeight = displayHeight;

		mTextTypeFace = getNormalFont();
		changeReadMode(StorageUtil.getInt(StorageUtil.KEY_READ_MODE, READ_MODE_NORMAL));
		changeReadFontSize(StorageUtil.getFloat(StorageUtil.KEY_FONT_SIZE, DEF_FONT_SIZE_SP));
		mReadAnimType = StorageUtil.getInt(StorageUtil.KEY_ANIMA_TYPE, ANIMATION_TYPE_SILIDE);
	}

	public static void clear()
	{
		if (mInstance != null) {
			// Log.d("ouyang", "ReadStyleManager--clear---1---:"+mInstance);
			mInstance.remove();
			mInstance = null;
		} else {
			// Log.d("ouyang", "ReadStyleManager--clear---2---:"+mInstance);
		}
	}

	private void remove()
	{
		if (null != mReadBackground) {
			mReadBackground.recycle();
			mReadBackground = null;
		}
		mReadBgCavas = null;
	}

	/**
	 * 获取ReadStyleManager实例
	 * 
	 * @param displayWidth
	 *            显示宽度
	 * @param displayHeight
	 *            显示高度
	 * @return
	 */
	public static ReadStyleManager getInstance(int displayWidth, int displayHeight)
	{
		if (mInstance == null) {
			synchronized (ReadStyleManager.class) {
				if (mInstance == null) {
					mInstance = new ReadStyleManager(displayWidth, displayHeight);
				}
			}
			// Log.d("ouyang",
			// "ReadStyleManager--getInstance---1---:"+mInstance);
		}
		return mInstance;
	}

	/**
	 * 获取BookPageFactory实例 R
	 * 
	 * @param context
	 * @return
	 */
	public static ReadStyleManager getInstance(Context context)
	{
		if (mInstance == null) {
			int displayWidth = context.getResources().getDisplayMetrics().widthPixels;
			int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
			mInstance = new ReadStyleManager(displayWidth, displayHeight);
			// Log.d("ouyang",
			// "ReadStyleManager--getInstance---2---:"+mInstance);
		}
		return mInstance;
	}
	
	public static ReadStyleManager getInstanceWithoutCreate(){
		return mInstance;
	}

	/**
	 * 获取当前阅读字体单位（sp）
	 * 
	 * @return
	 */
	public float getCurReadFontSizeInSp()
	{
		return mCurrentFontSize;
	}

	/**
	 * 当前字体是否已经是最大号
	 * 
	 * @return
	 */
	public boolean isBiggestReadFontSize()
	{
		return mCurrentFontSize >= MAX_FONT_SIZE_SP;
	}

	/**
	 * 当前字体是否已经是最小号
	 * 
	 * @return
	 */
	public boolean isSmallestReadFontSize()
	{
		return mCurrentFontSize <= MIN_FONT_SIZE_SP;
	}

	/**
	 * 在字体大小范围内放大字体（每次增加2.0sp）
	 */
	public boolean increaseReadFontSize()
	{
		if (!isBiggestReadFontSize()) {
			float newFontSize = mCurrentFontSize + 2.0f;
			changeReadFontSize(newFontSize);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 在字体大小范围内缩小字体（每次减少2.0sp）
	 * 
	 * @return 是否缩小成功
	 */
	public boolean decreaseReadFontSize()
	{
		if (!isSmallestReadFontSize()) {
			float newFontSize = mCurrentFontSize - 2.0f;
			changeReadFontSize(newFontSize);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 改变阅读字体的大小
	 * 
	 * @param fontSize
	 */
	public void changeReadFontSize(float fontSize)
	{
		mCurrentFontSize = fontSize;
		updateDimension();
		resetHeaderFooterPaint();
		updateVisibleArea();

		// 保存字体大小
		StorageUtil.saveFloat(StorageUtil.KEY_FONT_SIZE, fontSize);
	}

	/**
	 * 是否是夜间模式
	 * 
	 * @return
	 */
	public boolean isNightReadMode()
	{
		return mReadMode == READ_MODE_NIGHT;
	}

	/**
	 * 获取阅读模式
	 * 
	 * @return
	 */
	public int getReadMode()
	{
		// Log.e("ouyang", "ReadStyleManager--getReadMode------:"+this);
		return mReadMode;
	}

	/**
	 * 通过阅读模式，改变样式
	 * 
	 * @param readMode
	 *            阅读模式
	 */
	public void changeReadMode(int readMode)
	{
		// Log.e("ouyang",
		// "ReadStyleManager--changeReadMode-----readMode:"+readMode +
		// ", instance:"+ this);
		switch (readMode) {
		case READ_MODE_NORMAL:
			mReadMode = READ_MODE_NORMAL;
			mReadTextColor = ResourceUtil.getColor(R.color.reading_content);
			mHeaderTextColor = ResourceUtil.getColor(R.color.reading_title);
			mFooterTextColor = ResourceUtil.getColor(R.color.reading_title);
			mSelectionTextColor = ResourceUtil.getColor(R.color.selector_text_bg_color);
			mUnderLineColor = ResourceUtil.getColor(R.color.selector_under_line_color);

			String resName = StorageUtil.getString(StorageUtil.KEY_READ_BG_RES_NAME);
			setReadBgResId(ResourceUtil.getIdByName(resName));
			//
			mBatteryRectBgColor = "#A0736d5a";
			break;

		case READ_MODE_NIGHT:
			mReadMode = READ_MODE_NIGHT;
			mReadTextColor = ResourceUtil.getColor(R.color.reading_content_night);
			mHeaderTextColor = ResourceUtil.getColor(R.color.reading_title_night);
			mFooterTextColor = ResourceUtil.getColor(R.color.reading_title_night);
			mSelectionTextColor = ResourceUtil.getColor(R.color.selector_text_bg_color_night);
			mUnderLineColor = ResourceUtil.getColor(R.color.selector_under_line_color_night);
			releaseReadbgAndCanvas();
			//
			mBatteryRectBgColor = "#A06b696b";
			break;
		default:
			return;
		}

		resetHeaderFooterPaint();
		StorageUtil.saveInt(StorageUtil.KEY_READ_MODE, readMode);
	}

	/**
	 * 得到颜色资源，会根据当前阅读模式做资源替换
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public int getColorFromIdentifier(Context context, int resId)
	{
		if (isNightReadMode()) {
			int color = -1;
			Resources res = context.getResources();
			String ngName = res.getResourceEntryName(resId) + NIGHT_SUFFIX;
			int ngResId = res.getIdentifier(ngName, "color", context.getPackageName());
			boolean getSuccess;
			try {
				color = context.getResources().getColor(ngResId);
				getSuccess = true;
			} catch (Exception e) {
				getSuccess = false;
			}
			if (getSuccess) {
				return color;
			}
		}
		return context.getResources().getColor(resId);
	}

	/**
	 * 得到ColorState资源，会根据当前阅读模式做资源替换
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public ColorStateList getColorStateListFromIdentifier(Context context, int resId)
	{
		if (isNightReadMode()) {
			ColorStateList colorState;
			Resources res = context.getResources();
			String ngName = res.getResourceEntryName(resId) + NIGHT_SUFFIX;
			int ngResId = res.getIdentifier(ngName, "drawable", context.getPackageName());
			try {
				colorState = context.getResources().getColorStateList(ngResId);
			} catch (Exception e) {
				colorState = null;
			}
			if (colorState != null) {
				return colorState;
			}
		}
		return context.getResources().getColorStateList(resId);
	}

	/**
	 * 得到图片资源，会根据当前阅读模式做资源替换
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public Drawable getDrawableFromIdentifier(Context context, int resId)
	{
		if (isNightReadMode()) {
			Drawable drawable;
			Resources res = context.getResources();
			String ngName = res.getResourceEntryName(resId) + NIGHT_SUFFIX;
			int ngResId = res.getIdentifier(ngName, "drawable", context.getPackageName());
			try {
				drawable = context.getResources().getDrawable(ngResId);
			} catch (Exception e) {
				drawable = null;
			}
			if (drawable != null) {
				return drawable;
			}
		}
		return context.getResources().getDrawable(resId);
	}

	/**
	 * 获取当前翻页动画的类型
	 * 
	 * @return
	 */
	public int getReadAnim()
	{
		return mReadAnimType;
	}

	/**
	 * 存储翻页动画类型
	 * 
	 * @param readAnimType
	 */
	public void changeReadAnim(int readAnimType)
	{
		mReadAnimType = readAnimType;
		StorageUtil.saveInt(StorageUtil.KEY_ANIMA_TYPE, readAnimType);
	}

	/**
	 * 得到阅读页能画的行数
	 * 
	 * @return
	 */
	public int getLineCount()
	{
		return mLineCount;
	}

	/**
	 * 得到一行字体会占用的高度
	 * 
	 * @return
	 */
	public float getLineHeight()
	{
		return getFontHeight() + mLineSpaceHeight;
	}

	/**
	 * 得到一页最多多少字节
	 * 
	 * @return
	 */
	public int getMaxBytesApage()
	{
		return mMaxBytesApage;
	}

	/**
	 * 获取《标题+阅读内容+底部》draw的宽度
	 * 
	 * @return
	 */
	public float getVisibleWidth()
	{
		return mVisibleWidth;
	}

	/**
	 * 获取整个《阅读内容》能画的高度
	 * 
	 * @return
	 */
	public float getVisibleHeight()
	{
		return mVisibleHeight;
	}

	/**
	 * 获取《标题+阅读内容+底部》的左边开始x座标
	 * 
	 * @return
	 */
	public float getLeftX()
	{
		return 1.2f * mMarginHorizontal;
	}

	/**
	 * 获取《标题+阅读内容+底部》的右边结束x座标
	 * 
	 * @return
	 */
	public float getRightX()
	{
		return mDisplayWidth - getLeftX();
	}

	/**
	 * 获取《阅读内容》开始的y座标
	 * 
	 * @return
	 */
	public float getContentStartY()
	{
		return mTopMargin + getTitleHeight() + mContentMarginTop + getFontHeight();
	}

	/**
	 * 获取《阅读内容》的行间距
	 * 
	 * @return
	 */
	public float getLineSpaceHeight()
	{
		return mLineSpaceHeight;
	}

	/**
	 * 获取Content字体高度
	 * 
	 * @return
	 */
	public float getFontHeight()
	{
		Paint.FontMetrics fm = getReadPaint().getFontMetrics();
		return (float) Math.ceil(fm.descent - fm.top);
	}

	/**
	 * 获取Content字体缩放高度
	 * 
	 * @param scale
	 * @param bold
	 * @return
	 */
	public float getFontScaleHeight(float scale, boolean bold)
	{
		Paint.FontMetrics fm = getScaledReadPaint(scale, bold).getFontMetrics();
		return (float) Math.ceil(fm.descent - fm.top);
	}

	/**
	 * 获取《标题》开始的y座标
	 * 
	 * @return
	 */
	public float getTitleStartY()
	{
		return mTopMargin + getTitleHeight();
	}

	/**
	 * 获取title字体高度
	 * 
	 * @return
	 */
	public float getTitleHeight()
	{
		Paint.FontMetrics fm = mHeaderPaint.getFontMetrics();
		return (float) Math.ceil(fm.descent - fm.top);
	}

	/**
	 * 获取《底部》开始的y座标
	 * 
	 * @return
	 */
	public float getFooterStartY()
	{
		return mDisplayHeight - mBottomMargin;
	}

	/**
	 * 获取Bottom字体高度
	 * 
	 * @return
	 */
	public float getBottomHeight()
	{
		Paint.FontMetrics fm = mFooterPaint.getFontMetrics();
		return (float) Math.ceil(fm.descent - fm.top);
	}

	/**
	 * 装配阅读样式的参数，产出footer画笔
	 * 
	 * @return
	 */
	public Paint getFooterPaint()
	{
		return mFooterPaint;
	}

	/**
	 * 装配阅读样式的参数，产出header画笔
	 * 
	 * @return
	 */
	public Paint getHeaderPaint()
	{
		return mHeaderPaint;
	}

	/**
	 * 获取选择文字的背景色
	 * 
	 * @return
	 */
	public int getSelectionTextColor()
	{
		return mSelectionTextColor;
	}

	/**
	 * 获取下划线的颜色
	 * 
	 * @return
	 */
	public int getUnderLineColor()
	{
		return mUnderLineColor;
	}

	/**
	 * 获取画文字垂直方向的便宜值
	 * 
	 * @return
	 */
	public int getTextOffsetVertical()
	{
		return PixelUtil.dp2px(6.67f);
	}

	public int getScrollTop()
	{
		return (int) (mTopMargin + getTitleHeight() + mContentMarginTop);
	}

	public int getScrollContent()
	{
		return getScrollBottom() - getScrollTop();
	}

	public int getScrollBottom()
	{
		return getScrollTop() + (int) (mLineCount * (getFontHeight() + mLineSpaceHeight));
	}

	/**
	 * 装配阅读样式的参数，产出阅读画笔
	 * 
	 * @return
	 */
	public Paint getReadPaint()
	{
		Paint readPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		readPaint.setTextAlign(Align.LEFT);
		readPaint.setStyle(Style.FILL);
		readPaint.setTextSize(PixelUtil.sp2px(mCurrentFontSize));
		readPaint.setColor(mReadTextColor);
		readPaint.setTypeface(mTextTypeFace);
		readPaint.setSubpixelText(true);
		readPaint.setAntiAlias(true);
		readPaint.setDither(true);
		return readPaint;
	}

	/**
	 * 通过缩放系数装配阅读样式的参数，产出阅读画笔
	 * 
	 * @param scale
	 *            缩放系数
	 * @param bold
	 *            是否粗体
	 * @return
	 */
	public Paint getScaledReadPaint(float scale, boolean bold)
	{
		Paint readPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		readPaint.setTextAlign(Align.LEFT);
		readPaint.setStyle(Style.FILL);
		readPaint.setTextSize(scale * PixelUtil.sp2px(mCurrentFontSize));
		readPaint.setColor(mReadTextColor);
		readPaint.setTypeface(mTextTypeFace);
		readPaint.setSubpixelText(true);
		readPaint.setAntiAlias(true);
		if (bold) {
			readPaint.setFakeBoldText(true);
		}
		readPaint.setDither(true);
		return readPaint;
	}

	/**
	 * 更新字体颜色
	 * 
	 * @param oldPaint
	 */
	public void updateReadPaint(Paint oldPaint)
	{
		if (oldPaint == null) {
			return;
		}
		int oldColor = oldPaint.getColor();
		if (oldColor != mReadTextColor) {
			oldPaint.setColor(mReadTextColor);
		}
	}

	/**
	 * 根据当前阅读字体的变化，改变其它的样式
	 */
	private void updateDimension()
	{
		mFooterFontSize = getPixel(ReadPageDimenUtil.TITILE_FONT_SIZE);
		mHeaderFontSize = getPixel(ReadPageDimenUtil.TITILE_FONT_SIZE);
		mLineSpaceHeight = getPixel(ReadPageDimenUtil.CONTENT_LINE_SPACE);

		mMarginHorizontal = getPixel(ReadPageDimenUtil.CONTENT_MARGIN_LEFT_RIGHT);
		mTopMargin = getPixel(ReadPageDimenUtil.TITILE_MARGIN_TOP);
		mBottomMargin = getPixel(ReadPageDimenUtil.BOTTOM_MARGIN_BOTTOM);
		mContentMarginTop = getPixel(ReadPageDimenUtil.CONTENT_MARGIN_TOP);
		mContentMarginBottom = getPixel(ReadPageDimenUtil.CONTENT_MARGIN_BOTTOM);
	}

	/**
	 * 更新缓存的头部和底部的画笔
	 */
	private void resetHeaderFooterPaint()
	{
		mHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mHeaderPaint.setTextAlign(Align.LEFT);
		mHeaderPaint.setTextSize(mHeaderFontSize);
		mHeaderPaint.setColor(mHeaderTextColor);
		mHeaderPaint.setTypeface(mTextTypeFace);
		mHeaderPaint.setAntiAlias(true);

		mFooterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFooterPaint.setTextAlign(Align.LEFT);
		mFooterPaint.setTextSize(mFooterFontSize);
		mFooterPaint.setColor(mFooterTextColor);
		mFooterPaint.setTypeface(mTextTypeFace);
		mFooterPaint.setAntiAlias(true);
	}

	/**
	 * <p>
	 * 更新显示区域信息如高宽、行数等
	 * </p>
	 * <p/>
	 * <li>注意：需要在{@link com.sina.book.reader.ReadStyleManager#updateDimension()}
	 * 以及 {@link com.sina.book.reader.ReadStyleManager#resetHeaderFooterPaint()}
	 * 之后调用
	 */
	private void updateVisibleArea()
	{
		mVisibleWidth = mDisplayWidth - mMarginHorizontal * 2;
		mVisibleHeight = mDisplayHeight
				- (mTopMargin + mBottomMargin + mContentMarginTop + mContentMarginBottom + getTitleHeight() + getBottomHeight());

		// 更新阅读页显示行数
		mLineCount = (int) Math.floor(mVisibleHeight * 1.00 / getLineHeight());

		float last = mVisibleHeight % getLineHeight();

		// 如果剩余空间比字体高度大，则可以再增加一行
		if (last >= getLastLineFontHeight()) {
			mLineCount += 1;
		}

		// 将下面剩余空隙均分到每行
		float remainHeight = mVisibleHeight - getLineHeight() * mLineCount + mLineSpaceHeight;
		if (remainHeight > 0) {
			mLineSpaceHeight = remainHeight / mLineCount + mLineSpaceHeight;
		}

		// 算出每页最多多少字节
		Paint readPaint = getReadPaint();
		if (readPaint != null) {
			int maxLineLength = readPaint.breakText("测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试", true, mVisibleWidth,
					mMeasuredWidths) + 5;
			mMaxBytesApage = maxLineLength * mLineCount * 2;
		}
	}

	/**
	 * 获取最后一行文字的最小高度，主要用于不同字体排榜时尽量减少底部的空白
	 * 
	 * @return
	 */
	public int getLastLineFontHeight()
	{
		return (int) (getFontHeight() * 0.75);
	}

	/**
	 * 验证传入的阅读字体大小是否合法<br>
	 */
	public static boolean verifyReadFontSize(float fontSize)
	{
		return fontSize > 0 && fontSize <= MAX_FONT_SIZE_SP && fontSize >= MIN_FONT_SIZE_SP;
	}

	/**
	 * 根据当前的字体大小得到对应的样式
	 * 
	 * @param area
	 * @return
	 */
	private int getPixel(String area)
	{
		return ReadPageDimenUtil.getPixel(mCurrentFontSize, area);
	}

	/**
	 * 获取正常字体
	 * 
	 * @return
	 */
	public static Typeface getNormalFont()
	{
		return Typeface.SERIF;
	}

	/**
	 * 获取加粗字体
	 * 
	 * @return
	 */
	public static Typeface getBoldFont()
	{
		return Typeface.DEFAULT_BOLD;
	}

	/**
	 * 获取阅读背景资源ID
	 * 
	 * @return
	 */
	public int getReadBgResId()
	{
		int resId = -1;
		String resName = StorageUtil.getString(StorageUtil.KEY_READ_BG_RES_NAME);

		if (!TextUtils.isEmpty(resName)) {
			resId = ResourceUtil.getIdByName(resName);
		}

		if (-1 == resId) {
			resId = R.color.reading_bg;
			StorageUtil.saveString(StorageUtil.KEY_READ_BG_RES_NAME, ResourceUtil.getAllNameById(R.drawable.readbg_04));
		}

		return resId;
	}

	/**
	 * 获取阅读背景图片
	 * 
	 * @return
	 */
	public Bitmap getReadBackground()
	{
		if (mReadBackground == null) {
			// FIXME:ouyang 测试发现背景图片有可能会出现销毁，重新加载下背景资源
			int readMode = StorageUtil.getInt(StorageUtil.KEY_READ_MODE, READ_MODE_NORMAL);
			if (readMode == READ_MODE_NORMAL) {
				// 白天
				String resName = StorageUtil.getString(StorageUtil.KEY_READ_BG_RES_NAME);
				setReadBgResId(ResourceUtil.getIdByName(resName));
			}
		}
		return mReadBackground;
	}

	/**
	 * 设置阅读背景资源ID
	 * 
	 * @param readBgResId
	 */
	public void setReadBgResId(Integer readBgResId)
	{
		String resName = ResourceUtil.getAllNameById(readBgResId);

		if (TextUtils.isEmpty(resName)) {
			StorageUtil.saveString(StorageUtil.KEY_READ_BG_RES_NAME, ResourceUtil.getAllNameById(R.drawable.readbg_04));
			readBgResId = getReadBgResId();
		} else {
			StorageUtil.saveString(StorageUtil.KEY_READ_BG_RES_NAME, resName);
		}

		if (ThemeUtil.isDrawable(readBgResId)) {
			ensureReadbgAndCanvas(mDisplayWidth, mDisplayHeight);
			Bitmap bitmap = BitmapFactory.decodeResource(SinaBookApplication.gContext.getResources(), readBgResId);

			switch (ThemeUtil.getDrawMethod(readBgResId)) {
			case ThemeUtil.DRAW_REPEAT:
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();
				for (int y = 0; y < mDisplayHeight; y += h) {
					for (int x = 0; x < mDisplayWidth; x += w) {
						mReadBgCavas.drawBitmap(bitmap, x, y, null);
					}
				}
				break;

			case ThemeUtil.DRAW_ADJUST:
				// TODO:
				// bitmap = ImageUtil.zoom(bitmap, mDisplayWidth,
				// mDisplayHeight);
				if (bitmap != null) {
					// mReadBgCavas.drawBitmap(bitmap, 0, 0, null);

					float sx = mDisplayWidth * 1.0f / bitmap.getWidth();
					float sy = mDisplayHeight * 1.0f / bitmap.getHeight();
					Matrix matrix = new Matrix();
					matrix.setScale(sx, sy);
					mReadBgCavas.drawBitmap(bitmap, matrix, null);
				}
				break;
			}

			if (null != bitmap) {
				bitmap.recycle();
			}
		} else {
			releaseReadbgAndCanvas();
		}

		int textColor = ThemeUtil.getTextColor(readBgResId);
		if (ThemeUtil.DEFAULT_VALUE != textColor) {
			mReadTextColor = textColor;
		}

		int titleTextColor = ThemeUtil.getTitleTextColor(readBgResId);
		if (ThemeUtil.DEFAULT_VALUE != titleTextColor) {
			mHeaderTextColor = titleTextColor;
			mFooterTextColor = titleTextColor;
			resetHeaderFooterPaint();
		}
	}

	/**
	 * 获取实际显示内容的矩形
	 * 
	 * @return
	 */
	public RectF getContentRectF()
	{
		float left = getLeftX();
		float right = getRightX();

		float fontHeight = getFontHeight();
		float top = getContentStartY() - fontHeight;
		float bottom = top + mLineCount * (fontHeight + mLineSpaceHeight) - mLineSpaceHeight;

		return new RectF(left, top, right, bottom);
	}

	/**
	 * 获取可显示内容区域矩形
	 * 
	 * @return
	 */
	public RectF getVisibleRect()
	{
		int x0 = (int) ((mDisplayWidth - mVisibleWidth) / 2);
		int y0 = (int) ((mDisplayHeight - mVisibleHeight) / 2);

		int x1 = (int) (x0 + mVisibleWidth);
		int y1 = (int) (y0 + mVisibleHeight);

		return new RectF(x0, y0, x1, y1);
	}

	/**
	 * 获取屏幕宽度
	 * 
	 * @return
	 */
	public float getScreenWidth()
	{
		return mDisplayWidth;
	}

	/**
	 * 获取屏幕高度
	 * 
	 * @return
	 */
	public float getScreenHeight()
	{
		return mDisplayHeight;
	}

	/**
	 * 保证在背景为图片时，缓存该背景图片
	 * 
	 * @param displayWidth
	 * @param displayHeight
	 */
	private void ensureReadbgAndCanvas(int displayWidth, int displayHeight)
	{
		if (mReadBackground != null) {
			return;
		}
		mReadBackground = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_8888);
		mReadBgCavas = new Canvas(mReadBackground);
	}

	/**
	 * 当背景为颜色时，释放图片，节约内存占用
	 */
	private void releaseReadbgAndCanvas()
	{
		if (mReadBackground != null) {
			mReadBackground.recycle();
		}
		mReadBackground = null;
		mReadBgCavas = null;
	}

	/**
	 * 获取选中文字的高度，这里根据字体大小做了一定的修正
	 * 
	 * @return
	 */
	public float getSelectionHeight()
	{
		int fontSize = (int) mCurrentFontSize;
		float height = getFontHeight();

		switch (fontSize) {
		case 12:
			height = height + height / 5;
			break;
		case 14:
			height = height + height / 5;
			break;
		case 16:
			height = height + height / 8;
			break;
		case 18:
			height = height + height / 10;
			break;
		case 20:
		case 22:
		case 24:
			// 不变
			break;
		case 26:
			height = height - height / 10;
			break;
		case 28:
			height = height - height / 8;
			break;
		case 30:
			height = height - height / 8;
			break;
		default:
			break;
		}

		return height;
	}

	public String getBatteryRectBgColor()
	{
		return mBatteryRectBgColor;
	}
}
