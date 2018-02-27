package com.novasa.languagecenter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.novasa.languagecenter.interfaces.LanguageCenterCallback;
import com.novasa.languagecenter.util.LCUtil;

import java.util.Locale;

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

        lcServiceUtil = new LCService(baseUrl, userName, password);
        lcTranslationsDB = new LCTranslationsDB(context);

        //Getting all translations already in the language center by default
        lcServiceUtil.downloadTranslations(new LanguageCenterCallback() {
            @Override
            public void onLanguageCenterUpdated(boolean success) {
                if(success){
                    long endTime = System.currentTimeMillis();
                    Timber.d("TRANSLATIONS UPDATED CALLLBACK SUCCCES!!! time: %d", (endTime - LCTranslationsDB.startTime));
                } else {
                    Timber.d("TRANSLATIONS NOT UPDATED CALLLBACK ERROR!!!");
                }
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
            throwIfNull();
            synchronized (LanguageCenter.class) {
                if (singleton == null) {
                    singleton = new LanguageCenter(context, baseUrl, userName, password);
                }
            }
        }
        return singleton;
    }

    public LanguageCenter setDebugMode(final boolean debugging){
        throwIfNull();
        sDebugging = debugging;
        return singleton;
    }

    boolean isDebugMode(){
        throwIfNull();
        return sDebugging;
    }

    public String getTranslation(int resTrans, int resOrg){
        return getTranslationDB().getTranslation(resTrans, resOrg);
    }

    public String getTranslation(String resTrans, String resOrg){
        return getTranslationDB().getTranslation(resTrans, resOrg);
    }

    public String getTranslationWithStringFormat(int resTrans, int resOrg, Object... args){
        String translation = getTranslationDB().getTranslation(resTrans, resOrg);
        String formattedString;
        try{
            formattedString = String.format(Locale.getDefault(), translation, args);
        } catch (Exception e){
            Timber.e("Formatting error in translation to %s. Returning to original text.", LCUtil.getPreferredLanguageCode());
            Timber.e(translation);
            formattedString = String.format(Locale.getDefault(), getTranslationDB().getStringResource(resOrg), args);
        }
        return formattedString;
    }

    private static void throwIfNull() {
        if (null == singleton) {
            throw new IllegalArgumentException("LanguageCenter hasn't been initialized yet. Please call LanguageCenter.with() before setting the debugging flag.");
        }
    }


    /**
     *
     * Access the LanguageCenter methods with this call after initializing the singleton instance.
     *
     * @return the language center service util
     */
    protected LCService getService(){
        throwIfNull();
        return lcServiceUtil;
    }

    /**
     *
     * Access the LanguageCenter methods with this call after initializing the singleton instance.
     *
     * @return the translations database util
     */
    LCTranslationsDB getTranslationDB(){
        throwIfNull();
        return lcTranslationsDB;
    }

    public static LanguageCenter getInstance() {
        throwIfNull();
        return singleton;
    }

}
