package com.hxklabs.astroilce;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.widget.TextView;

public class BatteryStatusController {
    private static final long POLL_TIMER = 60 * 1000;

    private final TextView batteryTextView;
    private final Context context;
    private final Handler handler;

    private final Runnable queryBatteryStatus = new Runnable() {
        @Override
        public void run() {
            Log.d("Query battery status");
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, intentFilter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                int percent = Float.valueOf((float) level / (float) scale * 100f).intValue();
                Log.d("Battery percent = " + percent);
                batteryTextView.setText(String.format("%d%%", percent));
            }

            if (handler != null) {
                handler.postDelayed(queryBatteryStatus, POLL_TIMER);
            }
        }
    };

    public BatteryStatusController(Context context, Handler handler, TextView batteryTextView) {
        this.context = context;
        this.batteryTextView = batteryTextView;
        this.handler = handler;
    }

    public void startPolling() {
        if (handler != null) {
            handler.post(queryBatteryStatus);
        }
    }

    public void stopPolling() {
        if (handler != null) {
            handler.removeCallbacks(queryBatteryStatus);
        }
    }
}
