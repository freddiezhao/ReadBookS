package com.sina.book.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;

import com.sina.book.R;

public class Dlg
{
	/**
	 * Shows an info message.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param msg
	 *            the message.
	 */
	public static void showInfo(Context context, CharSequence msg)
	{
		AlertDialog dlg = newDlg(context);
		dlg.setIcon(android.R.drawable.ic_dialog_info);
		dlg.setTitle(R.string.tip_title_info);
		dlg.setMessage(msg);
		dlg.show();
	}// showInfo()

	/**
	 * Shows an info message.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param msg
	 *            the message.
	 */
	public static void showInfo(Context context, CharSequence title, CharSequence msg)
	{
		showInfo(context, title, msg, -1);
	}// showInfo()

	/**
	 * Shows an info message.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param msg
	 *            the message.
	 */
	public static void showInfo(Context context, CharSequence title, CharSequence msg
			, int iconResId)
	{
		AlertDialog dlg = newDlg(context);
		if (iconResId > 0) {
			dlg.setIcon(iconResId);
		}
		if (!TextUtils.isEmpty(title)) {
			dlg.setTitle(title);
		}
		dlg.setMessage(msg);
		dlg.show();
	}// showInfo()

	public interface IOnClickListener
	{
		public abstract void onClick(DialogInterface dialog, boolean clickPositive, boolean clickNegative);
	}

	public static void showInfo(Context context, CharSequence title, CharSequence msg
			, int iconResId, int positiveResId, int negaticeResId, boolean cancelable,
			boolean canceledOnTouchOutside, final IOnClickListener listener)
	{
		AlertDialog.Builder builder = newDlgBuilder(context);
		if (iconResId != -1) {
			builder.setIcon(iconResId);
		}
		if (!TextUtils.isEmpty(title)) {
			builder.setTitle(title);
		}
		builder.setMessage(msg);

		if (positiveResId != -1) {
			builder.setPositiveButton(positiveResId, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					if (listener != null) {
						listener.onClick(dialog, true, false);
					}
				}
			});
		}

		if (negaticeResId != -1) {
			builder.setNegativeButton(negaticeResId, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					if (listener != null) {
						listener.onClick(dialog, false, true);
					}
				}
			});
		}

		AlertDialog dlg = builder.create();
		dlg.setCancelable(cancelable);
		dlg.setCanceledOnTouchOutside(canceledOnTouchOutside);

		dlg.show();
	}

	public static void showInfo(Context context, CharSequence title, CharSequence msg,
			int positiveResId, IOnClickListener listener)
	{
		showInfo(context, title, msg, -1, positiveResId, -1, true, false, listener);
	}

	public static void showInfo(Context context, CharSequence title, CharSequence msg,
			int positiveResId, int negaticeResId, IOnClickListener listener)
	{
		showInfo(context, title, msg, -1, positiveResId, negaticeResId, true, false, listener);
	}

	public static void showInfo(Context context, CharSequence title, CharSequence msg,
			int positiveResId, int negaticeResId, boolean cancelable, IOnClickListener listener)
	{
		showInfo(context, title, msg, -1, positiveResId, negaticeResId, cancelable, cancelable, listener);
	}

	public static void showInfo_OnlyPositiveBtn_CannotCancelable(Context context, CharSequence title, CharSequence msg,
			int positiveResId, IOnClickListener listener)
	{
		showInfo(context, title, msg, -1, positiveResId, -1, false, false, listener);
	}

	/**
	 * Shows an info message.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param msgId
	 *            the resource ID of the message.
	 */
	public static void showInfo(Context context, int msgId)
	{
		showInfo(context, context.getString(msgId));
	}// showInfo()

	/**
	 * Shows an error message.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param msg
	 *            the message.
	 * @param listener
	 *            will be called after the user cancelled the dialog.
	 */
	public static void showError(Context context, CharSequence msg, DialogInterface.OnCancelListener listener)
	{
		AlertDialog dlg = newDlg(context);
		dlg.setIcon(android.R.drawable.ic_dialog_alert);
		dlg.setTitle(R.string.tip_title_error);
		dlg.setMessage(msg);
		dlg.setOnCancelListener(listener);
		dlg.show();
	}// showError()

	/**
	 * Shows an error message.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param msgId
	 *            the resource ID of the message.
	 * @param listener
	 *            will be called after the user cancelled the dialog.
	 */
	public static void showError(Context context, int msgId, DialogInterface.OnCancelListener listener)
	{
		showError(context, context.getString(msgId), listener);
	}// showError()

	/**
	 * Shows an unknown error.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param t
	 *            the {@link Throwable}
	 * @param listener
	 *            will be called after the user cancelled the dialog.
	 */
	public static void showUnknownError(Context context, Throwable t, DialogInterface.OnCancelListener listener)
	{
		showError(context, String.format(context.getString(R.string.tip_pmsg_unknown_error), t), listener);
	}// showUnknownError()

	/**
	 * Shows a confirmation dialog.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param msg
	 *            the message.
	 * @param onYes
	 *            will be called if the user selects positive answer (a
	 *            <i>Yes</i> or <i>OK</i>).
	 * @param onNo
	 *            will be called after the user cancelled the dialog.
	 */
	public static void confirmYesno(Context context, CharSequence msg, DialogInterface.OnClickListener onYes,
			DialogInterface.OnCancelListener onNo)
	{
		AlertDialog dlg = newDlg(context);
		dlg.setIcon(android.R.drawable.ic_dialog_alert);
		dlg.setTitle(R.string.tip_title_confirmation);
		dlg.setMessage(msg);
		dlg.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.yes), onYes);
		dlg.setOnCancelListener(onNo);
		dlg.show();
	}

	/**
	 * Shows a confirmation dialog.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param msg
	 *            the message.
	 * @param onYes
	 *            will be called if the user selects positive answer (a
	 *            <i>Yes</i> or <i>OK</i>).
	 */
	public static void confirmYesno(Context context, CharSequence msg, DialogInterface.OnClickListener onYes)
	{
		confirmYesno(context, msg, onYes, null);
	}// confirmYesno()

	/**
	 * Creates new {@link AlertDialog}. Set canceled on touch outside to
	 * {@code true}.
	 * 
	 * @param context
	 *            {@link Context}
	 * @return {@link AlertDialog}
	 * @since v4.3 beta
	 */
	public static AlertDialog newDlg(Context context)
	{
		AlertDialog res = newDlgBuilder(context).create();
		res.setCanceledOnTouchOutside(true);
		return res;
	}// newDlg()

	/**
	 * Creates new {@link AlertDialog.Builder}.
	 * 
	 * @param context
	 *            {@link Context}
	 * @return {@link AlertDialog}
	 * @since v4.3 beta
	 */
	public static AlertDialog.Builder newDlgBuilder(Context context)
	{
		return new AlertDialog.Builder(context);
	}// newDlgBuilder()
}
