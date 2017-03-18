package de.mygrades.main.core;

import android.content.SharedPreferences;
import android.util.Log;

import com.securepreferences.SecurePreferences;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.greenrobot.event.EventBus;
import de.mygrades.database.dao.Action;
import de.mygrades.database.dao.ActionParam;
import de.mygrades.database.dao.TransformerMapping;
import de.mygrades.main.events.IntermediateTableScrapingResultEvent;
import de.mygrades.main.events.ScrapeProgressEvent;
import de.mygrades.main.processor.GradesProcessor;
import de.mygrades.util.Config;
import de.mygrades.util.Constants;
import de.mygrades.util.exceptions.ParseException;
import info.guardianproject.netcipher.client.TlsOnlySocketFactory;


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
            SSLContext sc = SSLContext.getInstance("TLSv1");
            sc.init(null, trustAllCertificates, new SecureRandom());
            SSLSocketFactory noSSLv3Factory = new TlsOnlySocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultSSLSocketFactory(noSSLv3Factory);
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

    /**
     * Used as referrer.
     * Use google as first referrer due to issues with some websites without referrer.
     */
    private String previousUrl = "https://www.google.com";

    public Scraper(List<Action> actions, Parser parser, String gradeHash) {
        this.actions = actions;
        this.cookies = new HashMap<>();
        this.parser = parser;
        this.baseUri = null;
        this.gradeHash = gradeHash;
    }

    public Scraper(List<Action> actions, Parser parser) {
        this(actions, parser, null);
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
            Log.v(TAG, "--- Action " + (i + 1) + "/" + actions.size() + " -- Sending Request to url: " + url);

            // make request with data, cookies, current method
            getRequestData(requestData, action.getActionParams());
            makeJsoupRequest(requestData, getHttpMethodEnum(action.getMethod()), url);
            previousUrl = url;

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
            EventBus.getDefault().post(new ScrapeProgressEvent(i + 1, actions.size() + 1, tableAsInterimResult, gradeHash));
        }
        return parsedHtml;
    }

    /**
     * Scrape step by step by given Actions.
     * A clicking user is simulated with Jsoup and different steps to different urls.
     * Cookies and other request specific Data are transferred.
     *
     * Multiple Tables - the grades for each semester are at another page
     *  1. extract list of semesters (value of option form field)
     *  2. extract the form url
     *  3. make requests for semesters and extract semester name and the table of grades (html)
     *
     * @return Map semester name -> table of grades (html)
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public Map<String, String> scrapeMultipleTables(Map<String, TransformerMapping> transformerMappings) throws ParseException, IOException, URISyntaxException {
        Map<String, String> resultTables = new HashMap<>();
        String parsedHtml = null;
        Map<String, String> requestData = new HashMap<>();

        // iterate over all actions in order by position
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            Log.v(TAG, action.toString());

            String url = getUrl(parsedHtml, action.getUrl());
            Log.v(TAG, "--- Action " + (i + 1) + "/" + actions.size() + " -- Sending Request to url: " + url);

            // make request with data, cookies, current method
            getRequestData(requestData, action.getActionParams());
            makeJsoupRequest(requestData, getHttpMethodEnum(action.getMethod()), url);
            previousUrl = url;

            // reset request data -> now there can be added data for next result
            requestData = new HashMap<>();

            String documentAsString = document.toString();
            // parse Content to String if its not the table action -> so there is most likely a link in
            if (!action.getType().replace(":form", "").equals(GradesProcessor.ACTION_TYPE_TABLE_GRADES_ITERATOR)) {
                if (action.getType().endsWith(":form")) {
                    String parseExpression = action.getParseExpression();
                    parsedHtml = parser.parseToString(parseExpression + "/@action", documentAsString);
                    requestData.putAll(parser.getInputsAsMap(parseExpression + "//input[not(@type=\"submit\")]", documentAsString));
                } else {
                    parsedHtml = parser.parseToString(action.getParseExpression(), documentAsString);
                }
            } else {
                // extract list of semesters to make follow up requests
                NodeList nodeList = parser.parseToNodeList(transformerMappings.get(Transformer.MT_SEMESTER_OPTIONS).getParseExpression(), documentAsString);
                String[] semestersForFollowUpRequests = new String[nodeList.getLength()];
                for (int j = 0; j < nodeList.getLength(); j++) {
                    Node nNode = nodeList.item(j);
                    semestersForFollowUpRequests[j] = parser.getNodeAsString(nNode);
                }

                // extract form information (and hold it)
                String formUrl = getUrl(parser.parseToString(transformerMappings.get(Transformer.MT_FORM_URL).getParseExpression(), documentAsString));

                // make request for each semester in list -- get html table code and current semester name (e.g. SoSe 13)
                for (int j = 0; j < semestersForFollowUpRequests.length; j++) {
                    // fill request data
                    requestData.put("semester", semestersForFollowUpRequests[j]);

                    // add form inputs if necessary
                    if (action.getType().endsWith(":form")) {
                        // TODO: maybe use new transformer mapping MT_FORM to avoid string replacement?
                        String selectFormExpression = transformerMappings.get(Transformer.MT_FORM_URL).getParseExpression().replace("/@action", "");
                        requestData.putAll(parser.getInputsAsMap(selectFormExpression + "//input[not(@type=\"submit\")]", documentAsString));
                    }

                    // make request to retrieve HTML table code for current semester
                    Log.d(TAG, "Sending Request to url: " + formUrl + " -- requestData: " + requestData.toString());
                    makeJsoupRequest(requestData, Connection.Method.POST, formUrl);

                    documentAsString = document.toString();

                    // parse table with XML
                    parsedHtml = parser.parseToStringWithXML(action.getParseExpression(), documentAsString);
                    // extract separate semester
                    String parsedSemester = parser.parseToString(transformerMappings.get(Transformer.MT_SEMESTER_STRING).getParseExpression(), documentAsString);

                    // add semester -> grades table in map
                    resultTables.put(parsedSemester, parsedHtml);

                    // reset request data -> now there can be added data for next result
                    requestData = new HashMap<>();
                }
            }

            // post intermediate status event
            EventBus.getDefault().post(new ScrapeProgressEvent(i + 1, actions.size() + 1, false, gradeHash));
        }

        return resultTables;
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
    private void makeJsoupRequest(Map<String, String> requestData, Connection.Method method, String url) throws IOException, URISyntaxException {
        Connection.Response response = Jsoup.connect(url)
                .data(requestData)
                .cookies(cookies)
                .referrer(previousUrl) // some websites block without referrer
                .userAgent(Config.BROWSER_USER_AGENT) // set explicit user agent
                .method(method)
                .timeout(Config.SCRAPER_TIMEOUT)
                .followRedirects(false)
                .execute();

        // get cookies from response and add to all cookies
        addNewCookies(response.cookies());

        // get content from response
        document = response.parse();
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        document.select("script").remove();
        document.select("td:contains(aktuellen ECTS-Grades)").remove(); // remove invalid html (see error #71)

        // check for location redirect
        String location = response.header("location");
        if (location != null) {
            baseUri = new URL(location).toURI();
            makeJsoupRequest(new HashMap<String, String>(), Connection.Method.GET, location);
        }

        // check for meta refresh tag
        Element meta = document.select("meta[http-equiv=Refresh").first();
        if (meta != null) {
            String content = meta.attr("content");
            if (content != null) {
                meta.attr("refresh-url", content.replaceAll("(?i)^(\\d+;.*URL=)(.+)$", "$2"));
                makeJsoupRequest(requestData, Connection.Method.GET, meta.absUrl("refresh-url"));
            }
        }

        // check for refresh pseudo header
        String refreshHeader = response.header("refresh");
        if (refreshHeader != null) {
            String relativeUrl = refreshHeader.replaceAll("(?i)^(\\d+;.*URL=)(.+)$", "$2");
            String redirectUrl = StringUtil.resolve(document.baseUri(), relativeUrl);
            makeJsoupRequest(requestData, Connection.Method.GET, redirectUrl);
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
     * Evaluates the url from a previously parsed String.
     *
     * @param parsedHtml parsed HTML of previous action
     * @return url as string
     */
    private String getUrl(String parsedHtml) throws MalformedURLException, URISyntaxException {
        return getUrl(parsedHtml, null);
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
                            prefs = new SecurePreferences(parser.getContext(), Config.getSecurePreferencesKey(), Constants.NOT_SO_SECURE_PREF_FILE);
                        }
                        value = prefs.getString(Constants.PREF_KEY_PASSWORD, "");
                    } else if (actionParam.getType().equals("username")) {
                        if (prefs == null) {
                            prefs = new SecurePreferences(parser.getContext(), Config.getSecurePreferencesKey(), Constants.NOT_SO_SECURE_PREF_FILE);
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
