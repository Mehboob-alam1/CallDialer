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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.FirebaseDatabase;
import com.mehboob.simplecalldialer.R;
import com.mehboob.simplecalldialer.adapters.CallLogAdapter;
import com.mehboob.simplecalldialer.models.CallLogEntry;
import com.mehboob.simplecalldialer.models.CallLogItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallHistoryFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private CallHistoryPagerAdapter pagerAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_history, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupViewPager();



        return view;
    }

    private void setupViewPager() {
        pagerAdapter = new CallHistoryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "All Calls" : "Missed")
        ).attach();
    }

    private static class CallHistoryPagerAdapter extends FragmentStateAdapter {
        public CallHistoryPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new AllCallsFragment() : new MissedCallsFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    public static class AllCallsFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_call_list, container, false);
            setupCallList(view, false);
            return view;
        }
    }

    public static class MissedCallsFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_call_list, container, false);
            setupCallList(view, true);
            return view;
        }
    }

    private static void setupCallList(View view, boolean missedOnly) {
        RecyclerView recyclerView = view.findViewById(R.id.callRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // Add divider between items
        DividerItemDecoration divider = new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        // Create and set adapter with sample data
        List<CallLogEntry> calls = getCallLogs(missedOnly);
        CallLogAdapter adapter = new CallLogAdapter(calls);
        recyclerView.setAdapter(adapter);
    }

    private static List<CallLogEntry> getCallLogs(boolean missedOnly) {
        // Implement your call log loading logic here
        List<CallLogEntry> calls = new ArrayList<>();
        // Add sample data
//        calls.add(new CallLogEntry("John Doe", "1234567890", "INCOMING", "2 min", "Today, 10:30 AM"));
     //   calls.add(new CallLogEntry("Jane Smith", "2345678901", "MISSED", "12", "Yesterday, 4:15 PM"));
        return calls;
    }

    // CallLogEntry model class




}