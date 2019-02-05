package com.novasa.languagecenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.novasa.languagecenter.interfaces.OnLanguageCenterReadyCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by andersp on 04/10/16.
 * <p> Init framework with:
 * <p> {@link #with(Context)}
 * <p> These string resources needed by the framework to function:
 * <ul>
 * <li>language_center_user_name
 * <li>language_center_password
 * <li>language_center_base_url
 * </ul>
 * <p> Or:
 * <p> {@link #with(Context, String, String, String)}
 * <p> No string resources are needed when all parameters are supplied at init.
 * <p> LanguageCenter will automatically detect the device language for translation as default.
 * <p> Use {@link #registerOneShotCallback(OnLanguageCenterReadyCallback)} to receive a callback once LanguageCenter has finished initializing.
 *     The callback will be automatically cleaned up once the update has finished.
 * <p> Use {@link #setLanguage(String)} or {@link #setLanguage(String, OnLanguageCenterReadyCallback)} to manually set the language
 * <p> If the language is not available in LanguageCenter, the fallback language will be used.
 * <p> Use widgets {@link com.novasa.languagecenter.view.LanguageCenterTextView}, {@link com.novasa.languagecenter.view.LanguageCenterButton}, {@link com.novasa.languagecenter.view.LanguageCenterEditText}
 *     for handy xml properties <i>transKey</i> <i>transComment</i> <i>hintTransKey</i> <i>hintTransComment</i> which can be used to set the translation keys directly in xml.
 *     All LanguageCenter widgets update automatically when a language update finishes.
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "unused"})
public final class LanguageCenter implements OnLanguageCenterReadyCallback {

    public static final String LOG_TAG = "LanguageCenter";

    private static LanguageCenter sInstance;

    private boolean mDebugging = false;

    private Resources mResources;
    private LCService mService;
    private LCTranslationsDB mDatabase;

    private boolean mUpdateComplete;
    private boolean mUpdateSuccess;

    private String mLanguage;

    private LanguageCenter(Context context, String baseUrl, String userName, String password) {
        mResources = context.getResources();
        mService = new LCService(baseUrl, userName, password);
        mDatabase = new LCTranslationsDB(context);

        initialize(context);
    }

    private void initialize(Context context) {

        final IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
        final BroadcastReceiver localeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Logger.d("Locale change detected: %s", getDeviceLanguage());
                if (!mDatabase.isLanguageOverridden()) {
                    setDeviceLanguage();

                } else {
                    Logger.d("Ignoring locale change, language was overridden");
                }
            }
        };

        context.registerReceiver(localeChangeReceiver, filter);

        // Post this, so we have a chance to change the language first
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Logger.d("Initializing...");

                //Getting all translations already in the language center by default
                if (mLanguage == null) {
                    final String overriddenLanguage = mDatabase.getOverriddenLanguage();
                    if (!TextUtils.isEmpty(overriddenLanguage)) {
                        setLanguage(overriddenLanguage);
                    } else {
                        setDeviceLanguage();
                    }
                }
            }
        });
    }

    /**
     * Initialize LanguageCenter with this method.
     *
     * @param context application context for initialization
     * @return the LanguageCenter instance
     */
    public static LanguageCenter with(@NonNull Context context) {

        synchronized (LanguageCenter.class) {
            if (sInstance == null) {
                try {
                    final Resources res = context.getApplicationContext().getResources();
                    final String bUrl = res.getString(res.getIdentifier("language_center_base_url", "string", context.getPackageName()));
                    final String uName = res.getString(res.getIdentifier("language_center_username", "string", context.getPackageName()));
                    final String pWord = res.getString(res.getIdentifier("language_center_password", "string", context.getPackageName()));

                    sInstance = new LanguageCenter(context, bUrl, uName, pWord);

                } catch (Resources.NotFoundException e) {
                    throw new Resources.NotFoundException(
                            "\nPlease supply all required language string resources:\n" +
                                    "<string name='language_center_app_name'>\n" +
                                    "<string name='language_center_username'>\n" +
                                    "<string name='language_center_password'>\n" +
                                    "<string name='language_center_base_url'>");
                }
            }
        }

        return sInstance;
    }

    /**
     * Initialize LanguageCenter with this method.
     *
     * @param context  application context for initialization
     * @param baseUrl  the base url for the LanguageCenter server
     * @param userName user name for LC server auth
     * @param password password for LC server auth
     * @return the LanguageCenter instance
     */
    public static LanguageCenter with(@NonNull Context context, @NonNull String baseUrl, @NonNull String userName, @NonNull String password) {
        if (sInstance == null) {
            synchronized (LanguageCenter.class) {
                if (sInstance == null) {
                    sInstance = new LanguageCenter(context, baseUrl, userName, password);
                }
            }
        }
        return sInstance;
    }

    /**
     * <p> Get the LanguageCenter instance.
     * <p> This should be used anytime the instance is needed, after {@link #with(Context)} or {@link #with(Context, String, String, String)} have been called once.
     * @return the LanguageCenter instance
     */
    public static LanguageCenter getInstance() {
        throwIfNull();
        return sInstance;
    }

    /**
     * <p> Set language center to translate to the current device language.
     * <p> This is default behaviour.
     * <p> If the device language is not available in Language Center, the fallback language will be used.
     * @return true if LanguageCenter will update, false if the language is already set, and no update is required
     */
    public boolean setDeviceLanguage() {
        return setLanguage(getDeviceLanguage(), false, null);
    }

    /**
     * <p> Set language center to translate to the current device language.
     * <p> This is default behaviour.
     * <p> If the device language is not available in Language Center, the fallback language will be used.
     * @param callback A one shot callback that will be executed once the update has finished, and subsequently cleaned up.
     * @return true if LanguageCenter will update, false if the language is already set, and no update is required
     */
    public boolean setDeviceLanguage(final OnLanguageCenterReadyCallback callback) {
        return setLanguage(getDeviceLanguage(), false, callback);
    }

    /**
     * Set language center to manually translate to a language.
     * @param language The manual language code according to ISO 639-1, e.g. "en" for english
     * @return true if LanguageCenter will update, false if the language is already set, and no update is required
     */
    public boolean setLanguage(final String language) {
        return setLanguage(language, true, null);
    }

    /**
     * Set language center to manually translate to a language.
     * @param language The manual language code according to ISO 639-1, e.g. "en" for english.
     * @param callback A one shot callback that will be executed once the update has finished, and subsequently cleaned up.
     * @return true if LanguageCenter will update, false if the language is already set, and no update is required
     */
    public boolean setLanguage(final String language, @Nullable final OnLanguageCenterReadyCallback callback) {
        return setLanguage(language, true, callback);
    }

    private boolean setLanguage(final String language, final boolean override, @Nullable final OnLanguageCenterReadyCallback callback) {
        if (!TextUtils.equals(language, mLanguage)) {
            mUpdateComplete = false;

            if (callback != null) {
                registerOneShotCallback(callback);
            }

            mLanguage = language;

            Logger.d("Setting language: %s. Override: %b", language, override);
            mService.downloadTranslations(mLanguage, LanguageCenter.this);

            if (override) {
                mDatabase.setOverriddenLanguage(language);

            } else {
                mDatabase.clearOverriddenLanguage();
            }

            return true;
        }

        Logger.d("Language was up to date, skipping update.");
        return false;
    }

    /**
     * @return The current language.
     */
    public String getLanguage() {
        return mLanguage;
    }

    /**
     * @return true if the language has been manually set, false if default device language is used.
     */
    public boolean isDeviceLanguageOverridden() {
        return mDatabase.isLanguageOverridden();
    }

    /**
     * @return The current device language
     */
    public static String getDeviceLanguage() {
        String language = "en";
        final Locale locale = Locale.getDefault();
        if (locale != null) {
            String languageCode = locale.getLanguage();
            if (!TextUtils.isEmpty(languageCode)) {
                language = languageCode.toLowerCase();
            }
        }
        return language;
    }

    /**
     * Get a translated string for the current language
     *
     * @param keyRes      String resource for the LC translation key
     * @param fallbackRes Fallback string resource
     * @return The translated string
     */
    public String getTranslation(@StringRes final int keyRes, @StringRes final int fallbackRes) {
        return getTranslation(mResources.getString(keyRes), mResources.getString(fallbackRes));
    }

    /**
     * Get a translated string for the current language
     *
     * @param keyRes      String resource for the LC translation key
     * @param fallbackRes Fallback string resource
     * @param comment     Comment for Language Center CMS
     * @return The translated string
     */
    public String getTranslation(@StringRes final int keyRes, @StringRes final int fallbackRes, final String comment) {
        return getTranslation(mResources.getString(keyRes), mResources.getString(fallbackRes), comment);
    }

    /**
     * Get a translated string for the current language
     *
     * @param key      LC translation key
     * @param fallback Fallback text
     * @return The translated string
     */
    public String getTranslation(final String key, final String fallback) {
        return getTranslation(key, fallback, "");
    }

    /**
     * Get a translated string for the current language
     *
     * @param key      LC translation key
     * @param fallback Fallback text
     * @param comment  Comment for Language Center CMS
     * @return The translated string
     */
    public String getTranslation(final String key, final String fallback, final String comment) {
        return getTranslationDB().getTranslation(key, fallback, comment);
    }

    /**
     * Get a translated string for the current language with formatted varargs
     *
     * @param keyRes      String resource for the LC translation key
     * @param fallbackRes Fallback string resource
     * @return The translated string
     */
    public String getTranslationWithStringFormat(@StringRes final int keyRes, @StringRes final int fallbackRes, Object... args) {
        return getTranslationWithStringFormat(mResources.getString(keyRes), mResources.getString(fallbackRes), args);
    }

    /**
     * Get a translated string for the current language with formatted varargs
     *
     * @param key      LC translation key
     * @param fallback Fallback text
     * @return The translated string
     */
    public String getTranslationWithStringFormat(final String key, final String fallback, final Object... args) {
        final String translation = getTranslationDB().getTranslation(key, fallback, "");
        try {
            return String.format(new Locale(mLanguage), translation, args);

        } catch (Exception e) {
            if (isDebugMode()) {
                Logger.e("Formatting error in translation to %s (%s). Returning to fallback text.", translation, mLanguage);
            }
            return String.format(new Locale(mLanguage), fallback, args);
        }
    }

    public boolean didUpdate() {
        return mUpdateComplete;
    }

    /**
     * Access the LanguageCenter methods with this call after initializing the sInstance instance.
     *
     * @return the language center service util
     */
    LCService getService() {
        throwIfNull();
        return mService;
    }

    /**
     * Access the LanguageCenter methods with this call after initializing the sInstance instance.
     *
     * @return the translations database util
     */
    LCTranslationsDB getTranslationDB() {
        throwIfNull();
        return mDatabase;
    }


    private final List<WeakReference<OnLanguageCenterReadyCallback>> mOneShotCallbacks = new ArrayList<>();
    private final List<WeakReference<OnLanguageCenterReadyCallback>> mPersistentCallbacks = new ArrayList<>();

    @Override
    public void onLanguageCenterReady(@NonNull String language, boolean success) {
        mUpdateComplete = true;
        mUpdateSuccess = success;

        purgeCallbacks();

        Logger.d("Language Center updated (%s) - success: %b. Sending %d one shot callbacks and %d persistent callbacks.",
                language, success, mOneShotCallbacks.size(), mPersistentCallbacks.size());

        notify(language, success, mOneShotCallbacks);
        mOneShotCallbacks.clear();

        notify(language, success, mPersistentCallbacks);
    }

    /**
     * Register a callback that will be fired when LanguageCenter has finished updating.
     * The callback will be cleared once the update has finished.
     */
    public void registerOneShotCallback(OnLanguageCenterReadyCallback callback) {
        if (mUpdateComplete) {
            callback.onLanguageCenterReady(mLanguage, mUpdateSuccess);

        } else {
            register(callback, mOneShotCallbacks);
        }
    }

    /**
     * Remove a registered one shot callback
     */
    public void unregisterOneShotCallback(OnLanguageCenterReadyCallback callback) {
        unregister(callback, mOneShotCallbacks);
    }

    /**
     * Register a callback that will be fired whenever LanguageCenter has finished updating.
     */
    public void registerPersistentCallback(OnLanguageCenterReadyCallback callback) {
        register(callback, mPersistentCallbacks);
    }

    /**
     * Remove a registered persistant callback
     */
    public void unregisterPersistentCallback(OnLanguageCenterReadyCallback callback) {
        unregister(callback, mPersistentCallbacks);
    }

    private void register(final OnLanguageCenterReadyCallback target, final List<WeakReference<OnLanguageCenterReadyCallback>> callbacks) {
        purgeCallbacks();

        for (final WeakReference<OnLanguageCenterReadyCallback> callback : callbacks) {
            if (target == callback.get()) {
                // The callback is already registered. This probably means that someone made a boo boo
                Logger.w("Callback (%s) was already registered", target.getClass().getSimpleName());
                return;
            }
        }

        callbacks.add(new WeakReference<>(target));
    }

    private void unregister(final OnLanguageCenterReadyCallback target, final List<WeakReference<OnLanguageCenterReadyCallback>> callbacks) {
        for (int i = callbacks.size() - 1; i >= 0; i--) {
            final WeakReference<OnLanguageCenterReadyCallback> callback = callbacks.get(i);
            if (target == callback.get()) {
                callbacks.remove(i);
                break;
            }
        }
    }

    private void notify(String language, boolean success, final List<WeakReference<OnLanguageCenterReadyCallback>> callbacks) {
        for (final WeakReference<OnLanguageCenterReadyCallback> callback : callbacks) {
            final OnLanguageCenterReadyCallback ref = callback.get();
            if (ref != null) {
                callback.get().onLanguageCenterReady(language, success);
            }
        }
    }

    private void purgeCallbacks() {
        purgeCallbacks(mOneShotCallbacks);
        purgeCallbacks(mPersistentCallbacks);
    }

    private void purgeCallbacks(final List<WeakReference<OnLanguageCenterReadyCallback>> callbacks) {
        final List<WeakReference<OnLanguageCenterReadyCallback>> dead = new ArrayList<>();
        for (final WeakReference<OnLanguageCenterReadyCallback> callback : callbacks) {
            if (callback.get() == null) {
                dead.add(callback);
            }
        }

        callbacks.removeAll(dead);
    }

    public LanguageCenter setDebugMode(boolean debugMode) {
        throwIfNull();
        mDebugging = debugMode;
        mService.setDebugMode(debugMode);
        return sInstance;
    }

    public boolean isDebugMode() {
        throwIfNull();
        return mDebugging;
    }

    public LanguageCenter setLogLevel(int level) {
        Logger.setLogLevel(level);
        return this;
    }

    private static void throwIfNull() {
        if (null == sInstance) {
            throw new IllegalArgumentException("LanguageCenter hasn't been initialized yet. Please call LanguageCenter.with() before setting the debugging flag.");
        }
    }
}
