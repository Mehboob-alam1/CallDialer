package com.mehboob.dialeradmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mehboob.dialeradmin.R;
import com.mehboob.dialeradmin.models.CallLogEntry;

import java.util.List;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder> {
    private List<CallLogEntry> callLogs;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position, CallLogEntry callLog);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public CallLogAdapter(List<CallLogEntry> callLogs) {
        this.callLogs = callLogs;
    }

    @NonNull
    @Override
    public CallLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call_log, parent, false);
        return new CallLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallLogViewHolder holder, int position) {
        CallLogEntry callLog = callLogs.get(position);

        // Set contact name or number if name not available
        if (!callLog.getName().isEmpty()) {
            holder.nameTextView.setText(callLog.getName());
            holder.numberTextView.setText(callLog.getNumber());
            holder.numberTextView.setVisibility(View.VISIBLE);
        } else {
            holder.nameTextView.setText(callLog.getNumber());
            holder.numberTextView.setVisibility(View.GONE);
        }

        // Set call time and duration
        holder.timeTextView.setText(callLog.getTime());
        if (!callLog.getDuration().isEmpty()) {
            holder.durationTextView.setText(callLog.getDuration());
            holder.durationTextView.setVisibility(View.VISIBLE);
        } else {
            holder.durationTextView.setVisibility(View.GONE);
        }

        // Set call type icon and color
        int iconColor = R.color.gray;
        switch (callLog.getType()) {
            case "INCOMING":
                iconColor = R.color.green;
                break;
            case "MISSED":
                iconColor = R.color.red;
                break;
            case "OUTGOING":
                iconColor = R.color.blue;
                break;
        }

        holder.typeIcon.setImageResource(callLog.getIconRes());
        holder.typeIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), iconColor));

        // Set contact avatar
        String avatarText = callLog.getName().isEmpty() ? "#" : callLog.getName().substring(0, 1);
        holder.avatarTextView.setText(avatarText);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position, callLog);
            }
        });
    }

    @Override
    public int getItemCount() {
        return callLogs.size();
    }

    public void filterList(List<CallLogEntry> filteredList) {
        callLogs = filteredList;
        notifyDataSetChanged();
    }

    static class CallLogViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, numberTextView, timeTextView, durationTextView, avatarTextView;
        ImageView typeIcon;

        CallLogViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.callName);
            numberTextView = itemView.findViewById(R.id.callNumber);
            timeTextView = itemView.findViewById(R.id.callTime);
            durationTextView = itemView.findViewById(R.id.callDuration);
            typeIcon = itemView.findViewById(R.id.callTypeIcon);
            avatarTextView = itemView.findViewById(R.id.avatarText);
        }
    }
}