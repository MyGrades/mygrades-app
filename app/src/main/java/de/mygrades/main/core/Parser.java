package de.mygrades.main.core;

import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.mygrades.util.exceptions.ParseException;


/**
 * Parses String to Documents and evaluates xPath expressions on them.
 */
public class Parser {
    private Context context;

    /**
     * Tidy builder is needed for creating documents out of strings.
     */
    private Tidy tidyBuilder;

    /**
     * XPath evaluates xPath expressions on documents.
     */
    private XPath xPath;

    /**
     * Transformer is needed for converting a node to string.
     */
    private Transformer transformer;

    public Parser(Context context) throws ParseException {
        this.context = context;
        xPath = XPathFactory.newInstance().newXPath();

        initializeTidyBuilder();

        // initialize Transformer (needed for converting node to string)
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } catch (TransformerConfigurationException e) {
            throw new ParseException("Could not create Transformer!");
        }
    }

    /**
     * Initializes the tidy document builder.
     */
    private void initializeTidyBuilder() {
        tidyBuilder = new Tidy();
        tidyBuilder.setInputEncoding("UTF-8");
        tidyBuilder.setOutputEncoding("UTF-8");
        tidyBuilder.setXmlOut(true);
        tidyBuilder.setShowWarnings(false);
        tidyBuilder.setQuiet(true);
        tidyBuilder.setDropEmptyParas(false);
        tidyBuilder.setTidyMark(false);
        tidyBuilder.setFixComments(false);
        tidyBuilder.setTrimEmptyElements(false);
        tidyBuilder.setJoinStyles(false);
        tidyBuilder.setXmlTags(true); // important, otherwise jtidy manipulates the markup
    }

    /**
     * Parses a Document with given XPATH expression and returns result of expression as Boolean.
     *
     * @param xmlDocument Document which should get parsed
     * @param parseExpression XPATH expression
     * @return parsed result
     * @throws ParseException if something goes wrong at parsing or initializing Document Builder
     */
    public Boolean parseToBoolean(String parseExpression, Document xmlDocument) throws ParseException {
        return evaluateXpathExpressionBoolean(parseExpression, xmlDocument);
    }

    /**
     * Parses a Document with given XPATH expression and returns result of expression as String.
     *
     * @param xmlDocument Document which should get parsed
     * @param parseExpression XPATH expression
     * @return parsed result
     * @throws ParseException if something goes wrong at parsing or initializing Document Builder
     */
    public String parseToString(String parseExpression, Document xmlDocument) throws ParseException {
        return evaluateXpathExpressionString(parseExpression, xmlDocument);
    }

    /**
     * Parses a String with given XPATH expression and returns result of expression as String.
     *
     * @param html String which should get parsed
     * @param parseExpression XPATH expression
     * @return parsed result
     * @throws ParseException if something goes wrong at parsing or initializing Document Builder
     */
    public String parseToString(String parseExpression, String html) throws ParseException {
        Document xmlDocument = createXmlDocument(html);

        return evaluateXpathExpressionString(parseExpression, xmlDocument);
    }

    /**
     * Parses a String with given XPATH expression and returns result of expression as String
     * with XML-structure.
     *
     * @param html String which should get parsed
     * @param parseExpression XPATH expression
     * @return parsed result
     * @throws ParseException if something goes wrong at parsing or initializing Document Builder
     */
    public String parseToStringWithXML(String parseExpression, String html) throws ParseException {
        Document xmlDocument = createXmlDocument(html);

        return getNodeAsString(evaluateXpathExpressionNode(parseExpression, xmlDocument));
    }

    /**
     * Parses a String with given XPATH expression and returns result of expression as NodeList.
     *
     * @param html String which should get parsed
     * @param parseExpression XPATH expression
     * @return parsed result
     * @throws ParseException if something goes wrong at parsing or initializing Document Builder
     */
    public NodeList parseToNodeList(String parseExpression, String html) throws ParseException {
        Document xmlDocument = createXmlDocument(html);

        return evaluateXpathExpressionNodeList(parseExpression, xmlDocument);
    }

    /**
     * Creates a XML-document from a Node.
     *
     * @param node Node of content which is wanted
     * @return content of node as Document
     * @throws ParseException if something goes wrong with transforming
     */
    public Document getNodeAsDocument(Node node) throws ParseException {
        return createXmlDocument(getNodeAsString(node));
    }

    /**
     * Creates a XML-document from a String.
     *
     * @param string String of content which is wanted
     * @return content of String as Document
     * @throws ParseException if something goes wrong with transforming
     */
    public Document getStringAsDocument(String string) throws ParseException {
        return createXmlDocument(string);
    }

    /**
     * Extracts HTML-input fields from given html via parseExpression and
     * transforms them into a key-value map.
     *
     * @param html String which should get parsed
     * @param parseExpression XPATH expression
     * @return Map of key-value pairs of inputs matching parseExpression
     * @throws ParseException if something goes wrong at parsing or initializing Document Builder
     */
    public Map<String, String> getInputsAsMap(String parseExpression, String html) throws ParseException {
        NodeList inputs = parseToNodeList(parseExpression, html);
        Map<String, String> inputsMap = new HashMap<>();

        for (int n=0; n < inputs.getLength(); n++) {
            Element input = (Element)inputs.item(n);
            inputsMap.put(input.getAttribute("name"), input.getAttribute("value"));
        }
        return inputsMap;
    }




    /**
     * Gets content of node as string with XML-structure.
     *
     * @param node Node of content which is wanted
     * @return content of node as string with XML-Structure
     * @throws ParseException if something goes wrong with transforming
     */
    public String getNodeAsString(Node node) throws ParseException {
        StreamResult xmlOutput = new StreamResult(new StringWriter());

        try {
            transformer.transform(new DOMSource(node), xmlOutput);
        } catch (TransformerException e) {
            throw new ParseException("Could not transform node to stream!");
        }
        return xmlOutput.getWriter().toString();
    }

    /**
     * Creates a XML-document from a string.
     *
     * @param html string, which should get parsed into document
     * @return document which can get evaluated by xPath
     * @throws ParseException if something goes wrong at parsing document
     */
    private Document createXmlDocument(String html) throws ParseException {
        try {
            Document document = tidyBuilder.parseDOM(new ByteArrayInputStream(html.getBytes("UTF-8")), null);

            if (document == null || document.getFirstChild() == null) {
                throw new ParseException("Could not parse Document for XPATH!");
            }

            return document;
        } catch (IOException e) {
            throw new ParseException("Could not parse Document for XPATH!");
        }
    }

    /**
     * Evaluates xPath expression on given document as String.
     *
     * @param parseExpression xPath expression
     * @param xmlDocument document, which should get evaluated against expression
     * @return evaluated xPath expression as String
     * @throws ParseException if something goes wrong at parsing
     */
    private String evaluateXpathExpressionString(String parseExpression, Document xmlDocument) throws ParseException {
        try {
            return (String) xPath.compile(parseExpression).evaluate(xmlDocument, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new ParseException("Could not compile XPATH expression!");
        }
    }

    /**
     * Evaluates xPath expression on given document as Boolean.
     *
     * @param parseExpression xPath expression
     * @param xmlDocument document, which should get evaluated against expression
     * @return evaluated xPath expression as Boolean
     * @throws ParseException if something goes wrong at parsing
     */
    private Boolean evaluateXpathExpressionBoolean(String parseExpression, Document xmlDocument) throws ParseException {
        try {
            return (Boolean) xPath.compile(parseExpression).evaluate(xmlDocument, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException e) {
            throw new ParseException("Could not compile XPATH expression!");
        }
    }

    /**
     * Evaluates xPath expression on given document as Node.
     *
     * @param parseExpression xPath expression
     * @param xmlDocument document, which should get evaluated against expression
     * @return evaluated xPath expression as Node
     * @throws ParseException if something goes wrong at parsing
     */
    private Node evaluateXpathExpressionNode(String parseExpression, Document xmlDocument) throws ParseException {
        try {
            return (Node) xPath.compile(parseExpression).evaluate(xmlDocument, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new ParseException("Could not compile XPATH expression!");
        }
    }

    /**
     * Evaluates xPath expression on given document as NodeList.
     *
     * @param parseExpression xPath expression
     * @param xmlDocument document, which should get evaluated against expression
     * @return evaluated xPath expression as NodeList
     * @throws ParseException if something goes wrong at parsing
     */
    private NodeList evaluateXpathExpressionNodeList(String parseExpression, Document xmlDocument) throws ParseException {
        try {
            return (NodeList) xPath.compile(parseExpression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new ParseException("Could not compile XPATH expression!");
        }
    }

    /**
     * Get the context.
     *
     * @return context
     */
    public Context getContext() {
        return context;
    }
}
