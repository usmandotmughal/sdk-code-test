package com.adtrax.adt_android_sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ADTRaxLib implements InstallReferrerListener {
    private AdtInstallReferrerStateListener mInstallReferrer;
    private AdtRaxConversionListener conversionDataListener;
    private Context mContext;

    //API
    private static final String INSTALL_API_URL = "http://35.154.243.72:4001/v1/method1";
    private static final String EVENT_API_URL = "http://35.154.243.72:4001/v1/method2";
    private static final String DEBUG_TAG  = "ADTRaxLib";

    //Secret Key
    byte[] ivBytes = null;
    private String HEADER_SALT_KEY = "9686c39223e49c6ba56e315ae37e0cc70c0db877280343a79a51641dab693e70";
    private String BODY_SALT_KEY = "dd0370d167196516a44f211483350c41f871cdd0ee6053bfb7541b59e4a731e1";
    private String HMAC_KEY = "9e849e44f77d2f53a5f26cca";

    //Public vars
    public static boolean stopEventTracking = false;
    public static boolean stopSessionTracking = false;
    public static boolean stopScreenTracking = false;
    public static boolean stopAllTracking = false;

    private static final ADTRaxLib instance;
    private String[] propertiesArray = {ADTRaxProperties.ADT_REVENUE, ADTRaxProperties.ADT_LEVEL, ADTRaxProperties.ADT_CATEGORY,ADTRaxProperties.ADT_QUANTITY, ADTRaxProperties.ADT_CURRENCY, EventsType.EVENT_PARAM_1, EventsType.EVENT_PARAM_2, EventsType.EVENT_PARAM_3, EventsType.EVENT_PARAM_4, EventsType.EVENT_PARAM_5};

    static {
        instance = new ADTRaxLib();
    }

    public static ADTRaxLib getInstance() {
        return instance;
    }

    public void setDevKey(String key) {
        setProperty(ADTRaxProperties.ADTRAX_DEV_KEY, key);
    }

    public void setDevSecrect(String key) {
        setProperty(ADTRaxProperties.ADTRAX_DEV_SECRET, key);
    }

    public void setAppUserID(String key) {
        setProperty(ADTRaxProperties.APP_USER_ID, key);
    }

    public void setUserEmail(String key) {
        setProperty(ADTRaxProperties.APP_USER_EMAIL, key);
    }

    public void setBlockAndroidIMEI(String key) {
        setProperty(ADTRaxProperties.BLOCK_FETCH_IMEI, key);
    }
    public void setUserCountry(String countryName){
        setProperty(ADTRaxProperties.COUNTRY, countryName);
    }
    public void setUserCity(String cityName){
        setProperty(ADTRaxProperties.CITY, cityName);
    }
    public void setUserRegion(String regionName){
        setProperty(ADTRaxProperties.REGION, regionName);
    }
    public void setUserLatitude(String userLat){
        setProperty(ADTRaxProperties.ADT_LATITUDE, userLat);
    }
    public void setUserLongtitude(String userLong){
        setProperty(ADTRaxProperties.ADT_LONGITUDE, userLong);
    }
    public void setChannel(String key) {
        setProperty(ADTRaxProperties.CHANNEL, key);
    }

    public void setGoogleAdId(String key){ setProperty(ADTRaxProperties.GOOGLE_AD_ID , key);}

    public void setCollectIMEI(boolean check){
        ADTRaxProperties.getInstance().set(ADTRaxProperties.COLLECT_IMEI, check);
    }
    public void setCollectAndroidId(boolean check){
        ADTRaxProperties.getInstance().set(ADTRaxProperties.COLLECT_AID, check);
    }

    private void setPreferenceProperty(String key, String value) {
        SharedPreferences pref = getSharedPreferences(mContext);
        SharedPreferences.Editor editor= pref.edit();
        editor.putString(key, value);
        editor.commit();
    }
    private String getPreferenceProperty(String key, String defaultValue) {
        SharedPreferences pref = getSharedPreferences(mContext);
        String value = pref.getString(key, defaultValue);
        return value;
    }

    private void setProperty(String key, String value) {
        ADTRaxProperties.getInstance().set(key, value);
    }
    private void setProperty(String key, boolean value){
        ADTRaxProperties.getInstance().set(key, value);
    }

    private String getProperty(String key) {
        return ADTRaxProperties.getInstance().getString(key);
    }

    final SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(ADTRaxProperties.SDK_PREFERENCE, 0);
    }

    final int getLaunchCounter(SharedPreferences sharedPreferences, boolean isIncrease) {
        return this.getCounter(sharedPreferences, ADTRaxProperties.ADT_LAUNCH_COUNT, isIncrease);
    }

    final int getNonlaunchCounter(SharedPreferences sharedPreferences, boolean isIncrease) {
        return this.getCounter(sharedPreferences, ADTRaxProperties.ADT_EVENT_COUNT, isIncrease);
    }

    private int getCounter(SharedPreferences sharedPreferences, String parameterName, boolean isIncrease) {
        int value = sharedPreferences.getInt(parameterName, 0);
        if(isIncrease) {
            value ++;
            sharedPreferences.edit().putInt(parameterName, value);
            sharedPreferences.edit().commit();
        }

        return value;
    }

    public ADTRaxLib init(String key, String secretKey, AdtRaxConversionListener conversionDataListener, Context context) {
        if(context != null && this.allowInstallReferrer(context)) {
            if(this.mInstallReferrer == null) {
                this.mInstallReferrer = new AdtInstallReferrerStateListener();
                this.mInstallReferrer.start(context, this);
            } else {
                ADTRaxLog.adtWarnLog("AFInstallReferrer instance already created");
            }
        }

        this.getDeviceInfo(context);

        mContext = context;

        return this.init(key, secretKey,conversionDataListener);
    }

    private ADTRaxLib init(String key, String secretKey, AdtRaxConversionListener conversionDataListener) {
        setProperty(ADTRaxProperties.ADTRAX_DEV_KEY, key);
        setProperty(ADTRaxProperties.ADTRAX_DEV_SECRET, secretKey);
        this.conversionDataListener = conversionDataListener;
        if (ivBytes == null) {
            ivBytes = new byte[16];
        }
        new Random().nextBytes(ivBytes);
        return this;
    }

    private void getDeviceInfo(Context context) {
        this.getScreenInfo(context);
        this.getIMEIInfo(context);
        this.getRegionInfo(context);
        this.getOtherInfo(context);
    }

    private void getOtherInfo(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            this.setProperty(ADTRaxProperties.APP_VERSION, String.valueOf(pInfo.versionCode));
            this.setProperty(ADTRaxProperties.APP_USER_ID, String.valueOf(pInfo.versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void getIMEIInfo(Context context) {
        this.setProperty(ADTRaxProperties.IMEI, "");
        this.setProperty(ADTRaxProperties.AID, "");

        if (!Permissionmanager.isPermissionAvailable(context, "android.permission.READ_PHONE_STATE")) {
            return;
        }

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ADTRaxProperties.getInstance().getString(ADTRaxProperties.BLOCK_FETCH_IMEI) != null &&
                ADTRaxProperties.getInstance().getString(ADTRaxProperties.BLOCK_FETCH_IMEI).equals("No")) {
            if (null != tm) {
                this.setProperty(ADTRaxProperties.IMEI, tm.getDeviceId());
            }

            this.setProperty(ADTRaxProperties.IMEI, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        }
    }

    @SuppressLint("MissingPermission")
    private void getRegionInfo(Context context) {
        this.setProperty(ADTRaxProperties.LANGUAGE, Locale.getDefault().getLanguage());

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            this.setProperty(ADTRaxProperties.COUNTRY, "");
            this.setProperty(ADTRaxProperties.CITY, "");
            this.setProperty(ADTRaxProperties.REGION, "");

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                else {
                    Geocoder gcd = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses = gcd.getFromLocation(location.getLatitude(),
                                location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            this.setProperty(ADTRaxProperties.COUNTRY, addresses.get(0).getCountryName());
                            this.setProperty(ADTRaxProperties.CITY, addresses.get(0).getLocality());
                            this.setProperty(ADTRaxProperties.REGION, addresses.get(0).getAdminArea());
                            this.setProperty(ADTRaxProperties.ADT_LATITUDE, String.valueOf(addresses.get(0).getLatitude()));
                            this.setProperty(ADTRaxProperties.ADT_LONGITUDE, String.valueOf(addresses.get(0).getLongitude()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void getScreenInfo(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(displayMetrics);
        this.setProperty(ADTRaxProperties.SCREEN_WIDTH, String.valueOf(displayMetrics.widthPixels));
        this.setProperty(ADTRaxProperties.SCREEN_HEIGHT, String.valueOf(displayMetrics.heightPixels));

        Point outPoint = new Point();
        if (Build.VERSION.SDK_INT >= 19) {
            // include navigation bar
            display.getRealSize(outPoint);
        } else {
            // exclude navigation bar
            display.getSize(outPoint);
        }
        if (outPoint.y > outPoint.x) {
            this.setProperty(ADTRaxProperties.SCREEN_AVAILABLE_WIDTH, String.valueOf(outPoint.x));
            this.setProperty(ADTRaxProperties.SCREEN_AVAILABLE_HEIGHT, String.valueOf(outPoint.y));
        } else {
            this.setProperty(ADTRaxProperties.SCREEN_AVAILABLE_WIDTH, String.valueOf(outPoint.y));
            this.setProperty(ADTRaxProperties.SCREEN_AVAILABLE_HEIGHT, String.valueOf(outPoint.x));
        }

        this.setProperty(ADTRaxProperties.DEVICE_PIXEL_RATIO, String.valueOf(displayMetrics.densityDpi));

        this.setProperty(ADTRaxProperties.COLOR_DEPTH, String.valueOf(display.getPixelFormat()));
        this.setProperty(ADTRaxProperties.PIXEL_DEPTH, String.valueOf(display.getPixelFormat()));
        this.setProperty(ADTRaxProperties.LOAD_SPEED, "120");
        this.setProperty(ADTRaxProperties.FONT_WIDTH, "150");
        this.setProperty(ADTRaxProperties.FONT_FAMILY, "aerial");
        this.setProperty(ADTRaxProperties.JAVA_ENABLED, "true");
    }

    private boolean allowInstallReferrer(@NonNull Context context) {
        SharedPreferences var2 = this.getSharedPreferences(context);
        if(this.getLaunchCounter(var2, false) > 2) {
            ADTRaxLog.adtRDLog("Install referrer will not load, the counter > 2, ");
            return false;
        } else {
            try {
                Class.forName("com.android.installreferrer.api.InstallReferrerClient");
                if(Permissionmanager.isPermissionAvailable(context, "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE")) {
                    ADTRaxLog.adtDebugLog("Install referrer is allowed");
                    return true;
                }
            } catch (ClassNotFoundException var3) {
                ADTRaxLog.adtRDLog("Class com.android.installreferrer.api.InstallReferrerClient not found");
                return false;
            } catch (Throwable var4) {
                ADTRaxLog.adtErrorLog("An error occurred while trying to verify manifest : com.android.installreferrer.api.InstallReferrerClient", var4);
                return false;
            }

            ADTRaxLog.adtDebugLog("Install referrer is not allowed");
            return false;
        }
    }
    void onReceive(Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");
        String gaid = intent.getStringExtra("gaid");
        this.setProperty(ADTRaxProperties.REFFERRER, referrer != null ? referrer : "");
        this.setProperty(ADTRaxProperties.GAID, gaid != null ? gaid : "");

        this.sendInstallAPI();
    }

    public void setRevenue(float revenue) {
        setProperty(ADTRaxProperties.ADT_REVENUE, String.valueOf(revenue));
    }

    public void setLevel(int level) {
        setProperty(ADTRaxProperties.ADT_LEVEL, String.valueOf(level));
    }

    public void setCategory(String category) {
        setProperty(ADTRaxProperties.ADT_CATEGORY, category);
    }

    public void setQuantity(float quantity) {
        setProperty(ADTRaxProperties.ADT_QUANTITY, String.valueOf(quantity));
    }

    public void setCurrency(String currency) {
        setProperty(ADTRaxProperties.ADT_CURRENCY, currency);
    }
    public void setCurrentScreenName(String screenName) {
        setProperty(EventsType.EVENT_SCREEN_NAME, screenName);
    }

    public void setPrevScreenName(String screenName) {
        setProperty(EventsType.EVENT_PREV_SCREEN_NAME, screenName);
    }

    public void setParam1Event(String param) {
        setProperty(EventsType.EVENT_PARAM_1, param);
    }

    public void setParam2Event(String param) {
        setProperty(EventsType.EVENT_PARAM_2, param);
    }

    public void setParam3Event(String param) {
        setProperty(EventsType.EVENT_PARAM_3, param);
    }

    public void setParam4Event(String param) {
        setProperty(EventsType.EVENT_PARAM_4, param);
    }

    public void setParam5Event(String param) {
        setProperty(EventsType.EVENT_PARAM_5, param);
    }

    public void clearEvent() {
        setProperty(EventsType.EVENT_SCREEN_NAME, "");
        setProperty(EventsType.EVENT_PREV_SCREEN_NAME, "");
        setProperty(EventsType.EVENT_PARAM_1, "");
        setProperty(EventsType.EVENT_PARAM_2, "");
        setProperty(EventsType.EVENT_PARAM_3, "");
        setProperty(EventsType.EVENT_PARAM_4, "");
        setProperty(EventsType.EVENT_PARAM_5, "");
    }
    public void sendEventTracking(String eventName, int eventType, Map<String, Object> eventValue){
        if (EmulatorProtect.isEmulator()) {
            Log.d(DEBUG_TAG, "Don't spot event tracking on emulator. EventTracking is disabled");
            return;
        }
        if (stopAllTracking){
            Log.d(DEBUG_TAG, "stopAllTracking found true. EventTracking is disabled");
            return;
        }
        if (stopEventTracking){
            Log.d(DEBUG_TAG, "stopEventTracking found true. EventTracking is disabled");
            return;
        }
        if (eventName.equals(EventsType.EVENT_SCREEN) && stopScreenTracking){
            Log.d(DEBUG_TAG, "stopScreenTracking found true. EventTracking is disabled");
            return;
        }
        String header = "1"
                + randomGenerate(5)
                + String.valueOf(System.currentTimeMillis())
                + this.getProperty(ADTRaxProperties.ADTRAX_DEV_KEY)
                + randomGenerate(4)
                + this.getProperty(ADTRaxProperties.ADTRAX_DEV_SECRET)
                + getPreferenceProperty(ADTRaxProperties.ADTRAX_USER_ID, "")
                + ADTRaxProperties.SDK_VERSION
                + this.getProperty(ADTRaxProperties.SCREEN_WIDTH)
                + this.getProperty(ADTRaxProperties.SCREEN_HEIGHT);
          try {
              header = encryptAES(header, HEADER_SALT_KEY);
              HashMap<String, String> postData = new HashMap<>();
              addEncryptParameterPropertyValue(postData, "known1", ADTRaxProperties.COUNTRY, BODY_SALT_KEY);
              addEncryptParameterPropertyValue(postData, "known2", ADTRaxProperties.REGION, BODY_SALT_KEY);
              addEncryptParameterPropertyValue(postData, "known3", ADTRaxProperties.CITY, BODY_SALT_KEY);
              addEncryptParameterValue(postData, "key1", String.valueOf(EventsType.DYNAMIC_EVENT), BODY_SALT_KEY);
              addEncryptParameterValue(postData, "key2", String.valueOf(eventName), BODY_SALT_KEY);
              addEncryptParameterPropertyValue(postData, "key9", ADTRaxProperties.ADT_LATITUDE, BODY_SALT_KEY);
              addEncryptParameterPropertyValue(postData, "key10", ADTRaxProperties.ADT_LONGITUDE, BODY_SALT_KEY);
              addEncryptParameterPropertyValue(postData, "key11", EventsType.EVENT_SCREEN_NAME, BODY_SALT_KEY);
              addEncryptParameterPropertyValue(postData, "key12", EventsType.EVENT_PREV_SCREEN_NAME, BODY_SALT_KEY);
              Set set = eventValue.entrySet();
              Iterator iterator = set.iterator();
              while (iterator.hasNext()){
                  Map.Entry e = (Map.Entry) iterator.next();
                  if (propertiesArray.equals(e.getValue())){
                      Log.d(DEBUG_TAG, String.valueOf(e.getKey()) + String.valueOf(e.getValue()));
                  }
                  if (e.getKey() == ADTRaxProperties.ADT_REVENUE){
                      addEncryptParameterPropertyValue(postData, "key3", String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else {
                      addEncryptParameterPropertyValue(postData, "key3", ADTRaxProperties.ADT_REVENUE, BODY_SALT_KEY);
                  }
                  if (e.getKey() == ADTRaxProperties.ADT_LEVEL){
                      addEncryptParameterPropertyValue(postData, "key4",String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else {
                      addEncryptParameterPropertyValue(postData, "key4", ADTRaxProperties.ADT_REVENUE, BODY_SALT_KEY);
                  }if (e.getKey() == ADTRaxProperties.SUCCESS){
                      addEncryptParameterPropertyValue(postData, "key5", String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else {
                      addEncryptParameterPropertyValue(postData, "key5", ADTRaxProperties.SUCCESS, BODY_SALT_KEY);
                  }if (e.getKey() == ADTRaxProperties.ADT_CATEGORY){
                      addEncryptParameterPropertyValue(postData, "key6", String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else{
                      addEncryptParameterPropertyValue(postData, "key6", ADTRaxProperties.ADT_CATEGORY, BODY_SALT_KEY);
                  }if (e.getKey() == ADTRaxProperties.ADT_QUANTITY){
                      addEncryptParameterPropertyValue(postData, "key7", String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else {
                      addEncryptParameterPropertyValue(postData, "key7", ADTRaxProperties.ADT_QUANTITY, BODY_SALT_KEY);
                  }if (e.getKey() == ADTRaxProperties.ADT_CURRENCY){
                      addEncryptParameterPropertyValue(postData, "key8", String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else{
                      addEncryptParameterPropertyValue(postData, "key8", ADTRaxProperties.ADT_CURRENCY, BODY_SALT_KEY);
                  }if (e.getKey() == EventsType.EVENT_PARAM_1){
                      addEncryptParameterPropertyValue(postData, "key13", String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else{
                      addEncryptParameterPropertyValue(postData, "key13", EventsType.EVENT_PARAM_1, BODY_SALT_KEY);
                  }if (e.getKey() == EventsType.EVENT_PARAM_2){
                      addEncryptParameterPropertyValue(postData, "key14", String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else{
                      addEncryptParameterPropertyValue(postData, "key14", EventsType.EVENT_PARAM_2, BODY_SALT_KEY);
                  }if (e.getKey() == EventsType.EVENT_PARAM_3){
                      addEncryptParameterPropertyValue(postData, "key15", String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else{
                      addEncryptParameterPropertyValue(postData, "key15", EventsType.EVENT_PARAM_3, BODY_SALT_KEY);
                  }if (e.getKey() == EventsType.EVENT_PARAM_4){
                      addEncryptParameterPropertyValue(postData, "key16", String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else{
                      addEncryptParameterPropertyValue(postData, "key16", EventsType.EVENT_PARAM_5, BODY_SALT_KEY);
                  }if (e.getKey() == EventsType.EVENT_PARAM_5){
                      addEncryptParameterPropertyValue(postData, "key17",String.valueOf(e.getValue()), BODY_SALT_KEY);
                  }else{
                      addEncryptParameterPropertyValue(postData, "key17", EventsType.EVENT_PARAM_5, BODY_SALT_KEY);
                  }
              }
              HashMap<String, String> hMacMap = new HashMap<>();
              addEncryptParameterValue(hMacMap, "key1", String.valueOf(EventsType.DYNAMIC_EVENT), null);
              addEncryptParameterValue(hMacMap, "key2", String.valueOf(eventName), null);
              addEncryptParameterPropertyValue(hMacMap, "key11", EventsType.EVENT_SCREEN_NAME, null);
              String macValue = encodeHMAC(System.currentTimeMillis() + "&" + getDataFromParameters(hMacMap), HMAC_KEY);

              addEncryptParameterValue(postData, "key19", macValue, null);

              String data = getDataFromParameters(postData);

              new APIRequest().execute(EVENT_API_URL, header, data);

          }catch (Exception e ){
              e.printStackTrace();
          }
    }
    public void sendEventTracking(String eventName, int eventType, boolean success) {
        if (EmulatorProtect.isEmulator()) {
            Log.d(DEBUG_TAG, "Don't spot event tracking on emulator. EventTracking is disabled");
            return;
        }
        if (stopAllTracking){
            Log.d(DEBUG_TAG, "stopAllTracking found true. EventTracking is disabled");
            return;
        }
        if (stopEventTracking){
            Log.d(DEBUG_TAG, "stopEventTracking found true. EventTracking is disabled");
            return;
        }
        if (eventName.equals(EventsType.EVENT_SCREEN) && stopScreenTracking){
            Log.d(DEBUG_TAG, "stopScreenTracking found true. EventTracking is disabled");
            return;
        }
        String header = "1"
                + randomGenerate(5)
                + String.valueOf(System.currentTimeMillis())
                + this.getProperty(ADTRaxProperties.ADTRAX_DEV_KEY)
                + randomGenerate(4)
                + this.getProperty(ADTRaxProperties.ADTRAX_DEV_SECRET)
                + getPreferenceProperty(ADTRaxProperties.ADTRAX_USER_ID, "")
                + ADTRaxProperties.SDK_VERSION
                + this.getProperty(ADTRaxProperties.SCREEN_WIDTH)
                + this.getProperty(ADTRaxProperties.SCREEN_HEIGHT);

        try {
            header = encryptAES(header, HEADER_SALT_KEY);

            HashMap<String, String> postData = new HashMap<>();
            addEncryptParameterPropertyValue(postData, "known1", ADTRaxProperties.COUNTRY, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "known2", ADTRaxProperties.REGION, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "known3", ADTRaxProperties.CITY, BODY_SALT_KEY);
            addEncryptParameterValue(postData, "key1", String.valueOf(eventType), BODY_SALT_KEY);
            addEncryptParameterValue(postData, "key2", String.valueOf(eventName), BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key3", ADTRaxProperties.ADT_REVENUE, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key4", ADTRaxProperties.ADT_LEVEL, BODY_SALT_KEY);
            addEncryptParameterValue(postData, "key5", String.valueOf(success), BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key6", ADTRaxProperties.ADT_CATEGORY, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key7", ADTRaxProperties.ADT_QUANTITY, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key8", ADTRaxProperties.ADT_CURRENCY, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key9", ADTRaxProperties.ADT_LATITUDE, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key10", ADTRaxProperties.ADT_LONGITUDE, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key11", EventsType.EVENT_SCREEN_NAME, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key12", EventsType.EVENT_PREV_SCREEN_NAME, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key13", EventsType.EVENT_PARAM_1, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key14", EventsType.EVENT_PARAM_2, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key15", EventsType.EVENT_PARAM_3, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key16", EventsType.EVENT_PARAM_4, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key17", EventsType.EVENT_PARAM_5, BODY_SALT_KEY);
//            addEncryptParameterValue(postData, "key18", String.valueOf(System.currentTimeMillis()), BODY_SALT_KEY);


            HashMap<String, String> hMacMap = new HashMap<>();
            addEncryptParameterValue(hMacMap, "key1", String.valueOf(eventType), null);
            addEncryptParameterValue(hMacMap, "key2", String.valueOf(eventName), null);
            addEncryptParameterPropertyValue(hMacMap, "key11", EventsType.EVENT_SCREEN_NAME, null);
            String macValue = encodeHMAC(System.currentTimeMillis() + "&" + getDataFromParameters(hMacMap), HMAC_KEY);

            addEncryptParameterValue(postData, "key19", macValue, null);

            String data = getDataFromParameters(postData);

            new APIRequest().execute(EVENT_API_URL, header, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void sendInstallAPI() {
        if (EmulatorProtect.isEmulator()) {
            return;
        }

        String header = "1"
                + randomGenerate(5)
                + String.valueOf(System.currentTimeMillis())
                + this.getProperty(ADTRaxProperties.ADTRAX_DEV_KEY)
                + randomGenerate(4)
                + this.getProperty(ADTRaxProperties.ADTRAX_DEV_SECRET)
                + ADTRaxProperties.SDK_VERSION
                + this.getProperty(ADTRaxProperties.SCREEN_WIDTH)
                + this.getProperty(ADTRaxProperties.SCREEN_HEIGHT);
        try {
            header = encryptAES(header, HEADER_SALT_KEY);

            HashMap<String, String> postData = new HashMap<>();
            addEncryptParameterPropertyValue(postData, "known1", ADTRaxProperties.COUNTRY, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "known2", ADTRaxProperties.REGION, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "known3", ADTRaxProperties.CITY, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key1", ADTRaxProperties.APP_USER_ID, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key2", ADTRaxProperties.APP_USER_EMAIL, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key3", ADTRaxProperties.GAID, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key4", ADTRaxProperties.AID, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key5", ADTRaxProperties.IMEI, BODY_SALT_KEY);
            addEncryptParameterValue(postData, "key6", "", null);
            addEncryptParameterValue(postData, "key7", "", null);
            addEncryptParameterPropertyValue(postData, "key8", ADTRaxProperties.CHANNEL, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key9", ADTRaxProperties.REFFERRER, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key10", ADTRaxProperties.APP_VERSION, BODY_SALT_KEY);
            addEncryptParameterValue(postData, "key11", ADTRaxProperties.SDK_VERSION, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key12", ADTRaxProperties.DEVICE_PIXEL_RATIO, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key13", ADTRaxProperties.SCREEN_AVAILABLE_WIDTH, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key14", ADTRaxProperties.SCREEN_AVAILABLE_HEIGHT, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key15", ADTRaxProperties.SCREEN_WIDTH, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key16", ADTRaxProperties.SCREEN_HEIGHT, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key17", ADTRaxProperties.COLOR_DEPTH, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key18", ADTRaxProperties.PIXEL_DEPTH, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key19", ADTRaxProperties.LOAD_SPEED, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key20", ADTRaxProperties.LANGUAGE, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key21", ADTRaxProperties.JAVA_ENABLED, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key22", ADTRaxProperties.FONT_FAMILY, BODY_SALT_KEY);
            addEncryptParameterPropertyValue(postData, "key23", ADTRaxProperties.FONT_WIDTH, BODY_SALT_KEY);

            HashMap<String, String> hMacMap = new HashMap<>();
            addEncryptParameterPropertyValue(hMacMap, "key1", ADTRaxProperties.APP_USER_ID, null);
            addEncryptParameterValue(hMacMap, "key11", ADTRaxProperties.SDK_VERSION, null);
            addEncryptParameterPropertyValue(hMacMap, "key18", ADTRaxProperties.PIXEL_DEPTH, null);
            addEncryptParameterPropertyValue(hMacMap, "key20", ADTRaxProperties.LANGUAGE, null);
            addEncryptParameterPropertyValue(hMacMap, "key23", ADTRaxProperties.FONT_WIDTH, null);
            String macValue = encodeHMAC(System.currentTimeMillis() + "&" + getDataFromParameters(hMacMap), HMAC_KEY);

            addEncryptParameterValue(postData, "key24", macValue, null);

            String data = getDataFromParameters(postData);

            new APIRequest().execute(INSTALL_API_URL, header, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addEncryptParameterPropertyValue(HashMap<String, String> data, String key, String property, String secKey) {
        String value = this.getProperty(property);
        addEncryptParameterValue(data, key, value, secKey);
    }

    private void addEncryptParameterValue(HashMap<String, String> data, String key, String value, String secKey) {
        if (secKey == null || TextUtils.isEmpty(value)) {
            data.put(key, value);
        }
        else {
            try {
                String newValue = URLEncoder.encode(value,"UTF-8");
                data.put(key, encryptAES(newValue, secKey));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private String getDataFromParameters(HashMap<String, String> parameters)
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?,?> entry : parameters.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            String append = "";
            if (entry.getValue() != null) {
                append = entry.getValue().toString();
            }
            sb.append(String.format("%s=%s",
                    entry.getKey().toString(), append));
        }

        return sb.toString();
    }
    private class APIRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String apiUrl = params[0];
            String header = params[1];
            String postData = params[2];

            return sendCallAPI(apiUrl, header, postData);
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
        }
    }
    private String sendCallAPI(String apiUrl, String header, String postData) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-adtrax", header);
            conn.setConnectTimeout(10000);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            ADTRaxLog.adtInfoLog("Install Response: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Map<String, List<String>> headerResponse = conn.getHeaderFields();
                if (apiUrl.equals(INSTALL_API_URL) && headerResponse.get("x-adtrax-user-id") != null) {
                    String adtrax_user_id = headerResponse.get("x-adtrax-user-id").get(0);
                    setPreferenceProperty(ADTRaxProperties.ADTRAX_USER_ID, adtrax_user_id);
                }

                ADTRaxLog.adtInfoLog("Install Response Success: 1");
                return "1";
            }
            else {
                ADTRaxLog.adtInfoLog("Install Response Success: 0");
                return "0";
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "0";
    }
    private SecretKeySpec generateKey(final String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes();
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    public String encryptAES(String message, String key1) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = generateKey(key1);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] cipherText = cipher.doFinal(message.getBytes());

        byte[] combined = new byte[ivBytes.length + cipherText.length];

        System.arraycopy(ivBytes,0,combined,0, ivBytes.length);
        System.arraycopy(cipherText,0,combined, ivBytes.length, cipherText.length);

        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }
    static final String HEXES = "0123456789ABCDEF";
    public static String getHex( byte [] raw ) {
        if ( raw == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static String encodeHMAC(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hmac = sha256_HMAC.doFinal(data.getBytes());

        return getHex(hmac);
    }

    public String randomGenerate(int length) {
        String result = "";

        for (int i = 0; i < length; i ++) {
            result += String.valueOf(new Random().nextInt(10000) % 10);
        }
        return result;
    }

    @Override
    public void onHandlerReferrer(Map<String, String> properties) {

    }
}
