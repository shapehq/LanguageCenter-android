package com.novasa.languagecenter.util;

import java.util.Locale;

/**
 * Created by admin on 29/09/16.
 */

public final class LocaleUtil {

    public static String getPreferredLanguageCode() {
        String preferredLanguageCode = "en";
        final Locale locale = Locale.getDefault();
        if (locale != null) {
            String languageCode = locale.getLanguage();
            if (languageCode != null && languageCode.length() > 0) {
                preferredLanguageCode = languageCode.toLowerCase();
            }
        }
        return preferredLanguageCode;
    }
}
