package de.mygrades.main.scraping;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.mygrades.util.exceptions.ParseException;


/**
 * Parse a given Document or String with a given Expression either in CSS or in XPATH.
 */
class Parser {
    private static final String TAG = Parser.class.getSimpleName();

    private DocumentBuilderFactory builderFactory;
    private DocumentBuilder builder;
    private XPath xPath;

    public Parser() {
        builderFactory = null;
        builder = null;
        xPath = null;
    }

    /**
     * Parses a Document with given CSS or XPATH expression and returns result of expression.
     *
     * @param document Document which should get parsed
     * @param parseExpression CSS / XPATH expression
     * @param parseType String of type: CSS / XPATH
     * @return parsed result
     * @throws ParseException if something goes wrong at parsing
     */
    public String parse(Document document, String parseExpression, String parseType) throws ParseException {
        switch (parseType) {
            case "CSS":
                return parseCSS(document, parseExpression);
            case "XPATH":
                return parseXPATH(document.toString(), parseExpression);
            default:
                Log.e(TAG, "invalid parse type given");
                return parseCSS(document, parseExpression);
        }
    }

    /**
     * Parses a Document with given CSS expression and returns result of expression.
     *
     * @param document Document which should get parsed
     * @param parseExpression CSS expression
     * @return parsed result
     */
    private String parseCSS(Document document, String parseExpression) {
        return document.select(parseExpression).html();
    }

    /**
     * Parses a String with given XPATH expression and returns result of expression.
     *
     * @param html String which should get parsed
     * @param parseExpression XPATH expression
     * @return parsed result
     * @throws ParseException if something goes wrong at parsing or initializing Document Builder
     */
    private String parseXPATH(String html, String parseExpression) throws ParseException{
        String extracted;
        org.w3c.dom.Document xmlDocument;

        initXPathComponents();

        try {
            xmlDocument = builder.parse(new ByteArrayInputStream(html.getBytes("UTF-8")));
        } catch (SAXException | IOException e) {
            throw new ParseException("Could not parse Document for XPATH!");
        }

        try {
            extracted = xPath.compile(parseExpression).evaluate(xmlDocument);
        } catch (XPathExpressionException e) {
            throw new ParseException("Could not compile XPATH expression!");
        }

        return extracted;
    }

    /**
     * Init BuilderFactory, Builder and XPath only if its not initialized,
     * so it can be reused.
     *
     * @throws ParseException
     */
    private void initXPathComponents() throws ParseException {
        if (builderFactory == null) {
            builderFactory = DocumentBuilderFactory.newInstance();
        }
        if (xPath == null) {
            xPath = XPathFactory.newInstance().newXPath();
        }
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ParseException("Could not create DocumentBuilderFactory for XPATH!");
        }
    }
}
