package com.easyranktools.callhistoryforanynumber;

import android.telecom.Call;
import android.telecom.InCallService;
import android.util.Log;

public class MyInCallService extends InCallService {
    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        Log.d("MyInCallService", "📞 Call Added: " + call);
        // TODO: launch your custom call UI here if you want
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        Log.d("MyInCallService", "❌ Call Removed: " + call);
    }
}
