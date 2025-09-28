package com.mah.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {
    private Button playButton, stopButton;
    private TextView statusTextView;
    private SeekBar seekBar;
    private BroadcastReceiver progressReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);
        statusTextView = findViewById(R.id.statusTextView);
        seekBar = findViewById(R.id.seekBar);

        playButton.setOnClickListener(v -> {
            Intent playIntent = new Intent(MainActivity.this, MusicService.class);
            startService(playIntent);
            statusTextView.setText("قيد التشغيل");
        });

        stopButton.setOnClickListener(v -> {
            Intent stopIntent = new Intent(MainActivity.this, MusicService.class);
            stopService(stopIntent);
            statusTextView.setText("موقوف");
        });

        progressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int currentPosition = intent.getIntExtra("currentPosition", 0);
                seekBar.setProgress(currentPosition);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(progressReceiver,
                new IntentFilter("com.mah.musicplayer.UPDATE_PROGRESS"));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent changePositionIntent = new Intent(MainActivity.this, MusicService.class);
                    changePositionIntent.putExtra("newPosition", progress);
                    startService(changePositionIntent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(progressReceiver);
    }
}
