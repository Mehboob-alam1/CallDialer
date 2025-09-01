package com.easyranktools.callhistoryforanynumber.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.easyranktools.callhistoryforanynumber.R;
import com.easyranktools.callhistoryforanynumber.database.AppDatabase;
import com.easyranktools.callhistoryforanynumber.database.Favorite;
import com.easyranktools.callhistoryforanynumber.database.User;
import com.easyranktools.callhistoryforanynumber.databinding.FragmentCallHisAddtoContactBinding;

import java.util.Arrays;
import java.util.List;


public class CallHis_AddtoContactFragment extends Fragment {

    FragmentCallHisAddtoContactBinding binding;
    private AppDatabase database;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCallHisAddtoContactBinding.inflate(getLayoutInflater());

        database = AppDatabase.getInstance(getActivity());
        binding.phoneNumberDisplay.setShowSoftInputOnFocus(false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Integer> buttonIds = Arrays.asList(
                R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_4,
                R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9,
                R.id.button_star, R.id.button_hash, R.id.button_plus
        );

        for (int id : buttonIds) {
            view.findViewById(id).setOnClickListener(v -> {
                if (v instanceof LinearLayout) {
                    TextView textView = (TextView) ((LinearLayout) v).getChildAt(0);
                    binding.phoneNumberDisplay.append(textView.getText().toString());
                    String currentText = binding.phoneNumberDisplay.getText().toString();
                    if (!currentText.isEmpty()) {
                        binding.llSaveOption.setVisibility(View.VISIBLE);
                    }else {
                        binding.llSaveOption.setVisibility(View.GONE);
                    }
                }
            });
        }

        binding.buttonDelete.setOnClickListener(v -> {
            String currentText = binding.phoneNumberDisplay.getText().toString();
            if (!currentText.isEmpty()) {
                binding.phoneNumberDisplay.setText(currentText.substring(0, currentText.length() - 1));
            }
            if (!currentText.isEmpty()) {
                binding.llSaveOption.setVisibility(View.VISIBLE);
            }else {
                binding.llSaveOption.setVisibility(View.GONE);
            }
        });

        binding.buttonCall.setOnClickListener(v -> {
            String phoneNumber = binding.phoneNumberDisplay.getText().toString();
            makeCall(phoneNumber);
        });

        binding.llCreateContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.add_contact_dialog);
                ((EditText) bottomSheetDialog.findViewById(R.id.et_number)).setText(binding.phoneNumberDisplay.getText().toString());
                bottomSheetDialog.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = ((EditText) bottomSheetDialog.findViewById(R.id.et_name)).getText().toString();
                        String number = ((EditText) bottomSheetDialog.findViewById(R.id.et_number)).getText().toString();
                        saveUser(name, number,bottomSheetDialog);
                    }
                });

                bottomSheetDialog.show();
            }
        });

        binding.llAddFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.add_contact_dialog);
                ((EditText) bottomSheetDialog.findViewById(R.id.et_number)).setText(binding.phoneNumberDisplay.getText().toString());
                bottomSheetDialog.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = ((EditText) bottomSheetDialog.findViewById(R.id.et_name)).getText().toString();
                        String number = ((EditText) bottomSheetDialog.findViewById(R.id.et_number)).getText().toString();
                        savefavoriteUser(name, number,bottomSheetDialog);
                    }
                });
                bottomSheetDialog.show();
            }
        });

        binding.llSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:"));
                intent.putExtra("sms_body", "");
                startActivity(intent);
            }
        });
    }

    private void makeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        String temp = "tel:" + number;
        intent.setData(Uri.parse(temp));
        startActivity(intent);
    }

    private void saveUser(String username,String number,BottomSheetDialog bottomSheetDialog) {
        if (username.isEmpty() && number.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = new User(username, number);
        database.userDao().insertUser(user);
        Toast.makeText(getActivity(), "Contact Saved", Toast.LENGTH_SHORT).show();
        bottomSheetDialog.dismiss();
    }

    private void savefavoriteUser(String username,String number,BottomSheetDialog bottomSheetDialog) {
        if (username.isEmpty() && number.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        Favorite favorite = new Favorite(username, number);
        database.favoriteDao().insertUser(favorite);
        Toast.makeText(getActivity(), "Contact Saved", Toast.LENGTH_SHORT).show();
        bottomSheetDialog.dismiss();
    }
}