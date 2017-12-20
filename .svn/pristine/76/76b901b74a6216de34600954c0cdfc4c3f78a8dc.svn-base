package com.sina.book.control;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.htmlcleaner.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.sina.book.SinaBookApplication;
import com.sina.book.data.ConstantData;
import com.sina.book.db.DBService;
import com.sina.book.parser.IParser;
import com.sina.book.ui.BaseActivity;
import com.sina.book.ui.BaseFragmentActivity;
import com.sina.book.useraction.DeviceInfo;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;

public class RequestTask extends GenericTask
{

	private static final String			TAG						= "RequestTask";

	/**
	 * get请求
	 */
	public static final String			HTTP_GET				= "GET";

	/**
	 * post请求
	 */
	public static final String			HTTP_POST				= "POST";

	public static final String			PARAM_URL				= "url";
	public static final String			PARAM_HTTP_METHOD		= "httpmethod";

	protected static final int			MESSAGE_CACHE_RESULT	= 1;

	protected static final Handler		mUiHandler				= new Handler(Looper.getMainLooper());

	/**
	 * 解析器
	 */
	private IParser						mParser;
	private List<NameValuePair>			mPostParams;
	private TaskParams					mParams;
	private String						mUrl;
	private Bitmap						mImage;
	private String						mImageName;

	/**
	 * 相关对象的引用
	 */
	private Object						mExtra;

	/**
	 * 请求使用的httpClient，在需要强制中断该task时使用
	 */
	private HttpClient					mHttpClient;

	/**
	 * 缓存相关参数
	 */
	private ITaskCacheLoadedListener	mCacheListener;
	private boolean						mEnableCache			= false;
	private long						mCacheTime;
	/** 是否找到缓存后就不再网络请求了 */
	private boolean						mOnlyUseCache;

	/**
	 * 持有使用的Activity的弱引用
	 */
	private WeakReference<Activity>		mActivityReference;

	public RequestTask(final int threadPriority, IParser parser)
	{
		super(threadPriority);
		this.mParser = parser;
	}

	public RequestTask(IParser parser)
	{
		super();
		this.mParser = parser;
	}

	public RequestTask(IParser parser, Bitmap image)
	{
		super();
		this.mParser = parser;
		this.mImage = image;
	}

	public RequestTask(IParser parser, Bitmap image, String bitmapName)
	{
		super();
		this.mParser = parser;
		this.mImage = image;
		this.mImageName = bitmapName;
	}

	/**
	 * 绑定activity，如果activity已经destroy<br>
	 * 不再回调，防止异常情况产生<br>
	 */
	public void bindActivity(Activity activity)
	{
		mActivityReference = new WeakReference<Activity>(activity);
	}

	/**
	 * 同步执行网络请求并返回<br>
	 * 针对一个异步线程里可能需要执行多个网络请求<br>
	 * 请不要在UI线程使用该方法<br>
	 * 
	 * @return
	 */
	public final TaskResult syncExecute(TaskParams params)
	{
		mActivityReference = null;
		return doInBackground(params);
	}

	/**
	 * 网络请求（使用缓存）<br>
	 * 
	 * @param params
	 * @param cacheListener
	 *            缓存加载成功回调<br>
	 * @param cacheTime
	 *            缓存有效时间。设置为<=0时永久有效<br>
	 * @param onlyCache
	 *            是否只使用缓存<br>
	 */
	private final void executeWitchCache(TaskParams params, ITaskCacheLoadedListener cacheListener, long cacheTime,
			boolean onlyCache)
	{
		mCacheListener = cacheListener;
		mEnableCache = true;
		mCacheTime = cacheTime;
		mOnlyUseCache = onlyCache;
		execute(params);
	}

	/**
	 * 网络请求（使用缓存先给页面填数据，再网络请求回来)<br>
	 * 缓存加载完毕会回调ICacheLoadedListener<br>
	 * 网络请求回来回调onTaskFinished<br>
	 * 
	 * @param params
	 * @param cacheListener
	 */
	public final void executeWitchCache(TaskParams params, ITaskCacheLoadedListener cacheListener)
	{
		executeWitchCache(params, cacheListener, -1, false);
	}

