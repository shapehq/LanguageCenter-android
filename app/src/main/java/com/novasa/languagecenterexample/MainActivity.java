package com.novasa.languagecenterexample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.novasa.languagecenter.LanguageCenter;
import com.novasa.languagecenter.interfaces.OnLanguageCenterReadyCallback;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnLanguageCenterReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_english).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanguage("en");
            }
        });

        findViewById(R.id.button_danish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanguage("da");
            }
        });

        findViewById(R.id.button_chinese).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanguage("zh");
            }
        });

        findViewById(R.id.button_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LanguageCenter.getInstance().setDeviceLanguage(MainActivity.this);
            }
        });

        findViewById(R.id.button_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getApplicationContext();
                final Intent intent = new Intent(context, SecondActivity.class);
                context.startActivity(intent);
            }
        });

        LanguageCenter.getInstance().registerOneShotCallback(this);
    }

    private void setLanguage(String language) {
        LanguageCenter.getInstance().setLanguage(language, this);
    }

    @Override
    public void onLanguageCenterReady(@NonNull String language, boolean success) {
        // Languagecenter is updated. Show texts

        Log.d(LanguageCenter.LOG_TAG, String.format("Update complete. Language: %s, success: %b", language, success));

        TextView textView = findViewById(R.id.text_with_format);
        textView.setText(LanguageCenter.getInstance().getTranslationWithStringFormat(R.string.languagecenter_test_with_format_key, R.string.languagecenter_test_with_format, DateFormat.getTimeInstance().format(new Date())));
    }
}
