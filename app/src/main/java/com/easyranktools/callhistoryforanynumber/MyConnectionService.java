package com.easyranktools.callhistoryforanynumber;

import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.util.Log;

public class MyConnectionService extends ConnectionService {
    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d("MyConnectionService", "Incoming call: " + request.getAddress());
        return super.onCreateIncomingConnection(connectionManagerPhoneAccount, request);
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d("MyConnectionService", "Outgoing call: " + request.getAddress());
        return super.onCreateOutgoingConnection(connectionManagerPhoneAccount, request);
    }
}
