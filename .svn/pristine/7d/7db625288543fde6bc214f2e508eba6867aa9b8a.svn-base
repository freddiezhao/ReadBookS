package com.sina.book.useraction;

import java.util.Map;

import android.content.Context;
import android.os.Handler;

import com.sina.book.control.ITaskFinishListener;
import com.sina.book.util.LogUtil;

/**
 * 用户行为统计管理<br>
 * <p/>
 * <ul>
 * Copy from <a href=https://github.com/Countly>Countly</a>
 * </ul>
 * 
 * @author MarkMjw
 * @date 2013-4-24
 */
public class UserActionManager {
	// private static final String TAG = "UserActionManager";

	private static UserActionManager sInstance;

	/**
	 * 提交行为数，当对列中记录的行为类型达到20条则会提交一次数据，根据需要修改.
	 */
	private static final int COMMIT_SIZE = 20;

	private ConnectionQueue mConnectionQueue;
	private EventQueue mEventQueue;

	// private boolean isVisible;

	private int mUnsentSessionLength;
	private double mLastTime;
	private int mActivityCount;

	/**
	 * 获取实例
	 * 
	 * @return The instance of UserActionManager.
	 */
	static public UserActionManager getInstance() {
		if (sInstance == null) {
			synchronized (UserActionManager.class) {
				if (sInstance == null) {
					sInstance = new UserActionManager();
				}
			}
		}
		return sInstance;
	}

	private UserActionManager() {
		mConnectionQueue = new ConnectionQueue();
		mEventQueue = new EventQueue();

		// 生成一个计时器，定时提交
		// Timer mTimer = new Timer();
		// mTimer.schedule(new TimerTask() {
		// @Override
		// public void run() {
		// onTimer();
		// }
		// }, 30 * 1000, 30 * 1000);
		// isVisible = false;

		mUnsentSessionLength = 0;
		mActivityCount = 0;
	}

	// private void onTimer() {
	// if (!isVisible) {
	// return;
	// }
	//
	// double currTime = System.currentTimeMillis() / 1000.0;
	// mUnsentSessionLength += currTime - mLastTime;
	// mLastTime = currTime;
	//
	// int duration = (int) mUnsentSessionLength;
	// mConnectionQueue.updateSession(duration);
	// mUnsentSessionLength -= duration;
	//
	// if (mEventQueue.size() > 0) {
	// mConnectionQueue.recordEvents(mEventQueue.events());
	// }
	// }

	private Handler mHandler;

	/**
	 * 初始化
	 * 
	 * @param context
	 *            上下文引用
	 * @param serverURL
	 *            服务器地址
	 * @param appKey
	 *            appKey
	 */
	public synchronized void init(Context context, String serverURL, String appKey) {
		OpenUDIDManager.sync(context);
		mConnectionQueue.setContext(context);
		mConnectionQueue.setServerURL(serverURL);
		mConnectionQueue.setAppKey(appKey);
	}

	/**
	 * 开始某个页面统计,在Activity的OnStart中使用
	 */
	public void onStart() {
		mActivityCount++;
		// LogUtil.i("UserActionManager", "onStart >>>>  >>>> mActivityCount=" +
		// mActivityCount);
		if (mActivityCount == 1) {
			onStartHelper();
		}
	}

	/**
	 * 结束某个页面统计,在Activity的onStop中使用
	 */
	public void onStop() {
		mActivityCount--;
		// LogUtil.i("UserActionManager", "onStop >>>>  >>>> mActivityCount=" +
		// mActivityCount);
		if (mActivityCount == 0) {
			onStopHelper();
		}
	}

