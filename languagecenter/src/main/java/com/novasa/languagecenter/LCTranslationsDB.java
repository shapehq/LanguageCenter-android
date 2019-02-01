package com.novasa.languagecenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.novasa.languagecenter.model.Language;
import com.novasa.languagecenter.model.Translation;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by andersp on 28/09/16.
 * <p>
 * Sharedprefs based DB for Translations and Languages.
 */

final class LCTranslationsDB {

    private static final String PREFS_LANGUAGES_SPACE = "prefs_languages_space";
    private static final String PREFS_TRANSLATIONS_SPACE = "prefs_translations_space";
    private static final String PREFS_OVERRIDDEN_LANGUAGE = "prefs_overridden_language";

    private SharedPreferences mSPLanguages;
    private SharedPreferences mSPTranslations;

    LCTranslationsDB(Context context) {
        mSPLanguages = context.getSharedPreferences(PREFS_LANGUAGES_SPACE, MODE_PRIVATE);
        mSPTranslations = context.getSharedPreferences(PREFS_TRANSLATIONS_SPACE, MODE_PRIVATE);
    }

    /**
     * Check when the language was last persisted.
     *
     * @param languageCode the language code eg. "da", "no" etc.
     * @return the time of last database persist time
     */
    long getLanguagePersistedTime(String languageCode) {
        return mSPLanguages.getLong(languageCode, 0);
    }

    /**
     * Reset the language persisted time
     *
     * @param language the language code eg. "da", "no" etc.
     */
    void resetLanguagePersistedTime(Language language) {
        Logger.d("Resetting timestamp for language: %s", language);
        mSPLanguages.edit()
                .putLong(language.getCodename(), 0)
                .apply();
    }

    /**
     * When all translations of a language are persisted we save the time the language was last updated.
     *
     * @param language the language we want to save the updated time of
     */
    void setLanguagePersistTime(Language language) {
        Logger.d("Persisting timestamp for language: %s", language);
        mSPLanguages.edit()
                .putLong(language.getCodename(), language.getTimestamp())
                .apply();
    }

    void setOverriddenLanguage(final String language) {
        mSPLanguages.edit()
                .putString(PREFS_OVERRIDDEN_LANGUAGE, language)
                .apply();
    }

    boolean isLanguageOverridden() {
        return getOverriddenLanguage() != null;
    }

    void clearOverriddenLanguage() {
        mSPLanguages.edit()
                .remove(PREFS_OVERRIDDEN_LANGUAGE)
                .apply();
    }

    @Nullable
    String getOverriddenLanguage() {
        return mSPLanguages.getString(PREFS_OVERRIDDEN_LANGUAGE, null);
    }

    /**
     * Get a single translation and post a new string for creation to the language center api if missing.
     *
     * @param key      translation key for language center api
     * @param fallback fallback text
     * @return translation string or fallback text
     */
    String getTranslation(String key, String fallback, String comment) {

        String translation;

        if (TextUtils.isEmpty(key)) {

            translation = fallback;

            if (LanguageCenter.getInstance().isDebugMode()) {
                // In debug mode we add the translation key missing identifier
                translation = "(F)" + translation;
            }

            return translation;
        }

        translation = mSPTranslations.getString(key.toLowerCase(), null);

        if (TextUtils.isEmpty(translation)) {
            // If translation doesn't exist we show fallback text and create a new translation
            translation = fallback;

            // In debug mode we add the fallback identifier
            if (LanguageCenter.getInstance().isDebugMode()) {
                translation = "(F)" + translation;
            }

            LanguageCenter.getInstance().getService().createTranslation(key, fallback, comment);

        } else {
            if (LanguageCenter.getInstance().isDebugMode()) {
                translation = "(T)" + translation;
            }
        }

        return translation;
    }

    /**
     * Persist a list of translations.
     *
     * @param translations list of translations to persist
     */
    @SuppressLint("ApplySharedPref")
    void persistTranslationsList(final List<Translation> translations) {

        final long tRef = SystemClock.elapsedRealtime();

        Logger.d("Persisting %d translations...", translations.size());

        final SharedPreferences.Editor editor = mSPTranslations.edit();

        for (int i = 0, c = translations.size(); i < c; i++) {
            final Translation t = translations.get(i);
            editor.putString(t.getKey(), t.getValue());
        }

        editor.commit();

        Logger.d("Persist complete. Time spent: %d", SystemClock.elapsedRealtime() - tRef);
    }

    /**
     * Persist a single translation.
     *
     * @param translation translation to persist
     */
    void persistTranslation(final Translation translation) {

        Logger.d("Language persisting translation: %s", translation);

        mSPTranslations.edit()
                .putString(translation.getKey(), translation.getValue())
                .apply();
    }
}
