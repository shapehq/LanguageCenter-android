package com.novasa.languagecenter.model;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by andersp on 28/09/16.
 */
public class Translation {

    @SerializedName("key")
    @Expose
    private String key;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("language")
    @Expose
    private String language;

    @SerializedName("timestamp")
    @Expose
    private long timestamp;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getLanguage() {
        return language;
    }

    @SuppressWarnings("WeakerAccess")
    public long getTimestamp() {
        return timestamp;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("%s: %s (%d)", key, value, timestamp);
    }
}
