package com.novasa.languagecenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.util.AttributeSet;

import com.novasa.languagecenter.R;

/**
 * Created by martinwagner on 20/12/2017.
 */

@SuppressWarnings("unused")
public class LanguageCenterTextView extends AppCompatTextView {

    private final LanguageCenterDelegate mDelegate = new LanguageCenterDelegate(this);

    public LanguageCenterTextView(Context context) {
        super(context);
    }

    public LanguageCenterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LanguageCenterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private boolean mHtml;

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LanguageCenterTextView);

            mHtml = a.getBoolean(R.styleable.LanguageCenterTextView_html, false);

            final String key = a.getString(R.styleable.LanguageCenterTextView_transKey);
            final String comment = a.getString(R.styleable.LanguageCenterTextView_transComment);

            setTranslationWithComment(key, comment);

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

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (mHtml) {
            super.setText(Html.fromHtml(text.toString()), type);
        } else {
            super.setText(text, type);
        }
    }
}
