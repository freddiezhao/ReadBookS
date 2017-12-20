package com.sina.book.ui.view;

import java.util.Locale;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.BuyBookManager;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.IBuyBookListener;
import com.sina.book.data.Book;
import com.sina.book.data.BookBuyInfo;
import com.sina.book.data.BookPriceResult;
import com.sina.book.data.Chapter;
import com.sina.book.data.ChapterPriceResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.PriceTip;
import com.sina.book.data.UserInfoRole;
import com.sina.book.data.UserInfoUb;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.db.DBService;
import com.sina.book.parser.BookPriceParser;
import com.sina.book.parser.ChapterPriceParser;
import com.sina.book.parser.TicketToUsedParser;
import com.sina.book.ui.RechargeActivity;
import com.sina.book.ui.RechargeCenterActivity;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.widget.BaseDialog;
import com.sina.book.util.DialogUtils;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.URLUtil;
import com.sina.book.util.Util;

public class PayDialog extends BaseDialog implements ITaskFinishListener, View.OnClickListener {

	public static final int CODE_SUCCESS = 0;
	public static final int CODE_ERROR = -1;

	private static PayDialog mPayDlg;

	public PayFinishListener mPayFinishListener;
	public PayLoginSuccessListener mPayLoginListener;
	private Activity mActivity;
	private Book mBook;
	private Chapter mChapter;
	private UserInfoRole mRole;

	private ProgressBar mProgressBar;
	private TextView mProgressText;

	private View mPriceView;
	/**
	 * 原价
	 */
	private TextView mOriginalPrice;
	/**
	 * 折扣价
	 */
	private TextView mDiscountPrice;
	/**
	 * 消费提示
	 */
	private TextView mReadTicket;

	/**
	 * 赠书卡区域
	 */
	private View mCardView;
	private View mCardDividerView;
	private TextView mCardTitleView;

	private View mCheckBoxView;
	private CheckBox mCheckBox;

	private TextView mOkBtn;
	private TextView mCancelBtn;
	private Drawable mOkBtnBg;

	private Handler mHandler;

	public static void release(Activity activity) {
		if (mPayDlg != null) {
			if (getAssociatedActivity(mPayDlg) == activity) {
				mPayDlg.dismiss();
				mPayDlg = null;
			}
		}
	}

	/**
	 * 拿到dialog对应的Activity
	 * 
	 * @param dialog
	 * @return
	 */
	public static Activity getAssociatedActivity(Dialog dialog) {
		if (dialog == null) {
			return null;
		}
		Activity activity = null;
		Context context = dialog.getContext();
		while (activity == null && context != null) {
			if (context instanceof Activity) {
				activity = (Activity) context; // found it!
			} else {
				context = (context instanceof ContextWrapper) ? ((ContextWrapper) context).getBaseContext() : null;
			}
		}
		return activity == null ? null : activity;
	}

	/**
	 * 购买进入登录页，返回后
	 */
	public static void loginSuccess() {
		if (mPayDlg != null) {
			if (mPayDlg.mPayLoginListener != null) {
				mPayDlg.mPayLoginListener.onLoginSuccess();
			} else {
				mPayDlg.show();
			}
		}
	}

	/**
	 * 是否交换Book的isEpub属性值<br>
	 * 1）如果一本既有txt又有epub资源且当前处于书架内的收费书籍尚未购买，当前资源是txt，此时由书架点击<br>
	 * 		 进入阅读点击工具栏上的购买按钮，购买成功后下载的应该是txt，而如果由其他入口进入了摘要页，此时<br>
	 *		 点击“购买并立即下载”按钮进行购买成功后下载的应该是epub，因此需要判断是否需要交换isEpub属性值<br>
	 *		 以便DownBookManager中对下载逻辑的判断，到底是走下载txt还是走下载epub。<br>
	 * 2）从摘要页入口应该传入true，执行交换isEpub的行为，下载epub资源。<br>
	 * 3）其他入口传入false，下载txt资源。<br>
	 */
	private boolean exchangeBookIsEpubValue = false;
	
