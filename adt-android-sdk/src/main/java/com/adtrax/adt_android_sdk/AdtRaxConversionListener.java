package com.adtrax.adt_android_sdk;

import android.support.annotation.WorkerThread;

import java.util.Map;

public interface AdtRaxConversionListener {
    @WorkerThread
    void onInstallConversionDataLoaded(Map<String, String> var1);

    void onInstallConversionFailure(String var1);

    @WorkerThread
    void onAppOpenAttribution(Map<String, String> var1);

    void onAttributionFailure(String var1);
}
