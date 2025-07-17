package com.mehboob.simplecalldialer.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.mehboob.simplecalldialer.R;
import com.mehboob.simplecalldialer.adapters.CallLogAdapter;
import com.mehboob.simplecalldialer.models.CallLogEntry;

import java.util.ArrayList;
import java.util.List;

public class CallListFragment extends Fragment {
    private RecyclerView callRecyclerView;
    private CallLogAdapter callLogAdapter;
    private ExtendedFloatingActionButton fabClearHistory;
    private SearchView searchView;
    private List<CallLogEntry> originalCallLogs = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_list, container, false);

        callRecyclerView = view.findViewById(R.id.callRecyclerView);
        fabClearHistory = view.findViewById(R.id.fabClearHistory);
        searchView = view.findViewById(R.id.searchView);

        setupCallRecyclerView();
        setupSearchView();
        setupClearHistoryButton();

        return view;
    }

    private void setupCallRecyclerView() {
        callRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add item decoration for spacing
        callRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        // Get call logs (replace with your actual data loading)
        originalCallLogs = loadCallLogs();

        callLogAdapter = new CallLogAdapter(originalCallLogs);
        callRecyclerView.setAdapter(callLogAdapter);

        // Add item click listener
        callLogAdapter.setOnItemClickListener((position, callLog) -> {
            // Handle call log item click (e.g., redial)
            redialNumber(callLog.getNumber());
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCallLogs(newText);
                return true;
            }
        });
    }
    private void setupClearHistoryButton() {
        fabClearHistory.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Clear Call History")
                    .setMessage("Are you sure you want to delete all call history?")
                    .setPositiveButton("Clear", (dialog, which) -> clearCallHistory())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
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

    private List<CallLogEntry> loadCallLogs() {
        // Implement your actual call log loading logic here
        List<CallLogEntry> callLogs = new ArrayList<>();
        callLogs.add(new CallLogEntry("John Doe", "1234567890", "INCOMING", "2:30", "Today, 10:30 AM", R.drawable.ic_call_received));
        callLogs.add(new CallLogEntry("Jane Smith", "2345678901", "MISSED", "", "Yesterday, 4:15 PM", R.drawable.ic_call_missed));
        callLogs.add(new CallLogEntry("Mike Johnson", "3456789012", "OUTGOING", "1:45", "Yesterday, 11:20 AM", R.drawable.ic_call_made));
        return callLogs;
    }

    private void clearCallHistory() {
        // Implement your call history clearing logic
        originalCallLogs.clear();
        callLogAdapter.filterList(originalCallLogs);
        Toast.makeText(getContext(), "Call history cleared", Toast.LENGTH_SHORT).show();
    }

    private void redialNumber(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }
}