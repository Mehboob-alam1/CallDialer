package com.mehboob.simplecalldialer.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.mehboob.simplecalldialer.R;
import com.mehboob.simplecalldialer.adapters.ContactAdapter;
import com.mehboob.simplecalldialer.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private static final int PERMISSION_REQUEST = 100;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ContactAdapter contactAdapter;
    private List<Contact> allContacts;

    public ContactsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        recyclerView = view.findViewById(R.id.contactsRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allContacts = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST);
        } else {
            loadContacts();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (contactAdapter != null) {
                    contactAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        return view;
    }

    private void loadContacts() {
        allContacts.clear();
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        while (cursor != null && cursor.moveToNext()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER));
            allContacts.add(new Contact(name, number));
        }

        if (cursor != null) cursor.close();

        contactAdapter = new ContactAdapter(allContacts);
        recyclerView.setAdapter(contactAdapter);
    }
}

