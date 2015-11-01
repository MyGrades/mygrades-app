package de.mygrades.main.core;

import android.content.SharedPreferences;
import android.util.Log;

import com.securepreferences.SecurePreferences;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSocketFactory;

import javax.net.ssl.TrustManager;

import de.greenrobot.event.EventBus;
import de.mygrades.database.dao.Action;
import de.mygrades.database.dao.ActionParam;
import de.mygrades.main.events.IntermediateTableScrapingResultEvent;
import de.mygrades.main.events.ScrapeProgressEvent;
import de.mygrades.main.processor.GradesProcessor;
import de.mygrades.util.Config;
import de.mygrades.util.Constants;
import de.mygrades.util.NoSSLv3SocketFactory;
import de.mygrades.util.exceptions.ParseException;


/**
 * Scrape a Website or several Websites step by step in order by given actions.
 * Redirects are followed (301 and HTML redirects).
 */
public class Scraper {
    private static final String TAG = Scraper.class.getSimpleName();

    /**
     * Avoid SSLv3 as the only protocol available.
     * http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests/2793153#2793153
     * (not used) http://stackoverflow.com/questions/26633349/disable-ssl-as-a-protocol-in-httpsurlconnection
     * (not used) http://stackoverflow.com/a/29946540 -> initializing of SSLv3 Context
     */
    static {
        TrustManager[] trustAllCertificates = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null; // Not relevant.
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing. Just allow them all.
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing. Just allow them all.
                    }
                }
        };

        HostnameVerifier trustAllHostnames = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true; // Just allow them all.
            }
        };

        try {
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sc = SSLContext.getInstance("TLSv1");
            sc.init(null, trustAllCertificates, new SecureRandom());
            SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames);
        }
        catch (GeneralSecurityException e) {

        }
    }

    /**
     * List of Actions which are processed step by step.
     */
    private List<Action> actions;

    /**
     * Cookies of Websites, which get visited by crawling.
     */
    private HashMap<String, String> cookies;

    /**
     * Complete document with HTML of current Action.
     */
    private Document document;

    /**
     * Parser to extract values while scraping.
     */
    private Parser parser;

    private URI baseUri;

    private String gradeHash;

    public Scraper(List<Action> actions, Parser parser, String gradeHash) {
        this.actions = actions;
        this.cookies = new HashMap<>();
        this.parser = parser;
        this.baseUri = null;
        this.gradeHash = gradeHash;
    }

    /**
     * Scrape step by step by given Actions.
     * A clicking user is simulated with Jsoup and different steps to different urls.
     * Cookies and other request specific Data are transferred.
     *
     * @throws IOException
     * @throws ParseException
     */
    public String scrape() throws IOException, ParseException, URISyntaxException {
        return scrape(false);
    }

    /**
     * Scrape step by step by given Actions.
     * A clicking user is simulated with Jsoup and different steps to different urls.
     * Cookies and other request specific Data are transferred.
     *
     * @param tableAsInterimResult if set true, Action with table_grades is skipped and sent via EventBus
     * @throws IOException
     * @throws ParseException
     */
    public String scrape(boolean tableAsInterimResult) throws IOException, ParseException, URISyntaxException {
        String parsedHtml = null;
        Map<String, String> requestData = new HashMap<>();

        // iterate over all actions in order by position
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            Log.v(TAG, action.toString());

            String url = getUrl(parsedHtml, action.getUrl());
            Log.v(TAG, "Action " + (i + 1) + "/" + actions.size() + " -- Sending Request to url: " + url);

            // make request with data, cookies, current method
            getRequestData(requestData, action.getActionParams());
            makeJsoupRequest(requestData, getHttpMethodEnum(action.getMethod()), url);

            // reset request data -> now there can be added data for next result
            requestData = new HashMap<>();

            // send table of grades to processor, if tableAsInterimResult is set
            if (tableAsInterimResult && action.getType().equals(GradesProcessor.ACTION_TYPE_TABLE_GRADES)) {
                // parse with XML
                String parsedTable = parser.parseToStringWithXML(action.getParseExpression(), document.toString());
                EventBus.getDefault().post(new IntermediateTableScrapingResultEvent(parsedTable, gradeHash));

                // continue with next action
                i = i + 1;
                action = actions.get(i);
                Log.v(TAG, "Action " + (i + 1) + "/" + actions.size() + " -- Last Action used in other Thread.");
                //Log.v(TAG, action.toString());
            }

            // if action is a form
            if (action.getType().endsWith(":form")) {
                String documentAsString = document.toString();
                // get URL of form for next request
                parsedHtml = parser.parseToString(action.getParseExpression() + "/@action", documentAsString);
                // get all input fields within form -> send key value pairs with next request
                requestData = parser.getInputsAsMap(action.getParseExpression()+"//input[not(@type=\"submit\")]", documentAsString);
            } else {

                // parse Content to String if its not the last action
                if (i < actions.size() - 1) {
                    parsedHtml = parser.parseToString(action.getParseExpression(), document.toString());
                } else {
                    // parse with XML
                    parsedHtml = parser.parseToStringWithXML(action.getParseExpression(), document.toString());
                }
            }

            // post intermediate status event
            EventBus.getDefault().post(new ScrapeProgressEvent(i + 1, actions.size() + 1, tableAsInterimResult));
        }
        return parsedHtml;
    }

    /**
     * Makes request to the given url with given request data and method.
     * Follows redirects (including HTML redirects).
     *
     * @param requestData Map of key value pairs for request
     * @param method Connection.Method - HTTP method
     * @param url url as string
     * @throws IOException if there is an error connecting to the url
     */
    private void makeJsoupRequest(Map<String, String> requestData, Connection.Method method, String url) throws IOException {
        Connection.Response response = Jsoup.connect(url)
                .data(requestData)
                .cookies(cookies)
                .referrer("http://www.google.com") // some websites block without referrer
                .userAgent(Config.BROWSER_USER_AGENT) // set explicit user agent
                .method(method)
                .timeout(Config.SCRAPER_TIMEOUT)
                //.validateTLSCertificates(false) // do not validate ssl certificates -> must be used for self certified
                .execute();

        // get cookies from response and add to all cookies
        addNewCookies(response.cookies());

        // get content from response
        document = response.parse();
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        // check if there is a redirect via meta-equiv=refresh
        Element metaRefresh = document.select("html head meta[http-equiv=refresh]").first();
        if (metaRefresh != null) {
            // get url to redirect
            String refreshContent = metaRefresh.attr("content");
            if (refreshContent != null) {
                // split at ;URL= to get url
                String redirectUrl = refreshContent.split(";URL=")[1];
                if (redirectUrl != null) {
                    // if its a relative redirect url -> prepend with host
                    if (!redirectUrl.toLowerCase().startsWith("http")) {
                        // check if it starts with / and remove it
                        if (redirectUrl.startsWith("/") && response.url().toString().endsWith("/")) {
                            redirectUrl = redirectUrl.substring(1);
                        }
                        redirectUrl = response.url().toString().concat(redirectUrl);
                    }
                    makeJsoupRequest(requestData, Connection.Method.GET, redirectUrl);
                }
            }
        }
    }

    /**
     * Evaluates the url.
     * If an actionUrl is given it is used,
     * if not the parsed HTML of previous action is used.
     *
     * @param parsedHtml parsed HTML of previous action
     * @param actionUrl url of action, may be null
     * @return url as string
     */
    private String getUrl(String parsedHtml, String actionUrl) throws MalformedURLException, URISyntaxException {
        // set base uri if it isn't set and action url is set
        if (baseUri == null && actionUrl != null) {
            baseUri = new URL(actionUrl).toURI();
        }

        // if url of action == null -> use parse result of previous action
        if (actionUrl == null) {
            // if url doesn't start with http*
            if (!parsedHtml.toLowerCase().startsWith("http")) {
                return baseUri.resolve(parsedHtml).toString();
            }
            return parsedHtml;
        }
        return actionUrl;
    }

    /**
     * Creates a map with key value data of action params for request.
     *
     * @param actionParams action params of current action
     * @return Map of key value pairs for request
     */
    private Map<String, String> getRequestData(Map<String, String> requestData, List<ActionParam> actionParams) {
        SharedPreferences prefs = null;


        if (actionParams != null) {
            // iterate over all ActionParams and add params to Map
            for (ActionParam actionParam : actionParams) {
                String value = actionParam.getValue();
                // check if type == password or username -> get from secure shared preferences
                if (actionParam.getType() != null) {
                    if (actionParam.getType().equals("password")) {
                        if (prefs == null) {
                            prefs = new SecurePreferences(parser.getContext(), Constants.NOT_SO_SECURE_PREF_PW, Constants.NOT_SO_SECURE_PREF_FILE);
                        }
                        value = prefs.getString(Constants.PREF_KEY_PASSWORD, "");
                    } else if (actionParam.getType().equals("username")) {
                        if (prefs == null) {
                            prefs = new SecurePreferences(parser.getContext(), Constants.NOT_SO_SECURE_PREF_PW, Constants.NOT_SO_SECURE_PREF_FILE);
                        }
                        value = prefs.getString(Constants.PREF_KEY_USERNAME, "");
                    }
                } else {
                    value = actionParam.getValue();
                }
                requestData.put(actionParam.getKey(), value);
            }
        }
        return requestData;
    }

    /**
     * Saves new cookies to cookies map.
     *
     * @param newCookies Cookies to add
     */
    private void addNewCookies(Map<String, String> newCookies) {
        // iterate over new map and add all new cookies to cookie-Map
        for (Map.Entry<String, String> entry : newCookies.entrySet()) {
            if (entry.getValue() != null) {
                cookies.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Evaluates the HTTP method as enum from the given string.
     * Defaults to GET.
     *
     * @param methodString HTTP method as String
     * @return Connection.Method - HTTP method
     */
    private Connection.Method getHttpMethodEnum(String methodString) {
        Connection.Method method;
        switch (methodString) {
            case "GET":
                method = Connection.Method.GET;
                break;
            case "POST":
                method = Connection.Method.POST;
                break;
            default:
                method = Connection.Method.GET;
                break;
        }
        return method;
    }
}
