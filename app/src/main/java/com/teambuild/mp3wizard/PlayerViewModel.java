package com.teambuild.mp3wizard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.lifecycle.ViewModel;

import com.teambuild.mp3wizard.audioplayer.AudioPlayerService;
import com.teambuild.mp3wizard.audioplayer.AudioServiceConnectionSingleton;
import com.teambuild.mp3wizard.repository.RepositorySingleton;

import java.io.File;

public class PlayerViewModel extends ViewModel {
    private RepositorySingleton repository;
    private AudioServiceConnectionSingleton audioServiceConnection;
    String TAG = "AudioPlayer";

    public PlayerViewModel() {
        repository = RepositorySingleton.getInstance();
        audioServiceConnection = AudioServiceConnectionSingleton.getInstance();

    }

    public int playButton(String bookID, SeekBar positionBar, TextView elapsedTimeLabel, TextView remainingTimeLabel){
        if (!audioServiceConnection.isCurrentlyPlaying(bookID)) {
            Log.d(TAG, "playButton: reset data");
            Book tempBook = repository.getLocalBookByID(bookID);
            audioServiceConnection.setBook(tempBook);
            audioServiceConnection.setGUI(positionBar, elapsedTimeLabel, remainingTimeLabel);
            if (!repository.areCurrentCloudAndLocalLocationsEqual(tempBook)) return AudioPlayerService.WAITING;
        }
        Log.d(TAG, "playButton: State: " + audioServiceConnection.getCurrentPlayingState());
        return audioServiceConnection.toggleAudioPlayState();
    }


    public String getTitleById(String bookID){
        return repository.getLocalBookByID(bookID).getTitle();
    }

    public Bitmap getIconBitmapById(String bookID){
        File audioBookIconFile = new File(repository.getLocalBookByID(bookID).getPath(), "icon.png");
        if (audioBookIconFile.exists())
            return BitmapFactory.decodeFile(audioBookIconFile.getAbsolutePath());
        return null;
    }

    public int getLocalPositionById(String bookID){
        return repository.getLocalBookByID(bookID).getLocSecAsInt();
    }

//    public int getLocalTotalTimeById(String bookID){
//        Log.d(TAG, "getLocalTotalTimeById: ");
//        Book tempBook = repository.getLocalBookByID(bookID);
//        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//        int totalTime = 0;
//        for (int x = 1; x <= tempBook.getFileNumAsInt(); x++){
//            mmr.setDataSource(new File(tempBook.getPath(), String.format("%d.mp3", x)).getAbsolutePath());
//            totalTime += Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//        }
//        mmr.close();
//        Log.d(TAG, "getLocalTotalTimeById: ");
//        return totalTime;
//    }

    public void setCurrentTimePopupResponse(int selection, String bookID){
        switch (selection) {
            case 0:
                repository.setLocationToLocalValue(bookID);
                break;
            case 1:
                repository.setLocationToCloudValue(bookID);
                break;
        }
    }

    public int getFirebaseLoc(String bookID){
        return repository.getFirebaseLoc(bookID);
    }

    public int getSQLITELoc(String bookID){
        return repository.getSQLITELoc(bookID);
    }
}
