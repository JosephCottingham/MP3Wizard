package com.teambuild.mp3wizard.audioplayer;

import java.io.File;
import java.io.IOException;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.repository.RepositorySingleton;
import com.teambuild.mp3wizard.repository.database.local.LocalSQLiteDatabase;


public class AudioPlayerService extends Service implements AudioPlayerServiceInterface, AudioManager.OnAudioFocusChangeListener{
	private String TAG = "AudioPlayer";

	public final static int PAUSED = 0;
	public final static int PLAYING = 1;
	public final static int WAITING = 2;

	private int state;

	private AudioPlayerServiceBinder mAudioPlayerServiceBinder;
	private Queue mNowPlaying;
	private MediaPlayer mMediaPlayer;
	private OnCompletionListener mCompletionListener;

	private HeadPhoneBroadcastReceiver mHeadPhoneBroadcastReceiver;
	private SeekBar positionBar;
	private TextView elapsedTimeLabel, remainingTimeLabel;
	private AsyncTask<Void, Void, Void> seekBarChanger;
	private Thread passingTime;

	private RepositorySingleton repository;

	NotificationManager notificationManager;

	@Override
	public IBinder onBind(Intent intent) {
		repository = RepositorySingleton.getInstance(); //TODO Try this with old version
		mAudioPlayerServiceBinder = new AudioPlayerServiceBinder(this, this);
		state = PLAYING;

		mNowPlaying = new Queue();			// setup the now playing queue
		mMediaPlayer = new MediaPlayer();	// setup the media player

		mCompletionListener = new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				playNext();
			}
		};
		mMediaPlayer.setOnCompletionListener(mCompletionListener);

		mHeadPhoneBroadcastReceiver = new HeadPhoneBroadcastReceiver();
		registerReceiver(mHeadPhoneBroadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		mHeadPhoneBroadcastReceiver.registerMusicPlayerService(this);

		return mAudioPlayerServiceBinder;
	}


	public void configureQueueWithBook(Book book){
		mNowPlaying.clearQueue();
		mNowPlaying.setQueue(book);
		playFetched(mNowPlaying.getCurrentlyPlaying().getAbsolutePath(), false);
	}

	public Book getCurrentQueuedBook(){
		return mNowPlaying.currentBook;
	}


	public synchronized void play() {
		Log.d("AudioSystem", "play: ");
	    if (successfullyRetrievedAudioFocus()) {
            state = PLAYING;
			Log.d("AudioSystem", "play: ");
			mMediaPlayer.start();
			CreateNotification.createNotification(getApplicationContext(), mNowPlaying.currentBook, R.drawable.ic_simple_pause_button_white_foreground, 0);
        }
	}

	public synchronized void play(int position) {
		File file = mNowPlaying.playGet(position);
		playFetched(file.getAbsolutePath(), true);
		CreateNotification.createNotification(getApplicationContext(), mNowPlaying.currentBook, R.drawable.ic_simple_pause_button_white_foreground, 0);
	}

	public synchronized void playNext() {
		if(mNowPlaying.next() != null) {
			playFetched(mNowPlaying.next().getPath(), true);
		}
	}

	public void createNotificationChannel(){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID, "Dev", NotificationManager.IMPORTANCE_LOW);

			notificationManager = getSystemService(NotificationManager.class);
			if(notificationManager != null){
				notificationManager.createNotificationChannel(channel);
			}
		}
	}

	private synchronized void playFetched(final String path, final boolean beginPlaying) {
		state = PLAYING;
		mMediaPlayer.stop();
		mMediaPlayer.reset();
		try {
			Log.d(TAG, "playFetched: Path: " + path);
			mMediaPlayer.setDataSource(path);
			Log.d(TAG, "playFetched: Path: " + path);

			mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					try {
						Log.d(TAG, "onPrepared: OnPrepared");
						int totalTime = mp.getDuration();
						Log.d(TAG, "onPrepared: TotalTime: " + totalTime);
						Log.d(TAG, "onPrepared: CuretTime: " + mNowPlaying.currentBook.getLocSecAsInt());
						Log.d(TAG, "onPrepared: Title: " + mNowPlaying.currentBook.getTitle());

						NotificationConfig();
						positionBar.setMax(totalTime);
						positionBar.setProgress(mNowPlaying.currentBook.getLocSecAsInt());
						setSeekBarTracker();
						mp.seekTo(mNowPlaying.currentBook.getLocSecAsInt() * 1000);
						play();
						if (!beginPlaying) changeState();
					} catch (Exception e){
						Log.d(TAG, "onPrepared: " + e.getMessage());
						e.printStackTrace();
					}
				}
			});
			mMediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String createTimeLabel(int totalSec) {
		int hour = (int)(totalSec/3600);
		int min = (int)((totalSec-(3600*hour))/60);
		int sec = (int)(totalSec-((3600*hour)+(60*min)));
		String minS = String.valueOf(min);
		String secS = String.valueOf(sec);
		if (min < 10) minS = "0" + minS;
		if (sec < 10) secS = "0" + secS;
		return String.format("%s:%s:%s", hour, minS, secS);
	}

	private void setSeekBarTracker() {
		if (seekBarChanger != null)
			seekBarChanger.cancel(false);
		seekBarChanger = null;

		passingTime = new Thread(new Runnable() {
			@Override
			public void run() {
				while (mMediaPlayer != null) {
					try {
						if (mMediaPlayer.getCurrentPosition() > mMediaPlayer.getDuration()) {
							// TODO move to next file/ end
						}
						if (Math.abs((mMediaPlayer.getCurrentPosition() / 1000) - Integer.valueOf(mNowPlaying.currentBook.getLocSec())) > 30) {
							mNowPlaying.currentBook.setLocSec(String.valueOf(mMediaPlayer.getCurrentPosition() / 1000));
							repository.setCurrentLocation(mNowPlaying.currentBook);
						}
						Message msg = new Message();
						msg.what = mMediaPlayer.getCurrentPosition();
						handler.sendMessage(msg);
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}

			}
		});
		passingTime.start();
	}

	public void pause() {
		state = PAUSED;
		mMediaPlayer.pause();
		CreateNotification.createNotification(getApplicationContext(), mNowPlaying.currentBook, R.drawable.ic_simple_play_button_white_foreground, 0);
	}

	public int changeState() {
		Log.d("AudioSystems", "changeState: State: " + state);
		switch(state){
			case PLAYING:
				pause();
				break;
			case PAUSED:
				play();
				break;
		}

		return state;									// return the value of the changed state as confirmation
	}

	public int getState() {
		return state;
	}


	@Override
	public void skipToPoint(int point) {
		mMediaPlayer.seekTo(point);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		unregisterReceiver(mHeadPhoneBroadcastReceiver);


		if (seekBarChanger != null)
			seekBarChanger.cancel(false);
		seekBarChanger = null;

		mMediaPlayer.stop();
		mMediaPlayer.reset();
		mMediaPlayer.release();
		Toast.makeText(this, "unBind with state: " + ((state == PLAYING) ? "PLAYING" : "PAUSED"), Toast.LENGTH_SHORT).show();
		return true;
	}

	public void registerGui(SeekBar positionBar, TextView elapsedTimeLabel, TextView remainingTimeLabel) {
		this.positionBar = positionBar;
		this.elapsedTimeLabel = elapsedTimeLabel;
		this.remainingTimeLabel = remainingTimeLabel;
	}

	public synchronized void playLast() {
		File file = mNowPlaying.last();
		playFetched(file.getAbsolutePath(), true);
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

			String remainingTime = createTimeLabel((mMediaPlayer.getDuration()-currentPosition)/1000);
			remainingTimeLabel.setText("- " + remainingTime);
		}
	};


    public boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch( focusChange ) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if( state == PLAYING ) {
                    changeState();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                if( state == PLAYING ) {
                    pause();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if( mMediaPlayer != null ) {
                    mMediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if( mMediaPlayer != null ) {
                    if( state == PAUSED ) {
                        changeState();
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    private void NotificationConfig(){
		createNotificationChannel();
		registerReceiver(notificationBroadcastReceiver, new IntentFilter("NotificationAction"));

	}

	BroadcastReceiver notificationBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getExtras().getString("actionname");

			switch (action) {
				case CreateNotification.ACTION_PLAY:
					changeState();
					break;
			}
		}
	};
}
