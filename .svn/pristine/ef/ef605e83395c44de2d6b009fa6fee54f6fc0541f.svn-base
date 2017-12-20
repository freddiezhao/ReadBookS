package com.sina.book.data.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.GenericTask;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.ChannelActivityInfo;
import com.sina.book.data.ConstantData;
import com.sina.book.data.GeneralActivityInfo;
import com.sina.book.image.IImageLoadListener;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.ChannelActivityPaser;
import com.sina.book.parser.GeneralActivityParser;
import com.sina.book.parser.IParser;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.widget.CustonTipDialog;
import com.sina.book.ui.widget.GeneralActivityDialog;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.StorageUtil;

/**
 * 渠道活动工具类
 * 
 * @author chenjianli
 * @data 2014-01-14
 * 
 */
public class ChannelActivityUtil {

	private static RequestTask mActivityTask;
	private static RequestTask mGeneralActivityTask;

	// private static LinkedList<RequestTask> mRequestTasksList = new
	// LinkedList<RequestTask>();

	public static void start() {
		start(false, true, true, null, null, null);
	}

	public static void start(String uid) {
		start(false, true, true, null, null, uid);
	}

	public static void start(OnDismissListener dismissListener, String uid) {
		start(false, true, true, null, dismissListener, uid);
	}

	public static void start(boolean cancelable, boolean cancelOutside, OnCancelListener cancelListener,
			OnDismissListener dismissListener, String uid) {
		start(false, cancelable, cancelOutside, cancelListener, dismissListener, uid);
	}

	public static void start(boolean checkUserLoginState, final boolean cancelable, final boolean cancelOutside,
			final OnCancelListener cancelListener, final OnDismissListener dismissListener, String uid) {
		// 放行条件
		// 1.不检查登录状态
		// 2.检查登录状态并且用户已登录
		if (!checkUserLoginState
				|| LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			// 已经在请求了
			if (mActivityTask != null) {
				Object extra = mActivityTask.getExtra();
				if (uid != null && extra != null && extra instanceof String) {
					String preUid = (String) extra;
					if (uid.equals(preUid)) {
						// 已经有相同UID用户在请求了
						return;
					} else {
						// 切换了账户，可以将之前的Listener置空
						mActivityTask.setTaskFinishListener(null);
					}
				}
			}

			mActivityTask = new RequestTask(new ChannelActivityPaser());
			TaskParams params = new TaskParams();
			String url = ConstantData.URL_CHANNEL_ACTIVITY;
			url = HttpUtil.setURLParams(url, ConstantData.ACCESS_TOKEN_KEY, LoginUtil.getLoginInfo().getAccessToken());
			params.put(RequestTask.PARAM_URL, url);
			params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
			mActivityTask.setExtra(uid);
			mActivityTask.setTaskFinishListener(new ITaskFinishListener() {

				public void onTaskFinished(TaskResult taskResult) {
					if (taskResult != null && taskResult.retObj instanceof ChannelActivityInfo) {
						// 获取当前登录的用户与请求任务关联的账户是否一致，一致才弹出中奖信息对话框
						String currLoginUid = LoginUtil.getLoginInfo().getUID();
						GenericTask task = taskResult.task;
						if (task != null && task instanceof RequestTask) {
							RequestTask reqTask = (RequestTask) task;
							String tagUid = (String) reqTask.getExtra();
							if (tagUid != null && currLoginUid != null) {
								if (currLoginUid.equals(tagUid)) {
									ChannelActivityInfo info = (ChannelActivityInfo) taskResult.retObj;
									if (info != null && info.getStateCode() == 0 && !TextUtils.isEmpty(info.getDesc())) {
										// 该用户中过了，记录一下，下次登录不再请求了
										StorageUtil.saveBoolean("channel_activity_user_" + currLoginUid, false);
										// 弹出对话框
										CustonTipDialog customTipDialog = new CustonTipDialog(SinaBookApplication
												.getTopActivity());
										customTipDialog.setCancelable(cancelable);
										customTipDialog.setCanceledOnTouchOutside(cancelOutside);
										customTipDialog.setOnCancelListener(cancelListener);
										customTipDialog.setOnDismissListener(dismissListener);
										customTipDialog.show(info.getDesc(), info.getSubDesc(), info.getButtonDesc());
										// 重新请求余额！
										LoginUtil.reqBalance(SinaBookApplication.gContext);
									}
								}
							}
						}
					}
					mActivityTask = null;
				}
			});
			mActivityTask.execute(params);
		}
	}

	public static void generalActivityCheck(final Activity activity) {
		if (ConstantData.getChannelCode(SinaBookApplication.gContext) == ConstantData.CHANNEL_FOOLSDAY) {
			if (mGeneralActivityTask != null) {
				mGeneralActivityTask.abort();
				mGeneralActivityTask.setTaskFinishListener(null);
				mGeneralActivityTask = null;
			}

			mGeneralActivityTask = new RequestTask(new GeneralActivityParser());
			TaskParams params = new TaskParams();
			String url = ConstantData.GENERAL_ACTIVITY;
			params.put(RequestTask.PARAM_URL, url);
			params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
			mGeneralActivityTask.setTaskFinishListener(new GeneralActivityTaskFinishedListener(activity));
			mGeneralActivityTask.execute(params);
		}

	}

	public static class GeneralActivityTaskFinishedListener implements ITaskFinishListener {

		private Activity activity;

		public GeneralActivityTaskFinishedListener(Activity activity) {
			this.activity = activity;
		}

