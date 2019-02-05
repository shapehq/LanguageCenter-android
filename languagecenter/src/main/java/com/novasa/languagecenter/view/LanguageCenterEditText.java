package com.novasa.languagecenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.novasa.languagecenter.R;

/**
 * Created by martinwagner on 20/12/2017.
 */

public class LanguageCenterEditText extends AppCompatEditText {

    private LanguageCenterDelegate mDelegate = new LanguageCenterDelegate(this);

    public LanguageCenterEditText(Context context) {
        super(context);
    }

    public LanguageCenterEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LanguageCenterEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LanguageCenterEditText);

            final String transKey = a.getString(R.styleable.LanguageCenterEditText_transKey);
            final String transComment = a.getString(R.styleable.LanguageCenterEditText_transComment);
            final String hintTransKey = a.getString(R.styleable.LanguageCenterEditText_hintTransKey);
            final String hintTransComment = a.getString(R.styleable.LanguageCenterEditText_hintTransComment);

            final String fallback = getText() != null ? getText().toString() : "";
            final String hintFallback = getHint() != null ? getHint().toString() : "";
            setTranslation(transKey, fallback, transComment);
            setHintTranslation(hintTransKey, hintFallback, hintTransComment);

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

    public void setHintTranslation(String key) {
        mDelegate.setHintTranslation(key);
    }

    public void setHintTranslation(String key, String fallback) {
        mDelegate.setHintTranslation(key, fallback);
    }

    public void setHintTranslation(String key, String fallback, String comment) {
        mDelegate.setHintTranslation(key, fallback, comment);
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
