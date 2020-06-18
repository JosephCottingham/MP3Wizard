package com.teambuild.mp3wizard.repository;

import android.annotation.SuppressLint;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.PlayerActivity;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.repository.database.cloud.CloudFirebaseDatabase;
import com.teambuild.mp3wizard.repository.database.cloud.CloudListAdapterFirebase;
import com.teambuild.mp3wizard.repository.database.local.LocalListAdapterSQLITE;
import com.teambuild.mp3wizard.repository.database.local.LocalSQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class RepositorySingleton {
    private static RepositorySingleton inst;

    private Map<String, ProgressBar> progress;
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
        progress = new HashMap<String, ProgressBar>();
    }

    public LocalListAdapterSQLITE getLocalListAdapterSQLITE(Context context){
        return localSQLiteDatabase.getLocalListAdapterSQLITE(context);
    }

    public CloudListAdapterFirebase getCloudListAdapterFirebase(LifecycleOwner owner){
        Log.d("RepositorySingleton", "getCloudListAdapterFirebase: getCloudListAdapterFirebase");
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
    public String getFirebaseUserID(){
        return cloudFirebaseDatabase.getFirebaseUserID();
    }

    public boolean isDownloaded(String title){
        if (localSQLiteDatabase.getBookWithTitle(title)==null)
            return false;
        return true;
    }

    public Book getLocalBookByTitle(String title){
        return localSQLiteDatabase.getBookWithTitle(title);
    }

    public boolean DownloadAndConfigBook(final Book book){
        localSQLiteDatabase.addDownloadingItem(book.getTitle());

        // All books only have one icon file and therefore do not need to be rerefrenced
        final StorageReference iconRef = FirebaseStorage.getInstance().getReference("users" + File.separator + getFirebaseUserID() + File.separator + book.getTitle()).child("icon.png");
        @SuppressLint("RestrictedApi") final File rootPath = new File(getApplicationContext().getFilesDir() + File.separator + book.getTitle());
        // create directors if they do not exsist for stoarge of downloaded files
        if (!rootPath.exists()){
            rootPath.mkdirs();
        }
        final File localIconFile = new File(rootPath, "icon.png");

        // Download All Audio files
        for(int fileNum = 1; fileNum <= Integer.parseInt(book.getFileNum()); fileNum++) {
            StorageReference audioRef = FirebaseStorage.getInstance().getReference("users" + File.separator + getFirebaseUserID() + File.separator + book.getTitle()).child(String.format("%d.mp3", fileNum));
            final File localAudioFile = new File(rootPath, String.format("%d.mp3", fileNum));

            // download the files
            audioRef.getFile(localAudioFile).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                    progress.get(book.getTitle()).setProgress((int) ((int) 100.0 * ((double) taskSnapshot.getBytesTransferred() / (double)taskSnapshot.getTotalByteCount())));
                    Log.d("Download", "onProgress: " + 100.0 * ((double) taskSnapshot.getBytesTransferred() / (double)taskSnapshot.getTotalByteCount()));
                }
            }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    iconRef.getFile(localIconFile).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {

                            progress.get(book.getTitle()).setProgress((int) ((int) 100.0 * ((double) taskSnapshot.getBytesTransferred() / (double)taskSnapshot.getTotalByteCount())));
                            Log.d("Download", "onProgress: " + 100.0 * ((double) taskSnapshot.getBytesTransferred() / (double)taskSnapshot.getTotalByteCount()));
                        }
                    }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
                    // add book to database after all downing is complete
                    localSQLiteDatabase.addBook(book);
                    localSQLiteDatabase.removeDownloadingItem(book.getTitle());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
        return true;
    }


    public boolean isDownloading(String title){
        ArrayList<String> bookTitles = localSQLiteDatabase.getDownloadingItems();
        for (String bookTitle : bookTitles){
            if (bookTitle.equals(title)) return true;
        }
        return false;
    }

    public void setProgressBar(String title, ProgressBar progressBar){
        progress.put(title, progressBar);
    }

    public void signOut(){
        cloudFirebaseDatabase.signOut();
    }
}
