package com.novasa.languagecenter.interfaces;

import android.support.annotation.NonNull;

import com.novasa.languagecenter.LanguageCenter;

public interface OnLanguageCenterReadyCallback {
    void onLanguageCenterReady(@NonNull LanguageCenter languageCenter, @NonNull String language, @NonNull LanguageCenter.Status status);
}
