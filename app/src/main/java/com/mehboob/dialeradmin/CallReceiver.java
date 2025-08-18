package com.mehboob.dialeradmin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.mehboob.dialeradmin.models.CallHistory;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private static final String PHONE_STATE = TelephonyManager.ACTION_PHONE_STATE_CHANGED;
    private static final String EXTRA_STATE = "state";
    private static final String EXTRA_INCOMING_NUMBER = "incoming_number";
    
    // Define state constants as strings
    private static final String STATE_IDLE = "IDLE";
    private static final String STATE_RINGING = "RINGING";
    private static final String STATE_OFFHOOK = "OFFHOOK";
    
    private static String lastState = STATE_IDLE;
    private static long callStartTime = 0;
    private static String incomingNumber = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null || !intent.getAction().equals(PHONE_STATE)) {
            return;
        }

        String state = intent.getStringExtra(EXTRA_STATE);
        String number = intent.getStringExtra(EXTRA_INCOMING_NUMBER);

        if (state == null) {
            return;
        }

        Log.d(TAG, "Call state: " + state + ", Number: " + number);

        if (STATE_RINGING.equals(state)) {
            // Incoming call is ringing
            if (STATE_IDLE.equals(lastState)) {
                incomingNumber = number;
                callStartTime = System.currentTimeMillis();
                Log.d(TAG, "Incoming call from: " + incomingNumber);
            }
        } else if (STATE_OFFHOOK.equals(state)) {
            // Call is answered or outgoing call is dialing
            if (STATE_RINGING.equals(lastState)) {
                // Incoming call was answered
                Log.d(TAG, "Incoming call answered: " + incomingNumber);
            } else if (STATE_IDLE.equals(lastState)) {
                // Outgoing call
                callStartTime = System.currentTimeMillis();
                Log.d(TAG, "Outgoing call started");
            }
        } else if (STATE_IDLE.equals(state)) {
            // Call ended
            if (STATE_RINGING.equals(lastState)) {
                // Missed call
                if (incomingNumber != null && !incomingNumber.isEmpty()) {
                    saveCallToHistory(incomingNumber, "MISSED", callStartTime, System.currentTimeMillis());
                    Log.d(TAG, "Missed call from: " + incomingNumber);
                }
            } else if (STATE_OFFHOOK.equals(lastState)) {
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
