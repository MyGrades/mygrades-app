package de.mygrades.main.scraping;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.mygrades.util.exceptions.ParseException;



public class Parser {
    private static final String TAG = Parser.class.getSimpleName();

    private DocumentBuilder builder;
    private XPath xPath;
    private Transformer transformer;

    public Parser() throws ParseException {
        initXPathComponents();
    }

    /**
     * Init Builder, XPath and Transformer.
     *
     * @throws ParseException
     */
    private void initXPathComponents() throws ParseException {
        xPath = XPathFactory.newInstance().newXPath();

        // initialize Builder to build document from string
        try {
            builder =  DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ParseException("Could not create DocumentBuilderFactory for XPATH!");
        }

        // initialize Transformer (needed for converting node to string)
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } catch (TransformerConfigurationException e) {
            throw new ParseException("Could not create Transformer!");
        }
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





    public void testIterator(String table) throws ParseException, XPathExpressionException {
        org.w3c.dom.Document xmlDocument;

        initXPathComponents();

        try {
            xmlDocument = builder.parse(new ByteArrayInputStream(table.getBytes("UTF-8")));
        } catch (SAXException | IOException e) {
            throw new ParseException("Could not parse Document for XPATH!");
        }

        String expression = "//tr[./td[@class != 'qis_kontoOnTop']]";

        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nNode = nodeList.item(i);

            String nodeAsAString = getNodeAsString(nNode);

            System.out.println(nodeAsAString);
            //Element eElement = (Element) nNode;
            //System.out.println(eElement.getElementsByTagName("td").item(0).getTextContent());
        }


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
            return builder.parse(new ByteArrayInputStream(html.getBytes("UTF-8")));
        } catch (SAXException | IOException e) {
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
     * Gets content of node as string with XML-structure.
     *
     * @param node Node of content which is wanted
     * @return content of node as string with XML-Structure
     * @throws ParseException if something goes wrong with transforming
     */
    private String getNodeAsString(Node node) throws ParseException {
        StreamResult xmlOutput = new StreamResult(new StringWriter());

        try {
            transformer.transform(new DOMSource(node), xmlOutput);
        } catch (TransformerException e) {
            throw new ParseException("Could not transform node to stream!");
        }
        return xmlOutput.getWriter().toString();
    }
}
