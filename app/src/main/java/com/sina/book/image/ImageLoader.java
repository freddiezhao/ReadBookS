package com.sina.book.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.widget.ImageView;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.data.Book;
import com.sina.book.util.FileUtils;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;

/**
 * 图片加载器
 * 
 * @author Tsimle
 * 
 */
public class ImageLoader {

	private static final String LOG_TAG = "ImageLoader";

	/**
	 * 对下载完成的图片做缩放处理
	 */
	public static final int TYPE_SMALL_PIC = 1003;
	/**
	 * 不对下载完成的图片做任何处理,返回原始下载的图片
	 */
	public static final int TYPE_BIG_PIC = 1004;
	/**
	 * 圆角
	 */
	public static final int TYPE_ROUND_PIC = 1005;

	/**
	 * 下面这些图片类型是应用相关的<br>
	 * 主要是书城的内存占用过大，<br>
	 * 希望对一些常驻的图片做更精确的压缩，以减小内存<br>
	 */
	public static final int TYPE_COMMON_BOOK_COVER = 2001;

	public static final int TYPE_COMMON_BIGGER_BOOK_COVER = 2003;

	public static final int TYPE_BOOK_HOME_SHELVES_COVER = 2002;

	private HashMap<Integer, ImageResizeBean> resizeBeans;

	/**
	 * 与解析图片最大长宽相等
	 */
	public static final int ORIGINAL_SCALE_MAX_WH = 700;

	/**
	 * 解析图片最大长宽
	 */
	private static int SCALE_MAX_WH = 700;
	/**
	 * 解析小图的长宽
	 */
	private static int SCALE_SMALL_WIDTH_HEIGHT = 80;

	private static ImageLoader instance;

	private static Bitmap defaultHorizonalBannerPic;// 横幅Banner
	private static Bitmap defaultGenaralActivityPic;// 通用活动默认图
	private static Bitmap defaultPic;
	private static Bitmap defaultMainBookPic;//
	private static Bitmap defaultAvatar;// 灰色头像
	private static Bitmap defaultMainAvatar;// 蓝色头像
	private static Bitmap noImgPic;// 暂无封面
	private static Bitmap defaultPartitionLike;// 选你喜欢

	private Context mContext;
	private String sdCardDirectory;
	private List<BitmapAsyncLoadTask> runningTasks = new ArrayList<BitmapAsyncLoadTask>();

	private ImageCache imageCache;
	private final AtomicBoolean paused = new AtomicBoolean(false);

	private ImageLoader() {
		if (Build.VERSION.SDK_INT >= 11) {
			imageCache = new ImageHardCache();
		} else {
			imageCache = new ImageSoftCache();
		}
		initResizeBeans();
	}

