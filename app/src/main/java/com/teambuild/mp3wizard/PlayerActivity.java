package com.teambuild.mp3wizard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

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
import com.teambuild.mp3wizard.repository.database.local.LocalSQLiteDatabase;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {
    private PlayerViewModel playerViewModel;

    String TAG = "AudioPlayer";

    private AudioServiceConnectionSingleton audioServiceConnection;
    // Interface
    Button playBtn;
    SeekBar positionBar;
    TextView elapsedTimeLabel, remainingTimeLabel, titleLabel;
    ImageView icon;

    int totalTime;
    int curTime;

    String bookID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_player);
        playerViewModel = new PlayerViewModel();

        Intent in = getIntent();
        Bundle b = in.getExtras();
        bookID = b.getString("bookID");

        playBtn = (Button) findViewById(R.id.playBtn);
        playBtn.setClickable(false);
        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);
        positionBar = (SeekBar) findViewById(R.id.positionBar);
        icon = (ImageView) findViewById(R.id.audioIcon);
        titleLabel = findViewById(R.id.BookTitleLabel);

        // This is totalTime/curTime for the media being displayed not that which is being played
//        totalTime = playerViewModel.getLocalTotalTimeById(bookID);
        totalTime = 900004;
        curTime = playerViewModel.getLocalPositionById(bookID);

        // set GUI to show open book (Does not modify or affect Playing book)
        titleLabel.setText(playerViewModel.getTitleById(bookID));
        elapsedTimeLabel.setText(createTimeLabel(curTime));
        remainingTimeLabel.setText(createTimeLabel(totalTime-curTime));
        positionBar.setMax(totalTime);
        positionBar.setProgress(curTime);

        playBtnClickListener();
        setProgressBarListener();

        // Set Icon
        Bitmap iconBM = playerViewModel.getIconBitmapById(bookID);
        if (iconBM != null)
            icon.setImageBitmap(iconBM);

    }



    public void playBtnClickListener() {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Pressed");
                switch (playerViewModel.playButton(bookID, positionBar, elapsedTimeLabel, remainingTimeLabel)){
                    case AudioPlayerService.PLAYING:
                        playBtn.setBackgroundResource(R.drawable.ic_pause_button_white_trim_foreground);
                        break;
                    case AudioPlayerService.PAUSED:
                        playBtn.setBackgroundResource(R.drawable.ic_play_button_white);
                        break;
                    case AudioPlayerService.WAITING:
                        playBtn.setBackgroundResource(R.drawable.ic_pause_button_white_trim_foreground);
                        cloudVsLocalPopup();
                        break;
                }
            }
        });
        playBtn.setClickable(true);

    }

    private void cloudVsLocalPopup(){
        Log.d("AudioSystem", "cloudVsLocalPopup: Popupfasdfa");
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
        locTextView.setText(String.format("Local Location: %s", createTimeLabel(playerViewModel.getSQLITELoc(bookID))));
        cloTextView.setText(String.format("Cloud Location:  %s", createTimeLabel(playerViewModel.getFirebaseLoc(bookID))));
        locationPopupWindow.showAtLocation(getWindow().getDecorView().getRootView(), Gravity.CENTER, 0, 0);

        localBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerViewModel.setCurrentTimePopupResponse(0, bookID);
                locationPopupWindow.dismiss();
            }
        });
        cloudBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerViewModel.setCurrentTimePopupResponse(1, bookID);
                locationPopupWindow.dismiss();
                Intent playerIntent = new Intent(PlayerActivity.this, PlayerActivity.class);
                Bundle b = new Bundle();
                b.putString("bookID", bookID);
                playerIntent.putExtras(b);
                startActivity(playerIntent);
                finish();
            }
        });
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
        // Position Bar
        positionBar.setMax(totalTime);

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
