
package com.sina.book.ui.widget;

import com.sina.book.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.CheckBox;

/**
 * 自定义开关控件，仿Iphone<br>
 *
 * <ul>Copy from <a href=https://github.com/IssacWang/SwitchButton>SwitchButton</a> </ul>
 *
 * @author MarkMjw
 * @date 2013-4-18
 */
public class SwitchButton extends CheckBox {
//    private static final String TAG = "SwitchButton";
    
    private final int MAX_ALPHA = 255;
    
    private final float VELOCITY = 350;
    
    private final float EXTENDED_OFFSET_Y = 15;
    
    private Paint mPaint;

    private ViewParent mParent;

    /** 背景图片. */
    private Bitmap mBottom;

    /** 当前状态的按钮图片. */
    private Bitmap mCurBtnPic;

    /** 按下状态的图片. */
    private Bitmap mBtnPressed;

    /** 正常状态的图片. */
    private Bitmap mBtnNormal;

    /** 控件框架图片. */
//    private Bitmap mFrame;

    /** 控件蒙板图片. */
    private Bitmap mMask;

    private RectF mSaveLayerRectF;

    private PorterDuffXfermode mXfermode;

    /** 首次按下的Y. */
    private float mFirstDownY;

    /** 首次按下的X. */
    private float mFirstDownX;

    /** 图片的绘制位置. */
    private float mRealPos;

    /** 按钮的位置. */
    private float mBtnPos;

    /** 开关打开的位置. */
    private float mBtnOnPos;

    /** 开关关闭的位置. */
    private float mBtnOffPos;

    private float mMaskWidth;

    private float mMaskHeight;

    private float mBtnWidth;

    private float mBtnInitPos;

    private int mClickTimeout;

    private int mTouchSlop;

    private int mAlpha = MAX_ALPHA;

    private boolean mChecked = false;

    private boolean mBroadcasting;

    private boolean mTurningOn;

    private PerformClick mPerformClick;

    private OnCheckedChangeListener mOnCheckedChangeListener;

    private OnCheckedChangeListener mOnCheckedChangeWidgetListener;

    private boolean mAnimating;

    private float mVelocity;

    /** Y轴方向扩大的区域,增大点击区域. */
    private float mExtendOffsetY;

    private float mAnimationPosition;

    private float mAnimatedVelocity;
    
    private float mWidth;
    private float mHeight;

    public SwitchButton(Context context) {
        this(context, null);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkboxStyle);
    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SwitchButton);
        mWidth = a.getDimension(R.styleable.SwitchButton_swidth, 0.0f);
        mHeight = a.getDimension(R.styleable.SwitchButton_sheight, 0.0f);
        a.recycle();
        
        initView(context);
    }

    private void initView(Context context) {
        setFocusable(true);
        
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true); 
        
        Resources resources = context.getResources();

        // get viewConfiguration
        mClickTimeout = ViewConfiguration.getPressedStateDuration()
                + ViewConfiguration.getTapTimeout();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        // get Bitmap
        mBottom = BitmapFactory.decodeResource(resources, R.drawable.switch_btn_bg);
        mBtnPressed = BitmapFactory.decodeResource(resources, R.drawable.switch_btn_pressed);
        mBtnNormal = BitmapFactory.decodeResource(resources, R.drawable.switch_btn_unpressed);
