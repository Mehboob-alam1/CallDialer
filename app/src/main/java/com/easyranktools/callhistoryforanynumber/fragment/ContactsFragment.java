package com.easyranktools.callhistoryforanynumber.fragment;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.easyranktools.callhistoryforanynumber.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {
    private RecyclerView contactsRecyclerView;
    private ContactsAdapter contactsAdapter;
    private FloatingActionButton addContactFab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsRecyclerView = view.findViewById(R.id.contactsRecyclerView);
        addContactFab = view.findViewById(R.id.addContactFab);

        setupContactsRecyclerView();
        setupAddContactButton();

        return view;
    }

    private void setupContactsRecyclerView() {
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add divider between items
        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        contactsRecyclerView.addItemDecoration(divider);

        // Create and set adapter
        contactsAdapter = new ContactsAdapter(getContacts());
        contactsRecyclerView.setAdapter(contactsAdapter);
    }

    private List<Contact> getContacts() {
        // Implement your contact loading logic here
        List<Contact> contacts = new ArrayList<>();
        // Add sample data
        contacts.add(new Contact("John Doe", "1234567890"));
        contacts.add(new Contact("Jane Smith", "2345678901"));
        return contacts;
    }

    private void setupAddContactButton() {
        addContactFab.setOnClickListener(v -> {
            // Implement add contact functionality
        });
    }

    // Contact model class
    public static class Contact {
        public String name;
        public String phone;

        public Contact(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }
    }

    // Contacts Adapter
    private class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
        private List<Contact> contacts;

        public ContactsAdapter(List<Contact> contacts) {
            this.contacts = contacts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contact, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Contact contact = contacts.get(position);
            holder.nameTextView.setText(contact.name);
            holder.phoneTextView.setText(contact.phone);

            // Set first letter avatar
            holder.avatarTextView.setText(contact.name.substring(0, 1));

            holder.itemView.setOnClickListener(v -> {
                // Implement contact click action (e.g., dial the number)
            });
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, phoneTextView, avatarTextView;

            ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.contactName);
                phoneTextView = itemView.findViewById(R.id.contactPhone);
                avatarTextView = itemView.findViewById(R.id.contactAvatar);
            }
        }
    }
}

