package com.kristian.czepluch.snoozification.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kristian.czepluch.snoozification.Datastructures.Anwendung;
import com.kristian.czepluch.snoozification.R;

import java.util.ArrayList;

public class RecyclerViewAdapterAppDialog extends RecyclerView.Adapter<RecyclerViewAdapterAppDialog.MyViewHolderAppDialog> {

    private Context mContext;
    private ArrayList<Anwendung> mApps;

    public RecyclerViewAdapterAppDialog(Context mContext, ArrayList<Anwendung> mApps) {
        this.mContext = mContext;
        this.mApps = mApps;
    }

    @NonNull
    @Override
    public MyViewHolderAppDialog onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View myView = LayoutInflater.from(mContext).inflate(R.layout.app_item_row,viewGroup,false);
        final MyViewHolderAppDialog myViewHolder = new MyViewHolderAppDialog(myView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolderAppDialog myViewHolderAppDialog, int i) {
        myViewHolderAppDialog.appNameTextView.setText(mApps.get(i).getPackageName());
        myViewHolderAppDialog.appImageView.setImageDrawable(mApps.get(i).getLogo());
    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }

    public static class MyViewHolderAppDialog extends RecyclerView.ViewHolder{

        private TextView appNameTextView;
        private ImageView appImageView;

        public MyViewHolderAppDialog(View itemView){
            super(itemView);
            appNameTextView = itemView.findViewById(R.id.anwendungsname_TextView);
            appImageView = itemView.findViewById(R.id.anwendung_imageView);

        }
    }
}


