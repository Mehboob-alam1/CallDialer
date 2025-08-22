package com.mehboob.dialeradmin.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mehboob.dialeradmin.adapters.CallHis_AdapterContactList;
import com.mehboob.dialeradmin.database.AppDatabase;
import com.mehboob.dialeradmin.database.User;
import com.mehboob.dialeradmin.databinding.ContactlistFragmentBinding;

import java.util.ArrayList;
import java.util.List;


public class CallHis_ContactsFragment extends Fragment {

    ContactlistFragmentBinding binding;
    CallHis_AdapterContactList adapter;
    AppDatabase appDatabase;

    private static final int REQUEST_CONTACT_PERMISSION = 100;

    List<User> phoneContacts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ContactlistFragmentBinding.inflate(getLayoutInflater());
        AppDatabase appDatabase = AppDatabase.getInstance(getContext());

        List<User> dbUsers = appDatabase.userDao().getAllUsers();
        checkContactPermission();

        // Merge both lists
        dbUsers.addAll(phoneContacts);



        adapter = new CallHis_AdapterContactList(getContext(), dbUsers);
        binding.rvContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvContactList.setAdapter(adapter);

        if (dbUsers.isEmpty()) {
            binding.tvNodata.setVisibility(View.VISIBLE);
        } else {
            binding.tvNodata.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    private void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
        } else {
            phoneContacts  =getAllContacts(requireContext());
        }
    }

    private List<User> getAllContacts(Context context) {
        List<User> contacts = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                @SuppressLint("Range")
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                User user = new User();
                user.setUsername(name);
                user.setMobile(phoneNumber);
                contacts.add(user);
            }
            cursor.close();
        }
        return contacts;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               phoneContacts= getAllContacts(requireContext());
            } else {
                Toast.makeText(requireContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
