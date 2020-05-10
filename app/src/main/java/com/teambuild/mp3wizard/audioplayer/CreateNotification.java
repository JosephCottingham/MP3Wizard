package com.teambuild.mp3wizard.audioplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.R;

import java.io.File;

public class CreateNotification {

    public static final String CHANNEL_ID = "channel1";

    public  static final  String ACTION_PLAY = "actionplay";

    public static Notification notification;

    public static void createNotification(Context context, Book book, int playbutton, int pos){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "TEST");

            Bitmap icon = BitmapFactory.decodeFile(new File(book.getPath(), "icon.png").getAbsolutePath());

            Intent intentPlay = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PLAY);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);


    // TODO setup author....
            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_sound_on_foreground)
                    .setContentTitle(book.getTitle())
                    .setContentText("Author")
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(playbutton, "Play/Pause", pendingIntentPlay)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build();

            notificationManagerCompat.notify(1, notification);
        }
    }
}
