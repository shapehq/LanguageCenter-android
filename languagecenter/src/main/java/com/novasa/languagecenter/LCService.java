package com.novasa.languagecenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.novasa.languagecenter.interfaces.LanguageCenterCallback;
import com.novasa.languagecenter.model.Language;
import com.novasa.languagecenter.model.Translation;
import com.novasa.languagecenter.retrofit.LCRestClient;
import com.novasa.languagecenter.util.LCValues;
import com.novasa.languagecenter.util.LCUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


/**
 * Created by andersp on 03/10/16.
 */
public class LCService {

    private LCRestClient client;

    LCService(final String url, final String username, final String password){
        client = new LCRestClient(url, username, password);
    }

    public void downloadTranslations(final LanguageCenterCallback callback){

        Call<List<Language>> call = client.getApiService().getLanguages(LCValues.timestamp);

        call.enqueue(new Callback<List<Language>>() {
            @Override
            public void onResponse(@NonNull Call<List<Language>> call, @NonNull Response<List<Language>> response) {

                Language fallbackLanguage = null;
                Language preferredLanguage = null;
                Language chosenLanguage = null;

                List<Language> list = response.body();

                // we check for the best language (preferred or fallback)
                for (int i = 0; i < list.size(); i++) {

                    Language l = list.get(i);

                    if (l.getCodename().equalsIgnoreCase(LCUtil.getPreferredLanguageCode())) {

                        preferredLanguage = l;

                    }

                    if (l.getIsFallback()) {

                        fallbackLanguage = l;

                    }

                }

                if (preferredLanguage != null) {

                    chosenLanguage = preferredLanguage;

                } else if (fallbackLanguage != null) {

                    chosenLanguage = fallbackLanguage;

                }

                final long persistedTimeStamp = LanguageCenter.getInstance().getTranslationDB().getLanguagePersistedTime(chosenLanguage.getCodename());
                final long currentTimeStamp = chosenLanguage.getTimestamp();

                // we check if we need to update the translation DB.
                if (persistedTimeStamp < currentTimeStamp) {
                    if (LanguageCenter.getInstance().isDebugMode()) {
                        Timber.d("Language Center is updating language: %s (%s) (timestamp: %d < %d)", chosenLanguage.getCodename(), chosenLanguage.getName(), persistedTimeStamp, currentTimeStamp);
                    }
                    getTranslations(chosenLanguage, callback);
                } else {
                    if (LanguageCenter.getInstance().isDebugMode()) {
                        Timber.d("Language Center language is up-to-date: %s (%s)", chosenLanguage.getCodename(), chosenLanguage.getName());
                    }
                    callback.onLanguageCenterUpdated(true);
                }

            }

            @Override
            public void onFailure(Call<List<Language>> call, Throwable t) {

                Timber.d("LC getLanguages FAILURE!");

                callback.onLanguageCenterUpdated(false);

            }
        });
    }

    void createTranslation(String category, final String transKey, String origText, String comment) {

        String key = transKey;
        if (TextUtils.isEmpty(category)) {
            String[] categorySplit = key.split("\\.");
            category = categorySplit[0];

            key = "";

            for (int i = 1; i < categorySplit.length; i++){

                key = key + categorySplit[i];

            }
        }

        Call<Translation> call = client.getApiService().createTranslation(LCValues.lc_platform, category, key, origText, comment);

        call.enqueue(new Callback<Translation>() {
            @Override
            public void onResponse(@NonNull Call<Translation> call, @NonNull Response<Translation> response) {

                Translation t = response.body();

                if (t != null) {
                    LanguageCenter.getInstance().getTranslationDB().persistTranslation(t);

                    if (LanguageCenter.getInstance().isDebugMode()) {
                        Timber.d("Language Center successfully created translation %s.", t.getKey());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Translation> call, @NonNull Throwable t) {

                if (LanguageCenter.getInstance().isDebugMode()) {
                    Timber.e(t, "Language Center failed to create translation %s.", transKey);
                }
            }
        });
    }

    private void getTranslations(final Language language, final LanguageCenterCallback callback) {

        Call<List<Translation>> call = client.getApiService().getTranslations(LCValues.lc_platform, language.getCodename(), LCValues.indexing, LCValues.timestamp);
        call.enqueue(new Callback<List<Translation>>() {

            @Override
            public void onResponse(Call<List<Translation>> call, Response<List<Translation>> response) {

                final long lastPersistTime = LanguageCenter.getInstance().getTranslationDB().getLanguagePersistedTime(language.getCodename());

                List<Translation> list = response.body();
                List<Translation> listOfTranslationToPersist = new ArrayList<>();

//                // we add all updated or new translation to the list
//                for (int i = 0; i < list.size(); i++) {
//                    Translation t = list.get(i);
//
//                    if (t.getTimestamp() > lastPersistTime) {
//                        listOfTranslationToPersist.add(t);
//                    }
//
//                }

                listOfTranslationToPersist.addAll(list);

                // if there are any new or updated translations we persist them and the timestamp
                if (listOfTranslationToPersist.size() > 0) {

                    LanguageCenter.getInstance().getTranslationDB().persistTranslationsList(listOfTranslationToPersist, callback);

                } else {
                    if (LanguageCenter.getInstance().isDebugMode()) {
                        Timber.d("Language Center had no translations to persist.");
                    }
                    callback.onLanguageCenterUpdated(true);

                }

                if (language.getTimestamp() > LanguageCenter.getInstance().getTranslationDB().getLanguagePersistedTime(language.getCodename())){

                    LanguageCenter.getInstance().getTranslationDB().setLanguagePersistTime(language);

                }

            }

            @Override
            public void onFailure(Call<List<Translation>> call, Throwable t) {

                Timber.d("LC getTranslations FAILURE!");

                callback.onLanguageCenterUpdated(false);

            }
        });

    }

}
