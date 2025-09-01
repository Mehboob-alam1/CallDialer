package com.easyranktools.callhistoryforanynumber;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.easyranktools.callhistoryforanynumber.models.CallHistory;

import java.util.ArrayList;
import java.util.List;

public class CallManager {
    private static final String TAG = "CallManager";
    private static CallManager instance;
    private final DatabaseReference callHistoryRef;
    private final String adminId;
    private final String childNumber;

    private CallManager() {
        adminId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                 FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        childNumber = MyApplication.getInstance().getCurrentAdmin() != null ? 
                    MyApplication.getInstance().getCurrentAdmin().getChildNumber() : "";
        callHistoryRef = FirebaseDatabase.getInstance().getReference("call_logs");
    }

    public static CallManager getInstance() {
        if (instance == null) {
            instance = new CallManager();
        }
        return instance;
    }

    public void saveCallToFirebase(String contactNumber, String contactName, String callType, 
                                  long startTime, long endTime, long duration) {
        if (adminId.isEmpty()) {
            Log.e(TAG, "Admin ID is empty, cannot save call");
            return;
        }

        String callId = callHistoryRef.push().getKey();
        if (callId == null) {
            Log.e(TAG, "Failed to generate call ID");
            return;
        }

        boolean isPremiumCall = !MyApplication.getInstance().isPlanExpired();
        String planType = MyApplication.getInstance().getCurrentPlanType();
        long createdAt = System.currentTimeMillis();

        CallHistory callHistory = new CallHistory(
                callId, adminId, childNumber, contactNumber, contactName,
                callType, startTime, endTime, duration, isPremiumCall, planType, createdAt
        );

        callHistoryRef.child(callId).setValue(callHistory)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Call saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save call", e));
    }

    public void syncCallLogs(Context context) {
        if (adminId.isEmpty()) {
            Log.e(TAG, "Admin ID is empty, cannot sync calls");
            return;
        }

        try {
            String[] projection = {
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
            };

            String selection = CallLog.Calls.DATE + " > ?";
            String[] selectionArgs = {String.valueOf(System.currentTimeMillis() - (24 * 60 * 60 * 1000))}; // Last 24 hours

            Cursor cursor = context.getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    CallLog.Calls.DATE + " DESC"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                     @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    @SuppressLint("Range") long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                    @SuppressLint("Range") long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));

                    String callType = getCallTypeString(type);
                    String contactName = getContactName(context, number);

                    // Check if this call is already saved
                    checkAndSaveCall(number, contactName, callType, date, date + (duration * 1000), duration);
                }
                cursor.close();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for call log", e);
        }
    }

    private String getCallTypeString(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE:
                return "INCOMING";
            case CallLog.Calls.OUTGOING_TYPE:
                return "OUTGOING";
            case CallLog.Calls.MISSED_TYPE:
                return "MISSED";
            case CallLog.Calls.REJECTED_TYPE:
                return "REJECTED";
            case CallLog.Calls.BLOCKED_TYPE:
                return "BLOCKED";
            default:
                return "UNKNOWN";
        }
    }

    private String getContactName(Context context, String phoneNumber) {
        try {
            String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
            String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + "=?";
            String[] selectionArgs = {phoneNumber};

            Cursor cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                cursor.close();
                return name != null ? name : "Unknown";
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting contact name", e);
        }
        return "Unknown";
    }

    private void checkAndSaveCall(String number, String name, String type, long startTime, long endTime, long duration) {
        // Check if call already exists in Firebase
        callHistoryRef.orderByChild("contactNumber").equalTo(number)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        boolean exists = false;
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            CallHistory existingCall = child.getValue(CallHistory.class);
                            if (existingCall != null && 
                                existingCall.getAdminId().equals(adminId) &&
                                Math.abs(existingCall.getCallStartTime() - startTime) < 60000) { // Within 1 minute
                                exists = true;
                                break;
                            }
                        }
                        
                        if (!exists) {
                            saveCallToFirebase(number, name, type, startTime, endTime, duration);
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        Log.e(TAG, "Error checking existing call", error.toException());
                    }
                });
    }

    public void getCallHistory(OnCallHistoryListener listener) {
        if (adminId.isEmpty()) {
            listener.onError("Admin not authenticated");
            return;
        }

        callHistoryRef.orderByChild("adminId").equalTo(adminId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        List<CallHistory> callHistoryList = new ArrayList<>();
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            CallHistory callHistory = child.getValue(CallHistory.class);
                            if (callHistory != null) {
                                callHistoryList.add(callHistory);
                            }
                        }
                        listener.onSuccess(callHistoryList);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    public void getCallHistoryByChildNumber(String childNumber, OnCallHistoryListener listener) {
        if (adminId.isEmpty()) {
            listener.onError("Admin not authenticated");
            return;
        }

        callHistoryRef.orderByChild("childNumber").equalTo(childNumber)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        List<CallHistory> callHistoryList = new ArrayList<>();
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            CallHistory callHistory = child.getValue(CallHistory.class);
                            if (callHistory != null && callHistory.getAdminId().equals(adminId)) {
                                callHistoryList.add(callHistory);
                            }
                        }
                        listener.onSuccess(callHistoryList);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    public interface OnCallHistoryListener {
        void onSuccess(List<CallHistory> callHistoryList);
        void onError(String error);
    }
}