	public PayDialog(Activity activity, Book book, Chapter chapter, UserInfoRole role) {
		this(activity, book, chapter, role, false);
	}
	
	
	public PayDialog(Activity activity, Book book, Chapter chapter, UserInfoRole role, boolean exchangeBookIsEpubValue) {
		super(activity);
		this.mActivity = activity;
		this.mBook = book;
		this.mChapter = chapter;
		this.exchangeBookIsEpubValue = exchangeBookIsEpubValue;
		if (LoginUtil.isValidAccessToken(mActivity) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			this.mRole = LoginUtil.getLoginInfo().getUserInfoRole();
		}
		if (role != null) {
			this.mRole = role;
		}
		if (mPayDlg != null) {
			mPayDlg.dismiss();
			mPayDlg = null;
		}
		mPayDlg = this;
		if (mChapter != null && mChapter.getPrice() <= 0) {
			mChapter.setPrice(Chapter.CHAPTER_DEFAUTL_PRICE);
		}
		if (mBook != null && mBook.getBuyInfo().getPrice() <= 0) {
			mBook.getBuyInfo().setPrice(BookBuyInfo.BOOK_DEFAUTL_PRICE);
		}
		mHandler = new Handler();
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setTitle(R.string.pay_title);

		View content = LayoutInflater.from(mActivity).inflate(R.layout.vw_pay_dialog, mContentLayout);
		if (null != content) {
			initView();
			initData();
		}

	}

	// 是否有兑书卡
	private boolean isBookCard = false;

