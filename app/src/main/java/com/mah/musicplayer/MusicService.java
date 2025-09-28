package com.mah.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.app.NotificationCompat;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private int currentPosition = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.music);

        if (mediaPlayer == null) {
            Log.e("MusicService", "Failed to create MediaPlayer!");
        }

        handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopMusic();
        } else {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                createNotification();
                startProgressThread();
            }
        }

        if (intent != null && intent.hasExtra("newPosition")) {
            int newPosition = intent.getIntExtra("newPosition", 0);
            mediaPlayer.seekTo(newPosition);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    private void createNotification() {
        String CHANNEL_ID = "music_channel";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music Player", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("الموسيقى قيد التشغيل")
                .setContentText("اضغط لإيقاف الموسيقى")
                .setSmallIcon(R.drawable.music_notes_svgrepo_com)
                .setOngoing(true)
                .addAction(R.drawable.stop_circle_svgrepo_com, "إيقاف", stopIntent())
                .build();

        startForeground(1, notification);
    }

    private void startProgressThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    currentPosition = mediaPlayer.getCurrentPosition();

                    Intent intent = new Intent("com.mah.musicplayer.UPDATE_PROGRESS");
                    intent.putExtra("currentPosition", currentPosition);
                    LocalBroadcastManager.getInstance(MusicService.this).sendBroadcast(intent);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopForeground(true);
        stopSelf();
    }

    private PendingIntent stopIntent() {
        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction("STOP");

        return PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);
    }
}
