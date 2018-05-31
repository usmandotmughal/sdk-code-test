package com.adtrax;

import android.app.Application;


import com.adtrax.adt_android_sdk.ADTRaxLib;
import com.adtrax.adt_android_sdk.AdtRaxConversionListener;

import java.util.Map;

/**
 * Created by almond on 4/27/2018.
 */

public class ADTApplication extends Application {
    private static final String ADT_DEV_KEY = "58f15d99d37f8b04f8273ec7";
    private static final String ADT_DEV_SECRET = "04464e03f0c71bdc09665864007c557a";
    @Override
    public void onCreate() {
        super.onCreate();
        ADTRaxLib adtRaxLib = ADTRaxLib.getInstance();
//        adtRaxLib.setDevSecrect("04464e03f0c71bdc09665864007c557a");
//        adtRaxLib.setAppUserID("1");
//        adtRaxLib.setUserEmail("dheena@adofgames.com");
//        adtRaxLib.setBlockAndroidIMEI("No");
//        adtRaxLib.setChannel("Google Play Store");
//        adtRaxLib.setGoogleAdId("");

        AdtRaxConversionListener conversionListener = new AdtRaxConversionListener() {
            @Override
            public void onInstallConversionDataLoaded(Map<String, String> var1) {

            }

            @Override
            public void onInstallConversionFailure(String var1) {

            }

            @Override
            public void onAppOpenAttribution(Map<String, String> var1) {

            }

            @Override
            public void onAttributionFailure(String var1) {

            }
        };

        adtRaxLib.init(ADT_DEV_KEY, ADT_DEV_SECRET ,conversionListener, this);
    }
}
