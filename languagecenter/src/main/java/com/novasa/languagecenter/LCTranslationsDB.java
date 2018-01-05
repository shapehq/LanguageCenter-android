package com.novasa.languagecenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.novasa.languagecenter.interfaces.LanguageCenterCallback;
import com.novasa.languagecenter.model.Language;
import com.novasa.languagecenter.model.Translation;

import java.util.List;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by andersp on 28/09/16.
 *
 * Sharedprefs based DB for Translations & Languages.
 *
 */

public class LCTranslationsDB {

    private static final String PREFS_LANGUAGES_SPACE = "prefs_languages_space";
    private static final String PREFS_TRANSLATIONS_SPACE = "prefs_translations_space";

    private Context context;

    LCTranslationsDB (Context c) {
        this.context = c;
    }

    /**
     * check when the language was last persisted.
     *
     * @param languageCode the language code eg. "da", "no" etc.
     * @return the time of last database persist time
     */
    long getLanguagePersistedTime(String languageCode) {

        SharedPreferences prefs = context.getSharedPreferences(PREFS_LANGUAGES_SPACE, MODE_PRIVATE);
        return prefs.getLong(languageCode, 0);

    }

    /**
     * when all translations of a language is persisted we save the time the language was last updated.
     *
     * @param language  the language we want to save the updated time of
     */
    void setLanguagePersistTime(Language language) {

        Timber.d("LANGUAGE: PERSISTING TIMESTAMP: %d", language.getTimestamp());

        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_LANGUAGES_SPACE, MODE_PRIVATE).edit();
        editor.putLong(language.getCodename(), language.getTimestamp());
        editor.apply();

    }

    /**
     * get a single translation & post a new string for creation to the language center api if missing.
     *
     * @param key       translation key for language center api
     * @param orgText   fallback text
     * @return          translation string or fallback text
     */
    public String getTranslation(@StringRes int key, @StringRes int orgText) {
        return getTranslation(context.getString(key), context.getString(orgText));
    }

    /**
     * get a single translation & post a new string for creation to the language center api if missing.
     *
     * @param key       translation key for language center api
     * @param orgText   fallback text
     * @return          translation string or fallback text
     */
    public String getTranslation(String key, String orgText) {

        String translation;

        if (TextUtils.isEmpty(key)){

            translation = orgText;

            if (LanguageCenter.getInstance().isDebugMode()) {
                // in debug mode we add the translation key missing identifier
                translation = "(F)" + translation;
            }

            return translation;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_TRANSLATIONS_SPACE, MODE_PRIVATE);
        translation = prefs.getString(key.toLowerCase(), "");

        if (TextUtils.isEmpty(translation)) {
            //If translation doesn't exist we show original text and create a new translation

            translation = orgText;

            // in debug mode we add the fallback identifier
            if (LanguageCenter.getInstance().isDebugMode()) {
                translation = "(F)" + translation;
            }

            LanguageCenter.getInstance().getService().createTranslation("", key, orgText, "");
        } else {
            if (LanguageCenter.getInstance().isDebugMode()) {
                translation = "(T)" + translation;
            }
        }

        return translation;
    }

    public static long startTine;

    /**
     * persist a list of translation.
     *
     * @param translations  list of translations to persist
     */
    void persistTranslationsList(List<Translation> translations, LanguageCenterCallback.OnDownloadTranslationsCallback callback) {

        startTine = System.currentTimeMillis();

        Timber.d("LANGUAGE: PERSISTING NO. TRANSLATIONS: %d", translations.size());

        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_TRANSLATIONS_SPACE, MODE_PRIVATE).edit();

        for (int i = 0; i < translations.size(); i++) {

            Translation t = translations.get(i);
            editor.putString(t.getKey(), t.getValue());

        }

        editor.commit();

        callback.onTranslationsPersisted();

    }

    /**
     * persist a single translation.
     *
     * @param translation   translation to persist
     */
    void persistTranslation(Translation translation) {

        Timber.d("LANGUAGE PERSISTING NEW TRANSLATION: %s - %s", translation.getKey(), translation.getValue());

        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_TRANSLATIONS_SPACE, MODE_PRIVATE).edit();
        editor.putString(translation.getKey(), translation.getValue());
        editor.apply();
    }
}