//        mFrame = BitmapFactory.decodeResource(resources, R.drawable.switch_btn_mask);
        mMask = BitmapFactory.decodeResource(resources, R.drawable.switch_btn_mask);
        
        // 按照指定大小缩放图片
        zoomByNinePatch();
        
        mCurBtnPic = mBtnNormal;

        mBtnWidth = mBtnPressed.getWidth();
        mMaskWidth = mMask.getWidth();
        mMaskHeight = mMask.getHeight();

        mBtnOnPos = mBtnWidth / 2;
        mBtnOffPos = mMaskWidth - mBtnWidth / 2;

        mBtnPos = mChecked ? mBtnOnPos : mBtnOffPos;
        mRealPos = getRealPos(mBtnPos);

        final float density = getResources().getDisplayMetrics().density;
        mVelocity = (int) (VELOCITY * density + 0.5f);
        mExtendOffsetY = (int) (EXTENDED_OFFSET_Y * density + 0.5f);

        mSaveLayerRectF = new RectF(0, mExtendOffsetY, mMaskWidth, mMaskHeight + mExtendOffsetY);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    /**
     * 通过NinePatch缩放图片
     */
    private void zoomByNinePatch() {
        if (mWidth <= 0 || mHeight <= 0) {
            // 如果高宽任一值<=0则默认使用蒙板图片的大小
            return;
        }
        
        /*
         * 创建一个ninePatch的对象实例，
         * 第一个参数是bitmap
         * 
         * 第二个参数是byte[]，这里其实要求我们传入,如何处理拉伸方式，当然我们不需要自己传入，
         * 因为“.9.png”图片自身有这些信息数据，也就是我们用“9patch”工具操作的信息！
         * 我们直接用“.9.png”图片自身的数据调用getNinePatchChunk()即可
         * 
         * 第三个参数是图片源的名称，这个参数为可选参数，直接null~就OK~
         */
        NinePatch maskNinePatch = new NinePatch(mMask, mMask.getNinePatchChunk(), null);
        NinePatch bgNinePatch = new NinePatch(mBottom, mBottom.getNinePatchChunk(), null);
        NinePatch norNinePatch = new NinePatch(mBtnNormal, mBtnNormal.getNinePatchChunk(), null);
        NinePatch preNinePatch = new NinePatch(mBtnPressed, mBtnPressed.getNinePatchChunk(), null);

        float scaleW = mWidth / mMask.getWidth();
        float scaleH = mHeight / mMask.getHeight();

        Bitmap mask = Bitmap.createBitmap((int) mWidth, (int) mHeight, Config.ARGB_8888);
        Canvas maskCanvas = new Canvas(mask);
        RectF maskRect = new RectF(0, 0, mWidth, mHeight);
        maskNinePatch.draw(maskCanvas, maskRect);
        mMask = mask;

        int bgW = (int) (mBottom.getWidth() * scaleW);
        int bgH = (int) (mBottom.getHeight() * scaleH);
        Bitmap bg = Bitmap.createBitmap(bgW, bgH, Config.ARGB_8888);
        Canvas bgCanvas = new Canvas(bg);
        RectF bgRect = new RectF(0, 0, bgW, bgH);
        bgNinePatch.draw(bgCanvas, bgRect);
        mBottom = bg;

        int norW = (int) (mBtnNormal.getWidth() * scaleW);
        int norH = (int) (mBtnNormal.getHeight() * scaleH);
        Bitmap normal = Bitmap.createBitmap(norW, norH, Config.ARGB_8888);
        Canvas norCanvas = new Canvas(normal);
        RectF norRect = new RectF(0, 0, norW, norH);
        norNinePatch.draw(norCanvas, norRect);
        mBtnNormal = normal;

        int preW = (int) (mBtnPressed.getWidth() * scaleW);
        int preH = (int) (mBtnPressed.getHeight() * scaleH);
        Bitmap press = Bitmap.createBitmap(preW, preH, Config.ARGB_8888);
        Canvas preCanvas = new Canvas(press);
        RectF preRect = new RectF(0, 0, preW, preH);
        preNinePatch.draw(preCanvas, preRect);
        mBtnPressed = press;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mAlpha = enabled ? MAX_ALPHA : MAX_ALPHA / 2;
        super.setEnabled(enabled);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    /**
     * <p>
     * Changes the checked state of this button.
     * </p>
     * 
     * @param checked true to check the button, false to uncheck it
     */
    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;

            mBtnPos = checked ? mBtnOnPos : mBtnOffPos;
            mRealPos = getRealPos(mBtnPos);
            invalidate();

            // Avoid infinite recursions if setChecked() is called from a
            // listener
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(SwitchButton.this, mChecked);
            }
            if (mOnCheckedChangeWidgetListener != null) {
                mOnCheckedChangeWidgetListener.onCheckedChanged(SwitchButton.this, mChecked);
            }

            mBroadcasting = false;
        }
    }

    /**
     * <p>
     * Changes the checked state of this button, but this method not call onCheckedChanged.
     * </p>
     *
     * @param checked true to check the button, false to uncheck it
     */
    public void setCheckedWithOutListener(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;

            mBtnPos = checked ? mBtnOnPos : mBtnOffPos;
            mRealPos = getRealPos(mBtnPos);
            invalidate();
        }
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes.
     * 
     * @param listener the callback to call on checked state change
     */
    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes. This callback is used for internal purpose only.
     * 
     * @param listener the callback to call on checked state change
     * @hide
     */
    void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeWidgetListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        float deltaX = Math.abs(x - mFirstDownX);
        float deltaY = Math.abs(y - mFirstDownY);
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                attemptClaimDrag();
                mFirstDownX = x;
                mFirstDownY = y;
                mCurBtnPic = mBtnPressed;
                mBtnInitPos = mChecked ? mBtnOnPos : mBtnOffPos;
                break;
            case MotionEvent.ACTION_MOVE:
                float time = event.getEventTime() - event.getDownTime();
                mBtnPos = mBtnInitPos + event.getX() - mFirstDownX;
                if (mBtnPos <= mBtnOffPos) {
                    mBtnPos = mBtnOffPos;
                }
                if (mBtnPos >= mBtnOnPos) {
                    mBtnPos = mBtnOnPos;
                }
                mTurningOn = mBtnPos > (mBtnOnPos - mBtnOffPos) / 2 + mBtnOffPos;

                mRealPos = getRealPos(mBtnPos);
                break;
            case MotionEvent.ACTION_UP:
                mCurBtnPic = mBtnNormal;
                time = event.getEventTime() - event.getDownTime();
                if (deltaY < mTouchSlop && deltaX < mTouchSlop && time < mClickTimeout) {
                    if (mPerformClick == null) {
                        mPerformClick = new PerformClick();
                    }
                    if (!post(mPerformClick)) {
                        performClick();
                    }
                } else {
                    startAnimation(!mTurningOn);
                }
                break;
        }

        invalidate();
        return isEnabled();
    }
    
    @Override
    public boolean performClick() {
        startAnimation(mChecked);
        return true;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.saveLayerAlpha(mSaveLayerRectF, mAlpha, Canvas.MATRIX_SAVE_FLAG
                | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        
        // 绘制蒙板
        canvas.drawBitmap(mMask, 0, mExtendOffsetY, mPaint);
        mPaint.setXfermode(mXfermode);

        // 绘制底部图片
        canvas.drawBitmap(mBottom, mRealPos, mExtendOffsetY, mPaint);
        mPaint.setXfermode(null);

        // 绘制边框
        // canvas.drawBitmap(mFrame, 0, mExtendOffsetY, mPaint);

        // 绘制按钮
        canvas.drawBitmap(mCurBtnPic, mRealPos, mExtendOffsetY, mPaint);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) mMaskWidth, (int) (mMaskHeight + 2 * mExtendOffsetY));
    }

    /**
     * 内部调用此方法设置checked状态，此方法会延迟执行各种回调函数，保证动画的流畅度
     * 
     * @param checked
     */
    private void setCheckedDelayed(final boolean checked) {
        this.postDelayed(new Runnable() {

            @Override
            public void run() {
                setChecked(checked);
            }
        }, 10);
    }
    
    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        mParent = getParent();
        if (mParent != null) {
            mParent.requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * 将btnPos转换成RealPos
     * 
     * @param btnPos
     * @return
     */
    private float getRealPos(float btnPos) {
        return btnPos - mBtnWidth / 2;
    }

    private void startAnimation(boolean turnOn) {
        mAnimating = true;
        mAnimatedVelocity = turnOn ? -mVelocity : mVelocity;
        mAnimationPosition = mBtnPos;

        new SwitchAnimation().run();
    }

    private void stopAnimation() {
        mAnimating = false;
    }
    
    private void doAnimation() {
        mAnimationPosition += mAnimatedVelocity * SwitchButtonAnimationController.ANIMATION_FRAME_DURATION
                / 1000;
        if (mAnimationPosition >= mBtnOnPos) {
            stopAnimation();
            mAnimationPosition = mBtnOnPos;
            setCheckedDelayed(true);
        } else if (mAnimationPosition <= mBtnOffPos) {
            stopAnimation();
            mAnimationPosition = mBtnOffPos;
            setCheckedDelayed(false);
        }
        moveView(mAnimationPosition);
    }

    private void moveView(float position) {
        mBtnPos = position;
        mRealPos = getRealPos(mBtnPos);
        invalidate();
    }
    
    private final class PerformClick implements Runnable {
        public void run() {
            performClick();
        }
    }

    private final class SwitchAnimation implements Runnable {

        @Override
        public void run() {
            if (!mAnimating) {
                return;
            }
            doAnimation();
            SwitchButtonAnimationController.requestAnimationFrame(this);
        }
    }
}
