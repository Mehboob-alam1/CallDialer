package com.easyranktools.callhistoryforanynumber.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.easyranktools.callhistoryforanynumber.CallManager;
import com.easyranktools.callhistoryforanynumber.R;

public class DialPadFragment extends Fragment {
    private static final int CALL_PERMISSION_REQUEST_CODE = 101;
    private static final long VIBRATION_DURATION = 50;

    private TextView phoneNumberTextView, contactNameTextView;
    private ImageView deleteButton;
    private FloatingActionButton callButton;
    private MaterialCardView displayCard, dialpadCard;
    private String phoneNumber = "";
    private long callStartTime = 0;
    private Vibrator vibrator;

    // Animation constants
    private static final long BUTTON_ANIMATION_DURATION = 150;
    private static final long CARD_ANIMATION_DURATION = 300;

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

        initViews(view);
        setupDialPadButtons(view);
        setupCallButton();
        setupDeleteButton();
        setupAnimations();

        // Initialize vibrator for haptic feedback
        vibrator = (Vibrator) requireContext().getSystemService(requireContext().VIBRATOR_SERVICE);

        return view;
    }

    private void initViews(View view) {
        phoneNumberTextView = view.findViewById(R.id.phoneNumberTextView);
        contactNameTextView = view.findViewById(R.id.contactNameTextView);
        deleteButton = view.findViewById(R.id.deleteButton);
        callButton = view.findViewById(R.id.callButton);
        displayCard = view.findViewById(R.id.displayCard);
        dialpadCard = view.findViewById(R.id.dialpadCard);

        // Set initial state for phone number display
        phoneNumberTextView.setText("Enter number");
        phoneNumberTextView.setAlpha(0.6f);
    }

    private void setupDialPadButtons(View view) {
        int[] buttonIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnStar, R.id.btnHash};

        for (int id : buttonIds) {
            MaterialButton button = view.findViewById(id);

            button.setOnClickListener(v -> {
                String digit = getDigitFromButton((MaterialButton) v);
                appendDigit(digit);
                animateButtonPress(v);
                hapticFeedback();
            });

            // Enhanced touch feedback
            button.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        animateButtonDown(v);
                        return false; // Let onClick handle the actual click
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        animateButtonUp(v);
                        return false;
                }
                return false;
            });
        }
    }

    private String getDigitFromButton(MaterialButton button) {
        String text = button.getText().toString();
        if (text.contains("\n")) {
            return text.substring(0, text.indexOf("\n"));
        }
        return text.substring(0, 1);
    }

    private void appendDigit(String digit) {
        // If showing placeholder text, clear it
        if (phoneNumber.isEmpty()) {
            phoneNumberTextView.setText("");
            phoneNumberTextView.setAlpha(1.0f);
        }

        phoneNumber += digit;

        // Animate text change
        animateTextChange(phoneNumberTextView, phoneNumber);

        // Show delete button with animation
        if (deleteButton.getVisibility() != View.VISIBLE) {
            animateDeleteButtonVisibility(true);
        }

        // Animate display card
        animateCardPress(displayCard);

        checkContactMatch();
    }

    private void setupCallButton() {
        callButton.setOnClickListener(v -> {
            if (!phoneNumber.isEmpty()) {
                animateCallButtonPress();
                if (checkCallPermission()) {
                    makePhoneCall();
                }
            } else {
                animateCallButtonError();
                Toast.makeText(getContext(), "Enter a phone number", Toast.LENGTH_SHORT).show();
            }
        });

        // Initial scale animation for call button
        callButton.setScaleX(0f);
        callButton.setScaleY(0f);
    }

    private void setupDeleteButton() {
        deleteButton.setOnClickListener(v -> {
            if (!phoneNumber.isEmpty()) {
                phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 1);

                if (phoneNumber.isEmpty()) {
                    phoneNumberTextView.setText("Enter number");
                    phoneNumberTextView.setAlpha(0.6f);
                    animateDeleteButtonVisibility(false);
                    contactNameTextView.setVisibility(View.GONE);
                } else {
                    animateTextChange(phoneNumberTextView, phoneNumber);
                    checkContactMatch();
                }

                animateButtonPress(v);
                hapticFeedback();
            }
        });
    }

    private void setupAnimations() {
        // Entrance animations
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            animateCardEntrance(displayCard, 0);
            animateCardEntrance(dialpadCard, 150);
            animateCallButtonEntrance();
        }, 100);
    }

    private void animateCardEntrance(View view, long delay) {
        view.setTranslationY(100f);
        view.setAlpha(0f);

        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(CARD_ANIMATION_DURATION)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();
    }

    private void animateCallButtonEntrance() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            callButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(CARD_ANIMATION_DURATION)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }, 400);
    }

    private void animateButtonPress(View button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(50)
                .withEndAction(() -> {
                    button.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                })
                .start();
    }

    private void animateButtonDown(View button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .alpha(0.8f)
                .setDuration(50)
                .start();
    }

    private void animateButtonUp(View button) {
        button.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(100)
                .setInterpolator(new OvershootInterpolator(0.5f))
                .start();
    }

    private void animateTextChange(TextView textView, String newText) {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 1.1f, 1f);
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            textView.setScaleX(scale);
            textView.setScaleY(scale);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                textView.setText(newText);
            }
        });

        animator.start();
    }

    private void animateDeleteButtonVisibility(boolean show) {
        if (show) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setScaleX(0f);
            deleteButton.setScaleY(0f);
            deleteButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        } else {
            deleteButton.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(150)
                    .withEndAction(() -> deleteButton.setVisibility(View.INVISIBLE))
                    .start();
        }
    }

    private void animateCardPress(View card) {
        card.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(50)
                .withEndAction(() -> {
                    card.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void animateCallButtonPress() {
        callButton.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() -> {
                    callButton.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                })
                .start();
    }

    private void animateCallButtonError() {
        ObjectAnimator shake = ObjectAnimator.ofFloat(callButton, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(500);
        shake.start();
    }

    private void hapticFeedback() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(VIBRATION_DURATION, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(VIBRATION_DURATION);
            }
        }
    }

    private boolean checkCallPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
            return false;
        }
    }

    private void makePhoneCall() {
        try {
            callStartTime = System.currentTimeMillis();

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

            if (callIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                // Add call button animation before making call
                ObjectAnimator pulseAnimator = ObjectAnimator.ofFloat(callButton, "alpha", 1f, 0.3f, 1f);
                pulseAnimator.setDuration(300);
                pulseAnimator.setRepeatCount(2);
                pulseAnimator.start();

                startActivity(callIntent);
                saveCallToHistory(phoneNumber, "OUTGOING");
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
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkContactMatch() {
        // Enhanced contact matching with animation
        String contactName = getContactName(phoneNumber);
        if (!contactName.equals("Unknown") && !contactName.isEmpty()) {
            contactNameTextView.setText(contactName);
            if (contactNameTextView.getVisibility() != View.VISIBLE) {
                contactNameTextView.setVisibility(View.VISIBLE);
                contactNameTextView.setAlpha(0f);
                contactNameTextView.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
            }
        } else {
            if (contactNameTextView.getVisibility() == View.VISIBLE) {
                contactNameTextView.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> contactNameTextView.setVisibility(View.GONE))
                        .start();
            }
        }
    }

    private void saveCallToHistory(String number, String type) {
        long endTime = System.currentTimeMillis();
        long duration = (endTime - callStartTime) / 1000;

        String contactName = getContactName(number);

        CallManager.getInstance().saveCallToFirebase(
                number,
                contactName,
                type,
                callStartTime,
                endTime,
                duration
        );
    }

    private String getContactName(String phoneNumber) {
        try {
            android.database.Cursor cursor = requireContext().getContentResolver().query(
                    android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                    android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
                    new String[]{phoneNumber},
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String name = nameIndex >= 0 ? cursor.getString(nameIndex) : null;
                cursor.close();
                return name != null ? name : "Unknown";
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            // Handle exception gracefully
        }
        return "Unknown";
    }

    @Override
    public void onResume() {
        super.onResume();
        CallManager.getInstance().syncCallLogs(requireContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up animations
        if (callButton != null) {
            callButton.clearAnimation();
        }
        if (displayCard != null) {
            displayCard.clearAnimation();
        }
        if (dialpadCard != null) {
            dialpadCard.clearAnimation();
        }
    }
}