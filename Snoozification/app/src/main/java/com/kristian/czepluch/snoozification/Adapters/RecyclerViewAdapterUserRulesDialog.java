package com.kristian.czepluch.snoozification.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.kristian.czepluch.snoozification.Datastructures.Category;
import com.kristian.czepluch.snoozification.R;

import java.util.List;

public class RecyclerViewAdapterUserRulesDialog extends RecyclerView.Adapter<RecyclerViewAdapterUserRulesDialog.MyViewHolderUserRulesDialog> {

    private Context myContext;
    private List<Category> myCategories;

    public RecyclerViewAdapterUserRulesDialog(Context myContext, List<Category> myCategories) {
        this.myContext = myContext;
        this.myCategories = myCategories;
    }

    @NonNull
    @Override
    public MyViewHolderUserRulesDialog onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View myView = LayoutInflater.from(myContext).inflate(R.layout.rules_item, viewGroup, false);
        final MyViewHolderUserRulesDialog myViewHolder = new MyViewHolderUserRulesDialog(myView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolderUserRulesDialog myViewHolder, int i) {

        myViewHolder.myText.setText(myCategories.get(i).getName());
        myViewHolder.mySwitch.setChecked(myCategories.get(i).isMySwitch());
        myViewHolder.photo.setImageDrawable((myCategories.get(i).getIcon()));
        myViewHolder.mySwitch.setFocusable(false);
        myViewHolder.mySwitch.setClickable(false);
    }

    @Override
    public int getItemCount() {
        return myCategories.size();
    }

    public static class MyViewHolderUserRulesDialog extends RecyclerView.ViewHolder {

        private TextView myText;
        private ImageView photo;
        private Switch mySwitch;

        public MyViewHolderUserRulesDialog(View itemView) {
            super(itemView);
            myText = itemView.findViewById(R.id.kategory_textView);
            photo = itemView.findViewById(R.id.rules_image);
            mySwitch = itemView.findViewById(R.id.category_switch);

        }
    }
}


