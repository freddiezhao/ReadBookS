package com.sina.book.data.util;

import java.util.List;

import org.apache.http.HttpStatus;

import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.MyCollectedBooks;
import com.sina.book.data.MyPurchasedBooks;
import com.sina.book.data.PaymentMonthBookResult;
import com.sina.book.data.PaymentMonthMineResult;
import com.sina.book.data.PurchasedBook;
import com.sina.book.parser.CollectedBooksParser;
import com.sina.book.parser.PaymentMonthBookParser;
import com.sina.book.parser.PaymentMonthMineParser;
import com.sina.book.parser.PurchasedBooksParser;
import com.sina.book.util.LogUtil;

public class ListCacheUtil {
	private static final String TAG = "ListCacheUtil";

	/**
	 * 请求购买记录（首页）
	 */
	public static void requestPurchasedBooks() {
		String reqUrl = String.format(ConstantData.URL_PURCHASED_BOOKS, 1, 10);
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new PurchasedBooksParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
		reqTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
					MyPurchasedBooks pBooks = (MyPurchasedBooks) taskResult.retObj;

					if (pBooks != null && pBooks.getTotal() > 0) {
						List<PurchasedBook> list = pBooks.getList();

						if (list != null && list.size() != 0) {
							LogUtil.i(TAG, "取回购买记录条数：" + list.size());
							// 首页需要清掉数据
							PurchasedBookList.getInstance().cleanAndNotify();
							PurchasedBookList.getInstance().setTotal(pBooks.getTotal());
							PurchasedBookList.getInstance().addList(list);
							PurchasedBookList.getInstance().setPage(1);
							return;
						}
					}
				}

				PurchasedBookList.getInstance().notifyDataChanged();
			}
		});
	}

	/**
	 * 请求购买记录
	 * 
	 * @param page
	 *            页码
	 * 
	 * @param perPage
	 *            每页数量
	 */
	public static void requestPurchasedBooks(final int page, int perPage) {
		String reqUrl = String.format(ConstantData.URL_PURCHASED_BOOKS, page, perPage);
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new PurchasedBooksParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
		reqTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
					MyPurchasedBooks pBooks = (MyPurchasedBooks) taskResult.retObj;

					if (pBooks != null && pBooks.getTotal() > 0) {
						List<PurchasedBook> list = pBooks.getList();

						if (list != null && list.size() != 0) {
							LogUtil.i(TAG, "取回购买记录条数：" + list.size());

							PurchasedBookList.getInstance().setTotal(pBooks.getTotal());
							PurchasedBookList.getInstance().addList(list);
							PurchasedBookList.getInstance().setPage(page);
							return;
						}
					}
				}

				PurchasedBookList.getInstance().notifyDataChanged();
			}
		});
	}

	/**
	 * 请求收藏记录，指定页码
	 * 
	 * @param page
	 *            页码
	 * 
	 * @param perPage
	 *            每页数量
	 */
	public static void requestCollectedBooks(int page, int perPage) {
		String reqUrl = String.format(ConstantData.URL_COLLECTED_BOOKS, page, perPage);
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new CollectedBooksParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
		reqTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
					MyCollectedBooks cBooKs = (MyCollectedBooks) taskResult.retObj;

					if (cBooKs != null && cBooKs.getTotal() > 0) {
						List<Book> list = cBooKs.getList();

						if (list != null && list.size() != 0) {
							LogUtil.i(TAG, "取回收藏记录条数：" + list.size());

							CollectedBookList.getInstance().setTotal(cBooKs.getTotal());
							CollectedBookList.getInstance().addList(list);
							return;
						}
					}
				}

				CollectedBookList.getInstance().notifyDataChanged();
			}
		});
	}

	/**
	 * 请求收藏记录（首页）
	 */
	public static void requestCollectedBooks() {
		String reqUrl = String.format(ConstantData.URL_COLLECTED_BOOKS, 1, 10);
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new CollectedBooksParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
		reqTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
					MyCollectedBooks cBooKs = (MyCollectedBooks) taskResult.retObj;

					if (cBooKs != null && cBooKs.getTotal() > 0) {
						List<Book> list = cBooKs.getList();

						if (list != null && list.size() != 0) {
							LogUtil.i(TAG, "取回收藏记录条数：" + list.size());

							CollectedBookList.getInstance().cleanAndNotify();
							CollectedBookList.getInstance().setTotal(cBooKs.getTotal());
							CollectedBookList.getInstance().addList(list);
							return;
						}
					}
				}

				CollectedBookList.getInstance().notifyDataChanged();
			}
		});
	}

	/**
	 * 请求我的包月列表
	 */
	public static void requestPaymentMonthMine() {
		String reqUrl = ConstantData.URL_SUITE_MY;
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new PaymentMonthMineParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
		reqTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
					PaymentMonthMineResult result = (PaymentMonthMineResult) taskResult.retObj;
					PaymentMonthBookList.getInstance().setNetConnect(true);
					if (result == null) {
						PaymentMonthBookList.getInstance().setNetConnect(false);
						PaymentMonthBookList.getInstance().notifyDataChanged();
						return;
					}

					PaymentMonthMineUtil.getInstance().setList(result.getItem());
					PaymentMonthMineUtil.getInstance().setCount(result.getCount());

					if (result.getCount() == 0 || PaymentMonthMineUtil.getInstance().getMorePayId() == 0) {
						PaymentMonthBookList.getInstance().notifyDataChanged();
					} else {
						requestPaymentMonthBooks();
					}
				} else {
					PaymentMonthBookList.getInstance().notifyDataChanged();
				}

			}
		});

	}

	/**
	 * 请求包月书单列表（首页）
	 */
	public static void requestPaymentMonthBooks() {
		int payId = PaymentMonthMineUtil.getInstance().getMorePayId();
		if (payId == 0) {
			return;
		}

		String reqUrl = String.format(ConstantData.URL_SUITE_BOOK_LIST, payId, 1, 20);
		RequestTask reqTask = new RequestTask(new PaymentMonthBookParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
		reqTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
					PaymentMonthBookResult result = (PaymentMonthBookResult) taskResult.retObj;
					if (result != null && result.getTotal() > 0) {
						PaymentMonthBookList.getInstance().cleanList();
						PaymentMonthBookList.getInstance().setTotal(result.getTotal());
						PaymentMonthBookList.getInstance().addList(result.getItem());
						PaymentMonthBookList.getInstance().setSuiteName(result.getSuiteName());
						return;
					}
				}

				PaymentMonthBookList.getInstance().notifyDataChanged();
			}
		});
	}

	/**
	 * 请求包月书单列表，指定页码
	 * 
	 * @param page
	 *            页码
	 * 
	 * @param perPage
	 *            每页数量
	 */
	public static void requestPaymentMonthBooks(int page, int perPage) {
		int payId = PaymentMonthMineUtil.getInstance().getMorePayId();
		if (payId == 0) {
			return;
		}

		String reqUrl = String.format(ConstantData.URL_SUITE_BOOK_LIST, payId, page, perPage);
		RequestTask reqTask = new RequestTask(new PaymentMonthBookParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
		reqTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
					PaymentMonthBookResult result = (PaymentMonthBookResult) taskResult.retObj;
					if (result != null && result.getTotal() > 0) {
						PaymentMonthBookList.getInstance().setTotal(result.getTotal());
						PaymentMonthBookList.getInstance().addList(result.getItem());
						PaymentMonthBookList.getInstance().setSuiteName(result.getSuiteName());
						return;
					}
				}

				PaymentMonthBookList.getInstance().notifyDataChanged();
			}
		});
	}
}
