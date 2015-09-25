package de.mygrades.main.core;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import de.mygrades.database.dao.GradeEntry;
import de.mygrades.database.dao.TransformerMapping;
import de.mygrades.util.exceptions.ParseException;

/**
 * Creates GradeEntry Objects from given HTML with given TransformerMappings.
 */
public class Transformer {
    private static final String TAG = Transformer.class.getSimpleName();

    // mapping from TransformerMapping Name -> GradeEntry Property
    private static final String ITERATOR = "iterator";
    private static final String EXAM_ID = "exam_id";
    private static final String NAME = "name";
    private static final String SEMESTER = "semester";
    private static final String GRADE = "grade";
    private static final String STATE = "state";
    private static final String CREDIT_POINTS = "credit_points";


    /**
     * Parser to extract values into Models.
     */
    private Parser parser;

    /**
     *
     */
    private String html;

    /**
     *
     */
    private Map<String, TransformerMapping> transformerMapping;

    public Transformer(List<TransformerMapping> transformerMappingsList, String html, Parser parser) {
        this.transformerMapping = createTransformerMappingMap(transformerMappingsList);
        this.parser = parser;
        this.html = html;
    }


    public List<GradeEntry> transform() throws ParseException {
        List<GradeEntry> gradeEntries = new ArrayList<>();
        // get List to iterate through and respectively extract grade values
        NodeList nodeList = parser.parseToNodeList(transformerMapping.get(ITERATOR).getParseExpression(), html);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nNode = nodeList.item(i);
            // get Node as XML document -> so it must not created every time
            Document xmlDocument = parser.getNodeAsDocument(nNode);

            // create new GradeEntry and add all extracted values
            GradeEntry gradeEntry = new GradeEntry();
            gradeEntry.setExamId(getStringProperty(xmlDocument, EXAM_ID));
            gradeEntry.setName(getStringProperty(xmlDocument, NAME));
            gradeEntry.setSemester(getStringProperty(xmlDocument, SEMESTER));
            gradeEntry.setGrade(getDoubleProperty(xmlDocument, GRADE));
            gradeEntry.setState(getStringProperty(xmlDocument, STATE));
            gradeEntry.setCreditPoints(getDoubleProperty(xmlDocument, CREDIT_POINTS));


            gradeEntries.add(gradeEntry);
        }

        return gradeEntries;
    }

    private String getStringProperty(Document xmlDocument, String type) throws ParseException {
        String result = parser.parseToString(transformerMapping.get(type).getParseExpression(), xmlDocument).trim();

        return result;
    }

    private Double getDoubleProperty(Document xmlDocument, String type) throws ParseException {
        String result = parser.parseToString(transformerMapping.get(type).getParseExpression(), xmlDocument).trim();
        result = result.replace(',', '.');
        if (result.equals("")) {
           return null;
        }

        return Double.parseDouble(result);
    }


    private Map<String, TransformerMapping> createTransformerMappingMap(List<TransformerMapping> transformerMappings) {
        Map<String, TransformerMapping> map = new HashMap<>();
        for (TransformerMapping transformerMapping : transformerMappings) {
            map.put(transformerMapping.getName(), transformerMapping);
        }
        return map;
    }
}
