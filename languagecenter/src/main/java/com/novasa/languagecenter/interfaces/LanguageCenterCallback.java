package com.novasa.languagecenter.interfaces;

/**
 * Created by admin on 05/10/2016.
 */

public abstract class LanguageCenterCallback {

    public interface OnDownloadTranslationsCallback {

        void onTranslationsPersisted();

        void onTranslationsPersistedError();

        void onTranslationsDownloaded();

        void onTranslationsDownloadError();

    }

    public OnDownloadTranslationsCallback callback;

}
