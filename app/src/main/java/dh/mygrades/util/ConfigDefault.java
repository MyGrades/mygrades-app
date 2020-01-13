package dh.mygrades.util;

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
     * Get the API url.
     *
     * @return API url as String
     */
    public static String getApiUrl() {
        return getServerUrl() + "/api/v1";
    }

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

    /**
     * Get the public key to communicate with Play Store.
     *
     * @return base64 encoded Play Store public key as String.
     */
    public static String getPlayStorePublicKey() {
        return null; // must be implemented in Config.java
    }

    /**
     * Get the url for the test server.
     *
     * @return test server url as String.
     */
    public static String getTestServerUrl() {
        return null;
    }

    /**
     * Get the credentials for the test server.
     *
     * @return credentials as String.
     */
    public static String getTestServerCredentials() {
        return null;
    }
}
