package com.teambuild.mp3wizard.ui.dashboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

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
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.ui.home.DownloadBookAdapter;
import com.teambuild.mp3wizard.ui.localStorageDatabase;

import java.io.File;
import java.net.URI;
import java.util.EventListener;
import java.util.Locale;

import io.opencensus.internal.Utils;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class CloudListAdapterFirebase extends FirebaseListAdapter {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference audioRef;
    private StorageReference iconRef;
    private StorageReference storageReference;
    private localStorageDatabase db;

    public CloudListAdapterFirebase(FirebaseListOptions options){
        super(options);
    }
    @Override
    protected void populateView(View v, Object model, int position){

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final String userId = mFirebaseUser.getUid();

        TextView bookTitle = v.findViewById(R.id.bookTitle);
        TextView bookFile = v.findViewById(R.id.bookFile);
        TextView bookTime = v.findViewById(R.id.bookTime);
        TextView bookDownload = v.findViewById(R.id.bookDownloaded);
        Button bookBtn = v.findViewById(R.id.bookDownloadBtn);

        final Book book = (Book) model;

        bookTitle.setText(book.getTitle().toString());
        bookFile.setText(String.format("File %s / %s", book.getCurrentFile(), book.getFileNum()));
        long totalSec = book.getLocSecAsLong();
        int hour = (int)(totalSec/3600);
        int min = (int)((totalSec-(3600*hour))/60);
        int sec = (int)(totalSec-((3600*hour)+(60*min)));
        String minS = String.valueOf(min);
        String secS = String.valueOf(sec);
        if (min < 10) minS = "0" + minS;
        if (sec < 10) secS += "0" + secS;
        String time = String.format("%s:%s:%s", hour, minS, secS);
        bookTime.setText(time);
        Log.d("Hello", "populateView setDownloaded: " + book.getDownloaded());
        bookDownload.setText(book.getDownloaded());
        boolean downloadBtnDisabled = false;
        db = new localStorageDatabase(getApplicationContext());
        for (String title : db.getBookTitles()){
            if (title == book.getTitle().toString()){
                downloadBtnDisabled = true;
                bookDownload.setText("Downloaded");
                break;
            }
            bookDownload.setText("Cloud");
        }
        if (downloadBtnDisabled){
            bookBtn.setEnabled(false);
        } else {
            bookBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("TEST", "onClick: File cur Num: " + book.getCurrentFileAsInt());
                    Log.d("TEST", "onClick: File Num: " + book.getFileNumAsInt());
//                    File f = new File("/data/user/0/com.teambuild.mp3wizard/files/The Art of Invisibility Kevin Mitnick", "0.mp3");
//                    Boolean temp = f.delete();
                    DownloadRefSetup(book, userId);
                }
            });
        }
    }

    private boolean DownloadRefSetup(final Book book, final String userId){
        final String TAG = "Download";
        Log.d("Download", "DownloadRefSetup: " + book.getFileNum() + " " + book.getTitle() + " " + book.getLocSec());
        for(int fileNum = 1; fileNum <= Integer.parseInt(book.getFileNum()); fileNum++) {
            storageReference = firebaseStorage.getInstance().getReference("users" + File.separator + userId + File.separator + book.getTitle());
            audioRef = storageReference.child(String.format("%d.mp3", fileNum));
            iconRef = storageReference.child("icon.png");
            Log.d("Download", "DownloadRefSetup: " + audioRef.getPath());

            File rootPath = new File(getApplicationContext().getFilesDir() + File.separator + book.getTitle());
            if (!rootPath.exists()){
                rootPath.mkdirs();
            }

            Log.d(TAG, "DownloadRefSetup: File Created");
            final File localAudioFile = new File(rootPath, String.format("%d.mp3", fileNum));
            final File localIconFile = new File(rootPath, "icon.png");
            Log.d(TAG, "DownloadRefSetup: final path abs : " + localAudioFile.getAbsolutePath());
            Log.d(TAG, "DownloadRefSetup: Final path: " + localAudioFile.toString());
            Log.d(TAG, "DownloadRefSetup: root path: " + rootPath.toString());
            Log.d(TAG, "DownloadRefSetup: REF Path: " + audioRef.getPath());
            Log.d(TAG, "DownloadRefSetup: REF Bucket: " + audioRef.getBucket());
            Log.d(TAG, "DownloadRefSetup: REF Bucket: " + audioRef.getDownloadUrl().toString());


            audioRef.getFile(localAudioFile).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onProgress: getBytesTransferred: " + taskSnapshot.getBytesTransferred());
                    Log.d(TAG, "onProgress: getTotalByteCount: " + taskSnapshot.getTotalByteCount());

                    double progress = 100.0 * ((double) taskSnapshot.getBytesTransferred() / (double)taskSnapshot.getTotalByteCount());
                    Log.d(TAG, "onProgress: Current Status: " + progress);
                }
            }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onSuccess: Downloaded");
                    Log.d(TAG, "onSuccess: " + localAudioFile.toString());
                    iconRef.getFile(localIconFile).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, "onProgress: getBytesTransferred: " + taskSnapshot.getBytesTransferred());
                            Log.d(TAG, "onProgress: getTotalByteCount: " + taskSnapshot.getTotalByteCount());

                            double progress = 100.0 * ((double) taskSnapshot.getBytesTransferred() / (double)taskSnapshot.getTotalByteCount());
                            Log.d(TAG, "onProgress: Current Status: " + progress);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, "onSuccess: Downloaded");
                            Log.d(TAG, "onSuccess: " + localIconFile.toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: " + ";local tem file not created  created " + e.toString());
                        }
                    });
                    db.addBook(book);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: " + ";local tem file not created  created " + e.toString());
                }
            });
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void downloadfile(Context context, String url, Book book, int fileNum){

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("Test");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        Log.d("Download", "downloadfile: Path: " + (Environment.DIRECTORY_AUDIOBOOKS + File.separator + book.getTitle()));

        File rootPath = new File(Environment.DIRECTORY_AUDIOBOOKS + File.separator + book.getTitle(), String.format("%d.mp3", fileNum));
        if (!rootPath.exists()){
            rootPath.mkdirs();
        }
        request.setDestinationInExternalFilesDir(context, (Environment.DIRECTORY_AUDIOBOOKS + File.separator + book.getTitle()), String.format("%d.mp3", fileNum));
        request.setMimeType("audio/mp3");
//        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//        BroadcastReceiver receiver = new DownloadReceiver();
//        context.registerReceiver(receiver, filter);
//
//        long fileID=0;
//        try {
//            fileID = downloadManager.enqueue(request);
//            Log.d("Download", String.format(Locale.US, "networkHttpDownload download of %s with id %d", url, fileID));
//            if (!Utils.waitUntil(() -> mIsDownloadComplete, 30)) {
//
//            }
//        }


//        boolean mIsDownloadComplete = false;
        long fileID = downloadManager.enqueue(request);
//        Uri resp;
//        do {
//            resp = downloadManager.getUriForDownloadedFile(fileID);
//        } while (resp==null);

        Log.d("Download", "downloadfile: Path: " + Environment.DIRECTORY_AUDIOBOOKS);
        Log.d("Download", "downloadfile: FileID: " + fileID);
        Log.d("Download", "downloadfile: Downloaded");
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.Audio.AudioColumns.RELATIVE_PATH, (Environment.DIRECTORY_AUDIOBOOKS + File.separator + book.getTitle() + File.separator + String.format("%d.mp3", fileNum)));
//        contentValues.put(MediaStore.Audio.AudioColumns.TITLE, book.getTitle() + " Num:" + fileNum);
//        contentValues.put(MediaStore.Audio.AudioColumns.ALBUM, book.getTitle());
//        contentValues.put(MediaStore.Audio.AudioColumns.IS_AUDIOBOOK, 1);

//        Uri internalUri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
//        Log.d("Download", internalUri.toString());
    }

}

