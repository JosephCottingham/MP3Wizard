package com.teambuild.mp3wizard;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import com.teambuild.mp3wizard.audioplayer.AudioPlayerService;
import com.teambuild.mp3wizard.audioplayer.AudioServiceConnectionSingleton;
import com.teambuild.mp3wizard.repository.RepositorySingleton;
import com.teambuild.mp3wizard.repository.database.local.LocalListAdapterSQLITE;
import com.teambuild.mp3wizard.repository.database.local.LocalSQLiteDatabase;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {

    String TAG = "AudioPlayer";

    private AudioServiceConnectionSingleton audioServiceConnection;
    // Interface
    Button playBtn;
    SeekBar positionBar;
    TextView elapsedTimeLabel, remainingTimeLabel, titleLabel;
    ImageView icon;

    RepositorySingleton repository;

    int totalTime;
    int curTime;

    String bookID;
    private Book book;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_player);

        Intent in = getIntent();
        Bundle b = in.getExtras();
        bookID = b.getString("bookID");

        repository = RepositorySingleton.getInstance();
        audioServiceConnection = AudioServiceConnectionSingleton.getInstance();

        book = repository.getLocalBookByID(bookID);

        //        // This is totalTime/curTime for the media being displayed not that which is being played
        //totalTime = playerViewModel.getLocalTotalTimeById(bookID);
        curTime = book.getLocSecAsInt();

        // Connect GUI
        playBtn = (Button) findViewById(R.id.playBtn);
        playBtn.setClickable(false);

        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);
        positionBar = (SeekBar) findViewById(R.id.positionBar);

        icon = (ImageView) findViewById(R.id.audioIcon);
        titleLabel = findViewById(R.id.BookTitleLabel);

        // set GUI to show open book (Does not modify or affect Playing book)
        titleLabel.setText(book.getTitle());
        elapsedTimeLabel.setText(createTimeLabel(curTime));
        remainingTimeLabel.setText(createTimeLabel(totalTime-curTime));
        positionBar.setMax(totalTime);


        if (!repository.areCurrentCloudAndLocalLocationsEqual(bookID)){
            cloudVsLocalPopup();
        }

        setProgressBarListener();

        audioServiceConnection.setGUI(positionBar, elapsedTimeLabel, remainingTimeLabel);


        if (!audioServiceConnection.isCurrentlyPlaying(bookID)) {
            Log.d(TAG, "playButton: reset data");
            Book tempBook = repository.getLocalBookByID(bookID);
            audioServiceConnection.setBook(tempBook);
        }

        playBtnClickListener();

        // Set Icon
        File audioBookIconFile = new File(repository.getLocalBookByID(bookID).getPath(), "icon.png");
        if (audioBookIconFile.exists())
            icon.setImageBitmap(BitmapFactory.decodeFile(audioBookIconFile.getAbsolutePath()));

    }



    public void playBtnClickListener() {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    switch (audioServiceConnection.toggleAudioPlayState()){
                        case AudioPlayerService.PLAYING:
                            playBtn.setBackgroundResource(R.drawable.ic_pause_button_white_trim_foreground);
                            break;
                        case AudioPlayerService.PAUSED:
                            playBtn.setBackgroundResource(R.drawable.ic_play_button_white);
                            break;
                    }
            }
        });
        playBtn.setClickable(true);
    }


    private void cloudVsLocalPopup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getWindow().getDecorView().getContext());
        builder.setCancelable(false);
        builder.setTitle("Removing Content");
        builder.setMessage("Cloud and local locations do not match\n" + String.format("Local Location: %s", createTimeLabel(repository.getSQLITELoc(bookID))) + "\n" + String.format("Cloud Location:  %s", createTimeLabel(repository.getFirebaseLoc(bookID))) + "\nWhich would you like to use?");
        builder.setPositiveButton("Cloud",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repository.setLocationToCloudValue(bookID);
                        Intent playerIntent = new Intent(PlayerActivity.this, PlayerActivity.class);
                        Bundle b = new Bundle();
                        b.putString("bookID", bookID);
                        playerIntent.putExtras(b);
                        startActivity(playerIntent);
                        finish();
                    }
                });
        builder.setNegativeButton("Local", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                repository.setLocationToLocalValue(bookID);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



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

    private void setProgressBarListener() {
        elapsedTimeLabel.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
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
                            audioServiceConnection.goToPosition(progress);
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

}
