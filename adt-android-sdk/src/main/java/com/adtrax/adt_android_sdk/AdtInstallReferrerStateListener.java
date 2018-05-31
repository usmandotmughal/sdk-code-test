package com.adtrax.adt_android_sdk;

import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.util.HashMap;
import java.util.Map;

public class AdtInstallReferrerStateListener implements InstallReferrerStateListener {
    private InstallReferrerClient mReferrerClient;
    private InstallReferrerListener mInstallReferrerListener;

    protected final void start(Context context, InstallReferrerListener installReferrerListener) {
       this.mInstallReferrerListener = installReferrerListener;
       this.mReferrerClient = InstallReferrerClient.newBuilder(context).build();

        try {
            this.mReferrerClient.startConnection(this);
        } catch (Exception exception) {
            ADTRaxLog.adtErrorLog("referrerClient -> startConnection", exception);
        }
    }
    private void handleReferrer(@Nullable ReferrerDetails response, Map<String, String> referrer) {
        if(response != null) {
            if(response.getInstallReferrer() != null) {
                referrer.put("val", response.getInstallReferrer());
            }

            referrer.put("clk", Long.toString(response.getReferrerClickTimestampSeconds()));
            referrer.put("install", Long.toString(response.getInstallBeginTimestampSeconds()));
        }

        if(this.mInstallReferrerListener != null) {
            this.mInstallReferrerListener.onHandlerReferrer(referrer);
        }

    }

    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        HashMap map = new HashMap();
        map.put("code", String.valueOf(responseCode));
        ReferrerDetails details = null;
        switch(responseCode) {
            case 0:
                try {
                    ADTRaxLog.adtDebugLog("InstallReferrer connected");
                    details = this.mReferrerClient.getInstallReferrer();
                    this.mReferrerClient.endConnection();
                } catch (RemoteException exception) {
                    exception.printStackTrace();
                }
                break;
            case 1:
                ADTRaxLog.adtWarnLog("InstallReferrer not supported");
                break;
            case 2:
                ADTRaxLog.adtWarnLog("InstallReferrer not supported");
                break;
            default:
                ADTRaxLog.adtWarnLog("responseCode not found.");
        }

        this.handleReferrer(details, map);
    }

    @Override
    public void onInstallReferrerServiceDisconnected() {
        ADTRaxLog.adtDebugLog("Install Referrer service disconnected");

    }
}