	/**
	 * 网络请求（使用缓存）<br>
	 * 缓存未过期会直接返回缓存数据<br>
	 * 
	 * @param params
	 * @param cacheTime
	 */
	public final void executeWitchCache(TaskParams params, long cacheTime)
	{
		executeWitchCache(params, null, cacheTime, true);
	}

	/**
	 * 强制中止task
	 */
	public void abort()
	{
		cancel(true);
		if (mHttpClient != null) {
			mHttpClient.getConnectionManager().shutdown();
		}
	}

	public void setPostParams(List<NameValuePair> params)
	{
		mPostParams = params;
	}

	public IParser getParser()
	{
		return mParser;
	}

	public void setParser(IParser mParser)
	{
		this.mParser = mParser;
	}

	public TaskParams getParams()
	{
		return mParams;
	}

	public String getRequestUrl()
	{
		return mUrl;
	}

	public Object getExtra()
	{
		return mExtra;
	}

	public void setExtra(Object relativeObj)
	{
		this.mExtra = relativeObj;
	}

	@Override
	protected void onCancelled()
	{
		mActivityReference = null;
		// donothing,当RequestTask被取消后，不再回调listener
	}

	@Override
	protected void onPostExecute(TaskResult result)
	{

		// 当task没被取消时，调用父类回调taskFinished
		if (!isCancelled()) {
			// 1 bind activity, find the need for callback
			if (mActivityReference != null) {
				Activity relateActivity = mActivityReference.get();
				// 2 relate activity release,no need to callback
				if (relateActivity == null) {
					return;
				}

				// 3 is destroyed
				if (relateActivity instanceof BaseActivity) {
					if (((BaseActivity) relateActivity).isBeDestroyed()) {
						return;
					}
				}
				if (relateActivity instanceof BaseFragmentActivity) {
					if (((BaseFragmentActivity) relateActivity).isBeDestroyed()) {
						return;
					}
				}
			}
			super.onPostExecute(result);
		}
		mActivityReference = null;
	}

	private void checkPostParams(String carrier, String apn, String imei, String deviceId, String appChannel,
			String accessToken)
	{
		if (mPostParams == null) {
			mPostParams = new ArrayList<NameValuePair>();
		}
		if (!mPostParams.contains(new BasicNameValuePair(ConstantData.PHONE_IMEI_KEY, imei))) {
			mPostParams.add(new BasicNameValuePair(ConstantData.PHONE_IMEI_KEY, imei));
		}
		if (!mPostParams.contains(new BasicNameValuePair(ConstantData.APP_CHANNEL_KEY, appChannel))) {
			mPostParams.add(new BasicNameValuePair(ConstantData.APP_CHANNEL_KEY, appChannel));
		}
		if (!mPostParams.contains(new BasicNameValuePair(ConstantData.DEVICE_ID_KEY, deviceId))) {
			mPostParams.add(new BasicNameValuePair(ConstantData.DEVICE_ID_KEY, deviceId));
		}
		if (!mPostParams.contains(new BasicNameValuePair(ConstantData.APP_VERSION_KEY, ConstantData.APP_VERSION_VALUE))) {
			mPostParams.add(new BasicNameValuePair(ConstantData.APP_VERSION_KEY, ConstantData.APP_VERSION_VALUE));
		}
		if (!mPostParams.contains(new BasicNameValuePair(ConstantData.OPERATORS_NAME_KEY, carrier))) {
			mPostParams.add(new BasicNameValuePair(ConstantData.OPERATORS_NAME_KEY, carrier));
		}
		if (!mPostParams.contains(new BasicNameValuePair(ConstantData.APN_ACCESS_KEY, apn))) {
			mPostParams.add(new BasicNameValuePair(ConstantData.APN_ACCESS_KEY, apn));
		}
		if (!mPostParams.contains(new BasicNameValuePair(ConstantData.ACCESS_TOKEN_KEY, accessToken))) {
			mPostParams.add(new BasicNameValuePair(ConstantData.ACCESS_TOKEN_KEY, accessToken));
		}
	}