	/**
	 * 只在UI线程调用
	 * 
	 * @param context
	 * @return
	 */
	public static ImageLoader getInstance() {
		if (instance == null) {
			instance = new ImageLoader();
			instance.mContext = SinaBookApplication.gContext;
			instance.sdCardDirectory = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_IMAGE);
		}
		return instance;
	}

	/**
	 * 默认图片
	 * 
	 * @return
	 */
	public static Bitmap getDefaultHorizontalBannerPic() {
		if (defaultHorizonalBannerPic == null) {
			defaultHorizonalBannerPic = BitmapFactory.decodeResource(SinaBookApplication.gContext.getResources(),
					R.drawable.sina_horizontal_banner_default);
		}
		return defaultHorizonalBannerPic;
	}

	/**
	 * 默认图片
	 * 
	 * @return
	 */
	public static Bitmap getDefaultGiftPic() {

		// if (defaultHorizonalBannerPic == null) {
		// defaultHorizonalBannerPic =
		// BitmapFactory.decodeResource(SinaBookApplication.gContext.getResources(),
		// R.drawable.sina_bookshlef_gift_default);
		// }
		// return defaultHorizonalBannerPic;
		return null;
	}

	/**
	 * 默认图片
	 * 
	 * @return
	 */
	public static Bitmap getGenaralActivityDefaultPic() {
		if (defaultGenaralActivityPic == null) {
			defaultGenaralActivityPic = BitmapFactory.decodeResource(SinaBookApplication.gContext.getResources(),
					R.drawable.general_activity_bg);
		}
		return defaultGenaralActivityPic;
	}

	/**
	 * 默认图片
	 * 
	 * @return
	 */
	public static Bitmap getDefaultPic() {
		if (defaultPic == null) {
			defaultPic = BitmapFactory.decodeResource(SinaBookApplication.gContext.getResources(), R.drawable.sinabook);
		}
		return defaultPic;
	}

	/**
	 * 默认图片
	 * 
	 * @return
	 */
	public static Bitmap getDefaultLocalBookPic() {
		if (defaultMainBookPic == null) {
			defaultMainBookPic = BitmapFactory.decodeResource(SinaBookApplication.gContext.getResources(),
					R.drawable.sinabook_main_default);
		}
		return defaultMainBookPic;
	}

	/**
	 * 默认头像
	 * 
	 * @return
	 */
	public static Bitmap getDefaultAvatar() {
		if (defaultAvatar == null) {
			defaultAvatar = BitmapFactory.decodeResource(SinaBookApplication.gContext.getResources(),
					R.drawable.avatar_default);
		}
		return defaultAvatar;
	}

	/**
	 * 默认头像
	 * 
	 * @return
	 */
	public static Bitmap getDefaultMainAvatar() {
		if (defaultMainAvatar == null) {
			defaultMainAvatar = BitmapFactory.decodeResource(SinaBookApplication.gContext.getResources(),
					R.drawable.main_avatar_defaut);
		}
		return defaultMainAvatar;
	}

	/**
	 * 加载完未加载到使用
	 * 
	 * @return
	 */
	public static Bitmap getNoImgPic() {
		if (noImgPic == null) {
			noImgPic = BitmapFactory.decodeResource(SinaBookApplication.gContext.getResources(),
					R.drawable.sinabook_no_img);
		}
		return noImgPic;
	}

	/**
	 * 选你喜欢默认图
	 * 
	 * @return
	 */
	public static Bitmap getPartitionLikeDefault() {
		if (defaultPartitionLike == null) {
			defaultPartitionLike = BitmapFactory.decodeResource(SinaBookApplication.gContext.getResources(),
					R.drawable.like_cate_default);
		}
		return defaultPartitionLike;
	}

	public void pause() {
		paused.set(true);
	}

	public void resume() {
		synchronized (paused) {
			paused.set(false);
			paused.notifyAll();
		}
	}

	/**
	 * 释放队列中所有的图片加载任务
	 */
	public void release() {
		AbsImageAsyncTask.release();
		runningTasks.clear();
		// 释放加载的默认图片
		defaultPic = null;
		noImgPic = null;
	}

	/**
	 * 释放该Context的图片加载任务及它所有的图片缓存<br>
	 * Activity destroy时才能调用
	 */
	public void releaseContext(Context context) {
		String loadContext = getLoadContext(context);
		int size = runningTasks.size();
		if (size > 0) {
			BitmapAsyncLoadTask[] arrays = new BitmapAsyncLoadTask[size];
			runningTasks.toArray(arrays);
			for (BitmapAsyncLoadTask task : arrays) {
				if (task.getLoadContext().equals(loadContext)) {
					task.cancel(true);
					runningTasks.remove(task);
				}
			}
		}
		imageCache.clear(loadContext);
	}

	/**
	 * 取消掉该Context等待执行的图片加载任务<br>
	 * 可以在Activity pause时调用<br>
	 * 若调用了，在Activity resume时需要刷新列表，重新加载图片<br>
	 * 在将跳转的页面和本页面都有大量图片加载任务，防止前一页面的图片加载阻塞后面的<br>
	 */
	public void cancelContext(Context context) {
		String loadContext = getLoadContext(context);
		int size = runningTasks.size();
		if (size > 0) {
			BitmapAsyncLoadTask[] arrays = new BitmapAsyncLoadTask[size];
			runningTasks.toArray(arrays);
			for (BitmapAsyncLoadTask task : arrays) {
				if (task.getLoadContext().equals(loadContext)) {
					task.cancel(true);
					runningTasks.remove(task);
				}
			}
		}
	}

	public void cancelLoad(ImageView imageView) {
		BitmapAsyncLoadTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
		if (bitmapDownloaderTask != null) {
			bitmapDownloaderTask.cancel(true);
		}
	}

	// ***********************************************************************************
	// 1. load()方法未传入emptyOrErrorBitmap参数时emptyOrErrorBitmap使用getNoImgPic()。
	// 2. load2()方法emptyOrErrorBitmap与传入的defaultBitmap一致，
	// 3. load3()方法与load()的唯一区别是传入的Bitmap参数用来配置emptyOrErrorBitmap，
	// 而defaultBitmap默认使用getDefaultPic()
	// ***********************************************************************************

	public boolean load(String url, ImageView imageView, Bitmap defaultBitmap) {
		return load(url, imageView, TYPE_BIG_PIC, defaultBitmap, getNoImgPic(), null);
	}

	public boolean load2(String url, ImageView imageView, Bitmap defaultBitmap) {
		return load(url, imageView, TYPE_BIG_PIC, defaultBitmap, defaultBitmap, null);
	}

	public boolean load(String url, ImageView imageView, int type, Bitmap defaultBitmap) {
		return load(url, imageView, type, defaultBitmap, getNoImgPic(), null);
	}

	public boolean load2(String url, ImageView imageView, int type, Bitmap defaultBitmap) {
		return load(url, imageView, type, defaultBitmap, defaultBitmap, null);
	}

	public boolean load(String url, ImageView imageView, Bitmap defaultBitmap, Bitmap emptyOrErrorBitmap) {
		return load(url, imageView, TYPE_BIG_PIC, defaultBitmap, emptyOrErrorBitmap, null);
	}

	public boolean load(String url, ImageView imageView, int type, Bitmap defaultBitmap, Bitmap emptyOrErrorBitmap) {
		return load(url, imageView, type, defaultBitmap, emptyOrErrorBitmap, null);
	}

	public boolean load(String url, ImageView imageView, int type, Bitmap defaultBitmap, IImageLoadListener listener) {
		return load(url, imageView, type, defaultBitmap, getNoImgPic(), listener);
	}

	public boolean load2(String url, ImageView imageView, int type, Bitmap defaultBitmap, IImageLoadListener listener) {
		return load(url, imageView, type, defaultBitmap, defaultBitmap, listener);
	}

	// load3
	public boolean load3(String url, ImageView imageView, Bitmap emptyOrErrorBitmap) {
		return load(url, imageView, TYPE_BIG_PIC, getDefaultPic(), emptyOrErrorBitmap, null);
	}

	public boolean load3(String url, ImageView imageView, int type, Bitmap emptyOrErrorBitmap) {
		return load(url, imageView, type, getDefaultPic(), emptyOrErrorBitmap, null);
	}

	public boolean load3(String url, ImageView imageView, int type, Bitmap emptyOrErrorBitmap,
			IImageLoadListener listener) {
		return load(url, imageView, type, getDefaultPic(), emptyOrErrorBitmap, listener);
	}

	// 最终的处理方法
	public boolean load(String url, ImageView imageView, int type, Bitmap defaultBitmap, Bitmap emptyOrErrorBitmap,
			IImageLoadListener listener) {
		if (url == null || "".equals(url.trim())) {
			if (emptyOrErrorBitmap == null) {
				imageView.setImageDrawable(new NoRecycledDrawable(imageView.getResources(), getNoImgPic()));
			} else {
				imageView.setImageDrawable(new NoRecycledDrawable(imageView.getResources(), emptyOrErrorBitmap));
			}
			return false;
		}
		// 出现url中有空格的现象
		url = url.replaceAll(" ", "");

		// TODO:ouyang
		Bitmap bitmap = imageCache.get(url);
		// Bitmap bitmap = imageCache.get(getImageCacheKey(url,
		// getLoadContext(imageView)));
		if (bitmap == null) {
			asyncLoad(url, imageView, type, defaultBitmap, emptyOrErrorBitmap, listener);
			return false;
		} else {
			cancelPotentialAsyncLoad(url, imageView);
			imageView.setImageDrawable(new NoRecycledDrawable(imageView.getResources(), bitmap));
			if (listener != null) {
				listener.onImageLoaded(bitmap, imageView, true);
			}
			return true;
		}
	}

	/**
	 * 内存溢出发生后，缩小网络图片显示的清晰度
	 */
	public void oomHandled() {
		if (resizeBeans != null && !resizeBeans.isEmpty()) {
			// 如果已经缩小过，直接返回，不能无限制缩小
			if (ORIGINAL_SCALE_MAX_WH != SCALE_MAX_WH) {
				return;
			}
			ImageResizeBean commonCoverBean = resizeBeans.get(TYPE_COMMON_BOOK_COVER);
			if (commonCoverBean != null) {
				commonCoverBean.scaleWidth = commonCoverBean.scaleWidth * 2 / 3;
				commonCoverBean.scaleHeight = commonCoverBean.scaleHeight * 2 / 3;
			}

			ImageResizeBean bigImgBean = resizeBeans.get(TYPE_BIG_PIC);
			if (bigImgBean != null) {
				bigImgBean.scaleWidth = bigImgBean.scaleWidth * 2 / 3;
				bigImgBean.scaleHeight = bigImgBean.scaleHeight * 2 / 3;
			}

			SCALE_MAX_WH = SCALE_MAX_WH * 2 / 3;
		}
	}

	/**
	 * 直接从本地加载同步返回图片
	 * 
	 * @param url
	 * @return
	 */
	public Bitmap syncLoadBitmap(String url) {
		if (url == null) {
			return null;
		}
		Bitmap bm = null;
		if (url.startsWith(Book.LOCAL_PATH_IMG)) {
			bm = ImageUtil.getBitmapFromAssetsFile(mContext, url);
			return bm;
		} else if (url.startsWith(StorageUtil.EXTERNAL_STORAGE)) {
			bm = ImageUtil.getBitmapFromFile(url, SCALE_MAX_WH, SCALE_MAX_WH);
			return bm;
		} else {
			url = url.replaceAll(" ", "");
			String tempFileName = ImageUtil.getTempFileName(url);
			bm = ImageUtil.getBitmapFromFile(sdCardDirectory, tempFileName, SCALE_MAX_WH, SCALE_MAX_WH);
			return bm;
		}
	}

	private void asyncLoad(String url, ImageView imageView, int type, Bitmap defaultBitmap, Bitmap emptyOrErrorBitmap,
			IImageLoadListener listener) {
		if (cancelPotentialAsyncLoad(url, imageView)) {
			if (hasNoSameUrlTask(url, imageView, defaultBitmap, emptyOrErrorBitmap, listener)) {
				BitmapAsyncLoadTask task = new BitmapAsyncLoadTask(imageView, url);
				if (listener != null) {
					task.setListener(listener);
				}
				// 加载前设置默认图
				imageView.setImageDrawable(createAsyncDrawable(imageView.getResources(), task, defaultBitmap));
				// TODO
				// if (defaultBitmap == getNoImgPic()) {
				if (emptyOrErrorBitmap != null) {
					task.setNeedSetNoImgBitmap(false);
				}
				task.setLoadContext(getLoadContext(imageView));
				task.execute(new BitmapLoadTaskParams(type, emptyOrErrorBitmap));
				runningTasks.add(task);
				return;
			}
		}

		if (emptyOrErrorBitmap != null) {
			imageView.setImageDrawable(new NoRecycledDrawable(imageView.getResources(), emptyOrErrorBitmap));
		} else {
			imageView.setImageDrawable(new NoRecycledDrawable(imageView.getResources(), getNoImgPic()));
		}

	}

	/**
	 * Returns true if the current download has been canceled or if there was no
	 * download in progress on this image view. Returns false if the download in
	 * progress deals with the same url. The download is not stopped in that
	 * case.
	 */
	private boolean cancelPotentialAsyncLoad(String url, ImageView imageView) {
		BitmapAsyncLoadTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				if (bitmapDownloaderTask.isSuccessLoad()) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean hasNoSameUrlTask(String url, ImageView imageView, Bitmap defaultBitmap, Bitmap emptyOrErrorBitmap,
			final IImageLoadListener listener) {
		for (BitmapAsyncLoadTask task : runningTasks) {
			if (task.url.equals(url) && !task.isCancelled()) {

				ImageView prevImageView = null;
				IImageLoadListener prevListener = null;

				// 1 拿到原task的imageView及listener
				if (task.imageViewReference != null) {
					prevImageView = task.imageViewReference.get();
					if (prevImageView == imageView) {
						return false;
					} else {
						// 相同的URL但是不同的父容器，有可能是两个不同类型的视图
						// 比如精选推荐中的顶部TopBanner和选你喜欢Card
						// 因此可能预览图，异常默认图等都不一样
						if (prevImageView != null && imageView != null) {
							if (imageView.getParent() != prevImageView.getParent()) {
								return true;
							}
						}
					}
				}
				prevListener = task.listener;

				// 2 加入新的imageView及listener
				imageView.setImageDrawable(createAsyncDrawable(imageView.getResources(), task, defaultBitmap));
				task.imageViewReference = new WeakReference<ImageView>(imageView);
				task.setLoadContext(getLoadContext(imageView));
				task.setListener(listener);
				// TODO
				// if (defaultBitmap == getNoImgPic()) {
				if (emptyOrErrorBitmap != null) {
					task.setNeedSetNoImgBitmap(false);
				}

				// 3 重新存入旧的imageView及listener
				if (prevImageView != null) {
					task.setPrevImageView(prevImageView, prevListener);
				}

				// task.resetBitmapLoadTaskParamsAndImageViewReference(imageView,
				// new BitmapLoadTaskParams(
				// emptyOrErrorBitmap));
				return false;
			}
		}
		return true;
	}

	/**
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active download task (if any) associated
	 *         with this imageView. null if there is no such task.
	 */
	private static BitmapAsyncLoadTask getBitmapDownloaderTask(ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				AsyncDrawable downloadedDrawable = (AsyncDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			} else if (drawable instanceof AsyncNinePatchDrawable) {
				AsyncNinePatchDrawable downloadedDrawable = (AsyncNinePatchDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	private Bitmap downloadBitmap(String url, String tempFileName, int scaleWidth, int scaleHeight) {
		InputStream inputStream = null;
		HttpClient httpClient = null;
		FileOutputStream fos = null;
		try {
			httpClient = HttpUtil.getHttpClient(mContext);
			HttpResponse response = HttpUtil.doGetRequest(httpClient, url);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				// Log.w(LOG_TAG, "Error " + statusCode +
				// " while retrieving bitmap from " + url);
				return null;
			}
			HttpEntity entity = response.getEntity();
			inputStream = entity.getContent();
			if (inputStream != null) {
				if (StorageUtil.isSDCardExist()) {
					byte[] data = new byte[1024];
					int len = 0;
					File file = new File(sdCardDirectory, tempFileName);
					if (!file.exists()) {
						// TODO
						// file.createNewFile();
						file = FileUtils.checkAndCreateFile(file.getAbsolutePath());
						if (file != null && file.exists()) {
							fos = new FileOutputStream(file);
							while ((len = inputStream.read(data, 0, data.length)) != -1) {
								fos.write(data, 0, len);
							}
							return ImageUtil.getBitmapFromFile(sdCardDirectory, tempFileName, scaleWidth, scaleHeight);
						} else {
							return ImageUtil.getBitmapFromStream(inputStream, scaleWidth, scaleHeight);
						}
					}
				} else {
					return ImageUtil.getBitmapFromStream(inputStream, scaleWidth, scaleHeight);
				}
			}
		} catch (IOException e) {
			LogUtil.w(LOG_TAG, "I/O error " + url, e);
		} catch (Exception e) {
			LogUtil.w(LOG_TAG, "exception " + url, e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (fos != null) {
					fos.flush();
					fos.close();
					fos = null;
				}
				if (httpClient != null) {
					httpClient.getConnectionManager().shutdown();
				}
			} catch (IOException e) {
				LogUtil.w(LOG_TAG, "I/O error " + url, e);
			}
		}
		return null;
	}

	class BitmapLoadTaskParams {
		public int type;
		public Bitmap emptyOrErrorBitmap;

		public BitmapLoadTaskParams(int type, Bitmap emptyOrErrorBitmap) {
			this.type = type;
			this.emptyOrErrorBitmap = emptyOrErrorBitmap;
		}

		public BitmapLoadTaskParams(Bitmap emptyOrErrorBitmap) {
			this.emptyOrErrorBitmap = emptyOrErrorBitmap;
		}
	}

	private class BitmapAsyncLoadTask extends AbsImageAsyncTask<BitmapLoadTaskParams, Bitmap> {
		private String url;
		private IImageLoadListener listener;
		private WeakReference<ImageView> imageViewReference;
		private WeakReference<Bitmap> emptyOrErrorBitmapReference;
		private List<WeakReference<ImageView>> prevImageViewList;
		private Map<ImageView, IImageLoadListener> prevListeners;

		private boolean needSetNoImgBitmap = true;
		private boolean isSuccessLoad;
		private String loadContext;
		private int scaleWidth;
		private int scaleHeight;

		public void resetBitmapLoadTaskParamsAndImageViewReference(ImageView view,
				BitmapLoadTaskParams bitmapLoadTaskParams) {
			if (bitmapLoadTaskParams != null && view != null) {
				if (emptyOrErrorBitmapReference != null) {
					emptyOrErrorBitmapReference.clear();
					emptyOrErrorBitmapReference = new WeakReference<Bitmap>(bitmapLoadTaskParams.emptyOrErrorBitmap);
				}
				if (imageViewReference != null) {
					imageViewReference.clear();
					imageViewReference = new WeakReference<ImageView>(view);
				}
			}
		}

		public BitmapAsyncLoadTask(ImageView imageView, String url) {
			imageViewReference = new WeakReference<ImageView>(imageView);
			this.url = url;
			isSuccessLoad = false;
			if (imageView != null) {
				scaleWidth = imageView.getWidth();
				scaleHeight = imageView.getHeight();
			}
		}

		public void setPrevImageView(ImageView prevImageView, IImageLoadListener prevListener) {
			if (prevImageViewList == null) {
				prevImageViewList = new ArrayList<WeakReference<ImageView>>();
			}
			prevImageViewList.add(new WeakReference<ImageView>(prevImageView));

			if (prevListener != null) {
				if (prevListeners == null) {
					prevListeners = new HashMap<ImageView, IImageLoadListener>();
				}
				prevListeners.put(prevImageView, prevListener);
			}
		}

		public void setListener(IImageLoadListener listener) {
			this.listener = listener;
		}

		public boolean isSuccessLoad() {
			return isSuccessLoad;
		}

		public void setLoadContext(String loadContext) {
			this.loadContext = loadContext;
		}

		public String getLoadContext() {
			return loadContext;
		}

		public void setNeedSetNoImgBitmap(boolean needSetNoImgBitmap) {
			this.needSetNoImgBitmap = needSetNoImgBitmap;
		}

		@Override
		protected Bitmap doInBackground(BitmapLoadTaskParams... params) {
			if (waitIfPaused()) {
				return null;
			}

			int type = params[0].type;
			emptyOrErrorBitmapReference = new WeakReference<Bitmap>(params[0].emptyOrErrorBitmap);
			Bitmap bm = null;
			String tempFileName = ImageUtil.getTempFileName(url);
			if (scaleWidth == 0 || scaleHeight == 0) {
				ImageResizeBean bean = resizeBeans.get(type);
				if (bean != null) {
					scaleWidth = bean.scaleWidth;
					scaleHeight = bean.scaleHeight;
				} else {
					scaleWidth = SCALE_MAX_WH;
					scaleHeight = SCALE_MAX_WH;
				}
			}

			if (url.startsWith(Book.LOCAL_PATH_IMG)) {
				bm = ImageUtil.getBitmapFromAssetsFile(mContext, url);
			} else if (url.startsWith(StorageUtil.EXTERNAL_STORAGE)) {
				bm = ImageUtil.getBitmapFromFile(url, scaleWidth, scaleHeight);
			} else {
				bm = ImageUtil.getBitmapFromFile(sdCardDirectory, tempFileName, scaleWidth, scaleHeight);
				if (bm == null) {
					// TODO
					bm = downloadBitmap(url, tempFileName, scaleWidth, scaleHeight);
				}
				if (bm != null && type == TYPE_ROUND_PIC) {
					bm = ImageUtil.getRoundedCornerBitmap(bm, 4);
				}
			}

			if (bm != null) {
				// TODO:
				imageCache.put(url, bm);
				// imageCache.put(getImageCacheKey(url, loadContext), bm);
			}
			return bm;
		}

		@Override
		protected void onCancelled() {
			runningTasks.remove(this);
		}

		/**
		 * Once the image is downloaded, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			runningTasks.remove(this);

			if (isCancelled()) {
				bitmap = null;
			}

			if (bitmap != null) {
				if (imageViewReference != null) {
					ImageView imageView = imageViewReference.get();
					if (imageView != null) {
						BitmapAsyncLoadTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
						// Change bitmap only if this process is still
						// associated
						// with it
						// Or if we don't use any bitmap to task association
						if (this == bitmapDownloaderTask) {
							isSuccessLoad = true;
							imageView.setImageDrawable(new NoRecycledDrawable(imageView.getResources(), bitmap));
							if (listener != null) {
								listener.onImageLoaded(bitmap, imageView, true);
							}
						}
					}
				}

				if (prevImageViewList != null) {
					for (WeakReference<ImageView> prevImageViewRef : prevImageViewList) {
						ImageView prevImageView = prevImageViewRef.get();
						if (prevImageView != null) {
							BitmapAsyncLoadTask bitmapDownloaderTask = getBitmapDownloaderTask(prevImageView);
							if (this == bitmapDownloaderTask) {
								prevImageView.setImageDrawable(new NoRecycledDrawable(prevImageView.getResources(),
										bitmap));
								if (prevListeners != null) {
									IImageLoadListener prevListener = prevListeners.get(prevImageView);
									if (prevListener != null) {
										prevListener.onImageLoaded(bitmap, prevImageView, true);
									}
								}
							}
						}
					}
				}
			} else {
				// bitmap加载失败的情况下，还是去通知
				if (imageViewReference != null) {
					ImageView imageView = imageViewReference.get();
					if (imageView != null) {
						BitmapAsyncLoadTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
						if (this == bitmapDownloaderTask) {
							if (listener != null) {
								// TODO
								// if (emptyOrErrorBitmapReference.get() ==
								// null) {
								// bitmap = ((BitmapDrawable)
								// createAsyncDrawable(imageView.getResources(),
								// this,
								// getNoImgPic())).getBitmap();
								// } else {
								// bitmap = ((BitmapDrawable)
								// createAsyncDrawable(imageView.getResources(),
								// this,
								// emptyOrErrorBitmapReference.get())).getBitmap();
								// }
								listener.onImageLoaded(bitmap, imageView, false);
							} else {
								if (needSetNoImgBitmap) {
									// TODO
									imageView.setImageDrawable(createAsyncDrawable(imageView.getResources(), this,
											getNoImgPic()));
								} else {
									imageView.setImageDrawable(createAsyncDrawable(imageView.getResources(), this,
											emptyOrErrorBitmapReference.get()));
								}
							}
						}
					}
				}

				if (prevImageViewList != null) {
					for (WeakReference<ImageView> prevImageViewRef : prevImageViewList) {
						ImageView prevImageView = prevImageViewRef.get();
						if (prevImageView != null) {
							BitmapAsyncLoadTask bitmapDownloaderTask = getBitmapDownloaderTask(prevImageView);
							if (this == bitmapDownloaderTask) {
								boolean listenerHandle = false;
								if (prevListeners != null) {
									IImageLoadListener prevListener = prevListeners.get(prevImageView);
									if (prevListener != null) {
										// TODO
										// if (emptyOrErrorBitmapReference.get()
										// == null) {
										// bitmap = ((BitmapDrawable)
										// createAsyncDrawable(
										// prevImageView.getResources(), this,
										// getNoImgPic())).getBitmap();
										// } else {
										// bitmap = ((BitmapDrawable)
										// createAsyncDrawable(
										// prevImageView.getResources(), this,
										// emptyOrErrorBitmapReference.get())).getBitmap();
										// }
										prevListener.onImageLoaded(bitmap, prevImageView, false);
										listenerHandle = true;
									}
								}
								if (!listenerHandle && needSetNoImgBitmap) {
									// TODO
									prevImageView.setImageDrawable(createAsyncDrawable(prevImageView.getResources(),
											this, getNoImgPic()));
								} else {
									prevImageView.setImageDrawable(createAsyncDrawable(prevImageView.getResources(),
											this, emptyOrErrorBitmapReference.get()));
								}
							}
						}
					}
				}
			}

			// 为了放心，清理引用
			imageViewReference = null;
			listener = null;
			prevImageViewList = null;
			prevListeners = null;
		}
	}

	/**
	 * 通过imageview拿到它context的名字
	 * 
	 * @param imageView
	 * @return
	 */
	private String getLoadContext(ImageView imageView) {
		return imageView.getContext().getClass().getSimpleName()
				+ Integer.toHexString(imageView.getContext().hashCode());
	}

	private String getLoadContext(Context context) {
		return context.getClass().getSimpleName() + Integer.toHexString(context.hashCode());
	}

	/**
	 * 得到image cache的key
	 * 
	 * @param url
	 * @param loadContext
	 * @return
	 */
	private String getImageCacheKey(String url, String loadContext) {
		StringBuilder sb = new StringBuilder(loadContext);
		sb.append(url);
		return sb.toString();
	}

	private boolean waitIfPaused() {
		synchronized (paused) {
			if (paused.get()) {
				try {
					paused.wait();
				} catch (InterruptedException e) {
					return true;
				}
			}
		}
		return false;
	}

	private void initResizeBeans() {
		Resources res = SinaBookApplication.gContext.getResources();

		resizeBeans = new HashMap<Integer, ImageResizeBean>();
		ImageResizeBean commonCoverBean = new ImageResizeBean();
		commonCoverBean.scaleWidth = res.getDimensionPixelSize(R.dimen.book_common_item_img_width) * 4 / 5;
		commonCoverBean.scaleHeight = res.getDimensionPixelSize(R.dimen.book_common_item_img_height) * 4 / 5;
		resizeBeans.put(TYPE_COMMON_BOOK_COVER, commonCoverBean);

		ImageResizeBean commonBigCoverBean = new ImageResizeBean();
		commonBigCoverBean.scaleWidth = res.getDimensionPixelSize(R.dimen.book_big_common_item_img_width);
		commonBigCoverBean.scaleHeight = res.getDimensionPixelSize(R.dimen.book_big_common_item_img_height);
		resizeBeans.put(TYPE_COMMON_BIGGER_BOOK_COVER, commonBigCoverBean);
		resizeBeans.put(TYPE_BOOK_HOME_SHELVES_COVER, commonBigCoverBean);

		ImageResizeBean smallImgBean = new ImageResizeBean();
		smallImgBean.scaleWidth = SCALE_SMALL_WIDTH_HEIGHT;
		smallImgBean.scaleHeight = SCALE_SMALL_WIDTH_HEIGHT;
		resizeBeans.put(TYPE_SMALL_PIC, smallImgBean);

		ImageResizeBean bigImgBean = new ImageResizeBean();
		bigImgBean.scaleWidth = SCALE_MAX_WH;
		bigImgBean.scaleHeight = SCALE_MAX_WH;
		resizeBeans.put(TYPE_BIG_PIC, bigImgBean);

	}

	public static boolean isBitmapSupportNinePatch(Bitmap bitmap) {
		boolean isSupport = false;
		if (bitmap != null) {
			byte[] np = bitmap.getNinePatchChunk();
			isSupport = np != null && NinePatch.isNinePatchChunk(np);
		}
		return isSupport;
	}

	public static Drawable createAsyncDrawable(Resources res, BitmapAsyncLoadTask bitmapDownloaderTask,
			Bitmap defaultBitmap) {
		// boolean isNinePatch = false;
		// if (defaultBitmap != null) {
		// byte[] np = defaultBitmap.getNinePatchChunk();
		// isNinePatch = np != null && NinePatch.isNinePatchChunk(np);
		// }

		if (isBitmapSupportNinePatch(defaultBitmap)) {
			return new AsyncNinePatchDrawable(res, bitmapDownloaderTask, defaultBitmap);
		} else {
			return new AsyncDrawable(res, bitmapDownloaderTask, defaultBitmap);
		}
	}

	/**
	 * A fake Drawable that will be attached to the imageView while the download
	 * is in progress.
	 * 
	 * <p>
	 * Contains a reference to the actual download task, so that a download task
	 * can be stopped if a new binding is required, and makes sure that only the
	 * last started download process can bind its result, independently of the
	 * download finish order.
	 * </p>
	 */
	private static class AsyncNinePatchDrawable extends NinePatchDrawable {
		private WeakReference<BitmapAsyncLoadTask> bitmapDownloaderTaskReference;
		protected Bitmap bitmap;

		public AsyncNinePatchDrawable(Resources res, BitmapAsyncLoadTask bitmapDownloaderTask, Bitmap defaultBitmap) {
			super(res, defaultBitmap, defaultBitmap.getNinePatchChunk(), new Rect(), null);
			bitmapDownloaderTaskReference = new WeakReference<BitmapAsyncLoadTask>(bitmapDownloaderTask);
			this.bitmap = defaultBitmap;
		}

		public BitmapAsyncLoadTask getBitmapDownloaderTask() {
			if (bitmapDownloaderTaskReference != null) {
				return bitmapDownloaderTaskReference.get();
			} else {
				return null;
			}
		}

		@Override
		public void draw(Canvas canvas) {
			if (bitmap.isRecycled()) {
				return;
			}
			super.draw(canvas);
		}
	}

	private static class AsyncDrawable extends BitmapDrawable {
		private WeakReference<BitmapAsyncLoadTask> bitmapDownloaderTaskReference;

		public AsyncDrawable(Resources res, BitmapAsyncLoadTask bitmapDownloaderTask, Bitmap defaultBitmap) {
			super(res, defaultBitmap);
			bitmapDownloaderTaskReference = new WeakReference<BitmapAsyncLoadTask>(bitmapDownloaderTask);
		}

		public BitmapAsyncLoadTask getBitmapDownloaderTask() {
			if (bitmapDownloaderTaskReference != null) {
				return bitmapDownloaderTaskReference.get();
			} else {
				return null;
			}
		}

		@Override
		public void draw(Canvas canvas) {
			if (getBitmap() == null || getBitmap().isRecycled()) {
				return;
			}
			super.draw(canvas);
		}
	}

	/**
	 * 防止抛出recycled异常
	 * 
	 * @author Tsmile
	 * 
	 */
	private static class NoRecycledDrawable extends BitmapDrawable {

		public NoRecycledDrawable(Resources res, Bitmap bitmap) {
			super(res, bitmap);
		}

		@Override
		public void draw(Canvas canvas) {
			if (getBitmap() == null || getBitmap().isRecycled()) {
				return;
			}
			super.draw(canvas);
		}
	}

	public class ImageResizeBean {
		int scaleWidth;
		int scaleHeight;

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("{w:").append(scaleWidth);
			buffer.append(",h:").append(scaleHeight);
			buffer.append('}');
			return buffer.toString();
		}
	}
}
