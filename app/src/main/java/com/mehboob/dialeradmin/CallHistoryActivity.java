package com.mehboob.dialeradmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mehboob.dialeradmin.adapters.CallHistoryAdapter;
import com.mehboob.dialeradmin.models.CallHistory;
import com.mehboob.dialeradmin.models.AdminModel;
import com.mehboob.dialeradmin.Config;

import java.util.ArrayList;
import java.util.List;

public class CallHistoryActivity extends AppCompatActivity {
    private static final String TAG = "CallHistoryActivity";
    
    private Toolbar toolbar;
    private ChipGroup chipGroup;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;
    
    private CallHistoryAdapter adapter;
    private List<CallHistory> allCallHistory = new ArrayList<>();
    private List<CallHistory> filteredCallHistory = new ArrayList<>();
    
    private String selectedFilter = "all";
    private String childNumberFilter = null;
    private AdminModel currentAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_history);
        
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
        adapter = new CallHistoryAdapter(filteredCallHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
                    currentAdmin = snapshot.getValue(AdminModel.class);
                    if (currentAdmin != null) {
                        loadCallHistory();
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
        
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference callHistoryRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_CALL_HISTORY_NODE)
                .child(uid);
        
        callHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCallHistory.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    CallHistory call = child.getValue(CallHistory.class);
                    if (call != null) {
                        allCallHistory.add(call);
                    }
                }
                
                applyFilters();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CallHistoryActivity.this, "Error loading call history: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void applyFilters() {
        filteredCallHistory.clear();
        
        for (CallHistory call : allCallHistory) {
            // Apply child number filter if specified
            if (childNumberFilter != null && !childNumberFilter.equals(call.getChildNumber())) {
                continue;
            }
            
            // Apply call type filter
            if (selectedFilter.equals("all") || 
                selectedFilter.equals(call.getCallType().toLowerCase()) ||
                (selectedFilter.equals("premium") && call.isPremiumCall())) {
                filteredCallHistory.add(call);
            }
        }
        
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
                emptyStateText.setText("No call history found");
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
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
        
        List<CallHistory> searchResults = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (CallHistory call : filteredCallHistory) {
            if (call.getContactName() != null && call.getContactName().toLowerCase().contains(lowerQuery) ||
                call.getContactNumber() != null && call.getContactNumber().contains(query)) {
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
        super.onBackPressed();
    }
}