	@Override
	protected TaskResult doInBackground(TaskParams... params)
	{

		TaskResult result = new TaskResult(-1, this, null);
		mParams = params[0];
		if (mParams == null) {
			LogUtil.e(TAG, "params is null");
			return result;
		}

		mUrl = mParams.getString(PARAM_URL);
		String url = HttpUtil.addAuthCode2Url(mUrl);
		LogUtil.i(TAG, url);

		// 1 读取缓存处理
		if (mEnableCache && mParser != null) {
			DataCacheBean cacheBean = DBService.getDataCache(url);
			if (null != cacheBean && cacheBean.isValid()) {
				final Object retObj = mParser.parseString(cacheBean.getData());
				if (retObj != null) {
					if (mOnlyUseCache) {
						result.stateCode = HttpStatus.SC_OK;
						result.retObj = retObj;
						return result;
					} else {
						result.cacheAlreadySuccess = true;
						if (mCacheListener != null) {
							// 通知缓存加载成功
							mUiHandler.post(new Runnable()
							{

								@Override
								public void run()
								{
									mCacheListener.onTaskCacheLoaded(retObj);
								}
							});
						}
					}
				}
			}
		}

		HttpResponse response = null;
		HttpEntity entity = null;
		try {
			// 统计相关参数信息
			String carrier = DeviceInfo.getCarrier(SinaBookApplication.gContext);
			String apn = HttpUtil.getNetworkType(SinaBookApplication.gContext);
			String imei = ConstantData.getDeviceId();
			String deviceId = DeviceInfo.getUDID();
			String appChannel = String.valueOf(ConstantData.getChannelCode(SinaBookApplication.gContext));
			String accessToken = null;
			if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				accessToken = LoginUtil.getLoginInfo().getAccessToken();
			}

			mHttpClient = HttpUtil.getHttpClient(SinaBookApplication.gContext);
			if (HTTP_POST.equals(mParams.getString(PARAM_HTTP_METHOD))) {
				// TODO 检测POST参数
				checkPostParams(carrier, apn, imei, deviceId, appChannel, accessToken);
				if (mImage != null) {
					if (Utils.isEmptyString(mImageName)) {
						mImageName = "uploadfile";
					}
					response = HttpUtil.doFilePostRequest(mHttpClient, url, mPostParams, mImage, mImageName);
				} else {
					response = HttpUtil.doPostRequest(mHttpClient, url, mPostParams);
				}
			} else {
				// TODO 检测GET参数
				url = HttpUtil.setURLParams(url, ConstantData.OPERATORS_NAME_KEY, carrier);
				url = HttpUtil.setURLParams(url, ConstantData.APN_ACCESS_KEY, apn);
				url = HttpUtil.setURLParams(url, ConstantData.PHONE_IMEI_KEY, imei);
				url = HttpUtil.setURLParams(url, ConstantData.DEVICE_ID_KEY, deviceId);
				url = HttpUtil.setURLParams(url, ConstantData.APP_CHANNEL_KEY, appChannel);
				url = ConstantData.addLoginInfoToUrl(url);
				response = HttpUtil.doGetRequest(mHttpClient, url);
			}
			int stateCode = response.getStatusLine().getStatusCode();
			result.stateCode = stateCode;
			if (stateCode == HttpStatus.SC_OK || stateCode == HttpStatus.SC_PARTIAL_CONTENT) {
				entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				if (inputStream != null && mParser != null) {
					Object obj = null;
					String jsonString = HttpUtil.inputStream2String(inputStream);

					if (isCancelled() || TextUtils.isEmpty(jsonString)) {
						obj = null;
					} else {
						obj = mParser.parseString(jsonString);
					}
					// 2 返回的数据无问题，存储缓存
					if (obj != null && mEnableCache) {
						DBService.setDataCache(url, jsonString, mCacheTime);
					}
					result.stateCode = HttpStatus.SC_OK;
					result.retObj = obj;
				}
			}

			LogUtil.d(TAG, "Url -> " + url);
			LogUtil.d(TAG, "Code -> " + stateCode);
		} catch (IOException e) {
			LogUtil.w(TAG, "IO exception " + url, e);
		} catch (Exception e) {
			LogUtil.w(TAG, "exception " + url, e);
		} finally {
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException e) {
					LogUtil.w(TAG, "request fail IOerror:", e);
				}
				entity = null;
			}
			if (mHttpClient != null) {
				mHttpClient.getConnectionManager().shutdown();
				mHttpClient = null;
			}
		}
		return result;
	}
}
