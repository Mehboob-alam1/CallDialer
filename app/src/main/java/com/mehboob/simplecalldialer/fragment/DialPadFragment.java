package com.mehboob.simplecalldialer.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mehboob.simplecalldialer.R;

public class DialPadFragment extends Fragment {
    private static final int CALL_PERMISSION_REQUEST_CODE = 101;

    private TextView phoneNumberTextView, contactNameTextView;
    private ImageView deleteButton;
    private FloatingActionButton callButton;
    private String phoneNumber = "";

    // Permission launcher for newer API
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    makePhoneCall();
                } else {
                    showPermissionDeniedDialog();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dial_pad, container, false);

        phoneNumberTextView = view.findViewById(R.id.phoneNumberTextView);
        contactNameTextView = view.findViewById(R.id.contactNameTextView);
        deleteButton = view.findViewById(R.id.deleteButton);
        callButton = view.findViewById(R.id.callButton);

        setupDialPadButtons(view);
        setupCallButton();
        setupDeleteButton();

        return view;
    }

    private void setupDialPadButtons(View view) {
        int[] buttonIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnStar, R.id.btnHash};

        for (int id : buttonIds) {
            view.findViewById(id).setOnClickListener(v -> {
                MaterialButton button = (MaterialButton) v;
                String digit = button.getText().toString().substring(0, 1);
                appendDigit(digit);
            });

            // Add ripple effect
            view.findViewById(id).setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.performClick();
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                }
                return true;
            });
        }
    }

    private void appendDigit(String digit) {
        phoneNumber += digit;
        phoneNumberTextView.setText(phoneNumber);

        // Show delete button when there's input
        if (phoneNumber.length() > 0) {
            deleteButton.setVisibility(View.VISIBLE);
        }

        checkContactMatch();
    }

    private void setupCallButton() {
        callButton.setOnClickListener(v -> {
            if (phoneNumber.length() > 0) {
                if (checkCallPermission()) {
                    makePhoneCall();
                }
            } else {
                Toast.makeText(getContext(), "Enter a phone number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkCallPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // Request the permission
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
            return false;
        }
    }

    private void makePhoneCall() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

            // Verify that the intent will resolve to an activity
            if (callIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(callIntent);
                addToCallHistory(phoneNumber, "OUTGOING");
            } else {
                Toast.makeText(getContext(), "No app can handle this call", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Call permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPermissionDeniedDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Permission Required")
                .setMessage("This app needs call permission to make phone calls. Please grant the permission in app settings.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    // Open app settings
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupDeleteButton() {
        deleteButton.setOnClickListener(v -> {
            if (phoneNumber.length() > 0) {
                phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 1);
                phoneNumberTextView.setText(phoneNumber);

                if (phoneNumber.isEmpty()) {
                    deleteButton.setVisibility(View.INVISIBLE);
                    contactNameTextView.setVisibility(View.GONE);
                } else {
                    checkContactMatch();
                }
            }
        });
    }

    private void checkContactMatch() {
        // Implement contact matching logic here
        // If match found:
        // contactNameTextView.setText(contactName);
        // contactNameTextView.setVisibility(View.VISIBLE);
    }

    private void addToCallHistory(String number, String type) {
        // Implement call history saving logic
        // You can save to database or shared preferences
    }
}