package com.easyranktools.callhistoryforanynumber.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.easyranktools.callhistoryforanynumber.R;
import com.easyranktools.callhistoryforanynumber.database.Favorite;
import com.easyranktools.callhistoryforanynumber.databinding.ItemContactListBinding;

import java.util.List;

public class CallHis_AdapterFavouriteDetails extends RecyclerView.Adapter<CallHis_AdapterFavouriteDetails.ContactAdapterViewHolder> {

    Context context;
    List<Favorite> user;

    public CallHis_AdapterFavouriteDetails(Context context, List<Favorite> user) {
        this.context = context;
        this.user = user;
    }

    @NonNull
    @Override
    public ContactAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactAdapterViewHolder(LayoutInflater.from(context).inflate(R.layout.item_contact_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapterViewHolder holder, int position) {

        holder.binding.tvUserName.setText(user.get(position).username);
        holder.binding.tvUserNumber.setText(user.get(position).mobile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall(user.get(position).mobile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    class ContactAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemContactListBinding binding;

        public ContactAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ItemContactListBinding.bind(itemView);
        }
    }

    private void makeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        String temp = "tel:" + number;
        intent.setData(Uri.parse(temp));
        context.startActivity(intent);
    }
}
