package com.adtrax.adt_android_sdk;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ADTRaxLog {
    private static long startTime = System.currentTimeMillis();

    public ADTRaxLog() {
    }

    public static void adtInfoLog(String logMessage, boolean shouldRemoteDebug) {
        if(shouldLog(ADTRaxLog.LogLevel.INFO)) {
            Log.i(ADTRaxProperties.SDK_TAG, getMessage(logMessage));
        }
    }

    static void adtDebugLog(String debugLogMessage, boolean shouldRemoteDebug) {
        if(shouldLog(ADTRaxLog.LogLevel.DEBUG)) {
            Log.d(ADTRaxProperties.SDK_TAG, getMessage(debugLogMessage));
        }
    }

    static void adtErrorLog(String errorLogMessage, Throwable ex, boolean shouldRemoteDebug, boolean shouldOutputToLog) {
        if(shouldLog(ADTRaxLog.LogLevel.ERROR) && shouldOutputToLog) {
            Log.e(ADTRaxProperties.SDK_TAG, getMessage(errorLogMessage), ex);
        }
    }

    static void adtWarnLog(String warningLogMessage, boolean shouldRemoteDebug) {
        if(shouldLog(ADTRaxLog.LogLevel.WARNING)) {
            Log.w(ADTRaxProperties.SDK_TAG, getMessage(warningLogMessage));
        }
    }

    public static void adtRDLog(String rdLogMessage) {
        if(shouldLog(ADTRaxLog.LogLevel.VERBOSE)) {
            Log.v(ADTRaxProperties.SDK_TAG, getMessage(rdLogMessage));
        }
    }

    public static void adtDebugLog(String debugLogMessage) {
        adtDebugLog(debugLogMessage, true);
    }

    public static void adtInfoLog(String logMessage) {
        adtInfoLog(logMessage, true);
    }

    public static void adtErrorLog(String errorLogMessage, Throwable ex) {
        adtErrorLog(errorLogMessage, ex, true, false);
    }

    public static void adtErrorLog(String errorLogMessage, Throwable ex, boolean shouldOutputToLog) {
        adtErrorLog(errorLogMessage, ex, true, shouldOutputToLog);
    }

    public static void adtWarnLog(String warningLogMessage) {
        adtWarnLog(warningLogMessage, true);
    }

    private static boolean shouldLog(ADTRaxLog.LogLevel level) {
        return level.getLevel() <= ADTRaxProperties.getInstance().getLogLevel();
    }

    @NonNull
    private static String getMessage(String logMessage) {
        return getMessage(logMessage, false);
    }

    @NonNull
    private static String getMessage(String logMessage, boolean forceTimerDelta) {
        return !forceTimerDelta && ADTRaxLog.LogLevel.VERBOSE.getLevel() != ADTRaxProperties.getInstance().getLogLevel()?logMessage:"(" + timeString(System.currentTimeMillis() - startTime) + ") " + logMessage;
    }

    static String timeString(long gap) {
        long var2 = TimeUnit.MILLISECONDS.toHours(gap);
        gap -= TimeUnit.HOURS.toMillis(var2);
        long var4 = TimeUnit.MILLISECONDS.toMinutes(gap);
        gap -= TimeUnit.MINUTES.toMillis(var4);
        long var6 = TimeUnit.MILLISECONDS.toSeconds(gap);
        gap -= TimeUnit.SECONDS.toMillis(var6);
        long var8 = TimeUnit.MILLISECONDS.toMillis(gap);
        return String.format(Locale.getDefault(), "%02d:%02d:%02d:%03d", new Object[]{Long.valueOf(var2), Long.valueOf(var4), Long.valueOf(var6), Long.valueOf(var8)});
    }

    public static void resetDeltaTime() {
        startTime = System.currentTimeMillis();
    }

    public static enum LogLevel {
        NONE(0),
        ERROR(1),
        WARNING(2),
        INFO(3),
        DEBUG(4),
        VERBOSE(5);

        private int level;

        private LogLevel(int level) {
            this.level = level;
        }

        public final int getLevel() {
            return this.level;
        }
    }
}
