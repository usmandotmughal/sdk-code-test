package com.adtrax.adt_android_sdk;

import java.util.HashMap;
import java.util.Map;

public class ADTRaxProperties {
    public static final String SDK_VERSION= "1.0";
    public static final String SDK_TAG= "ADTRAX_" + SDK_VERSION;
    public static final String SDK_PREFERENCE= "adtrax-data";
    public static final String ADTRAX_DEV_KEY = "ADTRAX_DEV_KEY";
    public static final String ADTRAX_DEV_SECRET = "ADTRAX_DEV_SECRET";
    public static final String ADTRAX_USER_ID = "ADTRAX_USER_ID";
    public static final String APP_USER_ID = "APP_USER_ID";
    public static final String APP_USER_EMAIL = "APP_USER_EMAIL";
    public static final String BLOCK_FETCH_IMEI = "BLOCK_FETCH_IMEI";
    public static final String STOP_TRACKING_EVENT_FOR_THIS_APP_USER = "STOP_TRACKING_EVENT_FOR_THIS_APP_USER";
    public static final String STOP_TRACKING_SESSION_FOR_THIS_APP_USER = "STOP_TRACKING_SESSION_FOR_THIS_APP_USER";
    public static final String STOP_TRACKING_SCREEN_FOR_THIS_APP_USER = "STOP_TRACKING_SCREEN_FOR_THIS_APP_USER";
    public static final String STOP_ALL_TRACKING_FOR_THIS_APP_USER = "STOP_ALL_TRACKING_FOR_THIS_APP_USER";
    public static final String CHANNEL = "CHANNEL";
    public static final String ADDITIONAL_CUSTOM_DATA = "additionalCustomData";
    public static final String LOG_LEVEL = "logLevel";
    public static final String ADT_LAUNCH_COUNT="adtLaunchCount";
    public static final String ADT_EVENT_COUNT="adtInAppEventCount";
    public static final String ADT_REFERRER_TIMESTAMP = "ADT_REFERRER_TIMESTAMP";
    public static final String ADT_TEST_MODE = "ADTRax_Test";
    public static final String ADT_TEST_INTEGRATE_MODE = "TestingIntegrationMode";
    public static String ADT_REVENUE = "REVENUE";
    public static String ADT_LEVEL = "LEVEL";
    public static String ADT_CATEGORY = "CATEGORY";
    public static String ADT_QUANTITY = "QUANTITY";
    public static String ADT_CURRENCY = "CURRENCY";
    public static String ADT_LATITUDE = "LATITUDE";
    public static String ADT_LONGITUDE = "LONGITUDE";


    public static final String SCREEN_WIDTH = "SCREEN_WIDTH";
    public static final String SCREEN_HEIGHT = "SCREEN_HEIGHT";
    public static final String SCREEN_AVAILABLE_WIDTH = "SCREEN_AVAILABLE_WIDTH";
    public static final String SCREEN_AVAILABLE_HEIGHT = "SCREEN_AVAILABLE_HEIGHT";
    public static final String COUNTRY = "COUNTRY";
    public static final String REGION = "REGION";
    public static final String CITY = "CITY";
    public static final String GAID = "GAID";
    public static final String AID = "AID";
    public static final String IMEI = "IMEI";
    public static final String APP_VERSION= "APP_VERSION";
    public static final String REFFERRER = "REFFERRER";
    public static final String DEVICE_PIXEL_RATIO = "DEVICE_PIXEL_RATIO";
    public static final String COLOR_DEPTH = "COLOR_DEPTH";
    public static final String PIXEL_DEPTH = "PIXEL_DEPTH";
    public static final String LOAD_SPEED = "LOAD_SPEED";
    public static final String LANGUAGE = "LANGUAGE";
    public static final String JAVA_ENABLED = "JAVA_ENABLED";
    public static final String FONT_FAMILY = "FONT_FAMILY";
    public static final String FONT_WIDTH = "FONT_WIDTH";
    public static final String GOOGLE_AD_ID = "GOOGLE_AD_ID";
    public static final String COLLECT_IMEI = "COLLECT_IMEI";
    public static final String COLLECT_AID = "COLLECT_AID";
    public static final String SUCCESS = "true";

    private static ADTRaxProperties instance = new ADTRaxProperties();
    private Map<String, Object> properties = new HashMap();

    public void remove(String key) {
        this.properties.remove(key);
    }

    private ADTRaxProperties() {
    }

    public static ADTRaxProperties getInstance() {
        return instance;
    }

    public void set(String key, String value) {
        this.properties.put(key, value);
    }

    public void set(String key, String[] value) {
        this.properties.put(key, value);
    }

    public void set(String key, int value) {
        this.properties.put(key, Integer.toString(value));
    }

    public void set(String key, long value) {
        this.properties.put(key, Long.toString(value));
    }

    public void set(String key, boolean value) {
        this.properties.put(key, Boolean.toString(value));
    }

    public void setCustomData(String customData) {
        this.properties.put(ADDITIONAL_CUSTOM_DATA, customData);
    }

    public String getString(String key) {
        return (String)this.properties.get(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return (key = this.getString(key)) == null?defaultValue:Boolean.valueOf(key).booleanValue();
    }

    public int getInt(String key, int defaultValue) {
        return (key = this.getString(key)) == null?defaultValue:Integer.valueOf(key).intValue();
    }

    public long getLong(String key, long defaultValue) {
        return (key = this.getString(key)) == null?defaultValue:Long.valueOf(key).longValue();
    }

    public Object getObject(String key) {
        return this.properties.get(key);
    }

    int getLogLevel() {
        return this.getInt(LOG_LEVEL, ADTRaxLog.LogLevel.NONE.getLevel());
    }
}
