package com.mehboob.dialeradmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mehboob.dialeradmin.R;
import com.mehboob.dialeradmin.models.CallHistory;

import java.util.List;

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.ViewHolder> {
    
    private List<CallHistory> callHistoryList;
    
    public CallHistoryAdapter(List<CallHistory> callHistoryList) {
        this.callHistoryList = callHistoryList;
    }
    
    public void updateData(List<CallHistory> newData) {
        this.callHistoryList = newData;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallHistory call = callHistoryList.get(position);
        
        // Set contact name and number
        holder.tvContactName.setText(call.getContactName() != null ? call.getContactName() : "Unknown");
        holder.tvContactNumber.setText(call.getContactNumber());
        
        // Set call type with icon
        holder.tvCallType.setText(call.getCallType());
        setCallTypeIcon(holder.ivCallType, call.getCallType());
        
        // Set date and duration
        holder.tvDate.setText(call.getFormattedDate());
        holder.tvDuration.setText(call.getFormattedDuration());
        
        // Show premium indicator if it's a premium call
        if (call.isPremiumCall()) {
            holder.ivPremium.setVisibility(View.VISIBLE);
        } else {
            holder.ivPremium.setVisibility(View.GONE);
        }
        
        // Show child number if available
        if (call.getChildNumber() != null && !call.getChildNumber().isEmpty()) {
            holder.tvChildNumber.setVisibility(View.VISIBLE);
            holder.tvChildNumber.setText("Child: " + call.getChildNumber());
        } else {
            holder.tvChildNumber.setVisibility(View.GONE);
        }
    }
    
    private void setCallTypeIcon(ImageView imageView, String callType) {
        switch (callType.toUpperCase()) {
            case "INCOMING":
                imageView.setImageResource(R.drawable.ic_call_received);
                imageView.setColorFilter(imageView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "OUTGOING":
                imageView.setImageResource(R.drawable.ic_call_made);
                imageView.setColorFilter(imageView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case "MISSED":
                imageView.setImageResource(R.drawable.ic_call_missed);
                imageView.setColorFilter(imageView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                imageView.setImageResource(R.drawable.ic_phone);
                imageView.setColorFilter(imageView.getContext().getResources().getColor(android.R.color.darker_gray));
                break;
        }
    }
    
    @Override
    public int getItemCount() {
        return callHistoryList.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName, tvContactNumber, tvCallType, tvDate, tvDuration, tvChildNumber;
        ImageView ivCallType, ivPremium;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            tvCallType = itemView.findViewById(R.id.tvCallType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvChildNumber = itemView.findViewById(R.id.tvChildNumber);
            ivCallType = itemView.findViewById(R.id.ivCallType);
            ivPremium = itemView.findViewById(R.id.ivPremium);
        }
    }
}
