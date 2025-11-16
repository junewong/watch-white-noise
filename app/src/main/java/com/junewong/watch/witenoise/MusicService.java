package com.junewong.watch.witenoise;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private PowerManager.WakeLock wakeLock;
    private CountDownTimer countDownTimer;
    private long remainingTime;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WhiteNoise::lock");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if ("START".equals(action)) {
            long duration = intent.getLongExtra("duration", 30 * 60 * 1000);
            startForeground(1, createNotification());
            startPlayback(duration);
        } else if ("PAUSE".equals(action)) {
            pausePlayback();
        } else if ("RESUME".equals(action)) {
            resumePlayback();
        } else if ("STOP".equals(action)) {
            stopPlayback();
        } else if ("UPDATE_TIMER".equals(action)) {
            long duration = intent.getLongExtra("duration", 30 * 60 * 1000);
            updateTimer(duration);
        }

        return START_NOT_STICKY;
    }

    private void startPlayback(long duration) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.rain);
            mediaPlayer.setLooping(true);
        }
        mediaPlayer.start();
        wakeLock.acquire();

        if (duration > 0) {
            startTimer(duration);
        } else {
            remainingTime = 0;
            sendTimeUpdate(0);
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void resumePlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
            if (remainingTime > 0) {
                startTimer(remainingTime);
            }
        }
    }

    private void stopPlayback() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        stopForeground(true);
        stopSelf();
    }

    private void startTimer(long duration) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                sendTimeUpdate(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                fadeOutAndExit();
            }
        };
        countDownTimer.start();
    }

    private void updateTimer(long duration) {
        if (duration > 0) {
            startTimer(duration);
        } else {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            remainingTime = 0;
            sendTimeUpdate(0);
        }
    }

    private void fadeOutAndExit() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            new Thread(() -> {
                float volume = 1.0f;
                while (volume > 0) {
                    volume -= 0.05f;
                    if (volume < 0) volume = 0;
                    mediaPlayer.setVolume(volume, volume);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                stopPlayback();
                sendBroadcast(new Intent("PLAYBACK_FINISHED"));
            }).start();
        } else {
            stopPlayback();
            sendBroadcast(new Intent("PLAYBACK_FINISHED"));
        }
    }

    private void sendTimeUpdate(long millis) {
        Intent intent = new Intent("UPDATE_TIME");
        intent.putExtra("remaining", millis);
        sendBroadcast(intent);
    }

    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(
            "music_channel",
            "音乐播放",
            NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        return new Notification.Builder(this, "music_channel")
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.playing))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayback();
    }
}
