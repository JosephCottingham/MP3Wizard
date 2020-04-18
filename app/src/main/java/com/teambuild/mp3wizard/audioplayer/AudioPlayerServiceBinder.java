package com.teambuild.mp3wizard.audioplayer;

import android.content.Context;
import android.os.Binder;

public class AudioPlayerServiceBinder extends Binder{
	AudioPlayerService mMusicPlayerService;
	Context mApplication;
	SeekBarTextCallback mSeekBarTextCallback;

	public AudioPlayerServiceBinder(AudioPlayerService musicPlayerService, Context application) {
		mMusicPlayerService = musicPlayerService;
		mApplication = application;
		mApplication = application;
	}
	
	public AudioPlayerService getService() {
//		mSeekBarTextCallback = seekBarTextCallback;
		return mMusicPlayerService;
	}



}
