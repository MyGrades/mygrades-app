package de.mygrades.util;

import android.util.Base64;

/**
 * Global default configuration file.
 *
 * Copy this file and rename it to Config.java
 */
public class ConfigDefault {
    // server info
    private static final String DOMAIN = "https://mygrades.de";
    private static final int API_VERSION = 1;
    public static final String SERVER_URL = DOMAIN + "/api/v" + API_VERSION;
    public static final String API_USER = "MyGradesApi";
    public static final String API_PW = "AxPMiBxtgZXuGF28fVme";
    public static final String API_BASE64_CREDENTIALS = Base64.encodeToString((API_USER + ":" + API_PW).getBytes(), Base64.NO_WRAP);

    // timeouts
    public static final int SCRAPER_TIMEOUT = 25000;

    // user agent used in jsoup
    public static final String BROWSER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36";
}
