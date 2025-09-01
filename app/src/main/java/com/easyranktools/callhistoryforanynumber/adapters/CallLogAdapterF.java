package com.easyranktools.callhistoryforanynumber.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.easyranktools.callhistoryforanynumber.R;
import com.easyranktools.callhistoryforanynumber.models.CallLogModel;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class CallLogAdapterF extends RecyclerView.Adapter<CallLogAdapterF.LogViewHolder> {

    private List<CallLogModel> logs;

    public CallLogAdapterF(List<CallLogModel> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_logf, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        CallLogModel log = logs.get(position);
        holder.number.setText("Number: " + log.number);
        holder.type.setText("Type: " + log.type);
        holder.duration.setText("Duration: " + log.duration + "s");
        holder.date.setText("Time: " + DateFormat.getDateTimeInstance().format(new Date(log.timestamp)));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView number, type, duration, date;

        LogViewHolder(View view) {
            super(view);
            number = view.findViewById(R.id.number);
            type = view.findViewById(R.id.type);
            duration = view.findViewById(R.id.duration);
            date = view.findViewById(R.id.date);
        }
    }
}
