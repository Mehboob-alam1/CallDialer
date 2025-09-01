package com.easyranktools.callhistoryforanynumber.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.easyranktools.callhistoryforanynumber.R;
import com.easyranktools.callhistoryforanynumber.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements Filterable {

    private final List<Contact> contactList;
    private List<Contact> filteredList;

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList;
        this.filteredList = new ArrayList<>(contactList);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = filteredList.get(position);
        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhone());
        holder.icon.setText(String.valueOf(contact.getName().charAt(0)).toUpperCase());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateList(List<Contact> newList) {
        contactList.clear();
        contactList.addAll(newList);
        filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence keyword) {
            List<Contact> results = new ArrayList<>();
            if (keyword == null || keyword.length() == 0) {
                results.addAll(contactList);
            } else {
                String filterPattern = keyword.toString().toLowerCase().trim();
                for (Contact contact : contactList) {
                    if (contact.getName().toLowerCase().contains(filterPattern) ||
                            contact.getPhone().toLowerCase().contains(filterPattern)) {
                        results.add(contact);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = results;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            filteredList = (List<Contact>) results.values;
            notifyDataSetChanged();
        }
    };

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone, icon;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.contactAvatar);
            name = itemView.findViewById(R.id.contactName);
            phone = itemView.findViewById(R.id.contactPhone);
        }
    }
}
