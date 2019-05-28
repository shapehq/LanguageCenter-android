package com.novasa.languagecenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.novasa.languagecenter.R;

/**
 * Created by martinwagner on 20/12/2017.
 */
@SuppressWarnings("unused")
public class LanguageCenterEditText extends AppCompatEditText {

    private final LanguageCenterDelegate mDelegate = new LanguageCenterDelegate(this);

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

            final String key = a.getString(R.styleable.LanguageCenterEditText_transKey);
            final String comment = a.getString(R.styleable.LanguageCenterEditText_transComment);
            final String hintKey = a.getString(R.styleable.LanguageCenterEditText_hintTransKey);
            final String hintComment = a.getString(R.styleable.LanguageCenterEditText_hintTransComment);

            setTranslationWithComment(key, comment);
            setHintTranslationWithComment(hintKey, hintComment);

            a.recycle();
        }
    }

    public void setTranslation(String key) {
        mDelegate.setTranslation(key);
    }

    public void setTranslation(String key, String fallback) {
        mDelegate.setTranslation(key, fallback);
    }

    public void setTranslationWithComment(String key, String comment) {
        mDelegate.setTranslationWithComment(key, comment);
    }

    public void setTranslationWithComment(String key, String fallback, String comment) {
        mDelegate.setTranslationWithComment(key, fallback, comment);
    }

    public void setHintTranslation(String key) {
        mDelegate.setHintTranslation(key);
    }

    public void setHintTranslation(String key, String fallback) {
        mDelegate.setHintTranslation(key, fallback);
    }

    public void setHintTranslationWithComment(String key, String comment) {
        mDelegate.setHintTranslationWithComment(key, comment);
    }

    public void setHintTranslationWithComment(String key, String fallback, String comment) {
        mDelegate.setHintTranslationWithComment(key, fallback, comment);
    }

    public void updateTranslation() {
        mDelegate.updateTranslation();
        mDelegate.updateHintTranslation();
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
