package com.novasa.languagecenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.novasa.languagecenter.R;

/**
 * Created by martinwagner on 05/01/2018.
 */

public class LanguageCenterButton extends AppCompatButton {

    private LanguageCenterDelegate mDelegate = new LanguageCenterDelegate(this);

    public LanguageCenterButton(Context context) {
        super(context);
    }

    public LanguageCenterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LanguageCenterButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LanguageCenterButton);

            // language center
            final String transKey = a.getString(R.styleable.LanguageCenterButton_transKey);
            final String transComment = a.getString(R.styleable.LanguageCenterButton_transComment);
            setTranslation(transKey, getText().toString(), transComment);

            a.recycle();
        }
    }

    public void setTranslation(String key) {
        mDelegate.setTranslation(key);
    }

    public void setTranslation(String key, String fallback) {
        mDelegate.setTranslation(key, fallback);
    }

    public void setTranslation(String key, String fallback, String comment) {
        mDelegate.setTranslation(key, fallback, comment);
    }

    public void updateTranslation() {
        mDelegate.updateTranslation();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDelegate.onAttach();
    }

    @Override
    protected void onDetachedFromWindow() {
        mDelegate.onDetach();
        super.onDetachedFromWindow();
    }
}
