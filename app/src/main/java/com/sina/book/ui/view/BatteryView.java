package com.sina.book.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.util.PixelUtil;

/**
 * Class BatteryView.
 * 
 * @author MarkMjw
 * @date 14-1-2
 */
public class BatteryView {
	private Bitmap mBackground;
	private Bitmap mBackgroundNight;
	private Bitmap mBatteryImage;
	private Canvas mCanvas;

	private Paint mPaint;

	private final int MARGIN_LEFT = PixelUtil.dp2px(1.67f);
	private final int PADDING_VALUE = PixelUtil.dp2px(2.67f);

	private float mWidth;
	private float mHeight;

	private int mValue = 0;
	
	private boolean isFBReaderShow = false;

	public BatteryView(Context context) {
		this(context, -1, -1, false);
	}
	
	public BatteryView(Context context, boolean isFBReaderShow) {
		this(context, -1, -1, isFBReaderShow);
		
	}
	
	public BatteryView(Context context, int width, int height, boolean isFBReaderShow) {
		this.isFBReaderShow = isFBReaderShow;
		mBackground = BitmapFactory.decodeResource(context.getResources(), R.drawable.battery_frame);
		if(!isFBReaderShow){
			mBackgroundNight = BitmapFactory.decodeResource(context.getResources(), R.drawable.battery_frame_night);
		}

		mPaint = new Paint();
		// mPaint.setColor(Color.BLACK);
		// mPaint.setAlpha(150);

		updateSize(width, height);
		draw();
	}

	public void setValue(int value) {
		mValue = value;
		draw();
	}

	public Bitmap getBitmap() {
		return mBatteryImage;
	}

	public float getHeight() {
		return mHeight;
	}

	public void updateSize(float width, float height) {
		mHeight = height < 0 ? mBackground.getHeight() : height;

		double scale = mBackground.getWidth() * 1.0f / mBackground.getHeight();
		mWidth = width < 0 ? (float) Math.floor(mHeight * scale) : width;

		mBatteryImage = Bitmap.createBitmap((int) mWidth, (int) mHeight, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBatteryImage);
	}

	public void draw() {
		if(mBackground == null){
			return;
		}
		
		if (mValue < 0)
			throw new IllegalArgumentException("The level value must >= 0");
		
		int color = 0;
		if(isFBReaderShow){
			color = 0xA0736d5a;
		}else{
			color = Color.parseColor(ReadStyleManager.getInstance(SinaBookApplication.gContext)
					.getBatteryRectBgColor());
		}
		mPaint.setColor(color);
		
		int w = mBatteryImage.getWidth();
		int h = mBatteryImage.getHeight();

		// 1.清理画布
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		mCanvas.drawPaint(paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

		// 2.画背景
		NinePatch ninePatch = null;
		
		if(isFBReaderShow){
			ninePatch = new NinePatch(mBackground, mBackground.getNinePatchChunk(), null);
		}else{
			if (ReadStyleManager.READ_MODE_NORMAL == ReadStyleManager.getInstance(SinaBookApplication.gContext)
					.getReadMode()) {
				ninePatch = new NinePatch(mBackground, mBackground.getNinePatchChunk(), null);
			} else {
				ninePatch = new NinePatch(mBackgroundNight, mBackgroundNight.getNinePatchChunk(), null);
			}
		}
		
		RectF maskRect = new RectF(0, 0, mWidth, mHeight);
		ninePatch.setPaint(mPaint);
		ninePatch.draw(mCanvas, maskRect);

		// 3.画电量矩形
		int bitmapW = (int) Math.floor((w - PADDING_VALUE * 2 - MARGIN_LEFT) * mValue / 100);
		int bitmapH = h - PADDING_VALUE * 2;
		int leftX = w - bitmapW - PADDING_VALUE;
		int topY = PADDING_VALUE;

		RectF rectF = new RectF(leftX, topY, leftX + bitmapW, topY + bitmapH);
		mCanvas.drawRect(rectF, mPaint);
	}
	
	public void clear(){
		if(mBackground != null){
			mBackground.recycle();
			mBackground = null;
		}
		if(mBatteryImage != null){
			mBatteryImage.recycle();
			mBatteryImage = null;
		}
	}
}
