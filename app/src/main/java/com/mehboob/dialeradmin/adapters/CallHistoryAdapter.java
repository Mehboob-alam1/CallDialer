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

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.CallHistoryViewHolder> {

    private List<CallHistory> callHistoryList;
    private OnCallHistoryClickListener listener;

    public CallHistoryAdapter(List<CallHistory> callHistoryList, OnCallHistoryClickListener listener) {
        this.callHistoryList = callHistoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CallHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call_history, parent, false);
        return new CallHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallHistoryViewHolder holder, int position) {
        CallHistory callHistory = callHistoryList.get(position);
        holder.bind(callHistory);
    }

    @Override
    public int getItemCount() {
        return callHistoryList.size();
    }

    public void updateData(List<CallHistory> newCallHistoryList) {
        this.callHistoryList = newCallHistoryList;
        notifyDataSetChanged();
    }

    class CallHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView contactNameTv, contactNumberTv, callTypeTv, callDateTv, callDurationTv;
        private ImageView callTypeIcon, premiumIcon;

        public CallHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            contactNameTv = itemView.findViewById(R.id.contactNameTv);
            contactNumberTv = itemView.findViewById(R.id.contactNumberTv);
            callTypeTv = itemView.findViewById(R.id.callTypeTv);
            callDateTv = itemView.findViewById(R.id.callDateTv);
            callDurationTv = itemView.findViewById(R.id.callDurationTv);
            callTypeIcon = itemView.findViewById(R.id.callTypeIcon);
            premiumIcon = itemView.findViewById(R.id.premiumIcon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCallHistoryClick(callHistoryList.get(position));
                }
            });
        }

        public void bind(CallHistory callHistory) {
            contactNameTv.setText(callHistory.getContactName());
            contactNumberTv.setText(callHistory.getContactNumber());
            callTypeTv.setText(callHistory.getCallType());
            callDateTv.setText(callHistory.getFormattedDate());
            callDurationTv.setText(callHistory.getFormattedDuration());

            // Set call type icon
            switch (callHistory.getCallType()) {
                case "INCOMING":
                    callTypeIcon.setImageResource(R.drawable.ic_call_received);
                    callTypeIcon.setColorFilter(itemView.getContext().getColor(android.R.color.holo_green_dark));
                    break;
                case "OUTGOING":
                    callTypeIcon.setImageResource(R.drawable.ic_call_made);
                    callTypeIcon.setColorFilter(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                    break;
                case "MISSED":
                    callTypeIcon.setImageResource(R.drawable.ic_call_missed);
                    callTypeIcon.setColorFilter(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    break;
                default:
                    callTypeIcon.setImageResource(R.drawable.ic_call);
                    callTypeIcon.setColorFilter(itemView.getContext().getColor(android.R.color.darker_gray));
                    break;
            }

            // Show premium icon if it's a premium call
            if (callHistory.getIsPremiumCall()) {
                premiumIcon.setVisibility(View.VISIBLE);
                premiumIcon.setImageResource(R.drawable.ic_premium);
            } else {
                premiumIcon.setVisibility(View.GONE);
            }

            // Show child number if available
            if (callHistory.getChildNumber() != null && !callHistory.getChildNumber().isEmpty()) {
                contactNumberTv.setText(callHistory.getContactNumber() + " (via " + callHistory.getChildNumber() + ")");
            }
        }
    }

    public interface OnCallHistoryClickListener {
        void onCallHistoryClick(CallHistory callHistory);
    }
}
