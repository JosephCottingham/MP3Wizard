package com.teambuild.mp3wizard.repository.database.cloud;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teambuild.mp3wizard.repository.database.cloud.CloudFirebaseDatabase;
import com.teambuild.mp3wizard.repository.database.local.LocalSQLiteDatabase;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.R;

import java.util.ArrayList;
import java.util.Map;

public class CloudFirebaseDatabase {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private ArrayList<Map<String, Object>> firebaseData;

    public CloudFirebaseDatabase(){
        firebaseData = new ArrayList<Map<String, Object>>();
        configFirebaseAuth();
        setBookLocationListeners();
    }

    public CloudListAdapterFirebase getFirebaseListAdapter(LifecycleOwner owner) {

        Query query = com.google.firebase.database.FirebaseDatabase.getInstance().getReference().child(mFirebaseUser.getUid());
        FirebaseListOptions<Book> options = new FirebaseListOptions.Builder<Book>()
                .setLayout(R.layout.book_info)
                .setLifecycleOwner(owner)
                .setQuery(query, Book.class)
                .build();

        return new CloudListAdapterFirebase(options);
    }

    private void configFirebaseAuth(){
        if (mFirebaseAuth==null) mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseUser==null) mFirebaseUser = mFirebaseAuth.getCurrentUser();
    }

    private void setBookLocationListeners(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(mFirebaseAuth.getCurrentUser().getUid());
        ChildEventListener liveDataBaseListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                firebaseData.add((Map<String, Object>) dataSnapshot.getValue());
                for (String keys : (firebaseData.get(firebaseData.size()-1).keySet()))
                {
                    Log.d("FirebaseDatabase", "onChildAdded: " + keys);
                    Log.d("FirebaseDatabase", "onChildAdded: " + firebaseData.get(firebaseData.size()-1).get(keys).toString());
                }
                Log.d("FirebaseDatabase", "ENDEDD ++++++++++++++++++++++++++: ");

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Map<String, Object> tempChange = (Map<String, Object>) dataSnapshot.getValue();

                for (String keys : tempChange.keySet())
                {
                    Log.d("FirebaseDatabase", "onChildChanged: " + keys);
                    Log.d("FirebaseDatabase", "onChildChanged: " + tempChange.get(keys).toString());
                }

                for (int x = 0; x < firebaseData.size(); x++){
                    if (firebaseData.get(x).get("title").equals(tempChange.get("title"))){
                        firebaseData.set(x, tempChange);
                        return;
                    }
                }
                firebaseData.add(tempChange);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> tempRemove = (Map<String, Object>) dataSnapshot.getValue();

                for (String keys : tempRemove.keySet())
                {
                    Log.d("FirebaseDatabase", "onChildRemoved: " + keys);
                    Log.d("FirebaseDatabase", "onChildRemoved: " + tempRemove.get(keys).toString());
                }

                for (int x = 0; x < firebaseData.size(); x++){
                    if (firebaseData.get(x).get("title").equals(tempRemove.get("title"))){
                        firebaseData.remove(x);
                        return;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("FirebaseTestCase", "loadPost:onCancelled", databaseError.toException());

            }
        };
        Log.d("FirebaseDatabase", "setBookLocationListeners: ");

        databaseReference.addChildEventListener(liveDataBaseListener);
    }

    public int getFirebaseLocSec(String title){
        for (int x = 0; x < firebaseData.size(); x++){
            if (firebaseData.get(x).get("title").equals(title)){
                return Integer.parseInt(firebaseData.get(x).get("locSec").toString());
            }
        }
    return -1;
    }

    public void updateCurLoc(Book book){
        FirebaseDatabase.getInstance().getReference().child(mFirebaseAuth.getCurrentUser().getUid()).child(book.getTitle()).child("locSec").setValue(book.getLocSec());
    }
}
