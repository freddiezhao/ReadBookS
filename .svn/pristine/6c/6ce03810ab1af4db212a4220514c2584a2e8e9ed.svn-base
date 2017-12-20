package com.sina.book.useraction;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import android.text.TextUtils;

import com.sina.book.util.LogUtil;

/**
 * 行为事件队列
 * 
 * @author MarkMjw
 * @date 2013-4-25
 */
public class EventQueue {
	// private static final String TAG = "EventQueue";

	private ArrayList<Event> mEvents;

	public EventQueue() {
		mEvents = new ArrayList<Event>();
	}

	/**
	 * 列表大小
	 * 
	 * @return
	 */
	public int size() {
		synchronized (this) {
			return mEvents.size();
		}
	}

	/**
	 * 构造Json字符串
	 * 
	 * @return
	 */
	public String events() {
		String result = "[";

		synchronized (this) {
			for (int i = 0; i < mEvents.size(); ++i) {
				Event event = mEvents.get(i);

				result += "{";

				result += "\"" + "key" + "\"" + ":" + "\"" + event.key + "\"";

				if (event.segmentation != null) {
					String segmentation = "{";

					Set<String> strings = event.segmentation.keySet();
					String keys[] = strings.toArray(new String[strings.size()]);

					for (int j = 0; j < keys.length; ++j) {
						String key = keys[j];
						String value = event.segmentation.get(key);

						segmentation += "\"" + key + "\"" + ":" + "\"" + value + "\"";

						if (j + 1 < keys.length) {
							segmentation += ",";
						}
					}

					segmentation += "}";

					result += "," + "\"" + "segmentation" + "\"" + ":" + segmentation;
				}

				result += "," + "\"" + "count" + "\"" + ":" + event.count;

				if (event.sum > 0) {
					result += "," + "\"" + "sum" + "\"" + ":" + event.sum;
				}

				if (!TextUtils.isEmpty(event.eventType)) {
					result += "," + "\"" + "eventType" + "\"" + ":" + "\"" + event.eventType + "\"";
				}

				if (!TextUtils.isEmpty(event.extra)) {
					result += "," + "\"" + "extra" + "\"" + ":" + "\"" + event.extra + "\"";
				}

				result += "," + "\"" + "timestamp" + "\"" + ":" + (long) event.timestamp;

				result += "}";

				if (i + 1 < mEvents.size()) {
					result += ",";
				}
			}

			mEvents.clear();
		}

		result += "]";

		LogUtil.d(getClass().getSimpleName(), "Events -> " + result);

		// try {
		// result = java.net.URLEncoder.encode(result, HTTP.UTF_8);
		// } catch (UnsupportedEncodingException e) {
		//
		// }

		return result;
	}

	/**
	 * 记录数值类型，累加
	 * 
	 * @param key
	 *            类型
	 * @param count
	 *            次数
	 */
	public void recordEvent(String key, int count) {
		if (key != null) {
			synchronized (this) {
				for (Event event : mEvents) {
					if (key.equals(event.key)) {
						event.count += count;
						event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
						return;
					}
				}

				Event event = new Event();
				event.key = key;
				event.count = count;
				event.timestamp = System.currentTimeMillis() / 1000.0;
				mEvents.add(event);
			}
		}
	}

	/**
	 * 记录数值类型，累加
	 * 
	 * @param key
	 *            类型
	 * @param count
	 *            次数
	 */
	public void recordEvent(String key, String extra) {
		recordEvent(key, extra, 1);
	}

	/**
	 * 记录数值类型，累加
	 * 
	 * @param key
	 *            类型
	 * @param count
	 *            次数
	 */
	public void recordEvent(String key, String extra, int count) {
		if (key != null) {
			synchronized (this) {
				for (Event event : mEvents) {
					if (key.equals(event.key)) {
						event.count += count;
						event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
						return;
					}
				}

				Event event = new Event();
				event.key = key;
				event.count = count;
				event.extra = extra;
				event.timestamp = System.currentTimeMillis() / 1000.0;
				mEvents.add(event);
			}
		}
	}

