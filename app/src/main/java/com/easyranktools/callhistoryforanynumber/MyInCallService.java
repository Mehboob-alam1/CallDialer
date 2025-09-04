package com.easyranktools.callhistoryforanynumber;

import android.content.Intent;
import android.telecom.Call;
import android.telecom.InCallService;
import android.util.Log;

public class MyInCallService extends InCallService {
    private static final String TAG = "MyInCallService";

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        Log.d(TAG, "📞 Call Added: " + call);
        
        // Launch the dialer activity to show call UI
        Intent intent = new Intent(this, DialerHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("call_state", "incoming");
        startActivity(intent);
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        Log.d(TAG, "❌ Call Removed: " + call);
    }

    @Override
    public void onCallAudioStateChanged(CallAudioState audioState) {
        super.onCallAudioStateChanged(audioState);
        Log.d(TAG, "🔊 Call Audio State Changed: " + audioState);
    }

    @Override
    public void onCanAddCallChanged(boolean canAddCall) {
        super.onCanAddCallChanged(canAddCall);
        Log.d(TAG, "📱 Can Add Call: " + canAddCall);
    }

    @Override
    public void onSilenceRinger() {
        super.onSilenceRinger();
        Log.d(TAG, "🔇 Ringer silenced");
    }

    @Override
    public void onBringToForeground(boolean showDialpad) {
        super.onBringToForeground(showDialpad);
        Log.d(TAG, "📱 Bring to foreground, show dialpad: " + showDialpad);
    }
}
