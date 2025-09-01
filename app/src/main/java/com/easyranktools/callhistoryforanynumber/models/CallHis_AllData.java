package com.easyranktools.callhistoryforanynumber.models;

import com.easyranktools.callhistoryforanynumber.R;

import java.util.ArrayList;

public class CallHis_AllData {
    public ArrayList<CallHis_DataModel> getAllHistoryData() {
        ArrayList<CallHis_DataModel> historyModels = new ArrayList<>();
        historyModels.add(new CallHis_DataModel(R.drawable.ic_call, "Call History App", "160" , "#FFE4F2", R.drawable.item_icon_bg));
        historyModels.add(new CallHis_DataModel(R.drawable.ic_whatsapp, "Whatsapp History", "74" , "#C0FCEC", R.drawable.item_icon_bg));
        historyModels.add(new CallHis_DataModel(R.drawable.ic_instgram, "Instagram History", "36" , "#FFDBD1", R.drawable.item_icon_bg));
        historyModels.add(new CallHis_DataModel(R.drawable.ic_sms, "SMS History", "144" , "#E3F2FF", R.drawable.item_icon_bg));

        return historyModels;
    }
}
