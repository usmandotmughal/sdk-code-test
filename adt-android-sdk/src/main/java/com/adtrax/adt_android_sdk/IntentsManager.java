package com.adtrax.adt_android_sdk;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

class Permissionmanager {
    public static boolean isPermissionAvailable(Context context, String permissionString) {
        int checkSelf = ContextCompat.checkSelfPermission(context, permissionString);
        ADTRaxLog.adtRDLog("is Permission Available: " + permissionString + "; res: " + checkSelf);
        return checkSelf == PackageManager.PERMISSION_GRANTED;
    }
}