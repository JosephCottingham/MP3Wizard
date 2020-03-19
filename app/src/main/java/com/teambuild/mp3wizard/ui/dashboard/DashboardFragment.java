package com.teambuild.mp3wizard.ui.dashboard;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.HomeActivity;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.ui.dataStorage;


import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {
    private FileOutputStream fileOutputStream;
    private DashboardViewModel dashboardViewModel;
    private StorageReference StorageReference;
    private StorageReference ref;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private ListView listView;
    private FirebaseListAdapter firebaseListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        root = listViewPopulate(root);
        final TextView textView = root.findViewById(R.id.text_dashboard);

        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }



    protected View listViewPopulate(View root) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final String userId = mFirebaseUser.getUid();
//        storageRef = storage.getReference();

        listView = root.findViewById(R.id.cloudListView);
        Query query = FirebaseDatabase.getInstance().getReference().child(userId);
        FirebaseListOptions<Book> options = new FirebaseListOptions.Builder<Book>()
                .setLayout(R.layout.book_info)
                .setLifecycleOwner(DashboardFragment.this)
                .setQuery(query, Book.class)
                .build();
        firebaseListAdapter = new FirebaseListAdapter(options){
            @Override
            protected void populateView(View v, Object model, int position){
                TextView bookTitle = v.findViewById(R.id.bookTitle);
                TextView bookFile = v.findViewById(R.id.bookFile);
                TextView bookTime = v.findViewById(R.id.bookTime);
                TextView bookDownload = v.findViewById(R.id.bookDownloaded);
                Button bookBtn = v.findViewById(R.id.bookDownloadBtn);
                String[] downloadFileList = dataStorage.ReadDownloadedList(getContext(), userId);

                final Book book = (Book) model;

                bookTitle.setText(book.getTitle().toString());
                bookFile.setText(String.format("File %s / %s", book.getCurrentFile(), book.getFileNum()));
                int totalSec = Integer.parseInt(book.getLocSec());
                int hour = (totalSec%3600);
                int min = ((totalSec-(3600*hour))%60);
                int sec = (totalSec-((3600*hour)+(60*min)));
                String time = String.format("%d:%d:%d", hour, min, sec);
                bookTime.setText(time);
                Log.d("Hello", "populateView setDownloaded: " + book.getDownloaded());
                for (String line : downloadFileList)
                    if (line.equals(book.getTitle())){
                        Log.d("Hello", "populateView Line: " + line);
                        Log.d("Hello", "populateView Get Title: " + book.getTitle());
                        Log.d("Hello", "populateView setDownloaded: " + book.getDownloaded());
                        book.setDownloaded("Downloaded");
                        bookBtn.setEnabled(false);
                    }
                bookDownload.setText(book.getDownloaded());
                bookBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DownloadRefSetup(book, userId);
                    }
                });
            }
        };
        listView.setAdapter(firebaseListAdapter);
        return root;
    }
    private boolean DownloadRefSetup(final Book book, final String userId){
        Log.d("Hello", "DownloadRefSetup: " + book.getFileNum() + " " + book.getTitle() + " " + book.getLocSec());
        for(int fileNum = 0; fileNum < Integer.parseInt(book.getFileNum()); fileNum++) {
            StorageReference = firebaseStorage.getInstance().getReference("users" + File.separator + userId + File.separator + book.getTitle() + File.separator);
            ref = StorageReference.child(String.format("%d.mp3", fileNum));
            Log.d("Hello", "DownloadRefSetup: " + ref.getPath());
            Log.d("Hello", "DownloadRefSetup: " + StorageReference.getName() + " | " + Integer.toString(fileNum));
            final int fileNumName = fileNum;
//            Log.d("Hello", "DownloadRefSetup: " + StorageReference.getDownloadUrl().toString());
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String dir = getContext().getFilesDir().getAbsolutePath();
                    File FileDir = new File(dir + File.separator + mFirebaseUser.getUid() + File.separator + book.getTitle());
                    FileDir.mkdirs();
                    Log.d("Hello", "onSuccess X : " + FileDir.getAbsolutePath());
                    Log.d("Hello", "onSuccess: URL " + uri.toString());
                    Log.d("Hello", "onSuccess: ");
                    downloadfile(getContext(), String.format("%d.mp3", fileNumName), FileDir.getAbsolutePath(), uri.toString());
                    while (!(dataStorage.writeDownloadedList(book, getContext(), userId)));
                }
            });
        }
        return true;
    }
    private void downloadfile(Context context, String fileName, String destinationDirectory, String url){

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName);

        downloadManager.enqueue(request);
        Log.d("Hello", "downloadfile: Downloaded");
    }

    private void removeDownloadFile() {
        try {
            String dir = getContext().getFilesDir().getAbsolutePath();
            File FileDir = new File(dir + "/" + mFirebaseUser.getUid() + "/", "downloaded.txt");
            PrintWriter writer = new PrintWriter(FileDir);
            writer.close();

            Log.d("Hello", "removeDownloadFile: ");
        } catch (IOException ex){

        }
    }
}