package com.teambuild.mp3wizard.repository;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.PlayerActivity;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.repository.database.cloud.CloudFirebaseDatabase;
import com.teambuild.mp3wizard.repository.database.cloud.CloudListAdapterFirebase;
import com.teambuild.mp3wizard.repository.database.local.LocalListAdapterSQLITE;
import com.teambuild.mp3wizard.repository.database.local.LocalSQLiteDatabase;

public class RepositorySingleton {
    private static RepositorySingleton inst;


    private LocalSQLiteDatabase localSQLiteDatabase;
    private CloudFirebaseDatabase cloudFirebaseDatabase;


    public static void set(RepositorySingleton instance) {
        inst = instance;
    }

    public static synchronized RepositorySingleton getInstance() {
        if (inst == null)
            inst = new RepositorySingleton();
        return inst;
    }

    private RepositorySingleton(){
        localSQLiteDatabase = new LocalSQLiteDatabase();
        cloudFirebaseDatabase = new CloudFirebaseDatabase();
    }

    public LocalListAdapterSQLITE getLocalListAdapterSQLITE(Context context){
        return localSQLiteDatabase.getLocalListAdapterSQLITE(context);
    }

    public CloudListAdapterFirebase getCloudListAdapterFirebase(LifecycleOwner owner){
        return cloudFirebaseDatabase.getFirebaseListAdapter(owner);
    }

    public Book getLocalBookByID(String ID){
        return localSQLiteDatabase.getBookWithID(ID);
    }

    public boolean areCurrentCloudAndLocalLocationsEqual(String bookID) {
        Book book = getLocalBookByID(bookID);
        Log.d("AudioSystems", "areCurrentCloudAndLocalLocationsEqual: Firebase: " + cloudFirebaseDatabase.getFirebaseLocSec(book.getTitle()));
        Log.d("AudioSystems", "areCurrentCloudAndLocalLocationsEqual: SQLITEbase: " + book.getLocSecAsInt());

        if (Math.abs(cloudFirebaseDatabase.getFirebaseLocSec(book.getTitle()) - book.getLocSecAsInt()) > 30) {
            return false;
        }
        return true;
    }

    public int getFirebaseLoc(String bookID){
        return cloudFirebaseDatabase.getFirebaseLocSec(getLocalBookByID(bookID).getTitle());
    }

    public int getSQLITELoc(String bookID){
        return getLocalBookByID(bookID).getLocSecAsInt();
    }

    public void setLocationToCloudValue(String bookID){
        Book tempBook = getLocalBookByID(bookID);
        tempBook.setLocSec(String.valueOf(cloudFirebaseDatabase.getFirebaseLocSec(tempBook.getTitle())));
        setCurrentLocation(tempBook);
    }

    public void setLocationToLocalValue(String bookID){
        setCurrentLocation(getLocalBookByID(bookID));
    }

    public void setCurrentLocation(Book book){
        localSQLiteDatabase.updateCurLoc(book);
        cloudFirebaseDatabase.updateCurLoc(book);
    }
}
