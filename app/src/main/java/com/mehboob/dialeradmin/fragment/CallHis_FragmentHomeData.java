package com.mehboob.dialeradmin.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mehboob.dialeradmin.R;
import com.mehboob.dialeradmin.adapters.CallHis_AdapterOrganicPage;
import com.mehboob.dialeradmin.databinding.FragmentCallHisHomeDataBinding;


public class CallHis_FragmentHomeData extends Fragment {

    FragmentCallHisHomeDataBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCallHisHomeDataBinding.inflate(getLayoutInflater());

        CallHis_AdapterOrganicPage adapter = new CallHis_AdapterOrganicPage(getActivity());
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setUserInputEnabled(true);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Contacts");
                    break;
                case 1:
                    tab.setText("Emergency");
                    break;
                case 2:
                    tab.setText("Logs");
                    break;
            }
        }).attach();

        for (int i = 0; i < binding.tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = binding.tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(getTabView(tab.getText().toString(), i == 0));
            }
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabAppearance(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabAppearance(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        return binding.getRoot();
    }

    private View getTabView(String text, boolean isSelected) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
        TextView tabText = view.findViewById(R.id.tabText);
        tabText.setText(text);

        if (isSelected) {
            tabText.setBackgroundResource(R.drawable.tab_selected);
            tabText.setTextColor(Color.WHITE);
        } else {
            tabText.setBackgroundColor(Color.TRANSPARENT);
            tabText.setTextColor(Color.BLACK);
        }

        return view;
    }

    // Update tab appearance on selection change
    private void updateTabAppearance(TabLayout.Tab tab, boolean isSelected) {
        View view = tab.getCustomView();
        if (view != null) {
            TextView tabText = view.findViewById(R.id.tabText);
            if (isSelected) {
                tabText.setBackgroundResource(R.drawable.tab_selected);
                tabText.setTextColor(Color.WHITE);
            } else {
                tabText.setBackgroundColor(Color.TRANSPARENT);
                tabText.setTextColor(Color.BLACK);
            }
        }
    }
}