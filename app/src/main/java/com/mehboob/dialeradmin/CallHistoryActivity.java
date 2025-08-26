package com.mehboob.dialeradmin;

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
import com.mehboob.dialeradmin.adapters.ChildCallLogAdapter;
import com.mehboob.dialeradmin.models.CallHistory;
import com.mehboob.dialeradmin.models.ChildCallLog;
import com.mehboob.dialeradmin.models.AdminModel;
import com.mehboob.dialeradmin.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.List;

public class CallHistoryActivity extends AppCompatActivity {
    private static final String TAG = "CallHistoryActivity";
    
    private Toolbar toolbar;
    private ChipGroup chipGroup;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;
    
    private ChildCallLogAdapter adapter;
    private List<ChildCallLog> allCallHistory = new ArrayList<>();
    private List<ChildCallLog> filteredCallHistory = new ArrayList<>();
    
    private String selectedFilter = "all";
    private String childNumberFilter = null;
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
        setupChipGroup();
        setupSwipeRefresh();
        
        // Check if we're filtering by a specific child number
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("child_number")) {
            childNumberFilter = intent.getStringExtra("child_number");
        }
        
        loadAdminData();



        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        // Set Home selected
        bottomNav.setSelectedItemId(R.id.nav_history);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_history) {
               // startActivity(new Intent(this, CallHistoryActivity.class));
                return  true;
            }else  if (id == R.id.nav_info) {
                startActivity(new Intent(this, MainActivity.class));
              overridePendingTransition(0, 0);
                return  true;
            } else if (id == R.id.nav_profile) {

                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return  true;
            } else if (id == R.id.nav_premium) {
                startActivity(new Intent(this, PacakageActivity.class));
                overridePendingTransition(0, 0);
                return  true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SetttingActivity.class));
                overridePendingTransition(0, 0);
                return  true;
            }
            return true;
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroup = findViewById(R.id.chipGroup);
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
        
        // Set click listener
        adapter.setOnCallLogClickListener(callLog -> {
            // Handle call log click - could show details dialog
            showCallLogDetails(callLog);
        });
    }

    private void setupChipGroup() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
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

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadCallHistory);
    }

    private void loadAdminData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adminRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_ADMINS_NODE)
                .child(uid);
        
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        currentAdmin = snapshot.getValue(AdminModel.class);
                        if (currentAdmin != null) {
                            // Fix childNumbers if it's stored as HashMap (legacy format)
                            DataSnapshot childNumbersSnapshot = snapshot.child("childNumbers");
                            if (childNumbersSnapshot.exists()) {
                                List<String> childNumbers = new ArrayList<>();
                                for (DataSnapshot child : childNumbersSnapshot.getChildren()) {
                                    String number = child.getValue(String.class);
                                    if (number != null) {
                                        childNumbers.add(number);
                                    }
                                }
                                currentAdmin.setChildNumbers(childNumbers);
                            }
                            MyApplication.getInstance().setCurrentAdmin(currentAdmin);

                            if (!currentAdmin.isPremium() || !currentAdmin.isPlanActive()) {
//                                showNoPlanDialog();

                                startActivity(new Intent(CallHistoryActivity.this, PacakageActivity.class));
                                overridePendingTransition(0, 0);
                                return;
                            }

                            loadCallHistory();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error deserializing admin data: " + e.getMessage());
                        // Try manual deserialization for legacy data
                        try {
                            currentAdmin = new AdminModel();
                            currentAdmin.setUid(snapshot.child("uid").getValue(String.class));
                            currentAdmin.setEmail(snapshot.child("email").getValue(String.class));
                            currentAdmin.setPhoneNumber(snapshot.child("phoneNumber").getValue(String.class));
                            currentAdmin.setName(snapshot.child("name").getValue(String.class));
                            currentAdmin.setRole(snapshot.child("role").getValue(String.class));
                            currentAdmin.setIsActivated(snapshot.child("isActivated").getValue(Boolean.class) != null ? 
                                                       snapshot.child("isActivated").getValue(Boolean.class) : false);
                            currentAdmin.setIsPremium(snapshot.child("isPremium").getValue(Boolean.class) != null ? 
                                                     snapshot.child("isPremium").getValue(Boolean.class) : false);
                            currentAdmin.setPlanType(snapshot.child("planType").getValue(String.class));
                            currentAdmin.setPlanActivatedAt(snapshot.child("planActivatedAt").getValue(Long.class) != null ? 
                                                           snapshot.child("planActivatedAt").getValue(Long.class) : 0L);
                            currentAdmin.setPlanExpiryAt(snapshot.child("planExpiryAt").getValue(Long.class) != null ? 
                                                        snapshot.child("planExpiryAt").getValue(Long.class) : 0L);
                            currentAdmin.setCreatedAt(snapshot.child("createdAt").getValue(Long.class) != null ? 
                                                     snapshot.child("createdAt").getValue(Long.class) : 0L);
                            currentAdmin.setChildNumber(snapshot.child("childNumber").getValue(String.class));
                            
                            // Handle childNumbers (could be HashMap or List)
                            DataSnapshot childNumbersSnapshot = snapshot.child("childNumbers");
                            if (childNumbersSnapshot.exists()) {
                                List<String> childNumbers = new ArrayList<>();
                                for (DataSnapshot child : childNumbersSnapshot.getChildren()) {
                                    String number = child.getValue(String.class);
                                    if (number != null) {
                                        childNumbers.add(number);
                                    }
                                }
                                currentAdmin.setChildNumbers(childNumbers);
                            }
                            
                            loadCallHistory();
                            
                        } catch (Exception manualError) {
                            Log.e(TAG, "Manual deserialization also failed: " + manualError.getMessage());
                            Toast.makeText(CallHistoryActivity.this, "Failed to parse admin data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CallHistoryActivity.this, "Error loading admin data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCallHistory() {
        if (currentAdmin == null) return;
        
        allCallHistory.clear();
        
        // Get all child numbers for this admin
        List<String> childNumbers = currentAdmin.getChildNumbers();
        if (childNumbers == null || childNumbers.isEmpty()) {
            applyFilters();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        
        // Load call logs for each child number
        DatabaseReference callLogsRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_CALL_HISTORY_NODE);
        
        final int[] loadedCount = {0};
        final int totalChildNumbers = childNumbers.size();
        
        for (String childNumber : childNumbers) {
            callLogsRef.child(childNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot callSnapshot : snapshot.getChildren()) {
                            try {
                                ChildCallLog callLog = callSnapshot.getValue(ChildCallLog.class);
                                if (callLog != null) {
                                    // Set the child number and call ID
                                    callLog.setChildNumber(childNumber);
                                    callLog.setId(callSnapshot.getKey());
                                    allCallHistory.add(callLog);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing call log: " + e.getMessage());
                                // Try manual parsing for legacy format
                                try {
                                    ChildCallLog callLog = new ChildCallLog();
                                    callLog.setId(callSnapshot.getKey());
                                    callLog.setChildNumber(childNumber);
                                    callLog.setNumber(callSnapshot.child("number").getValue(String.class));
                                    callLog.setType(callSnapshot.child("type").getValue(String.class));
                                    callLog.setTimestamp(callSnapshot.child("timestamp").getValue(Long.class) != null ? 
                                                       callSnapshot.child("timestamp").getValue(Long.class) : 0L);
                                    callLog.setDuration(callSnapshot.child("duration").getValue(Long.class) != null ? 
                                                      callSnapshot.child("duration").getValue(Long.class) : 0L);
                                    allCallHistory.add(callLog);
                                } catch (Exception manualError) {
                                    Log.e(TAG, "Manual parsing also failed: " + manualError.getMessage());
                                }
                            }
                        }
                    }
                    
                    loadedCount[0]++;
                    if (loadedCount[0] >= totalChildNumbers) {
                        // All child numbers loaded, apply filters
                        applyFilters();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
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
            // Apply child number filter if specified
            if (childNumberFilter != null && !childNumberFilter.equals(call.getChildNumber())) {
                continue;
            }
            
            // Apply call type filter
            if (selectedFilter.equals("all") || 
                selectedFilter.equals(call.getType().toLowerCase())) {
                filteredCallHistory.add(call);
            }
        }
        
        // Sort by timestamp (most recent first)
        filteredCallHistory.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        
        adapter.notifyDataSetChanged();
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
        
        // Update toolbar title with call count
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
                .setMessage("Child Number: " + callLog.getChildNumber() + "\n" +
                           "Contact: " + callLog.getNumber() + "\n" +
                           "Type: " + callLog.getCallTypeDisplay() + "\n" +
                           "Duration: " + callLog.getFormattedDuration() + "\n" +
                           "Date: " + callLog.getFormattedDate() + "\n" +
                           "Time: " + callLog.getFormattedTime())
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_call_history, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        
        if (searchView != null) {
            searchView.setQueryHint("Search by name or number");
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
        }
        
        return true;
    }

    private void filterBySearch(String query) {
        if (query.isEmpty()) {
            applyFilters();
            return;
        }
        
        List<ChildCallLog> searchResults = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (ChildCallLog call : filteredCallHistory) {
            if (call.getContactName() != null && call.getContactName().toLowerCase().contains(lowerQuery) ||
                call.getChildNumber() != null && call.getChildNumber().contains(query)) {
                searchResults.add(call);
            }
        }
        
        adapter.updateData(searchResults);
        updateEmptyState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_sync) {
            loadCallHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();

    }

}
