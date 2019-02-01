package com.novasa.languagecenter;

import android.support.annotation.NonNull;

import com.novasa.languagecenter.interfaces.OnLanguageCenterReadyCallback;
import com.novasa.languagecenter.model.Language;
import com.novasa.languagecenter.model.Translation;
import com.novasa.languagecenter.service.LCRestClient;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by andersp on 03/10/16.
 */
final class LCService {

    private abstract class APICallback<TData> implements Callback<TData> {
        @Override
        public final void onResponse(Call<TData> call, Response<TData> response) {
            final TData result = response.body();
            if (response.isSuccessful() && result != null) {
                onSuccess(result);

            } else {
                final ResponseBody error = response.errorBody();

                try {
                    Logger.e("API error (%d): %s", response.code(), error != null ? error.string() : "null");

                } catch (IOException e) {
                    Logger.e(e);
                }

                onFailure();
            }
        }

        @Override
        public final void onFailure(Call<TData> call, Throwable t) {
            Logger.e(t, "API Failure");
            onFailure();
        }

        abstract void onSuccess(@NonNull TData data);
        void onFailure() { }
    }

    private final LCRestClient mClient;

    LCService(final String url, final String username, final String password){
        mClient = new LCRestClient(url, username, password);
    }

    void setDebugMode(boolean debugMode) {
        mClient.setDebugMode(debugMode);
    }

    void downloadTranslations(final String language, final OnLanguageCenterReadyCallback callback) {

        getLanguages(new APICallback<List<Language>>() {
            @Override
            void onSuccess(@NonNull List<Language> languages) {

                Language fallbackLanguage = null;
                Language preferredLanguage = null;
                Language choseLanguage = null;

                // we check for what language we use (preferred or fallback)
                for (final Language l : languages) {
                    if (l.getCodename().equalsIgnoreCase(language)) {
                        preferredLanguage = l;
                    }

                    if (l.getIsFallback()) {
                        fallbackLanguage = l;
                    }
                }

                if (preferredLanguage != null) {
                    choseLanguage = preferredLanguage;

                } else if (fallbackLanguage != null) {
                    choseLanguage = fallbackLanguage;
                }

                // Reset any other language timestamps
                for (final Language l : languages) {
                    if (choseLanguage != l) {
                        LanguageCenter.getInstance().getTranslationDB().resetLanguagePersistedTime(l);
                    }
                }

                if (choseLanguage != null) {
                    updateLanguage(choseLanguage, callback);

                } else {
                    Logger.e("No fallback language found");
                    callback.onLanguageCenterReady(language, false);

                }
            }
        });
    }

    private void getLanguages(final APICallback<List<Language>> callback) {
        final Call<List<Language>> call = mClient.getApiService().getLanguages(LCValues.PARAM_TIMESTAMP);
        call.enqueue(callback);
    }

    private void updateLanguage(final Language language, final OnLanguageCenterReadyCallback callback) {
        final long persistedTimeStamp = LanguageCenter.getInstance().getTranslationDB().getLanguagePersistedTime(language.getCodename());
        final long currentTimeStamp = language.getTimestamp();

        // we check if we need to update the translation DB.
        if (persistedTimeStamp < currentTimeStamp) {
            Logger.d("Language Center is updating language: %s (%s) (timestamp: %d < %d)", language.getCodename(), language.getName(), persistedTimeStamp, currentTimeStamp);
            getTranslations(language, callback);

        } else {
            Logger.d("Language Center language is up-to-date: %s (%s)", language.getCodename(), language.getName());
            callback.onLanguageCenterReady(language.getCodename(), true);
        }
    }

    private void getTranslations(final Language language, final OnLanguageCenterReadyCallback callback) {

        final String code = language.getCodename();

        final Call<List<Translation>> call = mClient.getApiService().getTranslations(LCValues.PARAM_PLATFORM, code, LCValues.PARAM_INDEXING, LCValues.PARAM_TIMESTAMP);
        call.enqueue(new APICallback<List<Translation>>() {

            @Override
            void onSuccess(@NonNull List<Translation> translations) {

                // if there are any new or updated translations we persist them and the timestamp
                if (!translations.isEmpty()) {
                    LanguageCenter.getInstance().getTranslationDB().persistTranslationsList(translations);

                } else {
                    Logger.d("Language Center had no translations to persist.");
                }

                callback.onLanguageCenterReady(code, true);

                if (language.getTimestamp() > LanguageCenter.getInstance().getTranslationDB().getLanguagePersistedTime(code)){
                    LanguageCenter.getInstance().getTranslationDB().setLanguagePersistTime(language);
                }
            }

            @Override
            void onFailure() {
                Logger.e("Failed to get translations for language: %s", language);
                callback.onLanguageCenterReady(code, false);
            }
        });
    }

    void createTranslation(final String key, final String fallback, final String comment) {

        final String category;
        final String actualKey;
        final String[] split = key.split("\\.", 2);
        if (split.length >= 2) {
            category = split[0];
            actualKey = split[1];

        } else {
            category = "";
            actualKey = key;
        }

        final Call<Translation> call = mClient.getApiService().createTranslation(LCValues.PARAM_PLATFORM, category, actualKey, fallback, comment);
        call.enqueue(new Callback<Translation>() {
            @Override
            public void onResponse(@NonNull Call<Translation> call, @NonNull Response<Translation> response) {

                final Translation t = response.body();

                if (t != null) {
                    LanguageCenter.getInstance().getTranslationDB().persistTranslation(t);
                    Logger.d("Language Center successfully created translation %s.", t.getKey());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Translation> call, @NonNull Throwable t) {
                Logger.e(t, "Language Center failed to create translation %s.", key);
            }
        });
    }
}
