package com.kristian.czepluch.snoozification.Adapters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kristian.czepluch.snoozification.Datastructures.Category;
import com.kristian.czepluch.snoozification.Datastructures.Database;
import com.kristian.czepluch.snoozification.R;

import java.util.List;

public class RecyclerViewAdapterRulesOverview extends RecyclerView.Adapter<RecyclerViewAdapterRulesOverview.MyViewHolderRulesOverview> {

    private Context myContext;
    private List<Category> myCategories;
    private OnCategoryClicked listener;

    public RecyclerViewAdapterRulesOverview(Context myContext, List<Category> myCategories, OnCategoryClicked listener) {
        this.myContext = myContext;
        this.myCategories = myCategories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolderRulesOverview onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View myView = LayoutInflater.from(myContext).inflate(R.layout.rules_item,viewGroup,false);
        final MyViewHolderRulesOverview myViewHolderRulesOverview = new MyViewHolderRulesOverview(myView, listener);

        myViewHolderRulesOverview.mySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    Toast.makeText(myContext, "Keine Internetverbindung vorhanden", Toast.LENGTH_SHORT).show();
                    myViewHolderRulesOverview.mySwitch.toggle();
                } else {
                    Database db = new Database(myContext);
                    String currentRules = db.getRulesFromInternalStorage("rules");
                    StringBuilder myNewRules = new StringBuilder(currentRules);
                    boolean is = myViewHolderRulesOverview.mySwitch.isChecked();
                    if(is){
                        myNewRules.setCharAt(myViewHolderRulesOverview.getAdapterPosition(), '1');
                    } else myNewRules.setCharAt(myViewHolderRulesOverview.getAdapterPosition(), '0');
                    db.storeRulesOnInternalStorage("rules", myNewRules.toString());
                    String uuid = db.getRulesFromInternalStorage("uuid");
                    db.overrideRules(uuid, myNewRules.toString());
                }
            }
        });

        return myViewHolderRulesOverview;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolderRulesOverview myViewHolderRulesOverview, int i) {

        myViewHolderRulesOverview.myText.setText(myCategories.get(i).getName());
        myViewHolderRulesOverview.mySwitch.setChecked(myCategories.get(i).isMySwitch());
        myViewHolderRulesOverview.photo.setImageDrawable((myCategories.get(i).getIcon()));
    }

    @Override
    public int getItemCount() {
        return myCategories.size();
    }

    public static class MyViewHolderRulesOverview extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView myText;
        private ImageView photo;
        private Switch mySwitch;
        private OnCategoryClicked listener;

        public MyViewHolderRulesOverview(View itemView, OnCategoryClicked listener){
            super(itemView);
            myText = itemView.findViewById(R.id.kategory_textView);
            photo = itemView.findViewById(R.id.rules_image);
            mySwitch = itemView.findViewById(R.id.category_switch);
            this.listener = listener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onCategoryItemClicked(getAdapterPosition());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public interface OnCategoryClicked{
        void onCategoryItemClicked(int position);
    }

}


