package com.teambuild.mp3wizard.repository.database.cloud;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.PlayerActivity;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.repository.RepositorySingleton;
import com.teambuild.mp3wizard.repository.database.local.LocalSQLiteDatabase;

import java.io.File;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class CloudListAdapterFirebase extends FirebaseListAdapter {
    private StorageReference audioRef;
    private StorageReference iconRef;
    private StorageReference storageReference;
    private RepositorySingleton repository;

    public CloudListAdapterFirebase(FirebaseListOptions options){
        super(options);
    }
    @Override
    protected void populateView(View v, Object model, int position){
        repository = RepositorySingleton.getInstance();

        // Cast model to book, model is the current postion in the lists book
        final Book book = (Book) model;

        // Ref views, and set reference values
        TextView bookTitle = v.findViewById(R.id.bookTitle);
        TextView bookFile = v.findViewById(R.id.bookFile);
        TextView bookTime = v.findViewById(R.id.bookTime);
        TextView bookDownload = v.findViewById(R.id.bookDownloaded);
        Button bookDownloadBtn = v.findViewById(R.id.bookDownloadBtn);

        // populate views with values for current book
        bookTitle.setText(book.getTitle());
        bookFile.setText(String.format("File %s / %s", book.getCurrentFile(), book.getFileNum()));
        bookTime.setText(createTimeLabel(book.getLocSecAsInt()));

        // checks if book is logged in SQLITE database (It is logged after download so all content is fully aviable if found)
        // sets button to play or download based on that
        final Boolean downloaded=repository.isDownloaded(book.getTitle());
        if (downloaded){
            bookDownload.setText("Downloaded");
            bookDownloadBtn.setText("Play");
        } else {
            bookDownload.setText("Cloud");
            bookDownloadBtn.setText("Download");
        }

        // Set Listener and configure listener to work adaptivly with the state of download
        bookDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (downloaded){
                    // gets local version of book, which will have the unique SQLITE book ID required by player to setup book
                    Book localBookVersion = repository.getLocalBookByTitle(book.getTitle());

                    Intent playerIntent = new Intent(getApplicationContext(), PlayerActivity.class);

                    // package the BookId into a bundle and hand it off to the intent before implementing the intent
                    Bundle b = new Bundle();
                    b.putString("bookID", book.getID());
                    playerIntent.putExtras(b);
                    getApplicationContext().startActivity(playerIntent);
                } else {
                    // if it is not already downloaded, then it can be via this meathod call
                    repository.DownloadAndConfigBook(book);
                }
            }
        });

    }


    private String createTimeLabel(int totalSec) {
        int hour = (int)(totalSec/3600);
        int min = (int)((totalSec-(3600*hour))/60);
        int sec = (int)(totalSec-((3600*hour)+(60*min)));
        String minS = String.valueOf(min);
        String secS = String.valueOf(sec);
        if (min < 10) minS = "0" + minS;
        if (sec < 10) secS = "0" + secS;
        return String.format("%s:%s:%s", hour, minS, secS);
    }


}

