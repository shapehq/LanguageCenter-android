package com.novasa.languagecenter.model;

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
    private long timestamp = 0;

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The codename
     */
    public String getCodename() {
        return codename;
    }

    /**
     *
     * @param codename
     * The codename
     */
    public void setCodename(String codename) {
        this.codename = codename;
    }

    /**
     *
     * @return
     * The isFallback
     */
    public Boolean getIsFallback() {
        return isFallback;
    }

    /**
     *
     * @param isFallback
     * The is_fallback
     */
    public void setIsFallback(Boolean isFallback) {
        this.isFallback = isFallback;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Language: " + this.getCodename() + " - Fallback: " + this.getIsFallback() + " : Timestamp: " + this.getTimestamp();
    }
}
