package com.novasa.languagecenter.interfaces;

import android.support.annotation.NonNull;

public interface OnLanguageCenterReadyCallback {
    void onLanguageCenterReady(@NonNull String language, boolean success);
}
