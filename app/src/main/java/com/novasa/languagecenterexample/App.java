package com.novasa.languagecenterexample;

import android.app.Application;

import com.novasa.languagecenter.LanguageCenter;

import timber.log.Timber;

/**
 * Created by martinwagner on 19/01/2018.
 */

public class App extends Application{

    private static App instance = null;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // LanguageCenter init
        LanguageCenter.with(this, "https://language.novasa.com/test/api/v1/",
                "test", "test");//.setDebugMode(true);
    }

    public static App getRunningApp() {
        return instance;
    }

}
