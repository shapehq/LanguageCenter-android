package com.novasa.languagecenter;

import android.util.Log;

@SuppressWarnings("WeakerAccess")
public final class Logger {

    private static int sLogLevel = Log.WARN;

    public static void setLogLevel(int logLevel) {
        Logger.sLogLevel = logLevel;
    }

    public static void v(String v) {
        log(Log.VERBOSE, v);
    }

    public static void v(String v, Object... args) {
        log(Log.VERBOSE, v, args);
    }

    public static void d(String d) {
        log(Log.DEBUG, d);
    }

    public static void d(String d, Object... args) {
        log(Log.DEBUG, d, args);
    }

    public static void i(String i) {
        log(Log.INFO, i);
    }

    public static void i(String i, Object... args) {
        log(Log.INFO, i, args);
    }

    public static void w(String w) {
        log(Log.WARN, w);
    }

    public static void w(String w, Object... args) {
        log(Log.WARN, w, args);
    }

    public static void e(String e) {
        log(Log.ERROR, e);
    }

    public static void e(String e, Object... args) {
        log(Log.ERROR, e, args);
    }

    public static void e(Throwable t) {
        if (LanguageCenter.DEBUGGABLE && sLogLevel >= Log.ERROR) {
            t.printStackTrace();
        }
    }

    public static void e(Throwable t, String e, Object... args) {
        e(e, args);
        e(t);
    }

    public static void a(String a) {
        log(Log.ASSERT, a);
    }

    public static void a(String a, Object... args) {
        log(Log.ASSERT, a, args);
    }

    public static void log(int level, String d) {
        if (LanguageCenter.DEBUGGABLE && sLogLevel <= level) {
            Log.d(LanguageCenter.LOG_TAG, d);
        }
    }

    public static void log(int level, String d, Object... args) {
        if (LanguageCenter.DEBUGGABLE && sLogLevel <= level) {
            Log.d(LanguageCenter.LOG_TAG, String.format(d, args));
        }
    }
}
