package com.novasa.languagecenterexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.novasa.languagecenter.LanguageCenter;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.text_with_format);
        textView.setText(LanguageCenter.getInstance().getTranslationWithStringFormat(R.string.languagecenter_test_with_format_key, R.string.languagecenter_test_with_format, System.currentTimeMillis()));

    }

}
