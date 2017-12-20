package com.sina.book.data.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.htmlcleaner.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.PaymentMonthDetail;
import com.sina.book.data.PaymentMonthMine;
import com.sina.book.data.PaymentMonthMineResult;
import com.sina.book.data.PaymentMonthPurchased;
import com.sina.book.parser.PaymentMonthMineParser;
import com.sina.book.parser.PaymentMonthPurchasedParser;
import com.sina.book.ui.BaseActivity;
import com.sina.book.ui.RechargeCenterActivity;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.util.Base64;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;

/**
 * 我的包月相关操作
 * 
 * @author YangZhanDong
 * 
 */
public class PaymentMonthMineUtil {

	private static PaymentMonthMineUtil instance;
	private static final String IS_BUY = "Y";
	private static final String PAYMENT_MONTH_FILE_NAME = "/readerSetting.set";

	/**
	 * 网络存储的包月信息<br>
	 */
	private List<PaymentMonthMine> mPaymentMonthMineList = new ArrayList<PaymentMonthMine>();
	/**
	 * 本地存储的包月信息<br>
	 */
	private List<PaymentMonthMine> mLocalPaymentMonthMineList = new ArrayList<PaymentMonthMine>();
	/**
	 * 是否已经去网络同步过包月信息<br>
	 * 再变化及未同步时设置为false<br>
	 */
	private boolean isAlreadySync = false;

	private int mCount;
	private String mPayId;
	private BaseActivity mContext;

	private long mLocalNow;

	/**
	 * 购买包月的网络请求
	 */
	private RequestTask mRequestTask;
	/**
	 * 获取包月信息的task
	 */
	private RequestTask mReqPaymentTask;

	private IListDataChangeListener mDataChangeListener;
	private boolean mIsOpenSuccess;

	public PaymentMonthMineUtil() {
	}

	public static PaymentMonthMineUtil getInstance() {
		if (instance == null) {
			synchronized (PaymentMonthMineUtil.class) {
				if (instance == null) {
					instance = new PaymentMonthMineUtil();
				}
			}
		}

		return instance;
	}

