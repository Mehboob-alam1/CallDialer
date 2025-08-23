package com.mehboob.dialeradmin.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.mehboob.dialeradmin.R;
import com.mehboob.dialeradmin.databinding.ItemAlldataBinding;
import com.mehboob.dialeradmin.models.CallHis_DataModel;

import java.util.ArrayList;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.RenderScriptBlur;


public class CallHis_AdapterlData extends RecyclerView.Adapter<CallHis_AdapterlData.AllHistoryAdapterViewHolder> {

    Context context;
    ArrayList<CallHis_DataModel> allHistoryData;
    ViewGroup viewGroup;

    public CallHis_AdapterlData(Context context, ArrayList<CallHis_DataModel> allHistoryData) {
        this.context = context;
        this.allHistoryData = allHistoryData;
    }

    @NonNull
    @Override
    public AllHistoryAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        viewGroup = parent;
        return new AllHistoryAdapterViewHolder(LayoutInflater.from(context).inflate(R.layout.item_alldata, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AllHistoryAdapterViewHolder holder, int position) {
        CallHis_DataModel historyModel = allHistoryData.get(position);

        holder.binding.title.setText(historyModel.getName());
        holder.binding.totalMessage.setText(historyModel.getTotalNumber());
        Glide.with(context).load(historyModel.getIcon()).into(holder.binding.img);

        Drawable backgroundDrawable = holder.binding.img.getBackground();
        if (backgroundDrawable != null) {
            DrawableCompat.setTint(backgroundDrawable, Color.parseColor(historyModel.getTintColor()));
            holder.binding.img.setBackground(backgroundDrawable);
        }

        float radius = 4f;

        // Root BlurTarget defined in XML
        BlurTarget target = holder.itemView.findViewById(R.id.target);

        // Background to clear
        Drawable windowBackground = ((Activity) context).getWindow().getDecorView().getBackground();

        holder.binding.topBlurView.setupWith(target)
                .setFrameClearDrawable(windowBackground)

                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);
    }


    @Override
    public int getItemCount() {
        return allHistoryData.size();
    }

    class AllHistoryAdapterViewHolder extends RecyclerView.ViewHolder {

        ItemAlldataBinding binding;

        public AllHistoryAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ItemAlldataBinding.bind(itemView);
        }
    }
}