	/**
	 * 记录数值类型，累加
	 * 
	 * @param key
	 *            类型
	 * @param count
	 *            次数
	 */
	public void recordEvent(String key, String eventType, String extra, int count) {
		if (key != null) {
			synchronized (this) {
				for (Event event : mEvents) {
					// 做一个特殊处理
					if (eventType != null && "book_read".equals(eventType)) {
						if (key != null && key.contains("_import")) {
							if (event.key != null && event.key.contains("_import")) {
								event.key = key;
								event.count += count;
								event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
								return;
							}
						}
					}

					if (key.equals(event.key)) {
						event.count += count;
						event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
						return;
					}
				}

				Event event = new Event();
				event.key = key;
				event.count = count;
				event.eventType = eventType;
				event.extra = extra;
				event.timestamp = System.currentTimeMillis() / 1000.0;
				mEvents.add(event);
			}
		}
	}

	/**
	 * 记录非数值类型值
	 * 
	 * @param value
	 *            值
	 */
	public void recordEvent(String key) {
		if (key != null) {
			synchronized (this) {
				for (Event event : mEvents) {
					if (key.equals(event.key)) {
						event.timestamp = System.currentTimeMillis() / 1000.0;
						return;
					}
				}

				Event event = new Event();
				event.key = key;
				event.count = 1;
				event.timestamp = System.currentTimeMillis() / 1000.0;
				mEvents.add(event);
			}
		}
	}

	/**
	 * 记录行为
	 * 
	 * @param key
	 * @param count
	 * @param sum
	 */
	public void recordEvent(String key, int count, double sum) {
		if (key != null) {
			synchronized (this) {
				for (Event event : mEvents) {
					if (key.equals(event.key)) {
						event.count += count;
						event.sum += sum;
						event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
						return;
					}
				}

				Event event = new Event();
				event.key = key;
				event.count = count;
				event.sum = sum;
				event.timestamp = System.currentTimeMillis() / 1000.0;
				mEvents.add(event);
			}
		}
	}

	/**
	 * 记录行为
	 * 
	 * @param key
	 * @param segmentation
	 * @param count
	 */
	public void recordEvent(String key, Map<String, String> segmentation, int count) {
		if (key != null) {
			synchronized (this) {
				for (Event event : mEvents) {
					if (key.equals(event.key) && event.segmentation != null && event.segmentation.equals(segmentation)) {
						event.count += count;
						event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
						return;
					}
				}

				Event event = new Event();
				event.key = key;
				event.segmentation = segmentation;
				event.count = count;
				event.timestamp = System.currentTimeMillis() / 1000.0;
				mEvents.add(event);
			}
		}
	}

	/**
	 * 记录行为
	 * 
	 * @param key
	 * @param segmentation
	 * @param count
	 * @param sum
	 */
	public void recordEvent(String key, Map<String, String> segmentation, int count, double sum) {
		if (key != null) {
			synchronized (this) {
				for (Event event : mEvents) {
					if (key.equals(event.key) && event.segmentation != null && event.segmentation.equals(segmentation)) {
						event.count += count;
						event.sum += sum;
						event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
						return;
					}
				}

				Event event = new Event();
				event.key = key;
				event.segmentation = segmentation;
				event.count = count;
				event.sum = sum;
				event.timestamp = System.currentTimeMillis() / 1000.0;
				mEvents.add(event);
			}
		}
	}

	/**
	 * 事件
	 * 
	 * @author MarkMjw
	 */
	private class Event {
		public String key = null;
		public Map<String, String> segmentation = null;
		public int count = 0;
		public double sum = 0;
		public double timestamp = 0;

		// 1.8.5版本添加了新的统计，增加两个字段来区分
		public String extra = null;// 额外的信息
		public String eventType = null;// 事件类型
		// public String countVersion = null;// 统计版本
	}
}
