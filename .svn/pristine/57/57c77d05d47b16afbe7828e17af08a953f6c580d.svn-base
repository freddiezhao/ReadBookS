package com.sina.book.ui.view;

import java.util.List;

import org.apache.http.HttpStatus;
import org.htmlcleaner.Utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.BookPriceResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.MainBookItem;
import com.sina.book.data.MainBookResult;
import com.sina.book.data.PriceTip;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.data.util.PurchasedBookList;
import com.sina.book.image.IImageLoadListener;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.BookPriceParser;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.ReadActivity;
import com.sina.book.ui.adapter.MainTabAdapter;
import com.sina.book.ui.widget.ImageFlowIndicator;
import com.sina.book.ui.widget.ViewFlow;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;

/**
 * Class MainBottomView.
 * 
 * @author MarkMjw
 * @date 13-12-27
 */
public class MainBottomView extends RelativeLayout {
	/** 播放时间间隔 */
	private final int UPDATE_TIME = 10000;

	private Context mContext;

	private View mDragView;

	// 书籍类型(左上角角标处视图，当前只有两种类型，1.今日免费， 2.好书推荐)
	private View mBookTypeView;

	// 拖拽视图区
	private View mDragTitleLayout;// 容器
	private TextView mDragTitle;// 推荐类型
	private TextView mDragBook;// 书籍名称
	private TextView mDragRecommend;// 推荐书籍推荐理由

	// 推荐书籍详细信息
	private LinearLayout mBookLayout;// 容器
	private TextView mBookLayoutTitle;// 推荐类型
	private ImageView mBookCover;// 封面
	private TextView mBookCoverText;// 封面标题
	private TextView mBookTitle;// 标题
	private TextView mBookAuthor;// 作者
	private TextView mBookContent;// 推荐理由

	private TextView mReadBtn;// 阅读按钮
	private TextView mDownBtn;// 下载按钮

	// 轮播视图区
	private ViewFlow mViewFlow;// 轮播控件
	private MainTabAdapter mViewFlowAdapter;// 轮播控件适配器

