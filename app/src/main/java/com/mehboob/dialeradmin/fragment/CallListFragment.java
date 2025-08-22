package com.mehboob.dialeradmin.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.mehboob.dialeradmin.R;
import com.mehboob.dialeradmin.adapters.CallLogAdapter;
import com.mehboob.dialeradmin.models.CallLogEntry;

import java.util.ArrayList;
import java.util.List;

public class CallListFragment extends Fragment {

    private RecyclerView callRecyclerView;
    private CallLogAdapter callLogAdapter;
    private ExtendedFloatingActionButton fabClearHistory;
    //private SearchView searchView;
    private List<CallLogEntry> originalCallLogs = new ArrayList<>();

    private static final int REQUEST_CODE_CALL_LOG = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_list, container, false);

        callRecyclerView = view.findViewById(R.id.callRecyclerView);
        fabClearHistory = view.findViewById(R.id.fabClearHistory);
       // searchView = view.findViewById(R.id.searchView);

        setupRecyclerView();
       // setupSearchView();
        setupClearHistoryButton();

        // Check permission and load data
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_CODE_CALL_LOG);
        } else {
            loadAndDisplayCallLogs();
        }

        return view;
    }

    private void setupRecyclerView() {
        callRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        callRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        callLogAdapter = new CallLogAdapter(originalCallLogs);
        callRecyclerView.setAdapter(callLogAdapter);

        callLogAdapter.setOnItemClickListener((position, callLog) -> redialNumber(callLog.getNumber()));
    }

//    private void setupSearchView() {
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) { return false; }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                filterCallLogs(newText);
//                return true;
//            }
//        });
//    }

    private void setupClearHistoryButton() {
        fabClearHistory.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear Call History")
                .setMessage("Are you sure you want to delete all call history?")
                .setPositiveButton("Clear", (dialog, which) -> clearCallHistory())
                .setNegativeButton("Cancel", null)
                .show());
    }

    private void filterCallLogs(String query) {
        List<CallLogEntry> filteredList = new ArrayList<>();
        for (CallLogEntry callLog : originalCallLogs) {
            if (callLog.getName().toLowerCase().contains(query.toLowerCase()) ||
                    callLog.getNumber().contains(query)) {
                filteredList.add(callLog);
            }
        }
        callLogAdapter.filterList(filteredList);
    }

    private void loadAndDisplayCallLogs() {
        originalCallLogs.clear();
        originalCallLogs.addAll(loadCallLogs());
        callLogAdapter.filterList(originalCallLogs);
    }

    private List<CallLogEntry> loadCallLogs() {
        List<CallLogEntry> callLogs = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return callLogs;
        }

        Uri callUri = CallLog.Calls.CONTENT_URI;
        String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE
        };

        Cursor cursor = requireContext().getContentResolver()
                .query(callUri, projection, null, null, CallLog.Calls.DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
            int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
            int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);

            do {
                String number = cursor.getString(numberIndex);
                String name = cursor.getString(nameIndex);
                if (name == null) name = "Unknown";

                int callType = cursor.getInt(typeIndex);
                String callTypeString = getCallTypeString(callType);

                String duration = cursor.getString(durationIndex);
                long callDate = cursor.getLong(dateIndex);

                String formattedDate = android.text.format.DateFormat.format("dd-MM-yyyy hh:mm a", callDate).toString();

                int icon = R.drawable.ic_call_received;
                if (callType == CallLog.Calls.OUTGOING_TYPE) {
                    icon = R.drawable.ic_call_made;
                } else if (callType == CallLog.Calls.MISSED_TYPE) {
                    icon = R.drawable.ic_call_missed;
                }

                callLogs.add(new CallLogEntry(name, number, callTypeString, duration, formattedDate, icon));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return callLogs;
    }

    private void clearCallHistory() {
        originalCallLogs.clear();
        callLogAdapter.filterList(originalCallLogs);
        Toast.makeText(getContext(), "Call history cleared", Toast.LENGTH_SHORT).show();
    }

    private void redialNumber(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permission required to make call", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
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
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CALL_LOG && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadAndDisplayCallLogs();
        } else {
            Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
