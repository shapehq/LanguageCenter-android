package com.novasa.languagecenter;

import android.text.TextUtils;

import com.novasa.languagecenter.interfaces.LanguageCenterCallback;
import com.novasa.languagecenter.model.Language;
import com.novasa.languagecenter.model.Translation;
import com.novasa.languagecenter.retrofit.LCRestClient;
import com.novasa.languagecenter.util.LCValues;
import com.novasa.languagecenter.util.LocaleUtil;

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

    public void downloadTranslations(final LanguageCenterCallback.OnDownloadTranslationsCallback callback){

        getLanguages(callback);

    }

    private void getLanguages(final LanguageCenterCallback.OnDownloadTranslationsCallback callback) {

        Call<List<Language>> call = client.getApiService().getLanguages(LCValues.timestamp);

        call.enqueue(new Callback<List<Language>>() {
            @Override
            public void onResponse(Call<List<Language>> call, Response<List<Language>> response) {

                Language fallbackLanguage = null;
                Language preferredLanguage = null;
                Language chosenLanguage = null;

                List<Language> list = response.body();

                // we check for the best language (preferred or fallback)
                for (int i = 0; i < list.size(); i++) {

                    Language l = list.get(i);

                    if (l.getCodename().equalsIgnoreCase(LocaleUtil.getPreferredLanguageCode())) {

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

                // we check if we need to update the translation DB.
//                if (LanguageCenter.getInstance().getTranslationDB().getLanguagePersistedTime(chosenLanguage.getCodename()) < chosenLanguage.getTimestamp()) {

                    Timber.d("LANGUAGE: HAS UPDATED. WE GET THE TRANSLATIONS");

                    getTranslations(chosenLanguage, callback);

//                } else {
//
//                    Timber.d("LANGUAGE: HASN'T UPDATED. NO NEED TO DOWNLOAD TRANSLATIONS.");
//                    callback.onTranslationsDownloaded();
//
//                }

            }

            @Override
            public void onFailure(Call<List<Language>> call, Throwable t) {

                Timber.d("LC getLanguages FAILURE!");

                callback.onTranslationsDownloadError();

            }
        });

    }

    void createTranslation(String category, String transKey, String origText, String comment) {

        if (TextUtils.isEmpty(category)) {
            String[] categorySplit = transKey.split("\\.");
            category = categorySplit[0];

            transKey = "";

            for (int i = 1; i < categorySplit.length; i++){

                transKey = transKey + categorySplit[i];

            }
        }

        Call<Translation> call = client.getApiService().createTranslation(LCValues.lc_platform, category, transKey, origText, comment);

        call.enqueue(new Callback<Translation>() {
            @Override
            public void onResponse(Call<Translation> call, Response<Translation> response) {

                Translation t = response.body();

                if (t != null){

                    LanguageCenter.getInstance().getTranslationDB().persistTranslation(t);

                }

                Timber.d("LC createTranslation SUCCES!");

            }

            @Override
            public void onFailure(Call<Translation> call, Throwable t) {

                Timber.d("LC createTranslation FAILURE!");

            }
        });

    }

    private void getTranslations(final Language language, final LanguageCenterCallback.OnDownloadTranslationsCallback callback) {

        Call<List<Translation>> call = client.getApiService().getTranslations(LCValues.lc_platform, language.getCodename(), LCValues.indexing, LCValues.timestamp);
        call.enqueue(new Callback<List<Translation>>() {

            @Override
            public void onResponse(Call<List<Translation>> call, Response<List<Translation>> response) {

//                final long lastPersistTime = LanguageCenter.getInstance().getTranslationDB().getLanguagePersistedTime(language.getCodename());

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

                    Timber.d("No translations to persist.");
                    callback.onTranslationsPersisted();

                }

                if (language.getTimestamp() > LanguageCenter.getInstance().getTranslationDB().getLanguagePersistedTime(language.getCodename())){

                    LanguageCenter.getInstance().getTranslationDB().setLanguagePersistTime(language);

                }

            }

            @Override
            public void onFailure(Call<List<Translation>> call, Throwable t) {

                Timber.d("LC getTranslations FAILURE!");

                callback.onTranslationsDownloadError();

            }
        });

    }

}
