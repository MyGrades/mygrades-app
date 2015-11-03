package de.mygrades.util;

/**
 * Global default configuration file.
 *
 * Copy this file and rename it to Config.java
 */
public class ConfigDefault {
    // timeouts
    public static final int SCRAPER_TIMEOUT = 25000;

    // user agent used in jsoup
    public static final String BROWSER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36";

    /**
     * Get the server url.
     *
     * @return server url as String
     */
    public static String getServerUrl() {
        return null; // must be implemented in Config.java
    }

    /**
     * Get the api credentials.
     *
     * @return api credentials as String.
     */
    public static String getApiCredentials() {
        return null; // must be implemented in Config.java
    }

    /**
     * Get the key to obfuscate the secure preferences.
     *
     * @return secure preferences key as String.
     */
    public static String getSecurePreferencesKey() {
        return null; // must be implemented in Config.java
    }
}
