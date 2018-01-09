package com.novasa.languagecenter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.novasa.languagecenter.interfaces.LanguageCenterCallback;

import timber.log.Timber;

/**
 * Created by andersp on 04/10/16.
 *
 * Init framework with:
 *
 *     LanguageCenter.with(Context context);
 *
 *     These string resources needed by the framework to function:
 *
 *     language_center_user_name
 *     language_center_password
 *     language_center_base_url
 *
 * Or:
 *
 *     LanguageCenter.with(Context context, String appName, String baseUrl, String userName, String password)
 *
 *     No string resources are needed when all parameters are supplied at init.
 *
 */
public class LanguageCenter {

    private static LanguageCenter singleton;

    private LCService lcServiceUtil;
    private LCTranslationsDB lcTranslationsDB;

    private static boolean sDebugging = false;

    private LanguageCenter(Context context, String baseUrl, String userName, String password){
        if (lcServiceUtil == null) {
            lcServiceUtil = new LCService(baseUrl, userName, password);
        }

        if (lcTranslationsDB == null) {
            lcTranslationsDB = new LCTranslationsDB(context);
        }

        //Getting all translations already in the language center by default
        lcServiceUtil.downloadTranslations(new LanguageCenterCallback.OnDownloadTranslationsCallback() {
            @Override
            public void onTranslationsPersisted() {
                long endTime = System.currentTimeMillis();
                Timber.d("TRANSLATIONS PERSISTED CALLLBACK SUCCCES!!! time: %d", (endTime - LCTranslationsDB.startTine));
            }

            @Override
            public void onTranslationsPersistedError() {
                Timber.d("TRANSLATIONS PERSISTED CALLLBACK ERROR!!!");
            }

            @Override
            public void onTranslationsDownloaded() {
                Timber.d("TRANSLATIONS DOWNLOADED CALLLBACK SUCCCES!!!");
            }

            @Override
            public void onTranslationsDownloadError() {
                Timber.d("TRANSLATIONS DOWNLOAD CALLLBACK ERROR!!!");
            }
        });
    }

    /**
     *
     * Initialize the LanguageCenter singleton instance with this method.
     *
     * @param context we need the context for the shared prefs database util
     * @return the static singleton instance of LanguageCenter
     */
    public static LanguageCenter with(@NonNull Context context) {
        if (singleton == null) {

            String bUrl;
            String uName;
            String pWord;

            try {
                Resources res = context.getApplicationContext().getResources();
                bUrl = res.getString(res.getIdentifier("language_center_base_url", "string", context.getPackageName()));
                uName = res.getString(res.getIdentifier("language_center_username", "string", context.getPackageName()));
                pWord = res.getString(res.getIdentifier("language_center_password", "string", context.getPackageName()));
            } catch (Resources.NotFoundException e) {
                throw new Resources.NotFoundException(
                        "\nPlease supply all required language string resouces:\n" +
                        "<string name='language_center_app_name'>\n" +
                        "<string name='language_center_username'>\n" +
                        "<string name='language_center_password'>\n" +
                        "<string name='language_center_base_url'>");
            }

            synchronized (LanguageCenter.class) {
                singleton = new LanguageCenter(context, bUrl, uName, pWord);
            }
        }
        return singleton;
    }

    /**
     *
     * Initialize the LanguageCenter singleton instance with this method.
     *
     * @param context the application context
     * @param baseUrl the base url for the LanguageCenter server
     * @param userName user name for LC server auth
     * @param password password for LC server auth
     * @return
     */
    public static LanguageCenter with(@NonNull Context context, @NonNull String baseUrl, @NonNull String userName, @NonNull String password) {
        if (singleton == null) {
            synchronized (LanguageCenter.class) {
                if (singleton == null) {
                    singleton = new LanguageCenter(context, baseUrl, userName, password);
                }
            }
        }
        return singleton;
    }

    public LanguageCenter setDebugMode(final boolean debugging){
        if (null == singleton){
            throw new IllegalArgumentException("LanguageCenter hasn't been initialized yet. Please call LanguageCenter.with() before setting the debugging flag.");
        }
        sDebugging = debugging;
        return singleton;
    }

    boolean isDebugMode(){
        if (null == singleton){
            throw new IllegalArgumentException("LanguageCenter hasn't been initialized yet. Please call LanguageCenter.with() before setting the debugging flag.");
        }
        return sDebugging;
    }

    public String getTranslation(int resTrans, int resOrg){
        return getTranslationDB().getTranslation(resTrans, resOrg);
    }

    public String getTranslation(String resTrans, String resOrg){
        return getTranslationDB().getTranslation(resTrans, resOrg);
    }

    /**
     *
     * Access the LanguageCenter methods with this call after initializing the singleton instance.
     *
     * @return the language center service util
     */
    protected LCService getService(){
        if (singleton == null){
            throw new IllegalStateException("LanguageCenter hasn't been initialized yet. Please call LanguageCenter.with() before using the service.");
        }
        return lcServiceUtil;
    }

    /**
     *
     * Access the LanguageCenter methods with this call after initializing the singleton instance.
     *
     * @return the translations database util
     */
    LCTranslationsDB getTranslationDB(){
        if (singleton == null){
            throw new IllegalStateException("LanguageCenter hasn't been initialized yet. Please call LanguageCenter.with() before using the service.");
        }
        return lcTranslationsDB;
    }

    public static LanguageCenter getInstance() {
        if (null == singleton) {
            throw new IllegalArgumentException("LanguageCenter hasn't been initialized yet. Please call LanguageCenter.with() before setting the debugging flag.");
        }
        return singleton;
    }

}