	@Override
	public void show() {
		if (LoginUtil.isValidAccessToken(mActivity) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			PriceTip tip = mBook.getBuyInfo().getPriceTip();

			if (tip != null && tip.getBuyType() == PriceTip.TYPE_DISCOUNT_NINE) {
				// 点击 立即用券
				isBookCard = true;
				DialogUtils.showProgressDialog(mActivity, "正在请求中...", true);
				// DialogUtils.showProgressDialog(mActivity, "正在请求中...", true,
				// false, new OnCancelListener() {
				// public void onCancel(DialogInterface dialog) {
				// }
				// }, new OnDismissListener() {
				// public void onDismiss(DialogInterface dialog) {
				// }
				// });

				buyBook(mBook);
			} else {
				// 点击 立即购买
				super.show();
			}
		} else {
			LoginDialog.launch(mActivity, new LoginStatusListener() {

				@Override
				public void onSuccess() {
					PayDialog.loginSuccess();
				}

				@Override
				public void onFail() {

				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pay_ok_btn:
			// 不可取消
			setCanceledOnTouchOutside(false);
			setCancelable(false);
			mCheckBox.setEnabled(false);
			mOkBtn.setEnabled(false);
			mCancelBtn.setEnabled(false);

			int payType = mBook.getBuyInfo().getPayType();
            if (payType == Book.BOOK_TYPE_VIP) {
                // 购买整本
                buyBook(mBook);
            } else if (payType == Book.BOOK_TYPE_CHAPTER_VIP) {
                // 购买章节
                buyChapter(mBook, mChapter);
            }
			
			mBook.getBuyInfo().setAutoBuy(mCheckBox.isChecked());
			break;

		case R.id.pay_cancel_btn:
			mPayDlg = null;
			dismiss();
			break;
		}
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		boolean isSuccess = false;
		if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
			if (taskResult.retObj instanceof ChapterPriceResult) {
				ChapterPriceResult result = (ChapterPriceResult) taskResult.retObj;
				updateChapter(result.getChapter());
				updateUserInfoRole(result.getUserInfoRole());

				if (mChapter.getPrice() != Chapter.CHAPTER_DEFAUTL_PRICE) {
					initData();
					isSuccess = true;
				} else {
					mProgressBar.setVisibility(View.GONE);
					mProgressText.setText(R.string.blance_change);
					mPriceView.setVisibility(View.GONE);
					mOkBtn.setEnabled(false);
				}

			} else if (taskResult.retObj instanceof BookPriceResult) {
				BookPriceResult result = (BookPriceResult) taskResult.retObj;

				updateBookInfo(result.getBook());
				updateUserInfoRole(result.getUserInfoRole());

				if (mBook.getBuyInfo().getPrice() != BookBuyInfo.BOOK_DEFAUTL_PRICE) {
					initData();
					isSuccess = true;
				} else {
					mProgressBar.setVisibility(View.GONE);
					mProgressText.setText(R.string.blance_change);
					mPriceView.setVisibility(View.GONE);
					mOkBtn.setEnabled(false);
					mOkBtnBg.setAlpha(100);
				}
			}
		}

		if (!isSuccess) {
			mProgressBar.setVisibility(View.GONE);
			mPriceView.setVisibility(View.GONE);
			mOkBtn.setEnabled(false);
			mOkBtnBg.setAlpha(100);
			// 判断一下网络连接情况
			// if (HttpUtil.isConnected(SinaBookApplication.gContext)) {
			mProgressText.setText(R.string.get_price_failed);
			// }
		}
	}

	@Override
	public void cancel() {
		mPayDlg = null;
		super.cancel();
	}

	/**
	 * 初始化对话框视图
	 */
	private void initView() {
		BitmapDrawable dotHDrawable;
		Bitmap dotHBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.divider_dot_virtual);
		dotHDrawable = new BitmapDrawable(mActivity.getResources(), dotHBitmap);
		dotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		dotHDrawable.setDither(true);

		mProgressBar = (ProgressBar) findViewById(R.id.pay_progress_bar);
		mProgressText = (TextView) findViewById(R.id.pay_progress_text);

		mPriceView = findViewById(R.id.pay_price_layout);
		mOriginalPrice = (TextView) findViewById(R.id.pay_original_price);
		mDiscountPrice = (TextView) findViewById(R.id.pay_discount_price);
		mReadTicket = (TextView) findViewById(R.id.pay_read_ticket);

		mCheckBoxView = findViewById(R.id.pay_checkbox_layout);
		mCheckBox = (CheckBox) findViewById(R.id.pay_checkbox);
		ImageView checkBoxDivider = (ImageView) findViewById(R.id.pay_checkbox_divider);
		checkBoxDivider.setBackgroundDrawable(dotHDrawable);

		mOkBtn = (TextView) findViewById(R.id.pay_ok_btn);
		mOkBtnBg = mOkBtn.getBackground();
		mOkBtn.setOnClickListener(this);
		mCancelBtn = (TextView) findViewById(R.id.pay_cancel_btn);
		mCancelBtn.setOnClickListener(this);

		mCardView = findViewById(R.id.pay_buy_by_bookcard_layout);
		mCardDividerView = findViewById(R.id.pay_buy_by_bookcard_top_divider);
		mCardTitleView = (TextView) findViewById(R.id.pay_buy_by_bookcard_text);

		// mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		// @Override
		// public void onCheckedChanged(CompoundButton compoundButton, boolean
		// isChecked) {
		// mBook.getBuyInfo().setAutoBuy(isChecked);
		// }
		// });
	}

	private void initData() {
		mCardView.setVisibility(View.GONE);
		if (LoginUtil.isValidAccessToken(mActivity) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			if (mBook != null) {
				int payType = mBook.getBuyInfo().getPayType();
				switch (payType) {
				case Book.BOOK_TYPE_VIP:
					initDataVIPBook();
					break;

				case Book.BOOK_TYPE_CHAPTER_VIP:
					initDataVIPChapter();
					break;

				default:
					break;
				}

				// 自动购买，默认选上
				mCheckBox.setChecked(true);
			}

		} else {
			dismiss();
			LoginDialog.launch(mActivity, new LoginStatusListener() {

				@Override
				public void onSuccess() {
					PayDialog.loginSuccess();
				}

				@Override
				public void onFail() {

				}
			});
		}
	}

	/**
	 * 整本购买的情况
	 */
	private void initDataVIPBook() {
		String format = mActivity.getString(R.string.pay_book);
		mProgressBar.setVisibility(View.GONE);
		mProgressText.setText(String.format(Locale.CHINA, format, mBook.getTitle()));

		mCheckBoxView.setVisibility(View.GONE);

		if (mBook.getBuyInfo().getPrice() == BookBuyInfo.BOOK_DEFAUTL_PRICE) {
			mProgressBar.setVisibility(View.VISIBLE);
			mProgressText.setText(R.string.get_price);
			mPriceView.setVisibility(View.GONE);
			mOkBtn.setEnabled(false);
			mOkBtnBg.setAlpha(100);

			reqBookPrice();
			return;
		}

		mProgressBar.setVisibility(View.GONE);
		mPriceView.setVisibility(View.VISIBLE);
		mOkBtn.setEnabled(true);
		mOkBtnBg.setAlpha(255);

		mDiscountPrice.setVisibility(View.GONE);
		mReadTicket.setVisibility(View.GONE);

		// 已经购买
		if (mBook.getBuyInfo().isHasBuy()) {
			int color = ResourceUtil.getColor(R.color.pay_dialog_price_font_color);
			String text = String.format(mActivity.getString(R.string.pay_price), 0.0);
			Spannable spannable = Util.highLight(text, color, 3, text.length());
			mOriginalPrice.setText(spannable);
			//
			dismiss();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					String ownerUid = LoginUtil.getLoginInfo().getUID();
					String bookAttachOwnerUid = mBook.getOwnerUid();
					if (bookAttachOwnerUid == null || !bookAttachOwnerUid.equals(ownerUid)) {
						mBook.setOwnerUid(ownerUid);
						DBService.saveBook(mBook);
					}
					mBook.getBuyInfo().setHasBuy(true);
					CloudSyncUtil.getInstance().add2Cloud(mContext, mBook);
					DownBookManager.getInstance().downBook(mBook, false, exchangeBookIsEpubValue);
					doFinish(CODE_SUCCESS);
					// 发生余额变化
					LoginUtil.reqBalance(SinaBookApplication.gContext);
				}
			}, 250);
		} else {
			double price = mBook.getBuyInfo().getPrice();
			double disPrice = mBook.getBuyInfo().getDiscountPrice();
			PriceTip priceTip = mBook.getBuyInfo().getPriceTip();

			updatePriceView(price, disPrice, priceTip);
		}
	}

	/**
	 * 章节收费情况
	 */
	private void initDataVIPChapter() {
		String format = mActivity.getString(R.string.pay_chapter);
		mProgressBar.setVisibility(View.GONE);
		mProgressText.setText(String.format(Locale.CHINA, format, mChapter.getTitle()));

		if (mChapter.getPrice() != Chapter.CHAPTER_DEFAUTL_PRICE) {
			// 已经有章节的价格信息和登录信息
			mPriceView.setVisibility(View.VISIBLE);
			mDiscountPrice.setVisibility(View.GONE);
			mReadTicket.setVisibility(View.GONE);

			// 已经购买
			if (mChapter.hasBuy()) {
				int color = ResourceUtil.getColor(R.color.pay_dialog_price_font_color);
				String text = String.format(mActivity.getString(R.string.pay_price), 0.0);
				Spannable spannable = Util.highLight(text, color, 3, text.length());
				mOriginalPrice.setText(spannable);
				// 已经购买了为啥还要显示价格嘞？
				// 直接回去下载
				dismiss();
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mChapter.setHasBuy(true);
						doFinish(CODE_SUCCESS);
						// 发生余额变化
						LoginUtil.reqBalance(SinaBookApplication.gContext);
					}
				}, 250);
			} else {
				updatePriceView(mChapter.getPrice(), mChapter.getDiscountPrice(), mChapter.getPriceTip());
			}

			mProgressBar.setVisibility(View.GONE);
			mCheckBoxView.setVisibility(View.VISIBLE);
			mOkBtn.setEnabled(true);
			mOkBtnBg.setAlpha(255);

		} else {
			// 没有则去请求价格
			mPriceView.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
			mProgressText.setText(R.string.get_price);
			mCheckBoxView.setVisibility(View.GONE);
			mOkBtn.setEnabled(false);
			mOkBtnBg.setAlpha(100);

			reqChapterPrice();
		}
	}

	/**
	 * 更新价格Vie
	 * 
	 * @param price
	 *            原价格
	 * @param disPrice
	 *            折扣价
	 * @param priceTip
	 */
	private void updatePriceView(double price, double disPrice, PriceTip priceTip) {
		int color = ResourceUtil.getColor(R.color.pay_dialog_price_font_color);

		String text = String.format(mActivity.getString(R.string.pay_price), price);
		Spannable spannable = Util.highLight(text, color, 3, text.length());
		mOriginalPrice.setText(spannable);

		boolean isBuyByBookCard = false;

		if (null != priceTip && priceTip.isPriceShow()) {
			mOriginalPrice
					.setText(String.format(mActivity.getString(R.string.pay_original_price), priceTip.getPrice()));
			// 删除线效果
			mOriginalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
			mDiscountPrice.setVisibility(View.VISIBLE);

			String discount = "";
			switch (priceTip.getBuyType()) {
			case PriceTip.TYPE_DISCOUNT_ONE:
			case PriceTip.TYPE_DISCOUNT_FIVE:
				discount = String.format(ResourceUtil.getString(R.string.pay_discount_price), mRole.getRoleName());
				break;

			case PriceTip.TYPE_DISCOUNT_TWO:
			case PriceTip.TYPE_DISCOUNT_THREE:
			case PriceTip.TYPE_DISCOUNT_SIX:
			case PriceTip.TYPE_DISCOUNT_SEVEN:
				discount = ResourceUtil.getString(R.string.pay_special_price);
				break;

			case PriceTip.TYPE_DISCOUNT_FOUR:
				// 暂时没什么作用
				break;
			case PriceTip.TYPE_DISCOUNT_EIGHT:
				isBuyByBookCard = true;
				// 1.购买：《XXX》全本
				String proText = mProgressText.getText().toString();
				Spannable proSpannable = Util.highLight(proText, color, 3, proText.length());
				mProgressText.setText(proSpannable);
				// 2.原价：X.X阅读币
				// 上面的代码逻辑已经完成了2步骤
				// 3.您的赠书卡特权：免费
				String tipText = ResourceUtil.getString(R.string.pay_by_bookcard_tip);
				Spannable tipSpannable = Util.highLight(tipText, color, tipText.length() - 2, tipText.length());
				mDiscountPrice.setText(tipSpannable);
				// 小点点图片
				Bitmap dotHBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.list_divide_dot);
				BitmapDrawable mDotHDrawable = new BitmapDrawable(mContext.getResources(), dotHBitmap);
				mDotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
				mDotHDrawable.setDither(true);
				mCardDividerView.setBackgroundDrawable(mDotHDrawable);
				// 您可使用的赠书卡名称
				if (priceTip.isTipShow()) {
					String cardTitleTip = String.format(ResourceUtil.getString(R.string.pay_by_bookcard_title),
							priceTip.getShowTip());
					mCardTitleView.setText(cardTitleTip);
				}
				mCardView.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}

			if (!isBuyByBookCard) {
				mDiscountPrice.setText(discount);
				String discountPrice = String.format(ResourceUtil.getString(R.string.pay_price_text),
						priceTip.getDiscountPrice());
				Spanned spanned = Util.highLight(discountPrice, color, 0, discountPrice.length());
				mDiscountPrice.append(spanned);
			}
		}

		if (null != priceTip && priceTip.isTipShow()) {
			mReadTicket.setVisibility(View.VISIBLE);
			String tipText = priceTip.getShowTip();
			if (!isBuyByBookCard && !TextUtils.isEmpty(tipText)) {
				mReadTicket.setText(tipText);
			} else {
				mReadTicket.setVisibility(View.GONE);
			}
		} else {
			mReadTicket.setVisibility(View.GONE);
		}
	}

	/**
	 * 更新章节列表信息
	 */
	private void updateChapter(Chapter chapter) {
		if (chapter != null) {
			if (mChapter.equals(chapter)) {
				mChapter.setPrice(chapter.getPrice());
				mChapter.setDiscountPrice(chapter.getDiscountPrice());
				mChapter.setHasBuy(chapter.hasBuy());
				mChapter.setPriceTip(chapter.getPriceTip());
			}
		}
	}

	/**
	 * 更新书籍信息
	 */
	private void updateBookInfo(Book book) {
		if (book != null) {
			mBook.setBookId(book.getBookId());
			mBook.getBuyInfo().setPayType(book.getBuyInfo().getPayType());
			mBook.getBuyInfo().setPrice(book.getBuyInfo().getPrice());
			mBook.getBuyInfo().setDiscountPrice(book.getBuyInfo().getDiscountPrice());
			mBook.getBuyInfo().setHasBuy(book.getBuyInfo().isHasBuy());
			mBook.getBuyInfo().setPriceTip(book.getBuyInfo().getPriceTip());
		}
	}

	private void updateUserInfoRole(UserInfoRole role) {
		if (role != null) {
			mRole = role;
		}
	}

	private void reqChapterPrice() {
		String reqUrl = String.format(ConstantData.URL_GET_SINGLE_CHAPTER_PRICE, mBook.getBookId(), mBook.getSid(),
				mBook.getBookSrc(), mChapter.getGlobalId());
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new ChapterPriceParser());
		reqTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
	}

	private void reqBookPrice() {
		String reqUrl = String.format(ConstantData.URL_BOOK_INFO_CHECK, mBook.getBookId(), mBook.getSid(),
				mBook.getBookSrc());
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new BookPriceParser());
		reqTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
	}

	/**
	 * 显示余额不足的对话框
	 * 
	 * @param context
	 */
	public static void showBalanceDlg(final Context context) {
		String balance = "0.00";
		if (LoginUtil.isValidAccessToken(context) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			balance = LoginUtil.getLoginInfo().getBalance();
		}
		String msg = String.format(context.getString(R.string.balance), balance);

		CommonDialog.show(context, msg, new CommonDialog.DefaultListener() {
			@Override
			public void onRightClick(DialogInterface dialog) {
				Intent intent = new Intent(context, RechargeCenterActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				context.startActivity(intent);
			}
		});
	}

	/**
	 * 显示余额不足的对话框
	 * 
	 * @param context
	 * @param price
	 */
	public static void showBalanceDlg(final Context context, final double price) {
		String balance = "";
		if (LoginUtil.isValidAccessToken(context) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			balance = LoginUtil.getLoginInfo().getBalance();
		}

		final double needPrice = price - Double.parseDouble(balance);
		String msg = String.format(context.getString(R.string.balance), balance);
		CommonDialog.show(context, msg, new CommonDialog.DefaultListener() {
			@Override
			public void onRightClick(DialogInterface dialog) {
				// 1、携带余额直接进入充值过去
				//				Intent intent = new Intent(context, RechargeCenterActivity.class);
				//				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				//				intent.putExtra("amount", String.valueOf(needPrice * 100));
				//				// intent.putExtra("amount", String.format("%.2f", needPrice));
				//				context.startActivity(intent);
				// 2、直接跳转到充值前的选择金额的界面
				Intent intent = new Intent(context, RechargeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				context.startActivity(intent);
			}
		});
	}

	/**
	 * 购买整本书
	 * 
	 * @param book
	 */
	public void buyBook(Book book) {
		if (book != null) {
			// 购买全本时，首先查找是否数据库中已经有该书了，如果有，则使用数据库中的book对象
			boolean hasBook = DownBookManager.getInstance().hasBook(book);
			if (hasBook) {
				book = DownBookManager.getInstance().getBook(book);
				book.setAutoDownBook(false);
				// shelfBook.exchangeBookContentType(book);
				// book = shelfBook;
				// 再次确认绑定用户ID
				// Log.e("PayDialog", "数据库中已经有该书了，使用数据库中的book对象");
				if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					// Log.e("PayDialog",
					// "数据库中已经有该书了，使用数据库中的book对象 >> accessToken有效");
					String ownerUid = LoginUtil.getLoginInfo().getUID();
					// Log.e("PayDialog",
					// "数据库中已经有该书了，使用数据库中的book对象 >> accessToken有效 >> 登录用户:"
					// +
					// ownerUid);
					String bookAttachOwnerUid = book.getOwnerUid();
					// Log.e("PayDialog",
					// "数据库中已经有该书了，使用数据库中的book对象 >> accessToken有效 >> 书籍绑定:"
					// +
					// bookAttachOwnerUid);
					if (bookAttachOwnerUid == null || !bookAttachOwnerUid.equals(ownerUid)) {
						// Log.e("PayDialog",
						// "数据库中已经有该书了，使用数据库中的book对象 >> accessToken有效 >> 同步");
						book.setOwnerUid(ownerUid);
						DBService.saveBook(book);
					}
				} else {
					// Log.e("PayDialog",
					// "数据库中已经有该书了，使用数据库中的book对象 >> accessToken为空或者已无效");
				}
			} else {
				//
				// Log.e("PayDialog", "数据库中已经没有该书");
			}

			final Book localBook = book;

			if (mProgressBar != null) {
				mProgressBar.setVisibility(View.VISIBLE);
			}
			if (mProgressText != null) {
				mProgressText.setText(R.string.buy_text);
			}

			BuyBookManager.getInstance().buyBook(book, null, new IBuyBookListener() {
				@Override
				public void onFinish(int state) {
					mPayDlg = null;
					dismiss();
					if (isBookCard) {
						DialogUtils.dismissProgressDialog();
					}

					final int stateCode = state;
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							switch (stateCode) {
							case BuyBookManager.STATE_SUCCESS:
								localBook.getBuyInfo().setHasBuy(true);
								// 修复问题情景：
								// 当该全本收费的书籍未加入书架，用户直接在摘要页点击购买全本，
								// 购买成功后未将该书同步到服务器，导致用户退出账户时该书籍仍然存在书架之中
								// 因此这里在下载书籍之前增加绑定UID到该书籍并且同步书籍到服务器的操作
								CloudSyncUtil.getInstance().add2Cloud(mContext, localBook);
								DownBookManager.getInstance().downBook(localBook, false, exchangeBookIsEpubValue);
								doFinish(CODE_SUCCESS);

								// 发生余额变化
								LoginUtil.reqBalance(SinaBookApplication.gContext);
								
								//TODO: ouyang 上传学朋结果
								reqTicketUsed(mBook.getBookId(), true);
								break;

							case BuyBookManager.STATE_RECHARGE:
								doFinish(CODE_ERROR);

								double price = mBook.getBuyInfo().getPrice();
								showBalanceDlg(mContext, price);
								break;

							default:
								doFinish(CODE_ERROR);
								CommonDialog.show(mContext, R.string.buy_failed, new CommonDialog.DefaultListener());
								break;
							}

							isBookCard = false;
						}
					}, 350);
				}
			});
		}
	}

	/**
	 * 购买章节
	 * 
	 * @param book
	 * @param chapter
	 */
	public void buyChapter(Book book, final Chapter chapter) {
		if (book != null) {
			/*
			 * // 设置是否自动购买 Book localBook =
			 * DownBookManager.getInstance().getBook(book); if (null !=
			 * localBook) {
			 * localBook.getBuyInfo().setAutoBuy(mCheckBox.isChecked()); }
			 */

			// Log.e("PayDialog", "buyChapter >> book:" + book);
			boolean hasBook = DownBookManager.getInstance().hasBook(book);
			if (hasBook) {
				Book shelfBook = DownBookManager.getInstance().getBook(book);
				shelfBook.exchangeBookContentType(book);
				book = shelfBook;
				book.getBuyInfo().setAutoBuy(mCheckBox.isChecked());

				// 再次确认绑定用户ID
				// Log.e("PayDialog", "数据库中已经有该书了，使用数据库中的book对象");
				if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					// Log.e("PayDialog",
					// "数据库中已经有该书了，使用数据库中的book对象 >> accessToken有效");
					String ownerUid = LoginUtil.getLoginInfo().getUID();
					// Log.e("PayDialog",
					// "数据库中已经有该书了，使用数据库中的book对象 >> accessToken有效 >> 登录用户:" +
					// ownerUid);
					String bookAttachOwnerUid = book.getOwnerUid();
					// Log.e("PayDialog",
					// "数据库中已经有该书了，使用数据库中的book对象 >> accessToken有效 >> 书籍绑定:" +
					// bookAttachOwnerUid);
					if (bookAttachOwnerUid == null || !bookAttachOwnerUid.equals(ownerUid)) {
						// Log.e("PayDialog",
						// "数据库中已经有该书了，使用数据库中的book对象 >> accessToken有效 >> 同步");
						book.setOwnerUid(ownerUid);
						DBService.saveBook(book);
					}
				} else {
					// Log.e("PayDialog",
					// "数据库中已经有该书了，使用数据库中的book对象 >> accessToken为空或者已无效");
				}
			} else {
				//
				// Log.e("PayDialog", "数据库中已经没有该书");
			}

			mProgressBar.setVisibility(View.VISIBLE);
			mProgressText.setText(R.string.buy_text);

			BuyBookManager.getInstance().buyBook(book, chapter, new IBuyBookListener() {
				@Override
				public void onFinish(int state) {
					mPayDlg = null;
					dismiss();

					final int stateCode = state;
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							switch (stateCode) {
							case BuyBookManager.STATE_SUCCESS:
								mChapter.setHasBuy(true);
								doFinish(CODE_SUCCESS);

								// 发生余额变化
								LoginUtil.reqBalance(SinaBookApplication.gContext);
								break;

							case BuyBookManager.STATE_RECHARGE:
								mChapter.setState(Chapter.CHAPTER_NEED_BUY);
								doFinish(CODE_ERROR);
								// TODO: 传入数值是章节费用还是书本费用，按章收费书本可能费用为0
								double price = chapter.getPrice();
								// double price = mBook.getBuyInfo().getPrice();
								showBalanceDlg(mContext, price);
								// showBalanceDlg(mContext);
								break;

							default:
								mChapter.setState(Chapter.CHAPTER_NEED_BUY);
								doFinish(CODE_ERROR);
								CommonDialog.show(mContext, R.string.buy_failed, new CommonDialog.DefaultListener());
								break;
							}
						}
					}, 350);
				}
			});
		}
	}

	private void doFinish(int code) {
		if (mPayFinishListener != null) {
			mPayFinishListener.onFinish(code);
		}
	}

	public void setOnPayFinishListener(PayFinishListener listener) {
		this.mPayFinishListener = listener;
	}

	public void setOnPayLoginSuccessListener(PayLoginSuccessListener listener) {
		this.mPayLoginListener = listener;
	}

	public interface PayFinishListener {
		public void onFinish(int code);
	}

	public interface PayLoginSuccessListener {
		public void onLoginSuccess();
	}
	
	// 上传阅读券使用记录
	private static int countDown = 0;
	public static void reqTicketUsed(final String bookId, boolean isInit){
		if(isInit){
			countDown = 0;
		}
		
		String uid = LoginUtil.getLoginInfo().getUID();
		
		// “4”标识 客户端
		String channel = "4";
		
		StringBuilder signFormat = new StringBuilder();
		signFormat.append(bookId);
		signFormat.append(channel);
		signFormat.append(uid);
		signFormat.append("fbQcd9GUEsj6Oe48IJxv");
		String key = URLUtil.md5Signature(signFormat.toString());
		
		String url = String.format(ConstantData.TICKET_TO_USED, uid, bookId, channel, key);
		RequestTask ticketReq = new RequestTask(new TicketToUsedParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, url);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		ticketReq.setTaskFinishListener(new ITaskFinishListener() {
			public void onTaskFinished(TaskResult taskResult) {
				if (taskResult.retObj instanceof String) {
					String code = (String)taskResult.retObj;
					if("2".equals(code)){
						/*
						 * 返回2表示写入错误，接着刷新
						 */
						if(countDown < 3){
							reqTicketUsed(bookId, false);
						}
						countDown++;
					}
				}
			}
		});
		ticketReq.execute(params);
	}
	
}
