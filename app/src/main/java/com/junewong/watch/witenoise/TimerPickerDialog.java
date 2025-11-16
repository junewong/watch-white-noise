package com.junewong.watch.witenoise;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class TimerPickerDialog extends Dialog {
    public interface OnTimerSelectedListener {
        void onTimerSelected(long duration);
    }

    private OnTimerSelectedListener listener;

    public TimerPickerDialog(Context context, OnTimerSelectedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_timer_picker);

        setupTimerOption(R.id.timer_10min, 10 * 60 * 1000);
        setupTimerOption(R.id.timer_20min, 20 * 60 * 1000);
        setupTimerOption(R.id.timer_30min, 30 * 60 * 1000);
        setupTimerOption(R.id.timer_1hour, 60 * 60 * 1000);
        setupTimerOption(R.id.timer_2hour, 120 * 60 * 1000);
        setupTimerOption(R.id.timer_unlimited, 0);
    }

    private void setupTimerOption(int viewId, long duration) {
        TextView textView = findViewById(viewId);
        textView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTimerSelected(duration);
            }
            dismiss();
        });
    }
}
