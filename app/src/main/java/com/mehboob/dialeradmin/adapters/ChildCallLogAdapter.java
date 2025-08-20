package com.mehboob.dialeradmin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mehboob.dialeradmin.R;
import com.mehboob.dialeradmin.models.ChildCallLog;

import java.util.List;

public class ChildCallLogAdapter extends RecyclerView.Adapter<ChildCallLogAdapter.ViewHolder> {
    
    private List<ChildCallLog> callLogs;
    private OnCallLogClickListener listener;
    
    public interface OnCallLogClickListener {
        void onCallLogClick(ChildCallLog callLog);
    }
    
    public ChildCallLogAdapter(List<ChildCallLog> callLogs) {
        this.callLogs = callLogs;
    }
    
    public void setOnCallLogClickListener(OnCallLogClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_call_log, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChildCallLog callLog = callLogs.get(position);
        holder.bind(callLog);
    }
    
    @Override
    public int getItemCount() {
        return callLogs.size();
    }
    
    public void updateData(List<ChildCallLog> newCallLogs) {
        this.callLogs = newCallLogs;
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCallType;
        private TextView tvContactNumber;
        private TextView tvChildNumber;
        private TextView tvCallType;
        private TextView tvDuration;
        private TextView tvDate;
        private TextView tvTime;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCallType = itemView.findViewById(R.id.ivCallType);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            tvChildNumber = itemView.findViewById(R.id.tvChildNumber);
            tvCallType = itemView.findViewById(R.id.tvCallType);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCallLogClick(callLogs.get(position));
                }
            });
        }
        
        public void bind(ChildCallLog callLog) {
            Context context = itemView.getContext();
            
            // Set call type icon and color
            ivCallType.setImageResource(callLog.getCallTypeIcon());
            tvCallType.setTextColor(context.getResources().getColor(callLog.getCallTypeColor()));
            
            // Set contact number
            String displayNumber = callLog.getNumber();
            if (callLog.getContactName() != null && !callLog.getContactName().isEmpty()) {
                displayNumber = callLog.getContactName() + " (" + callLog.getNumber() + ")";
            }
            tvContactNumber.setText(displayNumber);
            
            // Set child number
            tvChildNumber.setText("Child: " + callLog.getChildNumber());
            
            // Set call type
            tvCallType.setText(callLog.getCallTypeDisplay());
            
            // Set duration
            tvDuration.setText(callLog.getFormattedDuration());
            
            // Set date and time
            tvDate.setText(callLog.getFormattedDateOnly());
            tvTime.setText(callLog.getFormattedTime());
        }
    }
}
