package com.kristian.czepluch.snoozification.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kristian.czepluch.snoozification.R;
import com.kristian.czepluch.snoozification.Datastructures.User;

import java.util.List;

public class RecyclerViewAdapterNearUsers extends RecyclerView.Adapter<RecyclerViewAdapterNearUsers.MyViewHolderNearUsers> {

    private Context myContext;
    private List<User> myFriends;
    private RecyclerViewAdapterRulesOverview.OnCategoryClicked listener;

    public RecyclerViewAdapterNearUsers(Context myContext, List<User> myFriends, RecyclerViewAdapterRulesOverview.OnCategoryClicked listener ) {
        this.myContext = myContext;
        this.myFriends = myFriends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolderNearUsers onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View myView = LayoutInflater.from(myContext).inflate(R.layout.friends_item,viewGroup,false);
        final MyViewHolderNearUsers myViewHolder = new MyViewHolderNearUsers(myView, listener);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolderNearUsers myViewHolder, int i) {
        myViewHolder.textView.setText(myFriends.get(i).getName());
        myViewHolder.line.setAlpha(0.5f);
    }

    @Override
    public int getItemCount() {
        return myFriends.size();
    }

    public static class MyViewHolderNearUsers extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textView;
        private View line;
        private RecyclerViewAdapterRulesOverview.OnCategoryClicked listener;

        public MyViewHolderNearUsers(View itemView, RecyclerViewAdapterRulesOverview.OnCategoryClicked listener){
            super(itemView);
            this.listener = listener;
            textView = itemView.findViewById(R.id.friends_textView);
            line = itemView.findViewById(R.id.lineView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onCategoryItemClicked(getAdapterPosition());
        }
    }

}
