package com.novasa.languagecenter.model;

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
    private long timestamp = 0;

    /**
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key The key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return The value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return The language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language The language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "" + getKey() + " : " + getValue() + " : " + getTimestamp();
    }
}
