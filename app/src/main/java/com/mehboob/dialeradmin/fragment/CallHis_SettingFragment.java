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
import com.mehboob.dialeradmin.SetttingActivity;
import com.mehboob.dialeradmin.WebActivity;
import com.mehboob.dialeradmin.databinding.FragmentCallHisSettingBinding;


public class CallHis_SettingFragment extends Fragment {

    FragmentCallHisSettingBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCallHisSettingBinding.inflate(getLayoutInflater());

        binding.llPrivacy.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), WebActivity.class);
            intent.putExtra("url", "https://easyranktools.com/privacy.html");
            startActivity(intent);
        });

        binding.llTerms.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), WebActivity.class);
            intent.putExtra("url", "https://easyranktools.com/terms.html");
            startActivity(intent);
        });
        binding.llShare.setOnClickListener(view -> {
            String packageName = requireActivity().getPackageName(); // get current app package name

            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.SUBJECT", "I’ve use this Application. Download on Google Play..\n\n");
            StringBuilder sb2 = new StringBuilder();
            sb2.append("https://play.google.com/store/apps/details?id=");
            sb2.append(packageName);
            intent.putExtra("android.intent.extra.TEXT", sb2.toString());
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                startActivity(Intent.createChooser(intent, "Share App..."));
            }
        });

        binding.llRate.setOnClickListener(view -> {
            String packageName = requireActivity().getPackageName(); // get current app package name

            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id="+ packageName)));
            }catch (Exception e){
                Toast.makeText(getActivity(), "App not found", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }
}