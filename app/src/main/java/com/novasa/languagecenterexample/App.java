package com.novasa.languagecenterexample;

import android.app.Application;
import android.util.Log;

import com.novasa.languagecenter.LanguageCenter;

/**
 * Created by martinwagner on 19/01/2018.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // LanguageCenter init
        LanguageCenter.with(this, "https://language.novasa.com/test/api/v1/", "test", "test", false)
                .setLogLevel(Log.VERBOSE);
        //.setDebugMode(true);
    }
}
