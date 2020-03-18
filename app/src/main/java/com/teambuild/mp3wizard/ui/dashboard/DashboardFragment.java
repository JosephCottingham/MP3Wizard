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
                String[] downloadFileList = ReadDownloadedList();

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
    private boolean DownloadRefSetup(final Book book, String userId){
        Log.d("Hello", "DownloadRefSetup: " + book.getFileNum() + " " + book.getTitle() + " " + book.getLocSec());
        for(int fileNum = 0; fileNum < Integer.parseInt(book.getFileNum()); fileNum++) {
            StorageReference = firebaseStorage.getInstance().getReference("users/" + userId + "/" + book.getTitle() + "/");
            ref = StorageReference.child(String.format("%d.mp3", fileNum));
            Log.d("Hello", "DownloadRefSetup: " + ref.getPath());
            Log.d("Hello", "DownloadRefSetup: " + StorageReference.getName() + " | " + Integer.toString(fileNum));
            final int fileNumName = fileNum;
//            Log.d("Hello", "DownloadRefSetup: " + StorageReference.getDownloadUrl().toString());
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String dir = getContext().getFilesDir().getAbsolutePath();
                    File FileDir = new File(dir + "/" + mFirebaseUser.getUid() + "/" + book.getTitle());
                    FileDir.mkdirs();
                    dir = FileDir.getAbsolutePath();
                    Log.d("Hello", "onSuccess X : " + dir);
                    Log.d("Hello", "onSuccess: URL " + uri.toString());
                    Log.d("Hello", "onSuccess: ");
                    downloadfile(getContext(), String.format("%d.mp3", fileNumName), FileDir.getAbsolutePath(), uri.toString());
                    while (!(addDownloadToDownloadedList(book)));
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
    private boolean addDownloadToDownloadedList(final Book book){
        String dir = getContext().getFilesDir().getAbsolutePath();
        String userId = mFirebaseUser.getUid();
        File FileDir = new File(dir + "/" + userId + "/");
        String path = FileDir.getAbsolutePath();
        try {
            new File(path).mkdir();
            File file = new File(path + "downloaded.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write((book.getTitle() + System.getProperty("line.separator")).getBytes());

            FileDir = new File(dir + "/" + userId + "/" + book.getTitle() + "/");
            path = FileDir.getAbsolutePath();

            new File(path).mkdir();
            file = new File(path + "loc.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write((book.getCurrentFile() + System.getProperty("line.separator") +
                    book.getFileNum() + System.getProperty("line.separator") + book.getLocSec() +
                    System.getProperty("line.separator")).getBytes());

            return true;
        }  catch(FileNotFoundException ex) {
            Log.d("Hello", ex.getMessage());
        }  catch(IOException ex) {
            Log.d("Hello", ex.getMessage());
        }
        return  false;

    }


    private String[] ReadDownloadedList(){
        String line = null;
        String dir = getContext().getFilesDir().getAbsolutePath();
        File FileDir = new File(dir + "/" + mFirebaseUser.getUid() + "/", "downloaded.txt");
        try {
            FileInputStream fileInputStream = new FileInputStream (FileDir);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.d("Hello", ex.getMessage());
        }
        catch(IOException ex) {
            Log.d("Hello", ex.getMessage());
        }
        Log.d("Hello", "ReadFile: Line " + line);
        return line.split(System.getProperty("line.separator"));
    }

    private void removeDownloadFile() {
        try {
            String dir = getContext().getFilesDir().getAbsolutePath();
            File FileDir = new File(dir + "/" + mFirebaseUser.getUid() + "/", "downloaded.txt");
            PrintWriter writer = new PrintWriter(FileDir);
            writer.close();

            Log.d("test", "removeDownloadFile: " + ReadDownloadedList());
            Log.d("Hello", "removeDownloadFile: ");
        } catch (IOException ex){

        }
    }
}