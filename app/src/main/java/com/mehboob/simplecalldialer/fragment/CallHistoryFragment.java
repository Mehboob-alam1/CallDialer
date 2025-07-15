package com.mehboob.simplecalldialer.fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.mehboob.simplecalldialer.R;
import com.mehboob.simplecalldialer.adapters.CallLogAdapter;
import com.mehboob.simplecalldialer.models.CallLogItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallHistoryFragment extends Fragment {

    private static final int PERMISSION_REQUEST = 101;
    private RecyclerView recyclerView;

    public CallHistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_history, container, false);
        recyclerView = view.findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSION_REQUEST);
        } else {
            loadCallLogs();
        }

        return view;
    }

    private void loadCallLogs() {
        List<CallLogItem> logItems = new ArrayList<>();

        Cursor cursor = getContext().getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
        );

        int nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);

        while (cursor.moveToNext()) {
            String name = cursor.getString(nameIndex);
            String number = cursor.getString(numberIndex);
            String type;
            int callType = cursor.getInt(typeIndex);
            String dateStr = cursor.getString(dateIndex);
            String formattedDate = DateFormat.format("dd MMM yyyy, h:mm a",
                    new Date(Long.parseLong(dateStr))).toString();

            switch (callType) {
                case CallLog.Calls.INCOMING_TYPE:
                    type = "INCOMING";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    type = "OUTGOING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                default:
                    type = "MISSED";
                    break;
            }

            String caller = (name != null) ? name : number;
            logItems.add(new CallLogItem(caller, type, formattedDate));
        }

        cursor.close();
        recyclerView.setAdapter(new CallLogAdapter(logItems));
    }
}
