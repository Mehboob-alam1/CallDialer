package com.mehboob.dialeradmin.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mehboob.dialeradmin.R;
import com.mehboob.dialeradmin.databinding.FragmentCallHisSettingBinding;


public class CallHis_SettingFragment extends Fragment {

    FragmentCallHisSettingBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCallHisSettingBinding.inflate(getLayoutInflater());

        binding.llPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(""));
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(getActivity(), "App not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("text/plain");
                intent.putExtra("android.intent.extra.SUBJECT", "I’ve use this Application. Download on Google Play..\n\n");
                StringBuilder sb2 = new StringBuilder();
                sb2.append("https://play.google.com/store/apps/details?id=");
                sb2.append("getParentFragment().getPa");
                intent.putExtra("android.intent.extra.TEXT", sb2.toString());
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    startActivity(Intent.createChooser(intent, "Share App..."));
                }
            }
        });

        binding.llRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id="+ "")));
                }catch (Exception e){
                    Toast.makeText(getActivity(), "App not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return binding.getRoot();
    }
}