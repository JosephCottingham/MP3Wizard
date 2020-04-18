package com.teambuild.mp3wizard;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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
import com.teambuild.mp3wizard.audioplayer.AudioPlayerService;
import com.teambuild.mp3wizard.audioplayer.AudioPlayerServiceBinder;
import com.teambuild.mp3wizard.audioplayer.SeekBarTextCallback;
import com.teambuild.mp3wizard.ui.localStorageDatabase;

import java.io.File;
import java.io.IOException;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class PlayerActivity extends AppCompatActivity {
    String TAG = "AudioPlayer";

    Book book;


    // Interface
    Button playBtn;
    SeekBar positionBar;
    SeekBar volumeBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    ImageView icon;

    // Player System
    boolean mBound = false;
    int currentState;
    AudioPlayerService mService;
    AudioPlayerServiceBinder mBinder;
    ServiceConnection mConnection;
    SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener;

    int totalTime;
    localStorageDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_player);
        final String TAG = "Player";

        playBtn = (Button) findViewById(R.id.playBtn);
        playBtn.setClickable(false);
        icon = (ImageView) findViewById(R.id.audioIcon);
        Intent in = getIntent();
        Bundle b = in.getExtras();
        final String bookID = b.getString("bookID");

        Log.d("Player", "onCreate: BookID: " + bookID);

        db = new localStorageDatabase(getApplicationContext());
        book = db.getBookWithID(bookID);
        CurLocEqual(book);


        setProgressBarListener();
        defineServiceConnection();
        bindService(new Intent(PlayerActivity.this, AudioPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);

        File audioBookIconFile = new File(book.getPath(), "icon.png");
        if (audioBookIconFile.exists()) icon.setImageBitmap(BitmapFactory.decodeFile(audioBookIconFile.getAbsolutePath()));







//        // Media Player
//        if (book == null) {
//            getSupportActionBar().setTitle(bookID);
//            mp = MediaPlayer.create(this, Uri.parse("/data/user/0/com.teambuild.mp3wizard/files/Kevin Mitnick The Art of Invisibility Audiobook/1.mp3"));
//        } else {
//            getSupportActionBar().setTitle(book.getTitle());
//            Log.d("Player", "onCreate: current file int:" + book.getCurrentFileAsInt());
//            Log.d("Player", "onCreate: current file str:" + book.getCurrentFile());
//
//
//            File audioBookFile = new File(book.getPath(), String.format("%s.mp3", book.getCurrentFile()));
//            File audioBookIconFile = new File(book.getPath(), "icon.png");
//            if (audioBookFile.exists()) icon.setImageBitmap(BitmapFactory.decodeFile(audioBookIconFile.getAbsolutePath()));
//            Log.d("Player", "onCreate: Path:" + audioBookFile.getAbsolutePath());
//            mp = MediaPlayer.create(this, Uri.parse(audioBookFile.getAbsolutePath()));
//        }

//        // Volume Bar
//        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
//        volumeBar.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        float volumeNum = progress / 100f;
//                        mp.setVolume(volumeNum, volumeNum);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//
//                    }
//                }
//        );

        // Thread (Update positionBar & timeLabel)
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Book runBook = new Book(book.getTitle(), book.getFileNum(), book.getLocSec(), book.getCurrentFile(), book.getDownloaded());
//                runBook.setPath(book.getPath());
//                runBook.setID(book.getID());
//                while (mp != null) {
//                    try {
//                        Message msg = new Message();
//                        msg.what = mp.getCurrentPosition();
//                        Log.d(TAG, "run: " + (msg.what/1000));
//                        Log.d(TAG, "run: " + runBook.getLocSec());
//                        if (Math.abs((msg.what/1000)-Integer.valueOf(runBook.getLocSec()))>30){
//                            runBook.setLocSec(String.valueOf(msg.what/1000));
//                            db.updateCurLoc(runBook);
//                        }
//                        handler.sendMessage(msg);
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {}
//                }
//            }
//        }).start();

    }

//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            int currentPosition = msg.what;
//            // Update positionBar.
//            positionBar.setProgress(currentPosition);
//
//            // Update Labels.
//            String elapsedTime = createTimeLabel(currentPosition/1000);
//            elapsedTimeLabel.setText(elapsedTime);
//
//            String remainingTime = createTimeLabel((totalTime-currentPosition)/1000);
//            remainingTimeLabel.setText("- " + remainingTime);
//        }
//    };

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

    public void playBtnClickListener() {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound && mService.successfullyRetrievedAudioFocus()) {
                    currentState = mService.changeState();

                    switch (currentState){
                        case AudioPlayerService.PLAYING:
                            playBtn.setBackgroundResource(R.drawable.ic_pause_foreground);
                            break;
                        case AudioPlayerService.PAUSED:
                            playBtn.setBackgroundResource(R.drawable.ic_play_foreground);
                            break;
                    }

                }
            }
        });
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
                            finish();
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


    private void setProgressBarListener() {
        // Position Bar
        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);
        positionBar = (SeekBar) findViewById(R.id.positionBar);
        positionBar.setMax(totalTime);
        elapsedTimeLabel.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        Log.d("AudioPlayer", "onTextChanged: " + charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                }
        );
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            Log.d("AudioPlayer", "onProgressChanged: Progress: " + progress);
                            mService.skipToPoint(progress);
                            positionBar.setProgress(progress);
                            elapsedTimeLabel.setText(createTimeLabel(progress/1000));
                            remainingTimeLabel.setText(createTimeLabel((positionBar.getMax()-progress)/1000));
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
    }

    private synchronized void initQueue(){
        mService.configureQueueWithBook(book);
        playBtn.setClickable(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(PlayerActivity.this, AudioPlayerService.class));
        unbindService(mConnection);
        mBound = false;
    }

    private void defineServiceConnection(){
        Log.d(TAG, "defineServiceConnection: mConnection");
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: StartService");
                startService(new Intent(PlayerActivity.this, AudioPlayerService.class));
                Log.d(TAG, "onServiceConnected: mBinder");
                mBinder = (AudioPlayerServiceBinder) iBinder;
                Log.d(TAG, "onServiceConnected: mService");
                mService = mBinder.getService();
                currentState = mService.getState();
                playBtnClickListener();
                mService.registerGui(positionBar, elapsedTimeLabel, remainingTimeLabel);
                mBound = true;
                initQueue();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBound = false;
            }
        };
    }
}
