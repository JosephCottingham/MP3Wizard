package com.teambuild.mp3wizard.audioplayer;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.R;

import org.w3c.dom.Text;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class AudioServiceConnectionSingleton {
    String TAG = "AudioPlayer";

    private static AudioServiceConnectionSingleton inst;

    private AudioPlayerService mService;
    private AudioPlayerServiceBinder mBinder;
    private ServiceConnection mConnection;

    boolean boundService = false;


    public static void set(AudioServiceConnectionSingleton instance) {
        inst = instance;
    }

    public static synchronized AudioServiceConnectionSingleton getInstance() {
        if (inst == null)
            inst = new AudioServiceConnectionSingleton();
        return inst;
    }

    @SuppressLint("RestrictedApi")
    private AudioServiceConnectionSingleton() {
        defineServiceConnection();
        Log.d(TAG, "onServiceConnected: defineServiceConnection finished");
        getApplicationContext().bindService(new Intent(getApplicationContext(), AudioPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void defineServiceConnection() {
        Log.d(TAG, "onServiceConnected: defineServiceConnection Called");

        mConnection = new ServiceConnection() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: Connected");
                getApplicationContext().startService(new Intent(getApplicationContext(), AudioPlayerService.class));
                mBinder = (AudioPlayerServiceBinder) iBinder;
                mService = mBinder.getService();
                boundService = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                boundService = false;
            }
        };
    }

    public int toggleAudioPlayState(){
        Log.d(TAG, "toggleAudioPlayState: ");
        if (boundService && mService.successfullyRetrievedAudioFocus()) {
            Log.d(TAG, "toggleAudioPlayState: successfullyRetrievedAudioFocus + boundServicet");
            mService.changeState();
        }
        return mService.getState();
    }


    public synchronized void setBook(Book book){
        mService.configureQueueWithBook(book);
    }

    public synchronized void goToPosition(int position){
        mService.skipToPoint(position);
    }

    public int getCurrentPlayingState(){
        return mService.getState();
    }

    public boolean isCurrentlyPlaying(String bookID){
        Book queuedBook = mService.getCurrentQueuedBook();
        if (queuedBook==null){
            Log.d(TAG, "isCurrentlyPlaying: queued NULL");
        } else {
            Log.d(TAG, "isCurrentlyPlaying: queued Book" + queuedBook.getTitle());
            Log.d(TAG, "isCurrentlyPlaying: ID Value of Queued Book: " + queuedBook.getID());
            Log.d(TAG, "isCurrentlyPlaying: ID Value of new book: " + bookID);
        }
        if(queuedBook != null && bookID.equals(queuedBook.getID())) return true;
        return false;
    }

    public void setGUI(SeekBar positionBar, TextView elapsedTimeLabel, TextView remainingTimeLabel){
        mService.registerGui(positionBar, elapsedTimeLabel, remainingTimeLabel);
    }
}