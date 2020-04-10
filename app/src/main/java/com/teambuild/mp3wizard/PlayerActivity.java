package com.teambuild.mp3wizard;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teambuild.mp3wizard.ui.localStorageDatabase;

import java.io.File;
import java.io.IOException;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class PlayerActivity extends AppCompatActivity {

    Button playBtn;
    SeekBar positionBar;
    SeekBar volumeBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    MediaPlayer mp;
    int totalTime;
    localStorageDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        final String TAG = "Player";

        playBtn = (Button) findViewById(R.id.playBtn);
        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);
        Intent in = getIntent();
        Bundle b = in.getExtras();
        final String bookID = b.getString("bookID");

        Log.d("Player", "onCreate: BookID: " + bookID);

        db = new localStorageDatabase(getApplicationContext());
        final Book book = db.getBookWithID(bookID);
        CurLocEqual(book);

        // Media Player
        if (book == null) {
            getSupportActionBar().setTitle(bookID);
            mp = MediaPlayer.create(this, Uri.parse("/data/user/0/com.teambuild.mp3wizard/files/Kevin Mitnick The Art of Invisibility Audiobook/1.mp3"));
        } else {
            getSupportActionBar().setTitle(book.getTitle());
            Log.d("Player", "onCreate: current file int:" + book.getCurrentFileAsInt());
            Log.d("Player", "onCreate: current file str:" + book.getCurrentFile());


            File audioBookFile = new File(book.getPath(), String.format("%s.mp3", book.getCurrentFile()));
            Log.d("Player", "onCreate: Path:" + audioBookFile.getAbsolutePath());
            mp = MediaPlayer.create(this, Uri.parse(audioBookFile.getAbsolutePath()));
        }

        //        try {
////            AssetFileDescriptor afd = getApplicationContext().getAssets().openFd("/data/user/0/com.teambuild.mp3wizard/files/The Art of Invisibility Kevin Mitnick/0.mp3");
////            mp.setDataSource(afd.getFileDescriptor());
//        } catch (IOException e) {
//            Log.d("Play", "onCreate: " + e.getMessage());
//        }
        mp.setLooping(true);
        mp.seekTo(0);
        mp.setVolume(0.5f, 0.5f);
        mp.seekTo((int)(book.getLocSecAsLong()*1000));
        totalTime = mp.getDuration();


        // Position Bar
        positionBar = (SeekBar) findViewById(R.id.positionBar);
        positionBar.setMax(totalTime);
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            Log.d(TAG, "onProgressChanged: Progress: " + progress);
                            mp.seekTo(progress);
                            positionBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );


        // Volume Bar
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum = progress / 100f;
                        mp.setVolume(volumeNum, volumeNum);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        // Thread (Update positionBar & timeLabel)
        new Thread(new Runnable() {
            @Override
            public void run() {
                Book runBook = new Book(book.getTitle(), book.getFileNum(), book.getLocSec(), book.getCurrentFile(), book.getDownloaded());
                runBook.setPath(book.getPath());
                runBook.setID(book.getID());
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        Log.d(TAG, "run: " + (msg.what/1000));
                        Log.d(TAG, "run: " + runBook.getLocSec());
                        if (Math.abs((msg.what/1000)-Integer.valueOf(runBook.getLocSec()))>30){
                            runBook.setLocSec(String.valueOf(msg.what/1000));
                            db.updateCurLoc(runBook);
                        }
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            positionBar.setProgress(currentPosition);

            // Update Labels.
            String elapsedTime = createTimeLabel(currentPosition/1000);
            elapsedTimeLabel.setText(elapsedTime);

            String remainingTime = createTimeLabel((totalTime-currentPosition)/1000);
            remainingTimeLabel.setText("- " + remainingTime);
        }
    };

    public String createTimeLabel(int totalSec) {
        int hour = (int)(totalSec/3600);
        int min = (int)((totalSec-(3600*hour))/60);
        int sec = (int)(totalSec-((3600*hour)+(60*min)));
        String minS = String.valueOf(min);
        String secS = String.valueOf(sec);
        if (min < 10) minS = "0" + minS;
        if (sec < 10) secS += "0" + secS;
        return String.format("%s:%s:%s", hour, minS, secS);
    }

    public void playBtnClick(View view) {

        if (!mp.isPlaying()) {
            // Stopping
            mp.start();
            playBtn.setBackgroundResource(R.drawable.ic_pause_foreground);

        } else {
            // Playing
            mp.pause();
            playBtn.setBackgroundResource(R.drawable.ic_play_foreground);
        }

    }

    public void CurLocEqual(final Book book){
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String userId = mFirebaseAuth.getCurrentUser().getUid();
        final DatabaseReference ref = mDatabase.child(userId).child(book.getTitle()).child("locSec");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                final int firebaseLoc = Integer.valueOf((String) dataSnapshot.getValue());
                Log.d("FirebaseTestCase", "onDataChange: Firebase Time: " + firebaseLoc);
                Log.d("FirebaseTestCase", "onDataChange: Local Time: " + book.getLocSec());
                if (Math.abs(firebaseLoc-book.getLocSecAsLong())>30){
                    // TODO create a popup to ask which data to use
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View locationPopupView = inflater.inflate(R.layout.location_popup_window, null);
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = false; // lets taps outside the popup also dismiss it
                    final PopupWindow locationPopupWindow = new PopupWindow(locationPopupView, width, height, focusable);
                    View pView = locationPopupWindow.getContentView();
                    final Button localBtn = pView.findViewById(R.id.localBtn);
                    final Button cloudBtn = pView.findViewById(R.id.cloudBtn);
                    TextView locTextView = pView.findViewById(R.id.LocLocationTextView);
                    TextView cloTextView = pView.findViewById(R.id.CloudLocationTextView);
                    locTextView.setText(String.format("Local Location: %s", createTimeLabel((int)book.getLocSecAsLong())));
                    cloTextView.setText(String.format("Cloud Location:  %s", createTimeLabel(firebaseLoc)));
                    locationPopupWindow.showAtLocation(getWindow().getDecorView().getRootView(), Gravity.CENTER, 0, 0);


                    localBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ref.setValue(book.getLocSec());
                            locationPopupWindow.dismiss();
                        }
                    });
                    cloudBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            book.setLocSec(String.valueOf(firebaseLoc));
                            db.updateCurLoc(book);
                            locationPopupWindow.dismiss();
                            Intent playerIntent = new Intent(PlayerActivity.this, PlayerActivity.class);
                            Bundle b = new Bundle();
                            b.putString("bookID", book.getID());
                            playerIntent.putExtras(b);
                            startActivity(playerIntent);
                        }
                    });
                    // show the popup window
                    // which view you pass in doesn't matter, it is only used for the window tolken
                }
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("FirebaseTestCase", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        ref.addListenerForSingleValueEvent(postListener);
    }

//    public void openDialog(){
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        alertDialogBuilder.setMessage("Are you sure,You wanted to make decision");
//
//        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface arg0, int arg1) {
//                Toast.makeText(PlayerActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
//            }
//        });
//
//        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                finish();
//            }
//        });
//
//        AlertDialog alertDialog = alertDialogBuilder.create();
//        alertDialog.show();
//    }
}