	/**
	 * 退出登录时需要清除<br>
	 * 清除网络和本地的包月信息
	 */
	public void clear() {
		isAlreadySync = false;
		mPaymentMonthMineList.clear();
		mLocalPaymentMonthMineList.clear();
		// 清除本地保存的包月信息
		String fileName = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_LOG) + PAYMENT_MONTH_FILE_NAME;
		File f = new File(fileName);
		if (f.exists()) {
			f.delete();
		}
	}

	/**
	 * 设置我的包月信息列表<br>
	 * 依赖这里判断是否同步过包月信息列表<br>
	 * 
	 * @param lists
	 */
	public void setList(List<PaymentMonthMine> lists) {
		isAlreadySync = true;
		mPaymentMonthMineList.clear();
		if (lists != null && lists.size() > 0) {
			mPaymentMonthMineList.addAll(lists);
		}
	}

	public void setCount(int count) {
		this.mCount = count;
	}

	/**
	 * 获取较高级的我的包月套餐的id
	 */
	public int getMorePayId() {
		int payId = 0;
		if (mCount > 0 && mPaymentMonthMineList.size() > 0) {
			for (int i = 0; i < mPaymentMonthMineList.size(); i++) {
				PaymentMonthMine item = mPaymentMonthMineList.get(i);
				if (item.getPayOpen().equals(IS_BUY)) {
					payId = (payId <= item.getPayId()) ? item.getPayId() : payId;
				}
			}
		}

		return payId;
	}

	/**
	 * 是否为继续包月
	 */
	public boolean isContinuePaymentMonth() {
		if (mPaymentMonthMineList.size() > 0 && getMorePayId() == 0) {
			return true;
		}
		return false;
	}

	public void release(Context context) {
		if (context == mContext) {
			mContext = null;
		}
	}

	/**
	 * 购买包月功能
	 */
	public void openPaymentMonth(BaseActivity context, String payId, String payOpen, String payType) {
		if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS)
			return;

		this.mPayId = payId;
		this.mContext = context;
		String balance = "";
		balance = String.format(mContext.getString(R.string.your_balance), LoginUtil.getLoginInfo().getBalance(),
				mContext.getString(R.string.open), payType);

		if (mRequestTask != null) {
			Toast.makeText(mContext, mContext.getString(R.string.purchase_payment_month_attention), Toast.LENGTH_SHORT)
					.show();
			return;
		}

		String title = mContext.getString(R.string.open_month_pay);
		CommonDialog.show(mContext, title, balance, new CommonDialog.DefaultListener() {
			@Override
			public void onRightClick(DialogInterface dialog) {
				requestPaymentMonth(mPayId);
				LoginUtil.reqBalance(SinaBookApplication.gContext);
			}
		});
	}

	private void requestPaymentMonth(String payId) {
		String reqUrl = String.format(ConstantData.URL_SUITE_BUY, payId);
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		reqUrl = ConstantData.addDeviceIdToUrl(reqUrl);
		mRequestTask = new RequestTask(new PaymentMonthPurchasedParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		mRequestTask.execute(params);
		mRequestTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (mContext == null) {
					mRequestTask = null;
					return;
				}

				if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
					PaymentMonthPurchased result = (PaymentMonthPurchased) taskResult.retObj;

					if (result == null) {
						Toast.makeText(mContext, mContext.getString(R.string.purchase_payment_month_again),
								Toast.LENGTH_SHORT).show();
						mRequestTask = null;
						return;
					}

					String payOpen = result.getPayOpen();
					if (payOpen != null && payOpen.equals(IS_BUY)) {
						mIsOpenSuccess = true;
						showDialog(mContext.getString(R.string.note),
								mContext.getString(R.string.open_payment_month_sucessed));
					} else {
						showDialog(mContext.getString(R.string.note), mContext.getString(R.string.balance_not_enough));
					}
				} else {
					Toast.makeText(mContext, mContext.getString(R.string.purchase_payment_month_again),
							Toast.LENGTH_SHORT).show();
				}
				mRequestTask = null;
			}
		});
	}

	private void showDialog(String title, String msg) {
		CommonDialog.show(mContext, title, msg, new CommonDialog.DefaultListener() {
			@Override
			public void onRightClick(DialogInterface dialog) {
				if (mIsOpenSuccess) {
					mIsOpenSuccess = false;
					notifyDataChanged();
				} else {
					Intent intent = new Intent(mContext, RechargeCenterActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					mContext.startActivity(intent);
				}
			}
		});
	}

	/**
	 * 是否有订购包月
	 * 
	 * @return
	 */
	public boolean havePaymentMonth() {
		if (!mPaymentMonthMineList.isEmpty()) {
			return true;
		}
		if (!mLocalPaymentMonthMineList.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 通过这个方法判断书籍下载是否跟包月有关系<br>
	 * 来改变书籍的一些状态字
	 * 
	 * @param book
	 */
	public void downBecausePaymentMonth(Book book) {
		if (book == null) {
			return;
		}
		boolean hasBuy = book.getBuyInfo().isHasBuy();
		// buyType 0，未购买；1，单章,单本购买；2，套餐内购买
		int buytype = book.getBuyInfo().getBuyType();
		int paytype = book.getBuyInfo().getPayType();
		int suiteId = book.getSuiteId();
		int originSuiteId = book.getOriginSuiteId() > 0 ? book.getOriginSuiteId() : suiteId;

		// 1 非包月或免费书籍
		if (originSuiteId <= 0 || paytype == Book.BOOK_TYPE_FREE) {
			return;
		}

		// 2 针对明确已经购买的书
		if (hasBuy && buytype == 1) {
			book.setSuiteId(0);
			return;
		}

		// 3 书本类型是全本购买，且现在为套餐内购买，肯定是因为包月套餐
		if (hasBuy && buytype == 2 && paytype == Book.BOOK_TYPE_VIP) {
			book.setSuiteId(originSuiteId);
			return;
		}

		// 4 针对章节购买的书判断，是因为网络上包月信息才能下载
		for (PaymentMonthMine netPaymentMonth : mPaymentMonthMineList) {
			if (netPaymentMonth.getPayOpen().equals(IS_BUY)) {
				if (suiteId <= netPaymentMonth.getPayId()) {
					book.setSuiteId(originSuiteId);
					return;
				}
			}
		}

		// 5 针对章节购买的书判断，用本地存储信息判断
		for (PaymentMonthMine localPaymentMonth : mLocalPaymentMonthMineList) {
			if (localPaymentMonth.isLocalVaild()) {
				if (suiteId <= localPaymentMonth.getPayId()) {
					book.setSuiteId(originSuiteId);
					return;
				}
			}
		}

		// 与包月无关，把书籍的包月信息清除
		book.setSuiteId(0);
	}

	/**
	 * 一本书是否包月过期，不能阅读了<br>
	 * 
	 * @return
	 */
	public boolean canRead(Book book) {
		if (book == null) {
			return false;
		}

		int suiteId = book.getSuiteId();

		// 1 非包月书籍，能阅读
		if (suiteId <= 0) {
			return true;
		}

		// 2 先尽量从服务器数据中去判断是否能继续阅读
		for (PaymentMonthMine netPaymentMonth : mPaymentMonthMineList) {
			if (netPaymentMonth.getPayOpen().equals(IS_BUY)) {
				if (suiteId <= netPaymentMonth.getPayId()) {
					return true;
				}
			}
		}

		// 3 实在不行，用本地存储信息判断
		for (PaymentMonthMine localPaymentMonth : mLocalPaymentMonthMineList) {
			if (localPaymentMonth.isLocalVaild()) {
				if (suiteId <= localPaymentMonth.getPayId()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 检查是否需要网络同步包月信息<br>
	 * 针对本地存储的网络信息已不再有效时<br>
	 */
	public void checkPaymentMonth(Book book) {
		if (book == null) {
			return;
		}
		int suiteId = book.getSuiteId();
		boolean hasBuySuite = book.hasBuySuite();
		if (suiteId > 0 && hasBuySuite) {
			// 若找到相同的信息，包月信息应该是较新的，无需同步
			for (PaymentMonthMine netPaymentMonth : mPaymentMonthMineList) {
				if (netPaymentMonth.getPayOpen().equals(IS_BUY)) {
					if (suiteId == netPaymentMonth.getPayId()) {
						return;
					}
				}
			}

			// 包月信息可能有问题
			isAlreadySync = false;
			reqPaymentMonth();
		}
	}

	/**
	 * 网络同步包月信息
	 */
	public void reqPaymentMonth() {
		// 有包月信息，应该没必要
		if (!mPaymentMonthMineList.isEmpty()) {
			return;
		}

		// 已经同步过，不再同步
		if (isAlreadySync || mReqPaymentTask != null) {
			return;
		}

		String reqUrlOrign = ConstantData.URL_SUITE_MY;
		String reqUrl = ConstantData.addLoginInfoToUrl(reqUrlOrign);

		// 无登录信息，没必要请求
		if (reqUrlOrign.equals(reqUrl)) {
			return;
		}

		// 无网，没必要请求
		if (!HttpUtil.isConnected(SinaBookApplication.gContext)) {
			return;
		}

		mReqPaymentTask = new RequestTask(new PaymentMonthMineParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		mReqPaymentTask.execute(params);
		mReqPaymentTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (taskResult.retObj != null && taskResult.stateCode == HttpStatus.SC_OK) {
					PaymentMonthMineResult result = (PaymentMonthMineResult) taskResult.retObj;
					setList(result.getItem());
					setCount(result.getCount());
				}
				mReqPaymentTask = null;
			}
		});
	}

	/**
	 * 包月过期信息序列化到本地
	 */
	public void savePaymentMonthToFile() {
		// 每次存信息到本地时会取当前时间入文件
		long localNow = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();

		// 这次时间判断杜绝了，程序运行中改时间的情况
		if (localNow > mLocalNow) {
			if (mPaymentMonthMineList.isEmpty()) {
				// 如果未同步到服务器的包月信息，只是修改时间
				if (!isAlreadySync) {
					for (int i = 0; i < mLocalPaymentMonthMineList.size(); i++) {
						PaymentMonthMine paymentMonth = mLocalPaymentMonthMineList.get(i);
						sb.append(paymentMonth.getPayId()).append("|").append(paymentMonth.getBeginTime()).append("|")
								.append(paymentMonth.getEndTime()).append("|").append(localNow).append("|");
						if (i < mLocalPaymentMonthMineList.size() - 1) {
							sb.append("\n");
						}
					}
				}
			} else {
				for (int i = 0; i < mPaymentMonthMineList.size(); i++) {
					PaymentMonthMine paymentMonth = mPaymentMonthMineList.get(i);
					if (!paymentMonth.getPayOpen().equals(IS_BUY)) {
						continue;
					}
					sb.append(paymentMonth.getPayId()).append("|").append(paymentMonth.getBeginTime()).append("|")
							.append(paymentMonth.getEndTime()).append("|").append(localNow).append("|");
					if (i < mPaymentMonthMineList.size() - 1) {
						sb.append("\n");
					}
				}
			}
		}

		FileOutputStream fos = null;
		try {
			String fileName = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_LOG) + PAYMENT_MONTH_FILE_NAME;
			File f = new File(fileName);
			if (f.exists()) {
				f.delete();
			}
			if (sb.length() > 0) {
				fos = new FileOutputStream(f);
				fos.write(Base64.encode(sb.toString().getBytes(), Base64.DEFAULT));
			}
		} catch (Exception e) {
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 包月过期信息从本地读入
	 */
	public void readPaymentMonthFromFile() {
		FileInputStream fis = null;
		BufferedReader bufferedReader = null;
		mLocalNow = System.currentTimeMillis();

		try {
			String fileName = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_LOG) + PAYMENT_MONTH_FILE_NAME;
			File f = new File(fileName);
			if (f.exists()) {
				if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					f.delete();
					return;
				}
				fis = new FileInputStream(f);
				ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(HttpUtil.inputStream2String(fis),
						Base64.DEFAULT));
				if (fis != null) {
					fis.close();
				}
				bufferedReader = new BufferedReader(new InputStreamReader(bis));
				String line = null;
				long localPre;

				while ((line = bufferedReader.readLine()) != null) {
					String[] params = line.split("\\|");
					if (params.length == 4) {
						PaymentMonthMine paymentMonth = new PaymentMonthMine();
						String payId = params[0];
						if (Utils.isEmptyString(payId)) {
							continue;
						}
						paymentMonth.setPayId(Integer.parseInt(payId));
						paymentMonth.setBeginTime(params[1]);
						paymentMonth.setEndTime(params[2]);
						localPre = Long.valueOf(params[3]);

						// 时间应该总是递增的，没递增，时间被修改过，不再可靠
						if (mLocalNow + 300000 < localPre) {
							f.delete();
							return;
						}
						mLocalPaymentMonthMineList.add(paymentMonth);
					}
				}
			}
		} catch (Exception e) {
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 设置数据变化监听器
	 * 
	 * @param listener
	 */
	public void setDataChangeListener(IListDataChangeListener listener) {
		mDataChangeListener = listener;
	}

	/**
	 * 通知数据已经更新
	 */
	public void notifyDataChanged() {
		if (null != mDataChangeListener) {
			mDataChangeListener.dataChange();
		}
	}

	/**
	 * 判断包月厅页面数据是否需要刷新
	 */
	public boolean isNeedRefreshPaymentDetail(ArrayList<PaymentMonthDetail> lists) {
		if (lists == null) {
			return false;
		}

		ArrayList<PaymentMonthDetail> havePaymentList = new ArrayList<PaymentMonthDetail>();
		for (PaymentMonthDetail itemDetail : lists) {
			if (itemDetail.getPayOpen().equals(IS_BUY)) {
				havePaymentList.add(itemDetail);
			}
		}

		if (!isAlreadySync) {
			if (havePaymentList.size() > 0) {
				return true;
			} else {
				if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					return false;
				} else {
					return true;
				}
			}
		}

		if (havePaymentList.size() != mPaymentMonthMineList.size()) {
			return true;
		}

		for (int i = 0; i < havePaymentList.size(); i++) {
			PaymentMonthDetail paymentDetail = lists.get(i);
			for (int j = 0; j < mPaymentMonthMineList.size(); j++) {
				PaymentMonthMine itemMine = mPaymentMonthMineList.get(j);

				if (paymentDetail.getPayId().equals(String.valueOf(itemMine.getPayId()))
						&& !paymentDetail.getPayOpen().equals(itemMine.getPayOpen())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 判断包月书单页面数据是否要刷新
	 */
	public boolean isNeedRefreshPaymentBooklist(String payId, String payOpen) {
		for (int i = 0; i < mPaymentMonthMineList.size(); i++) {
			PaymentMonthMine item = mPaymentMonthMineList.get(i);
			if (payId.equals(String.valueOf(item.getPayId())) && !payOpen.equals(item.getPayOpen())) {
				return true;
			}
		}
		return false;
	}

}