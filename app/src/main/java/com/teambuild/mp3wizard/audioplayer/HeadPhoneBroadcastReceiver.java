package com.teambuild.mp3wizard.audioplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class HeadPhoneBroadcastReceiver extends BroadcastReceiver {

	private static final String IN = "IN";
	private static final String NOT_IN = "NOT IN";
	
	private AudioPlayerService mAudioPlayerService;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() != null && Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())){
			Bundle bundle = intent.getExtras();
			
			int state = bundle.getInt("state");
			
			if (state == 0){
				Log.d("HEADSET", NOT_IN);
				if (mAudioPlayerService != null && mAudioPlayerService.getState() == AudioPlayerService.PLAYING)
					mAudioPlayerService.pause();
			}
			else{
				Log.d("HEADSET", IN);
			}
		}
	}
	
	public void registerMusicPlayerService(AudioPlayerService musicPlayerService) {
		if (mAudioPlayerService != null)
			mAudioPlayerService = musicPlayerService;
	}
}
