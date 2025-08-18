package com.mehboob.dialeradmin;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.mehboob.dialeradmin.adapters.CallHistoryAdapter;
import com.mehboob.dialeradmin.models.CallHistory;

import java.util.ArrayList;
import java.util.List;

public class CallHistoryActivity extends AppCompatActivity implements CallManager.OnCallHistoryListener {

    private RecyclerView recyclerView;
    private CallHistoryAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateTv;
    private ChipGroup filterChipGroup;
    private List<CallHistory> allCallHistory;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_history);

        setupToolbar();
        initViews();
        setupRecyclerView();
        loadCallHistory();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Call History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyStateTv = findViewById(R.id.emptyStateTv);
        filterChipGroup = findViewById(R.id.filterChipGroup);

        swipeRefreshLayout.setOnRefreshListener(this::loadCallHistory);

        // Setup filter chips
        setupFilterChips();
    }

    private void setupFilterChips() {
        Chip allChip = findViewById(R.id.chipAll);
        Chip incomingChip = findViewById(R.id.chipIncoming);
        Chip outgoingChip = findViewById(R.id.chipOutgoing);
        Chip missedChip = findViewById(R.id.chipMissed);
        Chip premiumChip = findViewById(R.id.chipPremium);

        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = "ALL";
            } else {
                Chip checkedChip = findViewById(checkedIds.get(0));
                if (checkedChip != null) {
                    currentFilter = checkedChip.getText().toString().toUpperCase();
                }
            }
            filterCallHistory();
        });
    }

    private void setupRecyclerView() {
        adapter = new CallHistoryAdapter(new ArrayList<>(), callHistory -> {
            // Handle call history item click
            showCallDetails(callHistory);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadCallHistory() {
        CallManager.getInstance().getCallHistory(this);
    }

    private void filterCallHistory() {
        if (allCallHistory == null) return;

        List<CallHistory> filteredList = new ArrayList<>();
        for (CallHistory call : allCallHistory) {
            switch (currentFilter) {
                case "INCOMING":
                    if ("INCOMING".equals(call.getCallType())) {
                        filteredList.add(call);
                    }
                    break;
                case "OUTGOING":
                    if ("OUTGOING".equals(call.getCallType())) {
                        filteredList.add(call);
                    }
                    break;
                case "MISSED":
                    if ("MISSED".equals(call.getCallType())) {
                        filteredList.add(call);
                    }
                    break;
                case "PREMIUM":
                    if (call.getIsPremiumCall()) {
                        filteredList.add(call);
                    }
                    break;
                default:
                    filteredList.add(call);
                    break;
            }
        }

        adapter.updateData(filteredList);
        updateEmptyState(filteredList.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateTv.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateTv.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showCallDetails(CallHistory callHistory) {
        // Show call details dialog or navigate to details activity
        String details = String.format(
                "Contact: %s\nNumber: %s\nType: %s\nDate: %s\nDuration: %s\nPremium: %s",
                callHistory.getContactName(),
                callHistory.getContactNumber(),
                callHistory.getCallType(),
                callHistory.getFormattedDate(),
                callHistory.getFormattedDuration(),
                callHistory.getIsPremiumCall() ? "Yes" : "No"
        );

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Call Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onSuccess(List<CallHistory> callHistoryList) {
        swipeRefreshLayout.setRefreshing(false);
        allCallHistory = callHistoryList;
        filterCallHistory();
    }

    @Override
    public void onError(String error) {
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
        updateEmptyState(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_call_history, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBySearch(newText);
                return true;
            }
        });
        
        return true;
    }

    private void filterBySearch(String query) {
        if (allCallHistory == null) return;

        List<CallHistory> filteredList = new ArrayList<>();
        for (CallHistory call : allCallHistory) {
            if (call.getContactName().toLowerCase().contains(query.toLowerCase()) ||
                call.getContactNumber().contains(query)) {
                filteredList.add(call);
            }
        }

        adapter.updateData(filteredList);
        updateEmptyState(filteredList.isEmpty());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
