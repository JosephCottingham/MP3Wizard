package com.teambuild.mp3wizard.ui.dashboard;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.PlayerActivity;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.ui.localStorageDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

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
    private localStorageDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Log.d("TEST", "onCreateView Dashboard Fragment: " + getApplicationContext().getPackageName());
        db = new localStorageDatabase(getApplicationContext());

        root = listViewPopulate(root);
        ArrayList<Book> arrayList = db.getAllDownloadData();

        for (int x = 0; x < arrayList.size(); x++){
            Book book = arrayList.get(x);
            Log.d("TEST", "printDB: DBRow " + String.valueOf(x) + ": " + book.getTitle() + " | " + book.getCurrentFile() + " | " + book.getFileNum() + " | " + book.getLocSec() + " | " + book.getID());
        }
        final TextView textView = root.findViewById(R.id.text_dashboard);

        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void getMusic(){
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(audioUri, null, null, null, null);
        if (cursor!=null && cursor.moveToFirst()){
            int SongTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int SongPath = cursor.getColumnIndex (MediaStore.Audio.Media.DATA);
            int songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songAudiobook = cursor.getColumnIndex(MediaStore.Audio.Media.IS_AUDIOBOOK);
            int songAlbum = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

            Log.d("Download", "downloadfile: Path: " + Environment.DIRECTORY_AUDIOBOOKS);
            do {
                String curTitle = cursor.getString(SongTitle);
                String curPath = cursor.getString(SongPath);
                String curArtist = cursor.getString(songArtist);
                String curAudiobook = cursor.getString(songAudiobook);
                String curAlbum = cursor.getString(songAlbum);

                Log.d("test", "getMusic: " + curArtist + " | "+ curTitle + " | " + curPath + " | " + curAudiobook + " | " + curAlbum);
            } while (cursor.moveToNext());
        }
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

        firebaseListAdapter = new CloudListAdapterFirebase(options);
        listView.setAdapter(firebaseListAdapter);
        return root;
    }

}