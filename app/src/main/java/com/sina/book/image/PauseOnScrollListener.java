package com.sina.book.image;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * 
 * 优化图片加载引入ScrollListener<br>
 * 在滑动过程中，让ImageLoader处于等待状态<br>
 * 
 * @author Tsimle
 * 
 */
public class PauseOnScrollListener implements OnScrollListener {

    private ImageLoader imageLoader;

    private final boolean pauseOnScroll;
    private final boolean pauseOnFling;
    private final OnScrollListener externalListener;

    /**
     * Constructor
     * 
     * @param imageLoader
     *            {@linkplain ImageLoader} instance for controlling
     * @param pauseOnScroll
     *            Whether {@linkplain ImageLoader#pause() pause ImageLoader}
     *            during touch scrolling
     * @param pauseOnFling
     *            Whether {@linkplain ImageLoader#pause() pause ImageLoader}
     *            during fling
     */
    public PauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
        this(imageLoader, pauseOnScroll, pauseOnFling, null);
    }

    /**
     * Constructor
     * 
     * @param imageLoader
     *            {@linkplain ImageLoader} instance for controlling
     * @param pauseOnScroll
     *            Whether {@linkplain ImageLoader#pause() pause ImageLoader}
     *            during touch scrolling
     * @param pauseOnFling
     *            Whether {@linkplain ImageLoader#pause() pause ImageLoader}
     *            during fling
     * @param customListener
     *            Your custom {@link OnScrollListener} for
     *            {@linkplain AbsListView list view} which also will be get
     *            scroll events
     */
    public PauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling,
            OnScrollListener customListener) {
        this.imageLoader = imageLoader;
        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnFling = pauseOnFling;
        externalListener = customListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
        case OnScrollListener.SCROLL_STATE_IDLE:
            imageLoader.resume();
            break;
        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
            if (pauseOnScroll) {
                imageLoader.pause();
            }
            break;
        case OnScrollListener.SCROLL_STATE_FLING:
            if (pauseOnFling) {
                imageLoader.pause();
            }
            break;
        }
        if (externalListener != null) {
            externalListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (externalListener != null) {
            externalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
}
