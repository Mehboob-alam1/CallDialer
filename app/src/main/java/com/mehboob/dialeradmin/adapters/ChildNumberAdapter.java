package com.mehboob.dialeradmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mehboob.dialeradmin.R;

import java.util.List;

public class ChildNumberAdapter extends RecyclerView.Adapter<ChildNumberAdapter.ViewHolder> {
    
    private List<String> childNumbers;
    private OnChildNumberClickListener listener;
    
    public interface OnChildNumberClickListener {
        void onChildNumberClick(String number);
    }
    
    public ChildNumberAdapter(List<String> childNumbers, OnChildNumberClickListener listener) {
        this.childNumbers = childNumbers;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_number, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String number = childNumbers.get(position);
        holder.tvNumber.setText(number);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChildNumberClick(number);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return childNumbers.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
        }
    }
}
