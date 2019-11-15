package com.novasa.languagecenterexample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.novasa.languagecenter.LanguageCenter;
import com.novasa.languagecenter.interfaces.OnLanguageCenterReadyCallback;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnLanguageCenterReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_init).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LanguageCenter.getInstance().initialize(MainActivity.this, "en");
            }
        });

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
                final Context context = MainActivity.this;
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
    public void onLanguageCenterReady(@NonNull LanguageCenter languageCenter, @NonNull String language, @NonNull LanguageCenter.Status status) {
        // Languagecenter is updated. Show texts

        Log.d("LanguageCenter Test", String.format("Update complete. Language: %s, status: %s", language, status));

        TextView textView = findViewById(R.id.text_with_format);
        textView.setText(LanguageCenter.getInstance().getTranslationWithStringFormat(R.string.languagecenter_test_with_format_key, R.string.languagecenter_test_with_format, DateFormat.getTimeInstance().format(new Date())));
    }
}
