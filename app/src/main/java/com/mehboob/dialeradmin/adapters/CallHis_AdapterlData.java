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
import jp.wasabeef.blurry.Blurry;


public class CallHis_AdapterlData extends RecyclerView.Adapter<CallHis_AdapterlData.AllHistoryAdapterViewHolder> {

    private final Context context;
    private final ArrayList<CallHis_DataModel> allHistoryData;

    public CallHis_AdapterlData(Context context, ArrayList<CallHis_DataModel> allHistoryData) {
        this.context = context;
        this.allHistoryData = allHistoryData;
    }

    @NonNull
    @Override
    public AllHistoryAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alldata, parent, false);
        return new AllHistoryAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllHistoryAdapterViewHolder holder, int position) {
        CallHis_DataModel historyModel = allHistoryData.get(position);


        holder.binding.title.setText(historyModel.getName());
        holder.binding.totalMessage.setText(historyModel.getTotalNumber());

        // ✅ Safe Glide usage (binds to View lifecycle, not Activity)
        Glide.with(holder.itemView).load(historyModel.getIcon()).into(holder.binding.img);

        Drawable backgroundDrawable = holder.binding.img.getBackground();
        if (backgroundDrawable != null) {
            DrawableCompat.setTint(backgroundDrawable, Color.parseColor(historyModel.getTintColor()));
            holder.binding.img.setBackground(backgroundDrawable);
        }
//        ViewGroup rootView = (ViewGroup) holder.itemView;
//        Blurry.with(context)
//                .radius(50)      // blur radius
//                .sampling(2)     // downscale for performance
//                .async()         // do it on background thread
//                .onto(rootView); // apply blur overlay to the root view

      //  Blurry.with(context).capture(rootView).into(holder.binding.img);


    }

    @Override
    public int getItemCount() {
        return allHistoryData.size();
    }

    static class AllHistoryAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemAlldataBinding binding;

        public AllHistoryAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemAlldataBinding.bind(itemView);

//            // ✅ Setup blur ONCE here
//            float radius = 2f;
//            BlurTarget target = itemView.findViewById(R.id.target);
//
//            Drawable windowBackground = null;
//            Context ctx = itemView.getContext();
//            if (ctx instanceof Activity) {
//                windowBackground = ((Activity) ctx).getWindow().getDecorView().getBackground();
//            }
//
//            if (windowBackground != null) {
//                binding.topBlurView.setupWith(target)
//                        .setFrameClearDrawable(windowBackground)
//                        .setBlurRadius(radius)
//                        .setBlurAutoUpdate(true);
//            }
       }
    }
}