	/**
	 * 开始整个应用统计
	 */
	public void onStartHelper() {
		// LogUtil.d("UserActionManager", "onStartHelper >>>>  >>>> ");
		// 记录会话启动时间
		mLastTime = System.currentTimeMillis() / 1000.0;

		// 判断UUID是否可以获取到，获取不到则延迟几秒钟处理
		if (ConnectionQueue.getUDID() == null) {
			if (mHandler == null) {
				mHandler = new Handler();
			}
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					// LogUtil.e("UserActionManager",
					// "onStartHelper >>>>  >>>> 延迟处理");
					mConnectionQueue.beginSession();
				}
			}, 3000);
		} else {
			// LogUtil.e("UserActionManager", "onStartHelper >>>>  >>>> 立即处理");
			mConnectionQueue.beginSession();
		}

		// isVisible = true;
	}

	/**
	 * 结束整个应用统计
	 */
	public void onStopHelper() {
		// LogUtil.d("UserActionManager",
		// "onStopHelper >>>>  >>>> mEventQueue.size()=" + mEventQueue.size());
		if (mEventQueue.size() > 0) {
			mConnectionQueue.recordEvents(mEventQueue.events());
		}

		double currTime = System.currentTimeMillis() / 1000.0;
		mUnsentSessionLength += (int) (currTime - mLastTime);
		LogUtil.i("TimeCount", "onStopHelper >>>>  >>>>计算会话时间 mLastTime=" + mLastTime + ", mUnsentSessionLength="
				+ mUnsentSessionLength);

		int duration = mUnsentSessionLength;
		mConnectionQueue.endSession(duration);
		mUnsentSessionLength -= duration;
		LogUtil.i("TimeCount", "onStopHelper >>>>  >>>>回滚会话时间 mLastTime=" + mLastTime + ", mUnsentSessionLength="
				+ mUnsentSessionLength);

		// isVisible = false;
	}

	/**
	 * 结束整个应用统计,仅在结束进程时使用
	 */
	public void onStopOnKillProcess() {
		if (mEventQueue.size() > 0) {
			mConnectionQueue.recordEventsOnKillProcess(mEventQueue.events());
		}

		double currTime = System.currentTimeMillis() / 1000.0;
		mUnsentSessionLength += currTime - mLastTime;

		int duration = (int) mUnsentSessionLength;
		mConnectionQueue.endSessionOnKillProcess(duration);
		mUnsentSessionLength -= duration;

		// isVisible = false;
	}

	/**
	 * 记录行为
	 * <p/>
	 * <ul>
	 * <li>记录数值类型，默认+1</li>
	 * </ul>
	 * 
	 * @param key
	 *            类型
	 */
	public void recordEvent(String key) {
		mEventQueue.recordEvent(key, 1);

		if (mEventQueue.size() >= COMMIT_SIZE) {
			mConnectionQueue.recordEvents(mEventQueue.events());
		}
	}

	/**
	 * 立即发送统计请求
	 */
	public void recordInstalledDevice(final ITaskFinishListener listener) {
		if (ConnectionQueue.getUDID() == null) {
			if (mHandler == null) {
				mHandler = new Handler();
			}
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mConnectionQueue.recordEvents(null, listener);
				}
			}, 3000);
		} else {
			mConnectionQueue.recordEvents(null, listener);
		}
	}

	public void setConnectionQueueListener(ITaskFinishListener listener) {
		if (mConnectionQueue != null) {
			mConnectionQueue.setITaskFinishListener(listener);
		}
	}

	/**
	 * 记录行为
	 * <p/>
	 * <ul>
	 * <li>记录数值类型，默认+1</li>
	 * </ul>
	 * 
	 * @param key
	 *            类型
	 */
	public void recordEventNew(String key) {
		mEventQueue.recordEvent(key, "book_intro", null, 1);

		if (mEventQueue.size() >= COMMIT_SIZE) {
			mConnectionQueue.recordEvents(mEventQueue.events());
		}
	}

	/**
	 * 记录行为
	 * <p/>
	 * <ul>
	 * <li>记录数值类型，默认+1</li>
	 * </ul>
	 * 
	 * @param key
	 *            类型
	 */
	public void recordEventNew(String key, String extra) {
		recordEventNew(key, "book_intro", extra);
	}

	public void recordEventNew(String key, String eventType, String extra) {
		mEventQueue.recordEvent(key, eventType, extra, 1);

		if (mEventQueue.size() >= COMMIT_SIZE) {
			mConnectionQueue.recordEvents(mEventQueue.events());
		}
	}

	public void recordEvent(String key, String extra) {
		mEventQueue.recordEvent(key, extra);

		if (mEventQueue.size() >= COMMIT_SIZE) {
			mConnectionQueue.recordEvents(mEventQueue.events());
		}
	}

	/**
	 * 记录行为
	 * <p/>
	 * <ul>
	 * <li>记录数值类型，累加</li>
	 * </ul>
	 * 
	 * @param key
	 *            类型
	 * @param count
	 *            次数
	 */
	public void recordEvent(String key, int count) {
		mEventQueue.recordEvent(key, count);

		if (mEventQueue.size() >= COMMIT_SIZE) {
			mConnectionQueue.recordEvents(mEventQueue.events());
		}
	}

	/**
	 * 记录行为
	 * <p/>
	 * <ul>
	 * <li>非数值类型值，count默认为1</li>
	 * </ul>
	 * 
	 * @param value
	 *            Key_Value值
	 */
	public void recordEventValue(String value) {
		mEventQueue.recordEvent(value);

		if (mEventQueue.size() >= COMMIT_SIZE) {
			mConnectionQueue.recordEvents(mEventQueue.events());
		}
	}

	/**
	 * 记录行为
	 * 
	 * @param key
	 *            类型
	 * @param count
	 *            次数
	 * @param sum
	 *            总和(如：购买价格)
	 */
	public void recordEvent(String key, int count, double sum) {
		mEventQueue.recordEvent(key, count, sum);

		if (mEventQueue.size() >= COMMIT_SIZE) {
			mConnectionQueue.recordEvents(mEventQueue.events());
		}
	}

	/**
	 * 记录行为
	 * 
	 * @param key
	 *            类型
	 * @param segmentation
	 *            其他属性
	 * @param count
	 *            次数
	 */
	public void recordEvent(String key, Map<String, String> segmentation, int count) {
		mEventQueue.recordEvent(key, segmentation, count);

		if (mEventQueue.size() >= COMMIT_SIZE) {
			mConnectionQueue.recordEvents(mEventQueue.events());
		}
	}

	/**
	 * 记录行为
	 * 
	 * @param key
	 *            类型
	 * @param segmentation
	 *            其他属性
	 * @param count
	 *            次数
	 * @param sum
	 *            总和(如：购买价格)
	 */
	public void recordEvent(String key, Map<String, String> segmentation, int count, double sum) {
		mEventQueue.recordEvent(key, segmentation, count, sum);

		if (mEventQueue.size() >= COMMIT_SIZE) {
			mConnectionQueue.recordEvents(mEventQueue.events());
		}
	}
}
