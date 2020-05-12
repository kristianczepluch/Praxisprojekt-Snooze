package com.kristian.czepluch.snoozification.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

import com.kristian.czepluch.snoozification.Adapters.RecyclerViewAdapterAppDialog;
import com.kristian.czepluch.snoozification.Datastructures.Anwendung;
import com.kristian.czepluch.snoozification.R;

import java.util.ArrayList;

public class Applications_dialogFragment extends AppCompatDialogFragment {

    private RecyclerView mRecyclerView;
    private ArrayList<Anwendung> mAppArrayList = new ArrayList<>();
    private String title;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mDialogView = inflater.inflate(R.layout.dialog_all_applications, null);

        mRecyclerView = mDialogView.findViewById(R.id.all_applications_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerViewAdapterAppDialog adapter = new RecyclerViewAdapterAppDialog(getContext(), mAppArrayList);
        mRecyclerView.setAdapter(adapter);


        builder.setView(mDialogView);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog myDialog = builder.create();
        myDialog.setTitle(Html.fromHtml("<font color='#FFFFFF'>"+"Deine Apps der Kategorie: "+title+"</font>"));
        myDialog.getWindow().setBackgroundDrawableResource(R.color.colorPrimary);
        return myDialog;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public void setData(ArrayList<Anwendung> mAppArrayList, String title){
        this.mAppArrayList = mAppArrayList;
        this.title = title;
    }
}