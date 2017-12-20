package com.sina.book.useraction;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.TrafficStats;

import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.TaskResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.util.InstalledDeviceCountUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.Util;

/**
 * 连接队列管理器，主要用于发送数据
 * 
 * @author MarkMjw
 * @date 2013-4-25
 */
public class ConnectionQueue {
	private static final String TAG = "ConnectionQueue";

	private ConcurrentLinkedQueue<List<NameValuePair>> mLinkedQueue;
	private Thread mThread = null;
	private String mAppKey;
	private Context mContext;
	private String mServerURL;

	private boolean isBegin = true, isEnd = true;

	public ConnectionQueue() {
		mLinkedQueue = new ConcurrentLinkedQueue<List<NameValuePair>>();
	}

	public void setAppKey(String appKey) {
		mAppKey = appKey;
	}

	public void setContext(Context context) {
		mContext = context;
	}

	public void setServerURL(String serverURL) {
		mServerURL = serverURL;
	}

	private String uniqueSesstionId = null;

	public void beginSession() {
		String udid = getUDID();
		if (null == udid) {
			return;
		}

		if (uniqueSesstionId != null) {
			uniqueSesstionId = null;
		}
		uniqueSesstionId = Util.getUniqueString();

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("app_key", mAppKey));
		data.add(new BasicNameValuePair("device_id", udid));
		data.add(new BasicNameValuePair("timestamp", String.valueOf((long) (System.currentTimeMillis() / 1000.0))));
		data.add(new BasicNameValuePair("sdk_version", "1.0"));
		// data.add(new BasicNameValuePair("begin_session", "1"));
		data.add(new BasicNameValuePair("begin_session", uniqueSesstionId));
		data.add(new BasicNameValuePair("metrics", DeviceInfo.getMetrics(mContext)));

		mLinkedQueue.offer(data);

