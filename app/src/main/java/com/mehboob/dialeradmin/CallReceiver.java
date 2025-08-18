package com.mehboob.dialeradmin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.mehboob.dialeradmin.models.CallHistory;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static long callStartTime = 0;
    private static String incomingNumber = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null || !intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            return;
        }

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (state == null) {
            return;
        }

        Log.d(TAG, "Call state: " + state + ", Number: " + number);

        switch (state) {
            case TelephonyManager.EXTRA_STATE_RINGING:
                // Incoming call is ringing
                if (lastState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    incomingNumber = number;
                    callStartTime = System.currentTimeMillis();
                    Log.d(TAG, "Incoming call from: " + incomingNumber);
                }
                break;

            case TelephonyManager.EXTRA_STATE_OFFHOOK:
                // Call is answered or outgoing call is dialing
                if (lastState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    // Incoming call was answered
                    Log.d(TAG, "Incoming call answered: " + incomingNumber);
                } else if (lastState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    // Outgoing call
                    callStartTime = System.currentTimeMillis();
                    Log.d(TAG, "Outgoing call started");
                }
                break;

            case TelephonyManager.EXTRA_STATE_IDLE:
                // Call ended
                if (lastState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    // Missed call
                    if (incomingNumber != null && !incomingNumber.isEmpty()) {
                        saveCallToHistory(incomingNumber, "MISSED", callStartTime, System.currentTimeMillis());
                        Log.d(TAG, "Missed call from: " + incomingNumber);
                    }
                } else if (lastState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    // Call ended (either incoming or outgoing)
                    long callEndTime = System.currentTimeMillis();
                    long duration = (callEndTime - callStartTime) / 1000; // Convert to seconds
                    
                    if (incomingNumber != null && !incomingNumber.isEmpty()) {
                        saveCallToHistory(incomingNumber, "INCOMING", callStartTime, callEndTime);
                        Log.d(TAG, "Incoming call ended: " + incomingNumber + ", Duration: " + duration + "s");
                    }
                }
                
                // Reset state
                incomingNumber = "";
                callStartTime = 0;
                break;
        }

        lastState = state;
    }

    private void saveCallToHistory(String number, String callType, long startTime, long endTime) {
        long duration = (endTime - startTime) / 1000; // Convert to seconds
        
        // Get contact name
        String contactName = getContactName(number);
        
        // Save to Firebase via CallManager
        CallManager.getInstance().saveCallToFirebase(
                number,
                contactName,
                callType,
                startTime,
                endTime,
                duration
        );
    }

    private String getContactName(String phoneNumber) {
        // This would need to be implemented with proper context
        // For now, return "Unknown"
        return "Unknown";
    }
}
