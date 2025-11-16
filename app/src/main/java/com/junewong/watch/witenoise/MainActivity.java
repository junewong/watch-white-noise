package com.junewong.watch.witenoise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {
    private ImageButton btnPlayPause;
    private TextView tvTimerValue;
    private TextView tvRemaining;
    private boolean isPlaying = false;
    private Handler exitHandler = new Handler(Looper.getMainLooper());
    private Runnable exitRunnable;
    private SharedPreferences prefs;
    private BroadcastReceiver updateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new PagerAdapter());
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    setupPlayPage();
                } else {
                    setupSettingsPage();
                }
            }
        });
        
        viewPager.post(() -> setupPlayPage());
        
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("UPDATE_TIME".equals(action)) {
                    long remaining = intent.getLongExtra("remaining", 0);
                    updateRemainingTime(remaining);
                } else if ("PLAYBACK_FINISHED".equals(action)) {
                    finish();
                    System.exit(0);
                }
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATE_TIME");
        filter.addAction("PLAYBACK_FINISHED");
        registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        loadTimerSetting();
        startPlayback();
    }

    private void setupPlayPage() {
        View page = findViewById(R.id.viewPager).findViewById(R.id.btnPlayPause);
        if (page != null) {
            btnPlayPause = (ImageButton) page;
            btnPlayPause.setOnClickListener(v -> togglePlayPause());
            updatePlayPauseButton();
        }
    }

    private void setupSettingsPage() {
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        View page = viewPager.findViewWithTag("page_1");
        if (page != null) {
            tvTimerValue = page.findViewById(R.id.tvTimerValue);
            tvRemaining = page.findViewById(R.id.tvRemaining);
            page.findViewById(R.id.layoutTimer).setOnClickListener(v -> showTimerPicker());
            loadTimerSetting();
        }
    }

    private void startPlayback() {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("START");
        intent.putExtra("duration", getTimerDuration());
        startForegroundService(intent);
        isPlaying = true;
        updatePlayPauseButton();
    }

    private void togglePlayPause() {
        Intent intent = new Intent(this, MusicService.class);
        if (isPlaying) {
            intent.setAction("PAUSE");
            startService(intent);
            isPlaying = false;
        } else {
            intent.setAction("RESUME");
            startService(intent);
            isPlaying = true;
            cancelExitTimer();
        }
        updatePlayPauseButton();
    }

    private void updatePlayPauseButton() {
        btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void showTimerPicker() {
        TimerPickerDialog dialog = new TimerPickerDialog(this, duration -> {
            prefs.edit().putLong("timer_duration", duration).apply();
            updateTimerDisplay(duration);
            
            Intent intent = new Intent(this, MusicService.class);
            intent.setAction("UPDATE_TIMER");
            intent.putExtra("duration", duration);
            startService(intent);
        });
        dialog.show();
    }

    private void loadTimerSetting() {
        long duration = prefs.getLong("timer_duration", 30 * 60 * 1000);
        updateTimerDisplay(duration);
    }

    private void updateTimerDisplay(long duration) {
        String text;
        if (duration == 0) {
            text = getString(R.string.timer_unlimited);
        } else if (duration == 10 * 60 * 1000) {
            text = getString(R.string.timer_10min);
        } else if (duration == 20 * 60 * 1000) {
            text = getString(R.string.timer_20min);
        } else if (duration == 30 * 60 * 1000) {
            text = getString(R.string.timer_30min);
        } else if (duration == 60 * 60 * 1000) {
            text = getString(R.string.timer_1hour);
        } else if (duration == 120 * 60 * 1000) {
            text = getString(R.string.timer_2hour);
        } else {
            text = getString(R.string.timer_30min);
        }
        tvTimerValue.setText(text);
    }

    private void updateRemainingTime(long millis) {
        if (millis == 0) {
            tvRemaining.setText("∞");
        } else {
            int minutes = (int) (millis / 60000);
            int seconds = (int) ((millis % 60000) / 1000);
            tvRemaining.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }

    private long getTimerDuration() {
        return prefs.getLong("timer_duration", 30 * 60 * 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPlaying) {
            startExitTimer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelExitTimer();
    }

    private void startExitTimer() {
        exitRunnable = () -> {
            Intent intent = new Intent(this, MusicService.class);
            intent.setAction("STOP");
            startService(intent);
            finish();
        };
        exitHandler.postDelayed(exitRunnable, 5000);
    }

    private void cancelExitTimer() {
        if (exitRunnable != null) {
            exitHandler.removeCallbacks(exitRunnable);
            exitRunnable = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateReceiver != null) {
            unregisterReceiver(updateReceiver);
        }
        cancelExitTimer();
    }

    private class PagerAdapter extends RecyclerView.Adapter<PagerAdapter.PageViewHolder> {
        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(
                viewType == 0 ? R.layout.page_play : R.layout.page_settings,
                parent,
                false
            );
            if (viewType == 1) {
                view.setTag("page_1");
            }
            return new PageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
            if (position == 0) {
                btnPlayPause = holder.itemView.findViewById(R.id.btnPlayPause);
                btnPlayPause.setOnClickListener(v -> togglePlayPause());
                updatePlayPauseButton();
            } else {
                tvTimerValue = holder.itemView.findViewById(R.id.tvTimerValue);
                tvRemaining = holder.itemView.findViewById(R.id.tvRemaining);
                holder.itemView.findViewById(R.id.layoutTimer).setOnClickListener(v -> showTimerPicker());
                loadTimerSetting();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        class PageViewHolder extends RecyclerView.ViewHolder {
            PageViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