	private Book mBook;

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (DownBookManager.ACTION_INTENT_DOWNSTATE.equals(intent.getAction())) {
				updateBtnState();
			}
		}
	};

	public MainBottomView(Context context) {
		this(context, null);
	}

	public MainBottomView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	private void init(Context context) {
		mContext = context;
		LayoutInflater.from(mContext).inflate(R.layout.vw_main_bottom_layout, this);
		// setBackgroundResource(R.drawable.bg_nothing);
		initView();
		initListener();
	}

	public void onStart() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(DownBookManager.ACTION_INTENT_DOWNSTATE);
		mContext.registerReceiver(mReceiver, myIntentFilter);

		mViewFlow.startAutoFlow(UPDATE_TIME);

		updateBtnState();
	}

	public void onStop() {
		mContext.unregisterReceiver(mReceiver);

		mViewFlow.stopAutoFlow();
	}

	public View getDragView() {
		return mDragView;
	}

	public View getDragTitle() {
		return mDragTitleLayout;
	}

	private void initView() {
		mDragView = findViewById(R.id.drag_layout);
		mBookTypeView = findViewById(R.id.book_book_type);
		mDragTitleLayout = findViewById(R.id.drag_title_layout);
		mDragTitle = (TextView) findViewById(R.id.drag_title_text);
		mDragBook = (TextView) findViewById(R.id.drag_title_content);
		mDragRecommend = (TextView) findViewById(R.id.drag_title_recommend);

		mBookLayout = (LinearLayout) findViewById(R.id.book_layout);
		mBookLayoutTitle = (TextView) findViewById(R.id.book_layout_title);
		mBookCover = (ImageView) findViewById(R.id.book_cover);
		mBookCoverText = (TextView) findViewById(R.id.book_cover_text);
		mBookTitle = (TextView) findViewById(R.id.book_title);
		mBookAuthor = (TextView) findViewById(R.id.book_author);
		mBookContent = (TextView) findViewById(R.id.book_content);
		mReadBtn = (TextView) findViewById(R.id.book_read_btn);
		mDownBtn = (TextView) findViewById(R.id.book_down_btn);
		
		mViewFlowAdapter = new MainTabAdapter(mContext);
		mViewFlow = (ViewFlow) findViewById(R.id.books_view_flow);
		ImageFlowIndicator indicator = (ImageFlowIndicator) findViewById(R.id.books_flow_indicator);
		mViewFlow.setFlowIndicator(indicator);
		mViewFlow.setAdapter(mViewFlowAdapter);
	}

	private void updateDragView(MainBookResult mainBookInfo) {
		Book book = mainBookInfo.getEditorRecommend(true);
		if (null != book) {
			mDragTitle.setText(book.getComment());

			if (!TextUtils.isEmpty(book.getTitle())) {
				mDragBook.setText("《" + book.getTitle() + "》");

				if (!TextUtils.isEmpty(book.getAuthor())) {
					mDragBook.append("-" + book.getAuthor());
				}
			}

			if (null != book.getRecommendIntro()) {
				mDragRecommend.setText(book.getRecommendIntro());
				// mDragRecommend.setVisibility(View.VISIBLE);
			} else {
				mDragRecommend.setVisibility(View.GONE);
			}
		}
	}

	public void updateData(MainBookResult result) {
		updateDragView(result);

		// 更新编辑推荐
		mBook = result.getEditorRecommend(false);
		if (null != mBook) {
			//
			String commentTxt = mBook.getComment();
			if (!TextUtils.isEmpty(commentTxt)) {
				if ("今日免费".equals(commentTxt)) {
					mBookTypeView.setBackgroundResource(R.drawable.main_free_icon);
				} else if ("好书推荐".equals(commentTxt)) {
					mBookTypeView.setBackgroundResource(R.drawable.main_recommand_icon2);
				}
			}
			mBookLayoutTitle.setText(mBook.getComment());
			ImageLoader.getInstance().load(mBook.getDownloadInfo().getImageUrl(), mBookCover, ImageLoader.TYPE_BIG_PIC,
					ImageLoader.getDefaultPic(), new IImageLoadListener() {

						@Override
						public void onImageLoaded(Bitmap bm, ImageView imageView, boolean loadSuccess) {
							if (loadSuccess) {
								mBookCoverText.setText("");
							} else {
								// 如果image url加载失败，默认书皮上会显示书籍标题
								if (mBook != null) {
									mBookCoverText.setText(mBook.getTitle());
								}
							}
						}
					});

			mBookLayoutTitle.setText(mBook.getComment());
			mBookTitle.setText(mBook.getTitle());
			mBookAuthor.setText(mBook.getAuthor());
			mBookContent.setText(mBook.getRecommendIntro());

			updateBtnState();
		}

		// 更新书城tab信息
		List<MainBookItem> books = result.getMainBooks();
		if (null != books) {
			mViewFlowAdapter.setData(books);
			mViewFlowAdapter.notifyDataSetChanged();
		}
	}

	private void initListener() {
		// setOnTouchListener(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// // 屏蔽不需要的touch事件
		// return true;
		// }
		// });

		mBookLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (null != mBook) {
					BookDetailActivity.launchNew(mContext, mBook, "书架_推荐Pannel_01");
				}
			}
		});

		mReadBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (null == mBook) {
					return;
				}
				ReadActivity.setChapterReadEntrance("书架-推荐Pannel");
				Book book = DownBookManager.getInstance().getBook(mBook);
				if (book != null && Math.abs(book.getDownloadInfo().getProgress() - 1.0) < 0.0001
						&& book.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED) {
					// 如果是读的书架上的下载好的书，需要更新该book的最后阅读时间等属性
					mBook = book;
					ReadActivity.launch(mContext, mBook, false, false);
				} else {
					// 否则认为是在线试读
					ReadActivity.launch(mContext, mBook, true, false);
				}
				// postDelayed(new Runnable() {
				//
				// @Override
				// public void run() {
				//
				// }
				// }, 100);

			}
		});

		mDownBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onDownBtnClick();
			}
		});
	}

	private void onDownBtnClick() {
		if (null == mBook) {
			return;
		}

		if (mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE
				|| mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP) {

			CloudSyncUtil.getInstance().add2Cloud(mContext, mBook);
			DownBookManager.getInstance().downBook(mBook);
			updateBtnState();

		} else {
			if (!mBook.getBuyInfo().isHasBuy()) {
				PayDialog dlg = new PayDialog((Activity) mContext, mBook, null, null);
				dlg.setOnPayLoginSuccessListener(new PayDialog.PayLoginSuccessListener() {

					@Override
					public void onLoginSuccess() {
						if (mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
							reqBookPrice();
							updateBtnState();
						}
					}
				});
				dlg.setOnPayFinishListener(new PayDialog.PayFinishListener() {
					@Override
					public void onFinish(int code) {
						if (PayDialog.CODE_SUCCESS == code) {
							updateBtnState();

							// TODO: 兑书券订购成功，弹框提示
							PriceTip tip = mBook.getBuyInfo().getPriceTip();
							if (tip != null && tip.getBuyType() == PriceTip.TYPE_DISCOUNT_NINE) {
								double price = mBook.getBuyInfo().getPrice();
								String str_price = String.valueOf(price);

								String str_bookcard = mContext.getString(R.string.buy_bookcard_success);
								str_bookcard = String.format(str_bookcard, str_price);

								int color = ResourceUtil.getColor(R.color.pay_dialog_price_font_color);
								SpannableString spanString = new SpannableString(str_bookcard);
								int end = str_bookcard.length() - 3;
								int start = end - str_price.length();
								ForegroundColorSpan span = new ForegroundColorSpan(color);
								spanString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

								String left = mContext.getString(R.string.iknowed);
								CommonDialog.show(mContext, null, spanString, left, null,
										new CommonDialog.DefaultListener());
							} else {
								Toast.makeText(mContext, R.string.buy_success, Toast.LENGTH_LONG).show();
							}

							CloudSyncUtil.getInstance().add2Cloud(mContext, mBook);
						}
					}
				});
				dlg.show();
			} else {
				DownBookManager.getInstance().downBook(mBook);
			}
		}
	}

	private void updateBtnState() {
		if (mBook == null) {
			return;
		}
		
		if(mBook.isEpub() || mBook.isHtmlRead()){
			//TODO: epub资源直接进入摘要页，屏蔽掉 试读和订购的接口
			
			// 去掉按钮后，底部太空旷，将内容栏下移
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
					LinearLayout.LayoutParams.WRAP_CONTENT);
			int px = PixelUtil.dp2px(16);
			param.setMargins(0, px, 0, 0);
			mBookContent.setLayoutParams(param);
			
			mReadBtn.setVisibility(View.GONE);
			mDownBtn.setVisibility(View.GONE);
		}else{
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
					LinearLayout.LayoutParams.WRAP_CONTENT);
			int px = PixelUtil.dp2px(4);
			param.setMargins(0, px, 0, 0);
			mBookContent.setLayoutParams(param);
			
			mReadBtn.setVisibility(View.VISIBLE);
			mDownBtn.setVisibility(View.VISIBLE);
			Book book = DownBookManager.getInstance().getBook(mBook);
			DownBookJob job = DownBookManager.getInstance().getJob(mBook);
			boolean hasBook = (job != null);
			
			// 阅读按钮状态
			String readText;
			if (hasBook && (job.getState() == DownBookJob.STATE_FINISHED) && book != null && !book.isOnlineBook()) {
				readText = mContext.getString(R.string.main_has_read);
			} else {
				readText = mContext.getString(R.string.main_read);
			}
			mReadBtn.setText(readText);
			
			// 下载，购买按钮状态
			String buyText = "";
			boolean enableBuyButton = true;
			
			// 1 更新下载状态
			if (hasBook) {
				if (book != null) {
					mBook.getDownloadInfo().setDownLoadState(book.getDownloadInfo().getDownLoadState());
					mBook.getDownloadInfo().setProgress(book.getDownloadInfo().getProgress());
				}
			}
			
			// 2 设置按钮
			int payType = mBook.getBuyInfo().getPayType();
			switch (payType) {
			case Book.BOOK_TYPE_FREE:
				buyText = mContext.getString(R.string.main_download);
				if (hasBook) {
					if (mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
							|| mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
						buyText = String.format(mContext.getString(R.string.downloading_txt));
						enableBuyButton = false;
					} else if (book != null && !book.isOnlineBook()) {
						buyText = String.format(mContext.getString(R.string.has_down));
						enableBuyButton = false;
					}
				}
				break;
				
			case Book.BOOK_TYPE_CHAPTER_VIP:
				buyText = mContext.getString(R.string.main_download);
				if (hasBook) {
					if (mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
							|| mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
						buyText = String.format(mContext.getString(R.string.downloading_txt));
						enableBuyButton = false;
					} else if (book != null && !book.isOnlineBook()) {
						buyText = String.format(mContext.getString(R.string.has_down));
						enableBuyButton = false;
					}
				}
				break;
				
			case Book.BOOK_TYPE_VIP:
				// 全本收费
				boolean hasBuy = PurchasedBookList.getInstance().isBuy(mBook) || mBook.getBuyInfo().isHasBuy()
				|| DownBookManager.getInstance().hasBuy(mBook);
				mBook.getBuyInfo().setHasBuy(hasBuy);
				
				if (hasBuy) {
					buyText = mContext.getString(R.string.main_download);
					if (hasBook) {
						if (mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
								|| mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
							buyText = String.format(mContext.getString(R.string.downloading_txt));
							enableBuyButton = false;
						} else if (book != null && !book.isOnlineBook()) {
							buyText = String.format(mContext.getString(R.string.has_down));
							enableBuyButton = false;
						}
					}
				} else {
					PriceTip tip = mBook.getBuyInfo().getPriceTip();
					if (tip != null && tip.getBuyType() == PriceTip.TYPE_DISCOUNT_NINE) {
						// 立即用券
						buyText = mContext.getString(R.string.book_detail_usefee);
					} else {
						// 立即购买
						buyText = mContext.getString(R.string.book_detail_buy);
					}
				}
				break;
				
			default:
				break;
			}
			
			// 3 更新
			mDownBtn.setEnabled(enableBuyButton);
			mDownBtn.setText(buyText);
		}
	}

	private void reqBookPrice() {
		String reqUrl = String.format(ConstantData.URL_BOOK_INFO_CHECK, mBook.getBookId(), mBook.getSid(),
				mBook.getBookSrc());
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new BookPriceParser());
		reqTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (null != taskResult && taskResult.stateCode == HttpStatus.SC_OK) {
					if (taskResult.retObj instanceof BookPriceResult) {
						BookPriceResult result = (BookPriceResult) taskResult.retObj;
						Book book = result.getBook();
						updateBookPrice(book);
						updateBtnState();
					}
				}
			}
		});
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
	}

	private void updateBookPrice(Book book) {
		if (null != mBook && null != book) {
			mBook.getBuyInfo().setPrice(book.getBuyInfo().getPrice());
			mBook.getBuyInfo().setDiscountPrice(book.getBuyInfo().getDiscountPrice());
			mBook.getBuyInfo().setPriceTip(book.getBuyInfo().getPriceTip());
		}
	}
}
