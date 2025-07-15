package com.mehboob.simplecalldialer.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.mehboob.simplecalldialer.R;
import com.mehboob.simplecalldialer.models.CallLogItem;

import java.util.List;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder> {

    private final List<CallLogItem> callLogs;

    public CallLogAdapter(List<CallLogItem> callLogs) {
        this.callLogs = callLogs;
    }

    @NonNull
    @Override
    public CallLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call_history, parent, false);
        return new CallLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallLogViewHolder holder, int position) {
        CallLogItem item = callLogs.get(position);
        holder.name.setText(item.getNameOrNumber());
        holder.date.setText(item.getDate());

        switch (item.getType()) {
            case "INCOMING":
                holder.icon.setImageResource(android.R.drawable.sym_call_incoming);
                holder.icon.setColorFilter(0xFF4CAF50); // Green
                break;
            case "OUTGOING":
                holder.icon.setImageResource(android.R.drawable.sym_call_outgoing);
                holder.icon.setColorFilter(0xFF2196F3); // Blue
                break;
            case "MISSED":
                holder.icon.setImageResource(android.R.drawable.sym_call_missed);
                holder.icon.setColorFilter(0xFFF44336); // Red
                break;
        }
    }

    @Override
    public int getItemCount() {
        return callLogs.size();
    }

    static class CallLogViewHolder extends RecyclerView.ViewHolder {
        TextView name, date;
        ImageView icon;

        CallLogViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.callTypeIcon);
            name = view.findViewById(R.id.numberOrName);
            date = view.findViewById(R.id.callDate);
        }
    }
}
