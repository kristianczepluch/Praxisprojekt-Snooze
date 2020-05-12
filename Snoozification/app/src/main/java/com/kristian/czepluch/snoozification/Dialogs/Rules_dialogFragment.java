package com.kristian.czepluch.snoozification.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

import com.kristian.czepluch.snoozification.Adapters.RecyclerViewAdapterUserRulesDialog;
import com.kristian.czepluch.snoozification.Datastructures.Category;
import com.kristian.czepluch.snoozification.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rules_dialogFragment extends AppCompatDialogFragment {

    private RecyclerView mRecyclerView;
    private ArrayList<Category> mAppArrayList = new ArrayList<>();
    private String title;
    private ArrayList<String> allCategories;
    private ArrayList<Drawable> allIcons;
    private List<Category> myCategories;
    private Context c;
    private String rules;
    private String name;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mDialogView = inflater.inflate(R.layout.dialog_all_rules, null);

        c = getContext();
        allCategories = new ArrayList<>(Arrays.asList("Spiele", "Kommunikation", "Social Media", "News", "Bildung", "Finanzen & Business", "Fitness & Gesundheit", "Essen & Drinks", "Lifestyle", "Andere"));
        allIcons = new ArrayList<>(Arrays.asList(c.getDrawable(R.drawable.games),c.getDrawable(R.drawable.message), c.getDrawable(R.drawable.socialmedia),
                c.getDrawable(R.drawable.news), c.getDrawable(R.drawable.education), c.getDrawable(R.drawable.money), c.getDrawable(R.drawable.fitness), c.getDrawable(R.drawable.food),
                c.getDrawable(R.drawable.socialmedia), c.getDrawable(R.drawable.more2)));

        myCategories = getCurrentCategorieObjects(rules);

        mRecyclerView = mDialogView.findViewById(R.id.all_rules_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerViewAdapterUserRulesDialog adapter = new RecyclerViewAdapterUserRulesDialog(getContext(), myCategories);
        mRecyclerView.setAdapter(adapter);


        builder.setView(mDialogView);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog myDialog = builder.create();
        myDialog.setTitle(Html.fromHtml("<font color='#FFFFFF'>"+"Regeln von: "+ name + " </font>"));
        myDialog.getWindow().setBackgroundDrawableResource(R.color.colorPrimary);
        return myDialog;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public void setData(String rules, String name){
        this.rules = rules;
        this.name = name;
    }

    public List<Category> getCurrentCategorieObjects(String rules){
        myCategories = new ArrayList<Category>();
        char[] myRules = rules.toCharArray();
        for(int i=0;i<myRules.length;i++) {
            if(myRules[i]=='0'){
                myCategories.add(new Category(allIcons.get(i),allCategories.get(i),false));
            } else {
                myCategories.add(new Category(allIcons.get(i),allCategories.get(i),true));
            }
        }
        return myCategories;
    }
}