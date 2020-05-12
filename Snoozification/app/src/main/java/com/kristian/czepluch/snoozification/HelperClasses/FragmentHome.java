package com.kristian.czepluch.snoozification.HelperClasses;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;


import com.kristian.czepluch.snoozification.Activities.ContactsActivity;
import com.kristian.czepluch.snoozification.Adapters.RecyclerViewAdapterNearUsers;
import com.kristian.czepluch.snoozification.Adapters.RecyclerViewAdapterRulesOverview;
import com.kristian.czepluch.snoozification.BeaconConsumerApplication;
import com.kristian.czepluch.snoozification.Datastructures.Database;
import com.kristian.czepluch.snoozification.Datastructures.User;
import com.kristian.czepluch.snoozification.Datastructures.UserListeModel;
import com.kristian.czepluch.snoozification.Dialogs.Rules_dialogFragment;
import com.kristian.czepluch.snoozification.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends Fragment implements UserListeModel.UserListObserver, RecyclerViewAdapterRulesOverview.OnCategoryClicked {

    private View myView;
    private EditText benutzername_editText;
    private RecyclerView myRecyclerView;
    private List<User> currentList;
    private RecyclerViewAdapterNearUsers adapter;
    private final String TAG = this.getClass().getName();
    private ImageButton friends_btn;
    private Switch transmission_switch;
    private UserListeModel myUserListeModel;
    private TinyDB tinyDB;



    public FragmentHome() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Database db = new Database(getContext());
        tinyDB = new TinyDB(getContext());
        myView = inflater.inflate(R.layout.home_fragment, container, false);

        benutzername_editText = myView.findViewById(R.id.benutzername2_editText);
        String userName = db.getRulesFromInternalStorage("username");
        benutzername_editText.setText(userName);
        friends_btn = myView.findViewById(R.id.friends_btn);
        transmission_switch = myView.findViewById(R.id.transmitting_switch);
        boolean switch_state = tinyDB.getBoolean("app_running");
        transmission_switch.setChecked(switch_state);


        transmission_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkIfBluetoothEnabled() && isNetworkAvailable()) {
                    if(transmission_switch.isChecked()){
                        BeaconConsumerApplication beaconConsumerApplication = (BeaconConsumerApplication) getActivity().getApplication();
                        beaconConsumerApplication.startBeaconSearch();
                        tinyDB.putBoolean("app_running",true);

                    } else {
                        BeaconConsumerApplication beaconConsumerApplication = (BeaconConsumerApplication) getActivity().getApplication();
                        beaconConsumerApplication.stopBeaconSearch();
                        tinyDB.putBoolean("app_running",false);
                    }
                } else {
                    transmission_switch.toggle();
                    Toast.makeText(getContext(), "Bluetooth und Internet sind erforderlich", Toast.LENGTH_LONG).show();
                }
            }
        });

        friends_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ContactsActivity.class);
                startActivity(intent);
            }
        });

        myRecyclerView = myView.findViewById(R.id.friends_recyclerView);
        myUserListeModel = UserListeModel.getInstance();
        myUserListeModel.registerObserver(this);
        currentList = new ArrayList<>(myUserListeModel.getCurrentList());
        adapter = new RecyclerViewAdapterNearUsers(getContext(), currentList, this);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(adapter);
        return myView;
    }
    

    @Override
    public void onUserDataChanged(ArrayList<User> users) {
        Log.e(TAG, "UserDataChange wurde ausgeführt! Liste: ");
        currentList.clear();
        currentList.addAll(users);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        myUserListeModel.removeObserver(this);
        super.onDestroyView();
    }

    @Override
    public void onCategoryItemClicked(int position) {
        Rules_dialogFragment dialogFragment = new Rules_dialogFragment();
        dialogFragment.setData(currentList.get(position).getRules(),currentList.get(position).getName());
        dialogFragment.show(getFragmentManager(),"App_Dialog2");
    }

    private boolean checkIfBluetoothEnabled(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

