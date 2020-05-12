package com.kristian.czepluch.snoozification.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kristian.czepluch.snoozification.Datastructures.MyContact;
import com.kristian.czepluch.snoozification.R;

import java.util.ArrayList;

public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.MyViewHolder3> {

    private Context myContext;
    private ArrayList<MyContact> myFriends;

    public ContactsRecyclerViewAdapter(Context myContext, ArrayList<MyContact> myFriends) {
        this.myContext = myContext;
        this.myFriends = myFriends;
    }

    @NonNull
    @Override
    public MyViewHolder3 onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View myView = LayoutInflater.from(myContext).inflate(R.layout.contact_item,viewGroup,false);
        final MyViewHolder3 myViewHolder = new MyViewHolder3(myView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder3 myViewHolder, int i) {
        myViewHolder.benutzername.setText("~" + myFriends.get(i).getBenutzername());
        myViewHolder.kontaktname.setText(myFriends.get(i).getKontaktname());
        myViewHolder.benutzername.setAlpha(0.8f);
    }

    @Override
    public int getItemCount() {
        return myFriends.size();
    }

    public static class MyViewHolder3 extends RecyclerView.ViewHolder{

        private TextView benutzername;
        private TextView kontaktname;

        public MyViewHolder3(View itemView){
            super(itemView);
            benutzername = itemView.findViewById(R.id.username_item_textView);
            kontaktname = itemView.findViewById(R.id.contactsname_item_textView);
        }
    }

}
