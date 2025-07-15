package com.mehboob.simplecalldialer.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;

import com.mehboob.simplecalldialer.R;


public class DialPadFragment extends Fragment {

    private EditText inputNumber;
    private final String[] dialValues = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#"
    };

    public DialPadFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dial_pad, container, false);

        inputNumber = view.findViewById(R.id.inputNumber);
        GridLayout dialPad = view.findViewById(R.id.dial_pad);

        // Set text for each key
        for (int i = 0; i < dialPad.getChildCount(); i++) {
            View v = dialPad.getChildAt(i);
            if (v instanceof Button) {
                ((Button) v).setText(dialValues[i]);
                v.setOnClickListener(buttonClickListener);
            }
        }

        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            String current = inputNumber.getText().toString();
            if (!current.isEmpty()) {
                inputNumber.setText(current.substring(0, current.length() - 1));
            }
        });

        view.findViewById(R.id.btnCall).setOnClickListener(v -> {
            String phone = inputNumber.getText().toString();
            if (!phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phone));
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
            }
        });

        return view;
    }

    private final View.OnClickListener buttonClickListener = v -> {
        if (v instanceof Button) {
            String current = inputNumber.getText().toString();
            inputNumber.setText(current + ((Button) v).getText().toString());
        }
    };
}
