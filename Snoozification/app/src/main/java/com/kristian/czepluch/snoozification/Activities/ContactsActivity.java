package com.kristian.czepluch.snoozification.Activities;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kristian.czepluch.snoozification.Adapters.ContactsRecyclerViewAdapter;
import com.kristian.czepluch.snoozification.Datastructures.MyContact;
import com.kristian.czepluch.snoozification.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView contacts_recycler_view;
    private ImageButton myBtn;
    private static final String TAG = "ContactsActivity";
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private ArrayList<String> arrayList;
    private ArrayList<MyContact> allContacts;
    private ArrayList<String> allNumbers;
    private ContactsRecyclerViewAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        myBtn = findViewById(R.id.contacts_btn);
        contacts_recycler_view = findViewById(R.id.contacts_recycler_view);
        progressBar = findViewById(R.id.contacts_progressbar);
        arrayList = new ArrayList<>();
        allContacts = new ArrayList<>();
        allNumbers = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setTitle("Kontakte");
        adapter = new ContactsRecyclerViewAdapter(this, allContacts);
        contacts_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        contacts_recycler_view.setAdapter(adapter);

        myBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DEFAULT,ContactsContract.Contacts.CONTENT_URI);
                startActivity(intent);
            }
        });
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference myRef = database.getReference().child("uuids").child(mAuth.getCurrentUser().getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot datas: dataSnapshot.getChildren()) {
                    if (!datas.getKey().equals("uuid")) {
                        allNumbers.add(datas.getKey());
                        Log.e(TAG, "Added: " + datas.getValue().toString());
                    }
                }
                for(final String contact : allNumbers){
                    DatabaseReference myRef = database.getReference().child("phones").child(contact);
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null) {
                                String friend = dataSnapshot.getValue().toString();
                                Query q = Contacts.getQuery();
                                q.whereEqualTo(Contact.Field.PhoneNumber, dataSnapshot.getKey());
                                List<Contact> contacts = q.find();
                                if(contacts.isEmpty()){
                                    Query qs = Contacts.getQuery();
                                    qs.whereEqualTo(Contact.Field.PhoneNumber, formatPhoneNumber(dataSnapshot.getKey()));
                                    List<Contact> contacts1 = q.find();
                                    allContacts.add(new MyContact(friend,contacts1.get(0).getDisplayName(), 0));
                                    adapter.notifyDataSetChanged();
                                }else{
                                    allContacts.add(new MyContact(friend,contacts.get(0).getGivenName(), 0));
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.e(TAG,"No informations found for: "+ dataSnapshot.getKey());
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(ContactsActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void writeContact(String displayName, String number) {
        ArrayList contentProviderOperations = new ArrayList();
        //insert raw contact using RawContacts.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null).withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        //insert contact display name using Data.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName).build());
        //insert mobile number using Data.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number).withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try {
            getApplicationContext().getContentResolver().
                    applyBatch(ContactsContract.AUTHORITY, contentProviderOperations);
        } catch (RemoteException e) {
            Toast.makeText(this, "y", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            Toast.makeText(this, "y", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String formatPhoneNumber(String phoneNumber){
            return phoneNumber = "0" + phoneNumber.substring(3);

    }

}