		isBegin = true;
		commit();
	}

	public void updateSession(int duration) {
		String udid = getUDID();
		if (null == udid) {
			return;
		}

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("app_key", mAppKey));
		data.add(new BasicNameValuePair("device_id", udid));
		data.add(new BasicNameValuePair("timestamp", String.valueOf((long) (System.currentTimeMillis() / 1000.0))));
		data.add(new BasicNameValuePair("session_duration", duration + ""));

		mLinkedQueue.offer(data);

		commit();
	}

	public void endSession(int duration) {
		String udid = getUDID();
		if (null == udid) {
			return;
		}

		if (uniqueSesstionId == null) {
			uniqueSesstionId = Util.getUniqueString();
		}

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("app_key", mAppKey));
		data.add(new BasicNameValuePair("device_id", udid));
		data.add(new BasicNameValuePair("timestamp", String.valueOf((long) (System.currentTimeMillis() / 1000.0))));
		data.add(new BasicNameValuePair("end_session", uniqueSesstionId));
		data.add(new BasicNameValuePair("session_duration", duration + ""));

		mLinkedQueue.offer(data);

		isEnd = true;
		commit();
	}

	public void endSessionOnKillProcess(int duration) {
		String udid = getUDID();
		if (null == udid) {
			return;
		}

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("app_key", mAppKey));
		data.add(new BasicNameValuePair("device_id", udid));
		data.add(new BasicNameValuePair("timestamp", String.valueOf((long) (System.currentTimeMillis() / 1000.0))));
		data.add(new BasicNameValuePair("end_session", "1"));
		data.add(new BasicNameValuePair("session_duration", duration + ""));

		mLinkedQueue.offer(data);

		postEvents();
	}

	private ITaskFinishListener listener;

	public void setITaskFinishListener(ITaskFinishListener listener) {
		this.listener = listener;
	}

	public void recordEvents(String events) {
		recordEvents(events, null);
	}

	public void recordEvents(String events, ITaskFinishListener listener) {
		// 防止被null冲掉
		if (listener != null) {
			this.listener = listener;
		}

		String udid = getUDID();
		if (null == udid) {
			LogUtil.d("InstalledDeviceCountLog", "ConnectionQueue >> commit >> return 0");
			return;
		}

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("app_key", mAppKey));
		data.add(new BasicNameValuePair("device_id", udid));
		data.add(new BasicNameValuePair("timestamp", String.valueOf((long) (System.currentTimeMillis() / 1000.0))));
		// “活”的客户端的统计行为，listener不为空，提供正常统计参数之余，加上metrics参数
		if (this.listener != null) {
			data.add(new BasicNameValuePair("type", InstalledDeviceCountUtil.RECORD_KEY));
			data.add(new BasicNameValuePair("metrics", DeviceInfo.getMetrics(mContext)));
		} else {
			data.add(new BasicNameValuePair("events", events));
		}

		mLinkedQueue.offer(data);

		commit();
	}

	public void recordEventsOnKillProcess(String events) {
		String udid = getUDID();
		if (null == udid) {
			return;
		}

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("app_key", mAppKey));
		data.add(new BasicNameValuePair("device_id", udid));
		data.add(new BasicNameValuePair("timestamp", String.valueOf((long) (System.currentTimeMillis() / 1000.0))));
		data.add(new BasicNameValuePair("events", events));

		mLinkedQueue.offer(data);

		postEvents();
	}

	public static String getUDID() {
		String udid = DeviceInfo.getUDID();

		int index = udid.indexOf(DeviceInfo.REPLACE_UDID);
		if (index != -1) {
			if (OpenUDIDManager.isInitialized()) {
				udid = udid.replaceFirst(DeviceInfo.REPLACE_UDID, OpenUDIDManager.getOpenUDID());
			} else {
				udid = null;
			}
		}

		return udid;
	}

	private void commit() {

		LogUtil.d("InstalledDeviceCountLog", "ConnectionQueue >> commit >> ");

		if (mThread != null && mThread.isAlive()) {

			LogUtil.d("InstalledDeviceCountLog", "ConnectionQueue >> commit >> return 1");

			return;
		}

		if (mLinkedQueue.isEmpty()) {
			LogUtil.d("InstalledDeviceCountLog", "ConnectionQueue >> commit >> return 2");
			return;
		}

		mThread = new Thread() {
			@Override
			public void run() {
				postEvents();
			}
		};

		mThread.start();
	}

	private void postEvents() {

		LogUtil.d("InstalledDeviceCountLog", "ConnectionQueue >> postEvents >> ");

		if (mLinkedQueue.isEmpty()) {

			LogUtil.d("InstalledDeviceCountLog", "ConnectionQueue >> postEvents >> return 3");
			return;
		}

		// 统计耗费流量
		// LogUtil.d(TAG, "Before post send -> " + getTotalTxBytes() + " KB");
		// LogUtil.d(TAG, "Before post receive -> " + getTotalRxBytes() +
		// " KB");

		while (true) {
			List<NameValuePair> installedDeviceData = null;
			List<NameValuePair> data = mLinkedQueue.peek();

			if (null == data) {
				LogUtil.d("InstalledDeviceCountLog", "ConnectionQueue >> postEvents >> return 4");
				break;
			}

			// 打印键值
			for (NameValuePair valuePair : data) {
				LogUtil.w("InstalledDeviceCountLog",
						"post  >>>> " + valuePair.getName() + " -> " + valuePair.getValue());
				LogUtil.w("TimeCount", "post  >>>> " + valuePair.getName() + " -> " + valuePair.getValue());
				// 判断这次发送的统计数据是不是统计“活”设备的统计
				String name = valuePair.getName();
				if ("type".equals(name)) {
					String value = valuePair.getValue();
					if (value != null && value.contains(InstalledDeviceCountUtil.RECORD_KEY)) {
						installedDeviceData = data;
					}
				}

			}

			int code = -1;
			try {
				// HttpPost方式
				// 带上accesstoken
				String serverUrlWithToken = ConstantData.addLoginInfoToUrl(mServerURL);
				HttpPost method = new HttpPost(new URI(serverUrlWithToken));
				method.setEntity(new UrlEncodedFormEntity(data, HTTP.UTF_8));

				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = httpClient.execute(method);
				InputStream input = response.getEntity().getContent();

				// 打印返回内容
				StringBuilder builder = new StringBuilder();
				byte[] buf = new byte[1024];
				int len;
				while ((len = input.read(buf)) != -1) {
					builder.append(new String(buf, 0, len));
				}

				code = response.getStatusLine().getStatusCode();
				LogUtil.d(TAG, "Url -> " + serverUrlWithToken);
				LogUtil.d(TAG, "Code -> " + code);
				LogUtil.d(TAG, "Message -> " + builder);

				httpClient.getConnectionManager().shutdown();

				// 提交到自己服务器
				// commit2OurServer(data);

				mLinkedQueue.poll();
			} catch (Exception e) {
				LogUtil.d(TAG, "Error message -> " + e.getMessage());
				break;
			} finally {

				LogUtil.d("InstalledDeviceCountLog", "ConnectionQueue >> postEvents >> listener=" + listener);

				if (listener != null && installedDeviceData != null) {
					listener.onTaskFinished(new TaskResult(code, null, null));
				}
			}
		}

		// 统计耗费流量
		// LogUtil.d(TAG, "After post send -> " + getTotalTxBytes() + " KB");
		// LogUtil.d(TAG, "After post receive -> " + getTotalRxBytes() + " KB");
	}

	/**
	 * 提交到自己的服务器，只用于统计新增、日活等
	 */
	@SuppressWarnings("unused")
	private void commit2OurServer(List<NameValuePair> data) {
		if (isBegin || isEnd) {
			isBegin = false;
			isEnd = false;

			try {
				// HttpPost方式
				String url = ConstantData.USER_ACTION_URL_OLD;
				HttpPost method = new HttpPost(new URI(url));
				method.setEntity(new UrlEncodedFormEntity(data, HTTP.UTF_8));

				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = httpClient.execute(method);
				InputStream input = response.getEntity().getContent();

				// 打印返回内容
				StringBuilder builder = new StringBuilder();
				byte[] buf = new byte[1024];
				int len;
				while ((len = input.read(buf)) != -1) {
					builder.append(new String(buf, 0, len));
				}

				int code = response.getStatusLine().getStatusCode();
				LogUtil.i(TAG, "Url -> " + url);
				LogUtil.i(TAG, "Code -> " + code);
				LogUtil.i(TAG, "Message -> " + builder);

				httpClient.getConnectionManager().shutdown();
			} catch (Exception e) {
				LogUtil.i(TAG, "Error message -> " + e.getMessage());
			}
		}
	}

	/**
	 * 获取总的接受字节数，包含Mobile和WiFi等
	 * 
	 * @return
	 */
	public long getTotalRxBytes() {
		return TrafficStats.getTotalRxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);
	}

	/**
	 * 总的发送字节数，包含Mobile和WiFi等
	 * 
	 * @return
	 */
	public long getTotalTxBytes() {
		return TrafficStats.getTotalTxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalTxBytes() / 1024);
	}

	/**
	 * 某一个进程的总接收量
	 * 
	 * @param uid
	 * @return
	 */
	public long getUidRxBytes(int uid) {
		return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0
				: (TrafficStats.getUidRxBytes(uid) / 1024);
	}

	/**
	 * 某一个进程的总发送量
	 * 
	 * @param uid
	 * @return
	 */
	public long getUidTxBytes(int uid) {
		return TrafficStats.getUidTxBytes(uid) == TrafficStats.UNSUPPORTED ? 0
				: (TrafficStats.getUidTxBytes(uid) / 1024);
	}
}
