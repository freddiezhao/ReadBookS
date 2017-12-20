package com.sina.book.useraction;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.text.TextUtils;

import com.sina.book.util.LogUtil;

public class OpenUDIDManager implements ServiceConnection {
	public final static String TAG = "OpenUDIDManager";

	public final static String PREF_KEY = "openudid";
	public final static String PREFS_NAME = "openudid_prefs";

	/** Application context. */
	private final Context mContext;

	/** List of available OpenUDID Intents. */
	private List<ResolveInfo> mMatchingIntents;

	/** Map of OpenUDIDs found so far. */
	private Map<String, Integer> mReceivedOpenUDIDs;

	/** Preferences to store the OpenUDID. */
	private final SharedPreferences mPreferences;

	private final Random mRandom;

	private OpenUDIDManager(Context context) {
		mPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mContext = context;
		mRandom = new Random();
		mReceivedOpenUDIDs = new HashMap<String, Integer>();
	}

	@Override
	public synchronized void onServiceConnected(ComponentName className, IBinder service) {
		// Get the OpenUDID from the remote service
		try {
			// Send a random number to the service
			android.os.Parcel data = android.os.Parcel.obtain();
			data.writeInt(mRandom.nextInt());
			android.os.Parcel reply = android.os.Parcel.obtain();
			service.transact(1, android.os.Parcel.obtain(), reply, 0);

			// Check if the service returns us this number
			if (data.readInt() == reply.readInt()) {
				final String openUDID = reply.readString();
				// if valid OpenUDID, save it
				if (openUDID != null) {
					LogUtil.d(TAG, "Received " + openUDID);

					if (mReceivedOpenUDIDs.containsKey(openUDID)) {
						mReceivedOpenUDIDs.put(openUDID, mReceivedOpenUDIDs.get(openUDID) + 1);
					} else {
						mReceivedOpenUDIDs.put(openUDID, 1);
					}
				}
			}
			data.recycle();
		} catch (RemoteException e) {
			LogUtil.e(TAG, "RemoteException: " + e.getMessage());
		}
		mContext.unbindService(this);

		// Try the next one
		startService();
	}

	@Override
	public synchronized void onServiceDisconnected(ComponentName className) {
	}

	private synchronized void storeOpenUDID() {
		if (!TextUtils.isEmpty(OpenUDID)) {
			final Editor e = mPreferences.edit();
			e.putString(PREF_KEY, OpenUDID);
			e.commit();
		}
	}

	/*
	 * Generate a new OpenUDID
	 */
	private synchronized void generateOpenUDID() {
		LogUtil.d(TAG, "Generating openUDID");
		// Try to get the ANDROID_ID
		OpenUDID = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
		if (OpenUDID == null || OpenUDID.equals("9774d56d682e549c") || OpenUDID.length() < 15) {
			// if ANDROID_ID is null, or it's equals to the GalaxyTab generic
			// ANDROID_ID or bad, generates a new one
			final SecureRandom random = new SecureRandom();
			OpenUDID = new BigInteger(64, random).toString(16);
		}
	}

	/*
	 * Start the oldest service
	 */
	private synchronized void startService() {
		if (mMatchingIntents.size() > 0) {
			// There are some Intents untested
			LogUtil.d(TAG, "Trying service " + mMatchingIntents.get(0).loadLabel(mContext.getPackageManager()));

			final ServiceInfo servInfo = mMatchingIntents.get(0).serviceInfo;
			final Intent i = new Intent();
			i.setComponent(new ComponentName(servInfo.applicationInfo.packageName, servInfo.name));
			mMatchingIntents.remove(0);

			// try added by Lionscribe
			try {
				mContext.bindService(i, this, Context.BIND_AUTO_CREATE);
			} catch (SecurityException e) {
				startService(); // ignore this one, and start next one
			}
		} else {
			// No more service to test

			// Choose the most frequent
			getMostFrequentOpenUDID();
			// No OpenUDID was chosen, generate one
			if (OpenUDID == null) {
				generateOpenUDID();
			}
			LogUtil.d(TAG, "OpenUDID: " + OpenUDID);

			// Store it locally
			storeOpenUDID();
			mInitialized = true;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private synchronized void getMostFrequentOpenUDID() {
		if (!mReceivedOpenUDIDs.isEmpty()) {
			final TreeMap<String, Integer> sorted_OpenUDIDS = new TreeMap(new ValueComparator());
			sorted_OpenUDIDS.putAll(mReceivedOpenUDIDs);

			OpenUDID = sorted_OpenUDIDS.firstKey();
		}
	}

	private static String OpenUDID = null;
	private static boolean mInitialized = false;

	/**
	 * The Method to call to get OpenUDID
	 * 
	 * @return the OpenUDID
	 */
	public synchronized static String getOpenUDID() {
		if (!mInitialized) {
			LogUtil.e("OpenUDID", "Initialisation isn't done");
		}
		return OpenUDID;
	}

	/**
	 * The Method to call to get OpenUDID
	 * 
	 * @return the OpenUDID
	 */
	public synchronized static boolean isInitialized() {
		return mInitialized;
	}

	/**
	 * The Method the call at the init of your app
	 * 
	 * @param context
	 *            you current context
	 */
	public static synchronized void sync(Context context) {
		// Initialise the Manager
		OpenUDIDManager manager = new OpenUDIDManager(context);

		// Try to get the openudid from local preferences
		OpenUDID = manager.mPreferences.getString(PREF_KEY, null);
		if (OpenUDID == null) {
			// Not found

			// Get the list of all OpenUDID services available (including
			// itself)
			manager.mMatchingIntents = context.getPackageManager().queryIntentServices(
					new Intent(context, OpenUDIDService.class), 0);
			LogUtil.d(TAG, manager.mMatchingIntents.size() + " services matches OpenUDID");

			// Start services one by one
			manager.startService();
		} else {
			// Got it, you can now call getOpenUDID()
			LogUtil.d(TAG, "OpenUDID: " + OpenUDID);
			mInitialized = true;
		}
	}

	/*
	 * Used to sort the OpenUDIDs collected by occurrence
	 */
	@SuppressWarnings("rawtypes")
	private class ValueComparator implements Comparator {
		public int compare(Object a, Object b) {

			if (mReceivedOpenUDIDs.get(a) < mReceivedOpenUDIDs.get(b)) {
				return 1;
			} else if (mReceivedOpenUDIDs.get(a) == mReceivedOpenUDIDs.get(b)) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
