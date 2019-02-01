package com.novasa.languagecenter.model;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 29/09/16.
 */
public class Language {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("codename")
    @Expose
    private String codename;

    @SerializedName("is_fallback")
    @Expose
    private Boolean isFallback;

    @SerializedName("timestamp")
    @Expose
    private long timestamp;

    public String getName() {
        return name;
    }

    public String getCodename() {
        return codename;
    }

    public Boolean getIsFallback() {
        return isFallback;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("Language: %s (%s) - Fallback: %s : Timestamp: %d", name, codename, isFallback, timestamp);
    }
}
