package com.adtrax.adt_android_sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SingleInstallationBroadcastingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String referrer = intent.getStringExtra("referrer");
            if (referrer != null) {
                if (referrer.contains(ADTRaxProperties.ADT_TEST_MODE) && intent.getStringExtra(ADTRaxProperties.ADT_TEST_INTEGRATE_MODE) != null) {
                    ADTRaxLib.getInstance().onReceive(context, intent);
                    return;
                }
            }

            String referrer_timestamp = ADTRaxProperties.getInstance().getString(ADTRaxProperties.ADT_REFERRER_TIMESTAMP);
            long currentTime = System.currentTimeMillis();

            if (referrer_timestamp == null || currentTime - Long.valueOf(referrer_timestamp) > 2000L) {
                ADTRaxLog.adtInfoLog("SingleInstallBroadcastReceiver called");
                ADTRaxLib.getInstance().onReceive(context, intent);
                ADTRaxProperties.getInstance().set(ADTRaxProperties.ADT_REFERRER_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
            }
        }
    }
}
