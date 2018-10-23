package com.novasa.languagecenterexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.novasa.languagecenter.LanguageCenter;
import com.novasa.languagecenter.interfaces.LanguageCenterCallback;
import com.novasa.languagecenter.model.Language;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LanguageCenterCallback{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LanguageCenter.getInstance().registerUpdateListener(this);
    }

    @Override
    public void onLanguageCenterUpdated(boolean success) {
        //Languagecenter is updated. Show texts

        Timber.e("Update was: " + success);

        TextView textView = findViewById(R.id.text_with_format);
        textView.setText(LanguageCenter.getInstance().getTranslationWithStringFormat(R.string.languagecenter_test_with_format_key, R.string.languagecenter_test_with_format, System.currentTimeMillis()));


        //This text is set when the view is created. Therefore it can only be updated to the new language when we get the callback
        TextView textXml = findViewById(R.id.text_with_in_xml);
        textXml.setText(LanguageCenter.getInstance().getTranslation(R.string.languagecenter_test_basic_key, R.string.languagecenter_test_basic));
    }
}
