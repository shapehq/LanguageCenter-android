package com.novasa.languagecenter.view;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.novasa.languagecenter.LanguageCenter;
import com.novasa.languagecenter.interfaces.OnLanguageCenterReadyCallback;

class LanguageCenterDelegate implements OnLanguageCenterReadyCallback {

    private final TextView mTextView;

    private String mKey;
    private String mFallback;
    private String mComment;

    private String mHintKey;
    private String mHintFallback;
    private String mHintComment;

    LanguageCenterDelegate(@NonNull TextView textView) {
        mTextView = textView;
        mFallback = textView.getText().toString();
    }

    void setTranslation(String key) {
        setTranslation(key, mFallback, "");
    }

    void setTranslation(String key, String fallback) {
        setTranslation(key, fallback, "");
    }

    void setTranslation(String key, String fallback, String comment) {
        if (!TextUtils.equals(key, mKey) || !TextUtils.equals(fallback, mFallback) || !TextUtils.equals(comment, mComment)) {
            mKey = key;
            mFallback = fallback;
            mComment = comment;
            updateTranslation();
        }
    }

    void setHintTranslation(String key) {
        setHintTranslation(key, mHintFallback, "");
    }

    void setHintTranslation(String key, String fallback) {
        setHintTranslation(key, fallback, "");
    }

    void setHintTranslation(String key, String fallback, String comment) {
        if (!TextUtils.equals(key, mHintKey) || !TextUtils.equals(fallback, mHintFallback) || !TextUtils.equals(comment, mHintComment)) {
            mHintKey = key;
            mHintFallback = fallback;
            mHintComment = comment;
            updateTranslation();
        }
    }

    void updateTranslation() {
        if (!mTextView.isInEditMode()) {
            final String text = LanguageCenter.getInstance().getTranslation(mKey, mFallback, mComment);
            mTextView.setText(text);

            if (mTextView instanceof EditText) {
                final String hint = LanguageCenter.getInstance().getTranslation(mHintKey, mHintFallback, mHintComment);
                mTextView.setHint(hint);
            }
        }
    }

    void onAttach() {
        LanguageCenter.getInstance().registerPersistentCallback(this);
    }

    void onDetach() {
        LanguageCenter.getInstance().unregisterPersistentCallback(this);
    }

    @Override
    public void onLanguageCenterReady(String language, boolean success) {
        updateTranslation();
    }
}
