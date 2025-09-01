package com.easyranktools.callhistoryforanynumber;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.easyranktools.callhistoryforanynumber.adapters.ChildCallLogAdapter;
import com.easyranktools.callhistoryforanynumber.models.AdminModel;
import com.easyranktools.callhistoryforanynumber.models.ChildCallLog;

import java.util.ArrayList;
import java.util.List;

public class CallHistoryActivity extends AppCompatActivity {
    private static final String TAG = "CallHistoryActivity";

    private Toolbar toolbar;
    private ChipGroup chipGroupType;        // All / Incoming / Outgoing / Missed
    private ChipGroup chipGroupChildren;    // All Children / <child numbers>

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;

    private ChildCallLogAdapter adapter;
    private final List<ChildCallLog> allCallHistory = new ArrayList<>();
    private final List<ChildCallLog> filteredCallHistory = new ArrayList<>();

    private String selectedFilter = "all"; // type filter
    private String childNumberFilter = null; // null => all children
    private AdminModel currentAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_history);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupTypeChipGroup();
        setupSwipeRefresh();

        // Deep link (optional): pre-filter a specific child
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("child_number")) {
            childNumberFilter = intent.getStringExtra("child_number");
        }

        loadAdminData();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_history);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) {
                return true;
            } else if (id == R.id.nav_info) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_premium) {
                startActivity(new Intent(this, PacakageActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SetttingActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroupType = findViewById(R.id.chipGroup);
        chipGroupChildren = findViewById(R.id.chipGroupChildren);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        emptyStateText = findViewById(R.id.emptyStateText);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            if (childNumberFilter != null) {
                getSupportActionBar().setTitle("Call History - " + childNumberFilter);
            } else {
                getSupportActionBar().setTitle("Call History");
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        adapter = new ChildCallLogAdapter(filteredCallHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnCallLogClickListener(this::showCallLogDetails);
    }

    private void setupTypeChipGroup() {
        chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedFilter = "all";
            } else {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedFilter = chip.getText().toString().toLowerCase();
                }
            }
            applyFilters();
        });
    }

    private void setupChildChips(List<String> childNumbers) {
        chipGroupChildren.removeAllViews();

        // All Children chip
        Chip allChip = buildChildChip("All Children", null);
        chipGroupChildren.addView(allChip);

        // Add single (or many) child numbers
        if (childNumbers != null) {
            for (String n : childNumbers) {
                if (n == null || n.trim().isEmpty()) continue;
                chipGroupChildren.addView(buildChildChip(n, n));
            }
        }

        // Preselect based on deep link if provided; otherwise select "All Children"
        boolean preselected = false;
        if (childNumberFilter != null) {
            for (int i = 0; i < chipGroupChildren.getChildCount(); i++) {
                View v = chipGroupChildren.getChildAt(i);
                if (v instanceof Chip) {
                    Object tag = v.getTag();
                    if (tag != null && tag.equals(childNumberFilter)) {
                        ((Chip) v).setChecked(true);
                        preselected = true;
                        break;
                    }
                }
            }
        }
        if (!preselected) {
            allChip.setChecked(true);
        }

        chipGroupChildren.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                childNumberFilter = null;
            } else {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    Object tag = chip.getTag();
                    childNumberFilter = (tag instanceof String) ? (String) tag : null;
                }
            }
            applyFilters();
        });
    }

    // IMPORTANT: unique ID + checkable
    private Chip buildChildChip(String label, String childNumberTag) {
        Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice);
        chip.setId(View.generateViewId());   // unique ID fixes selection issue
        chip.setText(label);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setTag(childNumberTag);         // null => All Children

        // Optional styling:
        // chip.setChipBackgroundColorResource(R.color.white);
        // chip.setChipStrokeColorResource(R.color.primary);
        // chip.setChipStrokeWidth(1f);
        // chip.setTextColor(ContextCompat.getColor(this, R.color.black));
        // chip.setRippleColorResource(R.color.primary);

        return chip;
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadCallHistory);
    }

    private void loadAdminData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adminRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_ADMINS_NODE)
                .child(uid);

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                try {
                    currentAdmin = snapshot.getValue(AdminModel.class);
                    if (currentAdmin != null) {
                        // normalize childNumbers
                        DataSnapshot childNumbersSnapshot = snapshot.child("childNumbers");
                        List<String> childNumbers = new ArrayList<>();
                        if (childNumbersSnapshot.exists()) {
                            for (DataSnapshot child : childNumbersSnapshot.getChildren()) {
                                String number = child.getValue(String.class);
                                if (number != null) childNumbers.add(number);
                            }
                            currentAdmin.setChildNumbers(childNumbers);
                        }
                        MyApplication.getInstance().setCurrentAdmin(currentAdmin);

                        // ✅ Check plan validity
                        if (!currentAdmin.isPremium() || currentAdmin.getPlanExpiryAt() <= System.currentTimeMillis()) {
                            // User has no premium or plan expired → force to Packages page
                            Toast.makeText(CallHistoryActivity.this, "Premium plan required to view call history", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(CallHistoryActivity.this, PacakageActivity.class));
                            finish(); // prevent going back to CallHistory
                            return;
                        }

                        // Build chips and load history
                        setupChildChips(currentAdmin.getChildNumbers());
                        loadCallHistory();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing admin", e);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CallHistoryActivity.this, "Error loading admin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCallHistory() {
        if (currentAdmin == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        allCallHistory.clear();

        List<String> childNumbers = currentAdmin.getChildNumbers();
        if (childNumbers == null || childNumbers.isEmpty()) {
            applyFilters();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        DatabaseReference callLogsRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_CALL_HISTORY_NODE);

        final int[] loadedCount = {0};
        final int totalChildNumbers = childNumbers.size();

        for (String childNumber : childNumbers) {
            callLogsRef.child(childNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot callSnapshot : snapshot.getChildren()) {
                            try {
                                ChildCallLog callLog = callSnapshot.getValue(ChildCallLog.class);
                                if (callLog != null) {
                                    callLog.setChildNumber(childNumber);
                                    callLog.setId(callSnapshot.getKey());
                                    allCallHistory.add(callLog);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing call log: " + e.getMessage());
                                try {
                                    ChildCallLog callLog = new ChildCallLog();
                                    callLog.setId(callSnapshot.getKey());
                                    callLog.setChildNumber(childNumber);
                                    callLog.setNumber(callSnapshot.child("number").getValue(String.class));
                                    callLog.setType(callSnapshot.child("type").getValue(String.class));
                                    callLog.setTimestamp(callSnapshot.child("timestamp").getValue(Long.class) != null
                                            ? callSnapshot.child("timestamp").getValue(Long.class) : 0L);
                                    callLog.setDuration(callSnapshot.child("duration").getValue(Long.class) != null
                                            ? callSnapshot.child("duration").getValue(Long.class) : 0L);
                                    allCallHistory.add(callLog);
                                } catch (Exception manualError) {
                                    Log.e(TAG, "Manual parsing also failed: " + manualError.getMessage());
                                }
                            }
                        }
                    }

                    loadedCount[0]++;
                    if (loadedCount[0] >= totalChildNumbers) {
                        applyFilters();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error loading call logs for " + childNumber + ": " + error.getMessage());
                    loadedCount[0]++;
                    if (loadedCount[0] >= totalChildNumbers) {
                        applyFilters();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    private void applyFilters() {
        filteredCallHistory.clear();

        for (ChildCallLog call : allCallHistory) {
            // child filter
            if (childNumberFilter != null && !childNumberFilter.equals(call.getChildNumber())) continue;

            // type filter
            if ("all".equals(selectedFilter) ||
                    (call.getType() != null && selectedFilter.equals(call.getType().toLowerCase()))) {
                filteredCallHistory.add(call);
            }
        }

        // newest first
        filteredCallHistory.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        // update adapter
        if (adapter != null) {
            adapter.updateData(filteredCallHistory); // ensure your adapter implements this
        }
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredCallHistory.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            if (childNumberFilter != null) {
                emptyStateText.setText("No call history found for " + childNumberFilter);
            } else {
                emptyStateText.setText("No call history found for your child numbers");
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        updateToolbarTitle();
    }

    private void updateToolbarTitle() {
        if (getSupportActionBar() != null) {
            String title = "Call History";
            if (childNumberFilter != null) {
                title = "Call History - " + childNumberFilter;
            }
            title += " (" + filteredCallHistory.size() + " calls)";
            getSupportActionBar().setTitle(title);
        }
    }

    private void showCallLogDetails(ChildCallLog callLog) {
        new AlertDialog.Builder(this)
                .setTitle("Call Details")
                .setMessage("Child Number: " + safe(callLog.getChildNumber()) + "\n" +
                        "Contact: " + safe(callLog.getNumber()) + "\n" +
                        "Type: " + safe(callLog.getCallTypeDisplay()) + "\n" +
                        "Duration: " + safe(callLog.getFormattedDuration()) + "\n" +
                        "Date: " + safe(callLog.getFormattedDate()) + "\n" +
                        "Time: " + safe(callLog.getFormattedTime()))
                .setPositiveButton("OK", null)
                .show();
    }

    private String safe(String s) { return s == null ? "-" : s; }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_call_history, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setQueryHint("Search by name or number");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { return false; }
                @Override public boolean onQueryTextChange(String newText) {
                    filterBySearch(newText);
                    return true;
                }
            });
        }
        return true;
    }

    private void filterBySearch(String query) {
        if (query == null || query.isEmpty()) {
            applyFilters();
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<ChildCallLog> searchResults = new ArrayList<>();

        for (ChildCallLog call : filteredCallHistory) {
            boolean matchName   = call.getContactName() != null && call.getContactName().toLowerCase().contains(lowerQuery);
            boolean matchChild  = call.getChildNumber() != null && call.getChildNumber().contains(query);
            boolean matchDialed = call.getNumber() != null && call.getNumber().contains(query);
            if (matchName || matchChild || matchDialed) {
                searchResults.add(call);
            }
        }

        adapter.updateData(searchResults);
        if (searchResults.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setText("No results for \"" + query + "\"");
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        updateToolbarTitle();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_sync) {
            swipeRefreshLayout.setRefreshing(true);
            loadCallHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() { finishAffinity(); }
}
