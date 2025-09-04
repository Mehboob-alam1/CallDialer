package com.easyranktools.callhistoryforanynumber;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import android.telecom.DisconnectCause;
import android.util.Log;

public class MyConnectionService extends ConnectionService {
    private static final String TAG = "MyConnectionService";

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "Incoming call: " + request.getAddress());
        
        MyConnection connection = new MyConnection();
        connection.setConnectionCapabilities(Connection.CAPABILITY_SUPPORT_HOLD | 
                                           Connection.CAPABILITY_HOLD);
        connection.setAddress(request.getAddress(), Connection.PRESENTATION_ALLOWED);
        connection.setCallerDisplayName(request.getAddress().getSchemeSpecificPart(), 
                                      Connection.PRESENTATION_ALLOWED);
        
        return connection;
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "Outgoing call: " + request.getAddress());
        
        MyConnection connection = new MyConnection();
        connection.setConnectionCapabilities(Connection.CAPABILITY_SUPPORT_HOLD | 
                                           Connection.CAPABILITY_HOLD);
        connection.setAddress(request.getAddress(), Connection.PRESENTATION_ALLOWED);
        
        return connection;
    }

    @Override
    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "Failed to create incoming connection: " + request.getAddress());
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request);
    }

    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "Failed to create outgoing connection: " + request.getAddress());
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request);
    }

    private static class MyConnection extends Connection {
        @Override
        public void onAnswer() {
            Log.d(TAG, "Call answered");
            setActive();
        }

        @Override
        public void onReject() {
            Log.d(TAG, "Call rejected");
            setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
            destroy();
        }

        @Override
        public void onDisconnect() {
            Log.d(TAG, "Call disconnected");
            setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
            destroy();
        }

        @Override
        public void onAbort() {
            Log.d(TAG, "Call aborted");
            setDisconnected(new DisconnectCause(DisconnectCause.CANCELED));
            destroy();
        }

        @Override
        public void onHold() {
            Log.d(TAG, "Call held");
            setOnHold();
        }

        @Override
        public void onUnhold() {
            Log.d(TAG, "Call unheld");
            setActive();
        }
    }
}
