package com.mehboob.dialeradmin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.mehboob.dialeradmin.adapters.CallHis_AdapterFavouriteDetails;
import com.mehboob.dialeradmin.database.Favorite;
import com.mehboob.dialeradmin.databinding.EmergencyFragmentBinding;

import java.util.ArrayList;
import java.util.List;

public class CallHis_EmergencyFragment extends Fragment {

    EmergencyFragmentBinding binding;
    CallHis_AdapterFavouriteDetails adapter;
    List<Favorite> user = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = EmergencyFragmentBinding.inflate(getLayoutInflater());

        user.add(new Favorite("Fire","101"));
        user.add(new Favorite("LPG Leak Helpline","1906"));
        user.add(new Favorite("Police","100"));
        user.add(new Favorite("Women Helpline","1091"));
        user.add(new Favorite("Disaster Management Service","112"));
        user.add(new Favorite("Ambulance","108"));
        user.add(new Favorite("Aids Helpline","1097"));
        user.add(new Favorite("Railway Enquiry","139"));
        user.add(new Favorite("National emergency number","112"));

        adapter = new CallHis_AdapterFavouriteDetails(getContext(),user);
        binding.rvEmergenceList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEmergenceList.setAdapter(adapter);
        return binding.getRoot();
    }
}