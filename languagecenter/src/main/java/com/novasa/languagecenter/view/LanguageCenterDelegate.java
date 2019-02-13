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

        final CharSequence text = textView.getText();
        mFallback = text != null ? text.toString() : "";

        if (textView instanceof EditText) {
            final CharSequence hint = textView.getHint();
            mHintFallback = hint != null ? hint.toString() : "";
        }
    }

    void setTranslation(String key) {
        setTranslationWithComment(key, mFallback, "");
    }

    void setTranslation(String key, String fallback) {
        setTranslationWithComment(key, fallback, "");
    }

    void setTranslationWithComment(String key, String comment) {
        setTranslationWithComment(key, mFallback, comment);
    }

    void setTranslationWithComment(String key, String fallback, String comment) {
        if (!TextUtils.equals(key, mKey) || !TextUtils.equals(fallback, mFallback) || !TextUtils.equals(comment, mComment)) {
            mKey = key;
            mFallback = fallback;
            mComment = comment;
            updateTranslation();
        }
    }

    void setHintTranslation(String key) {
        setHintTranslationWithComment(key, mHintFallback, "");
    }

    void setHintTranslation(String key, String fallback) {
        setHintTranslationWithComment(key, fallback, "");
    }

    void setHintTranslationWithComment(String key, String comment) {
        setHintTranslationWithComment(key, mHintFallback, comment);
    }

    void setHintTranslationWithComment(String key, String fallback, String comment) {
        if (!TextUtils.equals(key, mHintKey) || !TextUtils.equals(fallback, mHintFallback) || !TextUtils.equals(comment, mHintComment)) {
            mHintKey = key;
            mHintFallback = fallback;
            mHintComment = comment;
            updateHintTranslation();
        }
    }

    void updateTranslation() {
        if (!mTextView.isInEditMode() && !TextUtils.isEmpty(mKey)) {
            final String text = LanguageCenter.getInstance().getTranslation(mKey, mFallback, mComment);
            mTextView.setText(text);
        }
    }

    void updateHintTranslation() {
        if (!mTextView.isInEditMode() && mTextView instanceof EditText && !TextUtils.isEmpty(mHintKey)) {
            final String hint = LanguageCenter.getInstance().getTranslation(mHintKey, mHintFallback, mHintComment);
            mTextView.setHint(hint);
        }
    }

    void onAttach() {
        if (!mTextView.isInEditMode()) {
            LanguageCenter.getInstance().registerPersistentCallback(this);
        }
    }

    void onDetach() {
        if (!mTextView.isInEditMode()) {
            LanguageCenter.getInstance().unregisterPersistentCallback(this);
        }
    }

    @Override
    public void onLanguageCenterReady(@NonNull LanguageCenter languageCenter, @NonNull String language, @NonNull LanguageCenter.Status status) {
        if (status == LanguageCenter.Status.READY) {
            updateTranslation();
            updateHintTranslation();
        }
    }
}
