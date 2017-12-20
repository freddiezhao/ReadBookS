package com.sina.book.useraction;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

/*
 * You have to add this in your manifest

 <service android:name="com.sina.book.useraction.OpenUDIDService">
 <intent-filter>
 <action android:name="com.sina.book.useraction.OpenUDID.GETUDID" />
 </intent-filter>
 </service>

 */

public class OpenUDIDService extends Service {
    @Override
    public IBinder onBind(Intent arg0) {
        return new Binder() {
            @Override
            public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply,
                    int flags) {
                final SharedPreferences preferences = getSharedPreferences(
                        OpenUDIDManager.PREFS_NAME, Context.MODE_PRIVATE);

                // Return to the sender the input random number
                reply.writeInt(data.readInt());

                reply.writeString(preferences.getString(OpenUDIDManager.PREF_KEY, null));
                return true;
            }
        };
    }
}
