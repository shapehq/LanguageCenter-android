package com.novasa.languagecenter;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

import com.novasa.languagecenter.interfaces.OnLanguageCenterReadyCallback;
import com.novasa.languagecenter.interfaces.UpdateCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by andersp on 04/10/16.
 * <p> Init framework with:
 * <p> {@link #with(Context)}
 * <p> These string resources are needed by the framework to function:
 * <ul>
 * <li>language_center_user_name
 * <li>language_center_password
 * <li>language_center_base_url
 * </ul>
 *
 * <p> Or:
 * <p> {@link #with(Context, String, String, String)}
 * <p> No string resources are needed when all parameters are supplied at init.
 *
 * <p>Auto initialization can be deactivated by passing false as the last parameter of {@link #with(Context, boolean)} or {@link #with(Context, String, String, String, boolean)}.
 * {@link #initialize(Context)} must be called manually after. Use this for example if the app needs to receive a remote language setting before initializing. Default behaviour is to automatically initialize.
 *
 * <p> LanguageCenter will automatically detect the device language for translation as default.
 *
 * <p> Use {@link #registerOneShotCallback(OnLanguageCenterReadyCallback)} to receive a callback once LanguageCenter has finished initializing.
 * The callback will be automatically cleaned up once the update has finished.
 *
 * <p> Use {@link #setLanguage(String)} or {@link #setLanguage(String, OnLanguageCenterReadyCallback)} to manually set the language
 * <p> If the language is not available in LanguageCenter, the fallback language will be used.
 *
 * <p> Use widgets {@link com.novasa.languagecenter.view.LanguageCenterTextView}, {@link com.novasa.languagecenter.view.LanguageCenterButton}, {@link com.novasa.languagecenter.view.LanguageCenterEditText}
 * for handy xml properties <i>transKey</i> <i>transComment</i> <i>hintTransKey</i> <i>hintTransComment</i>, which can be used to set the translation keys directly in xml.
 * All LanguageCenter widgets update automatically when a language update finishes.
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "unused"})
public final class LanguageCenter implements UpdateCallback {

    public enum Status {
        NOT_INITIALIZED,
        INITIALIZING,
        UPDATING,
        READY,
        FAILED
    }

    public static final String LOG_TAG = "LanguageCenter";
    public static boolean DEBUGGABLE;

    private static LanguageCenter sInstance;


    public static LanguageCenter with(@NonNull Context context) {
        return with(context, true);
    }

    /**
     * Initialize LanguageCenter with this method.
     *
     * @param context application context for initialization
     * @param autoInit if LanguageCenter should update automatically. If this is false, {@link LanguageCenter#initialize(Context)} must be called manually.
     * @return the LanguageCenter instance
     */
    public static LanguageCenter with(@NonNull Context context, boolean autoInit) {
        try {
            final Resources res = context.getApplicationContext().getResources();
            final String bUrl = res.getString(res.getIdentifier("language_center_base_url", "string", context.getPackageName()));
            final String uName = res.getString(res.getIdentifier("language_center_username", "string", context.getPackageName()));
            final String pWord = res.getString(res.getIdentifier("language_center_password", "string", context.getPackageName()));

            return with(context, bUrl, uName, pWord, autoInit);

        } catch (Resources.NotFoundException e) {
            throw new Resources.NotFoundException(
                    "\nPlease supply all required language string resources:\n" +
                            "<string name='language_center_app_name'>\n" +
                            "<string name='language_center_username'>\n" +
                            "<string name='language_center_password'>\n" +
                            "<string name='language_center_base_url'>");
        }
    }

    public static LanguageCenter with(@NonNull Context context, @NonNull String baseUrl, @NonNull String userName, @NonNull String password) {
        return with(context, baseUrl, userName, password, true);
    }

    /**
     * Initialize LanguageCenter with this method.
     *
     * @param context  application context for initialization
     * @param baseUrl  the base url for the LanguageCenter server
     * @param userName user name for LC server auth
     * @param password password for LC server auth
     * @param autoInit if LanguageCenter should update automatically. If this is false, {@link LanguageCenter#initialize(Context)} must be called manually.
     * @return the LanguageCenter instance
     */
    public static LanguageCenter with(@NonNull Context context, @NonNull String baseUrl, @NonNull String userName, @NonNull String password, boolean autoInit) {
        DEBUGGABLE = 0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);

        throwIfNotApplicationContext(context);

        if (sInstance == null) {
            synchronized (LanguageCenter.class) {
                if (sInstance == null) {
                    sInstance = new LanguageCenter(context, baseUrl, userName, password);
                    if (autoInit) {
                        sInstance.initialize(context);
                    }
                }
            }
        }
        return sInstance;
    }

    /**
     * <p> Get the LanguageCenter instance.
     * <p> This should be used anytime the instance is needed, after {@link #with(Context)} or {@link #with(Context, String, String, String)} have been called once.
     *
     * @return the LanguageCenter instance
     */
    public static LanguageCenter getInstance() {
        throwIfNull();
        return sInstance;
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

    private static void throwIfNull() {
        if (null == sInstance) {
            throw new IllegalArgumentException("LanguageCenter hasn't been initialized yet. Please call LanguageCenter.with().");
        }
    }

    private static void throwIfNotApplicationContext(Context context) {
        if (!(context instanceof Application)) {
            throw new IllegalArgumentException("Please supply LanguageCenter with an instance of Application Context");
        }
    }

    private boolean mDebugging = false;

    private Resources mResources;
    private LCService mService;
    private LCTranslationsDB mDatabase;

    private Status mStatus;

    private String mLanguage;

    private long mTimeRef;

    private LanguageCenter(Context context, String baseUrl, String userName, String password) {
        mStatus = Status.NOT_INITIALIZED;

        mResources = context.getResources();
        mService = new LCService(baseUrl, userName, password);
        mDatabase = new LCTranslationsDB(context);

        final String overriddenLanguage = mDatabase.getOverriddenLanguage();
        mLanguage = !TextUtils.isEmpty(overriddenLanguage) ? overriddenLanguage : getDeviceLanguage();
    }

    /**
     * Initialize LanguageCenter. This should only be called if {@link #with} was called without auto init.
     * @param context Must be Application context, since LanguageCenter registers a receiver to listen for device language changes.
     * @param language Initialize LanguageCenter with a specific language, overriding the device language setting.
     */
    public void initialize(@NonNull final Context context, @NonNull String language) {
        if (!language.equals(mLanguage)) {
            mLanguage = language;
            mDatabase.setOverriddenLanguage(language);
        }
        initialize(context);
    }

    /**
     * Initialize LanguageCenter. This should only be called if {@link #with} was called without auto init.
     * @param context Must be Application context, since LanguageCenter registers a receiver to listen for device language setting changes.
     */
    public void initialize(@NonNull final Context context) {
        if (mStatus != Status.NOT_INITIALIZED) {
            Logger.e("Language Center was already initialized!");
            return;
        }

        throwIfNotApplicationContext(context);

        mStatus = Status.INITIALIZING;

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

        // Getting all translations already in the language center by default
        // Post this, so we have a chance to change the language first
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                updateInitial(context);
            }
        });
    }

    private void updateInitial(final Context context) {
        update(new OnLanguageCenterReadyCallback() {
            @Override
            public void onLanguageCenterReady(@NonNull LanguageCenter languageCenter, @NonNull String language, @NonNull Status status) {
                if (status == Status.FAILED) {
                    Logger.d("Language Center failed to update.");

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !hasNetwork(context)) {
                        awaitNetwork(context);
                    }
                }
            }
        });
    }

    private void awaitNetwork(Context context) {
        Logger.d("Awaiting network...");

        final BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || hasNetwork(context)) {
                    // If we're below 21 we just retry instead of making sure we have network.
                    // If it fails again we will just be back here until it succeeds.
                    context.unregisterReceiver(this);
                    updateInitial(context);
                }
            }
        };

        context.registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean hasNetwork(Context context) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            for (final Network network : manager.getAllNetworks()) {
                final NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
                if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    Logger.d("Network established.");
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return The current status
     */
    public Status getStatus() {
        return mStatus;
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
     * <p> Set language center to translate to the current device language.
     * <p> This is default behaviour.
     * <p> If the device language is not available in Language Center, the fallback language will be used.
     *
     * @return true if LanguageCenter will update, false if the language is already set, and no update is required
     */
    public boolean setDeviceLanguage() {
        return setLanguage(getDeviceLanguage(), false, null);
    }

    /**
     * <p> Set language center to translate to the current device language.
     * <p> This is default behaviour.
     * <p> If the device language is not available in Language Center, the fallback language will be used.
     *
     * @param callback A one shot callback that will be executed once the update has finished, and subsequently cleaned up.
     *                 NOTE: This is stored as a weak reference!
     * @return true if LanguageCenter will update, false if the language is already set, and no update is required
     */
    public boolean setDeviceLanguage(@Nullable final OnLanguageCenterReadyCallback callback) {
        return setLanguage(getDeviceLanguage(), false, callback);
    }

    /**
     * Set language center to manually translate to a language.
     *
     * @param language The manual language code according to ISO 639-1, e.g. "en" for english
     * @return true if LanguageCenter will update, false if the language is already set, and no update is required
     */
    public boolean setLanguage(@NonNull final String language) {
        return setLanguage(language, true, null);
    }

    /**
     * Set language center to manually translate to a language.
     *
     * @param language The manual language code according to ISO 639-1, e.g. "en" for english.
     * @param callback A one shot callback that will be executed once the update has finished, and subsequently cleaned up.
     *                 NOTE: This is stored as a weak reference!
     * @return true if LanguageCenter will update, false if the language is already set, and no update is required
     */
    public boolean setLanguage(@NonNull final String language, @Nullable final OnLanguageCenterReadyCallback callback) {
        return setLanguage(language, true, callback);
    }

    private boolean setLanguage(final String language, final boolean override, final OnLanguageCenterReadyCallback callback) {
        if (mStatus == Status.NOT_INITIALIZED) {
            throw new IllegalStateException("LanguageCenter has not been initialized. Please call initialize() before changing the language");
        }

        if (!TextUtils.equals(language, mLanguage)) {

            Logger.d("Setting language: %s. Override: %b", language, override);
            mLanguage = language;

            if (override) {
                mDatabase.setOverriddenLanguage(language);

            } else {
                mDatabase.clearOverriddenLanguage();
            }

            update(callback);

            return true;
        }

        Logger.d("Language was up to date, skipping update.");
        return false;
    }

    public void update() {
        update(null);
    }

    /**
     * Update LanguageCenter with the current language
     *
     * @param callback A one shot callback that will be called once the update has completed.
     *                 NOTE: This is stored as a weak reference!
     */
    public void update(@Nullable final OnLanguageCenterReadyCallback callback) {
        mStatus = Status.UPDATING;

        if (callback != null) {
            registerOneShotCallback(callback);
        }

        mTimeRef = SystemClock.elapsedRealtime();

        mService.downloadTranslations(mLanguage, this);
    }

    @Override
    public void onUpdated(String language, boolean success) {

        mStatus = success ? Status.READY : Status.FAILED;

        purgeCallbacks();

        Logger.d("Language Center updated (%s) - status: %s. Time spent: %d ms. Sending %d one shot callbacks and %d persistent callbacks.",
                language, mStatus, SystemClock.elapsedRealtime() - mTimeRef, mOneShotCallbacks.size(), mPersistentCallbacks.size());

        notify(language, mStatus, mOneShotCallbacks);
        mOneShotCallbacks.clear();

        notify(language, mStatus, mPersistentCallbacks);
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
        return mStatus == Status.READY;
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

    /**
     * Register a callback that will be fired when LanguageCenter has finished updating.
     * The callback will be cleared once the update has finished.
     */
    public void registerOneShotCallback(OnLanguageCenterReadyCallback callback) {
        if (mStatus == Status.READY) {
            callback.onLanguageCenterReady(this, mLanguage, mStatus);

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

    private void notify(String language, Status status, final List<WeakReference<OnLanguageCenterReadyCallback>> callbacks) {
        for (final WeakReference<OnLanguageCenterReadyCallback> callback : callbacks) {
            final OnLanguageCenterReadyCallback ref = callback.get();
            if (ref != null) {
                callback.get().onLanguageCenterReady(this, language, status);
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
        if (DEBUGGABLE) {
            throwIfNull();
            mDebugging = debugMode;
            mService.setDebugMode(debugMode);
        }
        return sInstance;
    }

    public boolean isDebugMode() {
        throwIfNull();
        return DEBUGGABLE && mDebugging;
    }

    public LanguageCenter setLogLevel(int level) {
        Logger.setLogLevel(level);
        return this;
    }
}