		@Override
		public void onTaskFinished(TaskResult taskResult) {
			// TODO Auto-generated method stub
			if (taskResult != null && taskResult.retObj instanceof GeneralActivityInfo) {
				GenericTask task = taskResult.task;
				if (task != null && task instanceof RequestTask) {
					RequestTask reqTask = (RequestTask) task;
					IParser parser = reqTask.getParser();
					if (parser != null && "0".equals(parser.getCode())) {
						final GeneralActivityInfo info = (GeneralActivityInfo) taskResult.retObj;
						if (info != null) {
							String imgUrl = info.getImgUrl();
							final ArrayList<HashMap<String, String>> buttonLists = info.getButtons();
							// 图片资源和显示按钮的数据不为空的时候才有必要显示
							if (!TextUtils.isEmpty(imgUrl) && buttonLists != null && buttonLists.size() != 0) {
								// 判断actId和showNum
								try {
									int curActId = info.getActId();
									int preShowNum = StorageUtil.getInt("actId" + curActId, -1);
									if (preShowNum != -1 && preShowNum == 0) {
										return;
									}
									
									// TODO 延迟3秒 为了测试
//									Thread.sleep(5000);

									// final int fPreActId = preActId;
									final int fPreShowNum = preShowNum;
									// 在当前界面弹出
									final GeneralActivityDialog dialog = new GeneralActivityDialog(activity);
									final LinearLayout buttonsContainer = dialog.getButtonsContainer();
									final TextView subTitleView = dialog.getSubTitleView();
									ImageView imageView = dialog.getImageView();
									// imgUrl =
									// "http://book1.sina.cn/dpool/newbook/lottery/phone_input.php";
									ImageLoader.getInstance().load(imgUrl, imageView, ImageLoader.TYPE_BIG_PIC,
											ImageLoader.getGenaralActivityDefaultPic(),
											ImageLoader.getGenaralActivityDefaultPic(), new IImageLoadListener() {

												@Override
												public void onImageLoaded(Bitmap bm, ImageView imageView,
														boolean loadSuccess) {
													boolean shouldShowDialog = false;
													if (loadSuccess) {
														shouldShowDialog = true;
													} else {
														// 图片载入失败的话，显示文字标题吧
														if (!TextUtils.isEmpty(info.getSubTitle())) {
															shouldShowDialog = true;
															subTitleView.setVisibility(View.VISIBLE);
															subTitleView.setText(info.getSubTitle());
														}
													}

													// 服务器数据作为首个判断条件
													boolean isShow = info.isShow();
													if (isShow && shouldShowDialog) {
														// 成功展示
														// 本地存储必要数据
														int actId = info.getActId();
														int showNum = info.getShowNum();
														if (fPreShowNum != -1) {
															showNum = fPreShowNum;
														}
														showNum--;
														StorageUtil.saveInt("actId" + actId, showNum);

														for (int i = 0; i < buttonLists.size(); i++) {
															int childCount = buttonsContainer.getChildCount();

															final HashMap<String, String> buttonMap = buttonLists
																	.get(i);
															if (buttonMap.containsKey("type")
																	&& !TextUtils.isEmpty(buttonMap.get("type"))
																	&& buttonMap.containsKey("title")
																	&& !TextUtils.isEmpty(buttonMap.get("title"))) {
																LayoutInflater inflater = LayoutInflater
																		.from(SinaBookApplication.gContext);
																String type = buttonMap.get("type");
																String title = buttonMap.get("title");

																LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
																		0, PixelUtil.dp2px(37.33f), 1);

																// 积极的响应
																if ("1".equals(type)) {
																	TextView positiveBtn = (TextView) inflater.inflate(
																			R.layout.vm_general_btn_positive, null);
																	positiveBtn.setText(title);
																	positiveBtn
																			.setOnClickListener(new OnClickListener() {

																				@Override
																				public void onClick(View v) {
																					if (dialog != null) {
																						dialog.dismiss();
																					}
																					String webUrl = buttonMap
																							.get("url");
																					handlePositiveBtn(webUrl,
																							info.getTitle());
																				}
																			});
																	if (childCount >= 1) {
																		params.setMargins(PixelUtil.dp2px(20.00f), 0,
																				0, 0);
																	}
																	buttonsContainer.addView(positiveBtn, params);
																	// 消极的取消
																} else if ("-1".equals(type)) {
																	TextView negativeBtn = (TextView) inflater.inflate(
																			R.layout.vm_general_btn_negative, null);
																	negativeBtn.setText(title);
																	negativeBtn
																			.setOnClickListener(new OnClickListener() {

																				@Override
																				public void onClick(View v) {
																					if (dialog != null) {
																						dialog.dismiss();
																					}
																				}
																			});
																	if (childCount >= 1) {
																		params.setMargins(PixelUtil.dp2px(20.00f), 0,
																				0, 0);
																	}
																	buttonsContainer.addView(negativeBtn, params);
																}
															}
														}
														dialog.show();
													}
												}
											});
								} catch (Exception e) {
									LogUtil.d("GeneralActivityTaskFinishedListener", "e=" + e);
								}
							}
						}
					}
				}
			}
			mGeneralActivityTask = null;
		}

		private void handlePositiveBtn(final String webUrl, final String webTitle) {
			// 判断登录与否
			if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				// 拼接GSID等参数
				// RecommendWebUrlActivity.launch(SinaBookApplication.getTopActivity(),
				// webUrl, webTitle);
				LoginUtil.reqGsidAndEnterWebView(activity, webUrl, webTitle);
			} else {
				LoginDialog.launch(activity, new LoginStatusListener() {

					@Override
					public void onSuccess() {
						// 拼接GSID等参数
						LoginUtil.reqGsidAndEnterWebView(activity, webUrl, webTitle);
					}

					@Override
					public void onFail() {

					}
				});
			}
		}
	}
}
