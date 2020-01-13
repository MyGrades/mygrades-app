package dh.mygrades.main.core;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dh.mygrades.database.dao.GradeEntry;
import dh.mygrades.database.dao.Overview;
import dh.mygrades.database.dao.Rule;
import dh.mygrades.database.dao.TransformerMapping;
import dh.mygrades.util.SemesterMapper;
import dh.mygrades.util.exceptions.ParseException;

/**
 * Creates GradeEntry Objects from given HTML with given TransformerMappings.
 */
public class Transformer {
    // mapping from TransformerMapping Name -> GradeEntry Property
    private static final String ITERATOR = "iterator";
    public static final String EXAM_ID = "exam_id";
    public static final String NAME = "name";
    private static final String SEMESTER = "semester";
    private static final String GRADE = "grade";
    private static final String STATE = "state";
    private static final String CREDIT_POINTS = "credit_points";
    private static final String ANNOTATION = "annotation";
    public static final String ATTEMPT = "attempt";
    private static final String EXAM_DATE = "exam_date";
    private static final String TESTER = "tester";
    private static final String OVERVIEW_POSSIBLE = "overview_possible";

    // mapping from TransformerMapping Name -> Overview Property
    private static final String OVERVIEW_SECTION_1 = "overview_section1";
    private static final String OVERVIEW_SECTION_2 = "overview_section2";
    private static final String OVERVIEW_SECTION_3 = "overview_section3";
    private static final String OVERVIEW_SECTION_4 = "overview_section4";
    private static final String OVERVIEW_SECTION_5 = "overview_section5";
    private static final String OVERVIEW_PARTICIPANTS = "overview_participants";
    private static final String OVERVIEW_AVERAGE = "overview_average";

    // multiple tables of grades
    public static final String MT_SEMESTER_OPTIONS = "mt_semester_options";
    public static final String MT_FORM_URL = "mt_form_url";
    public static final String MT_SEMESTER_STRING = "mt_semester_string";

    /**
     * Parser to extract values into Models.
     */
    private Parser parser;

    /**
     * HTML from which the data gets extracted.
     */
    private String html;

    /**
     * Map of String -> TransformerMapping for easy access.
     */
    private Map<String, TransformerMapping> transformerMapping;
    /**
     * Map of String -> TransformerMapping for easy access to overview Mappings.
     */
    private Map<String, List<TransformerMapping>> transformerMappingOverviewSection;

    /**
     * Rule getting transformed
     */
    private Rule rule;

    /**
     * SemesterTransformer to transform the specific semester pattern.
     */
    private SemesterTransformer semesterTransformer;

    /**
     * SemesterMapper is used to create a map for semester->semesterNumber.
     */
    private SemesterMapper semesterMapper;

    public Transformer(Rule rule, String html, Parser parser) {
        this.rule = rule;
        this.parser = parser;
        this.html = html;
        this.semesterTransformer = new SemesterTransformer(rule);
        this.semesterMapper = new SemesterMapper();
        // initialize transformerMapping and transformerMappingOverviewSection
        createTransformerMappingMap(rule.getTransformerMappings());
    }

    /**
     * Creates Overview object from HTML via
     * xPath expressions from TransformerMapping.
     *
     * @return Overview
     * @throws ParseException if something goes wrong at parsing
     */
    public Overview transformOverview(Double userGrade) throws ParseException {
        // get Node as XML document -> so it must not created every time
        Document xmlDocument = parser.getStringAsDocument(html);

        // create Pattern for Integer Extraction
        Pattern pattern = Pattern.compile("[0-9]+");

        // extract Overview values
        Overview overview = new Overview();
        overview.setSection1(getIntegerPropertyOverview(xmlDocument, OVERVIEW_SECTION_1, pattern));
        overview.setSection2(getIntegerPropertyOverview(xmlDocument, OVERVIEW_SECTION_2, pattern));
        overview.setSection3(getIntegerPropertyOverview(xmlDocument, OVERVIEW_SECTION_3, pattern));
        overview.setSection4(getIntegerPropertyOverview(xmlDocument, OVERVIEW_SECTION_4, pattern));
        overview.setSection5(getIntegerPropertyOverview(xmlDocument, OVERVIEW_SECTION_5, pattern));
        overview.setParticipants(getIntegerProperty(xmlDocument, OVERVIEW_PARTICIPANTS));
        overview.setAverage(getDoubleProperty(xmlDocument, OVERVIEW_AVERAGE));
        overview.setUserSection((int) Math.round(userGrade));

        // calculate participants from the sections if it is not given
        if (overview.getParticipants() == null) {
            int participants = overview.getSection(1) + overview.getSection(2) + overview.getSection(3)
                    + overview.getSection(4) + overview.getSection(5);
            overview.setParticipants(participants);
        }

        // calculate average from the sections and the participants if it is not given
        if (overview.getAverage() == null) {
            if (overview.getParticipants() != null && overview.getParticipants() != 0) {
                double average = (overview.getSection(1) + overview.getSection(2) * 2 + overview.getSection(3) * 3
                        + overview.getSection(4) * 4 + overview.getSection(5) * 5) / (double)overview.getParticipants();
                overview.setAverage(average);
            }

        }

        return overview;
    }

    /**
     * Creates GradeEntry objects for all matching elements from html via
     * xPath expression 'iterator' from TransformerMapping.
     *
     * @return List of extracted GradeEntries
     * @throws ParseException if something goes wrong at parsing
     */
    public List<GradeEntry> transform() throws ParseException {
        return this.transform(null);
    }

    /**
     * Creates GradeEntry objects for all matching elements from html via
     * xPath expression 'iterator' from TransformerMapping.
     * The property semester of a grade entry is respectively set to globalSemester
     * for all created grade entries if it is not null.
     *
     * @param globalSemester A global semester for all created grade entries. If it is null, it's ignored.
     * @return List of extracted GradeEntries
     * @throws ParseException if something goes wrong at parsing
     */
    private List<GradeEntry> transform(String globalSemester) throws ParseException {
        List<GradeEntry> gradeEntries = new ArrayList<>();

        // get List to iterate through and respectively extract GradeEntry values
        NodeList nodeList = parser.parseToNodeList(transformerMapping.get(ITERATOR).getParseExpression(), html);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nNode = nodeList.item(i);
            // get Node as XML document -> so it must not created every time
            Document xmlDocument = parser.getNodeAsDocument(nNode);

            // create new GradeEntry and add all extracted values
            GradeEntry gradeEntry = new GradeEntry();
            gradeEntry.setExamId(getStringProperty(xmlDocument, EXAM_ID));
            gradeEntry.setName(getStringProperty(xmlDocument, NAME));
            // name is mandatory, so ignore this entry if it has none
            if (gradeEntry.getName() == null) {
                continue;
            }

            // extract semester from line if there is no global semester given
            String semesterRaw = globalSemester == null ? getStringProperty(xmlDocument, SEMESTER) : globalSemester;
            String semester = semesterTransformer.calculateGradeEntrySemester(semesterRaw);
            // ignore entry if there could no semester determined
            if (semester == null) {
                continue;
            }
            gradeEntry.setSemester(semester);

            gradeEntry.setGrade(getDoubleProperty(xmlDocument, GRADE, rule.getGradeFactor()));
            gradeEntry.setState(getStringProperty(xmlDocument, STATE));
            gradeEntry.setCreditPoints(getDoubleProperty(xmlDocument, CREDIT_POINTS));
            gradeEntry.setAnnotation(getStringProperty(xmlDocument, ANNOTATION));
            gradeEntry.setAttempt(getStringProperty(xmlDocument, ATTEMPT));
            gradeEntry.setExamDate(getStringProperty(xmlDocument, EXAM_DATE));
            gradeEntry.setTester(getStringProperty(xmlDocument, TESTER));
            gradeEntry.setOverviewPossible(getBooleanProperty(xmlDocument, OVERVIEW_POSSIBLE));

            // set default weight
            gradeEntry.setWeight(1.0);

            // update hash, used as primary key
            gradeEntry.updateHash();

            // add GradeEntry to list
            gradeEntries.add(gradeEntry);
        }

        return gradeEntries;
    }

    /**
     * Creates GradeEntry objects for all matching elements from respectively html table out of
     * resultTables via xPath expression 'iterator' from TransformerMapping.
     * The property semester of a grade entry is respectively set to the key of the map entry
     *
     * @param resultTables Map semester -> html table of grades
     * @return List of extracted GradeEntries
     * @throws ParseException if something goes wrong at parsing
     */
    public List<GradeEntry> transformMultipleTables(Map<String, String> resultTables) throws ParseException {
        List<GradeEntry> gradeEntries = new ArrayList<>();

        // extract grades from each html table and merge them into one list
        for (Entry<String, String> entry : resultTables.entrySet()) {
            // set html from which the grades get extracted
            html = entry.getValue();
            // extract grades and add to list
            List<GradeEntry> test = transform(entry.getKey());
            gradeEntries.addAll(test);
        }

        return gradeEntries;
    }

    /**
     * Gets the value from Document determined by type of TransformerMapping as String.
     *
     * @param xmlDocument Document which should get parsed
     * @param type Type of TransformerMapping regarding to GradeEntry
     * @return extracted value as String
     * @throws ParseException if something goes wrong at parsing
     */
    private String getStringProperty(Document xmlDocument, String type) throws ParseException {
        TransformerMapping transformerMappingVal = transformerMapping.get(type);
        if (transformerMappingVal == null) {
            return null;
        }

        String parseResult = parser.parseToString(transformerMappingVal.getParseExpression(), xmlDocument);
        parseResult = trimAdvanced(parseResult);
        return parseResult.equals("") ? null : parseResult;
    }

    /**
     * Gets the value from Document determined by type of TransformerMapping as String.
     *
     * @param xmlDocument Document which should get parsed
     * @param type Type of TransformerMapping regarding to GradeEntry
     * @return extracted value as String
     * @throws ParseException if something goes wrong at parsing
     */
    private boolean getBooleanProperty(Document xmlDocument, String type) throws ParseException {
        TransformerMapping transformerMappingVal = transformerMapping.get(type);
        if (transformerMappingVal == null) {
            return false;
        }
        Boolean parseResult = parser.parseToBoolean(transformerMappingVal.getParseExpression(), xmlDocument);

        return parseResult == null ? false : parseResult;
    }

    /**
     * Gets the value from Document determined by type of TransformerMapping as Double.
     *
     * @param xmlDocument Document which should get parsed
     * @param type Type of TransformerMapping regarding to GradeEntry
     * @param factor A factor can be used to multiply the double value
     * @return extracted value as Double
     * @throws ParseException if something goes wrong at parsing
     */
    private Double getDoubleProperty(Document xmlDocument, String type, Double factor) throws ParseException {
        TransformerMapping transformerMappingVal = transformerMapping.get(type);
        if (transformerMappingVal == null) {
            return null;
        }

        Double property;
        String result = parser.parseToString(transformerMappingVal.getParseExpression(), xmlDocument);
        result = trimAdvanced(result);
        result = result.replace(',', '.');

        // if cannot parse to Double -> return null
        try {
            property = Double.parseDouble(result);
        } catch (NumberFormatException e) {
            return null;
        }

        // if factor is given -> multiply
        if (factor != null) {
            property = property * factor;
        }

        return property;
    }

    /**
     * Gets the value from Document determined by type of TransformerMapping as Double.
     *
     * @param xmlDocument Document which should get parsed
     * @param type Type of TransformerMapping regarding to GradeEntry
     * @return extracted value as Double
     * @throws ParseException if something goes wrong at parsing
     */
    private Double getDoubleProperty(Document xmlDocument, String type) throws ParseException {
        return getDoubleProperty(xmlDocument, type, null);
    }

    /**
     * Gets the value for overview_section* from Document determined by type of TransformerMapping as Integer.
     * The integers are extracted from string via Regex.
     *
     * @param xmlDocument Document which should get parsed
     * @param type Type of TransformerMapping regarding to Overview
     * @param pattern Regex pattern of how to extract Integer
     * @return extracted value as Integer
     * @throws ParseException if something goes wrong at parsing
     */
    private Integer getIntegerPropertyOverview(Document xmlDocument, String type, Pattern pattern) throws ParseException {
        List<TransformerMapping> transformerMappingList = transformerMappingOverviewSection.get(type);
        Integer propertySum = 0;
        Integer property;

        // iterate all transformerMappings for specific section and add up results
        for (TransformerMapping tsMapping : transformerMappingList) {
            property = extractIntegerFromDoc(xmlDocument, tsMapping, pattern);
            if (property != null) {
                propertySum += property;
            }
        }
        return propertySum;
    }

    /**
     * Gets the value from Document determined by type of TransformerMapping as Integer.
     *
     * @param xmlDocument Document which should get parsed
     * @param type Type of TransformerMapping regarding to Overview
     * @return extracted value as Integer
     * @throws ParseException if something goes wrong at parsing
     */
    private Integer getIntegerProperty(Document xmlDocument, String type) throws ParseException {
        return getIntegerProperty(xmlDocument, type, null);
    }

    /**
     * Gets the value from Document determined by type of TransformerMapping as Integer.
     * The integer is extracted from string via Regex.
     *
     * @param xmlDocument Document which should get parsed
     * @param type Type of TransformerMapping regarding to Overview
     * @param pattern Regex pattern of how to extract Integer
     * @return extracted value as Integer
     * @throws ParseException if something goes wrong at parsing
     */
    private Integer getIntegerProperty(Document xmlDocument, String type, Pattern pattern) throws ParseException {
        TransformerMapping transformerMappingVal = transformerMapping.get(type);
        return extractIntegerFromDoc(xmlDocument, transformerMappingVal, pattern);
    }

    /**
     * Gets the value from Document determined by TransformerMapping as Integer.
     * The integer is extracted from string via Regex.
     *
     * @param xmlDocument Document which should get parsed
     * @param transformerMappingVal TransformerMapping -> which value should get extracted
     * @param pattern  Regex pattern of how to extract Integer
     * @return extracted value as Integer
     * @throws ParseException if something goes wrong at parsing
     */
    private Integer extractIntegerFromDoc(Document xmlDocument, TransformerMapping transformerMappingVal, Pattern pattern) throws ParseException {
        if (transformerMappingVal == null) {
            return null;
        }

        Integer property;
        String result = parser.parseToString(transformerMappingVal.getParseExpression(), xmlDocument);
        result = trimAdvanced(result);

        if (pattern != null) {
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                result = matcher.group(0);
            }
        }

        // if cannot parse to Integer -> return null
        try {
            property = Integer.parseInt(result);
        } catch (NumberFormatException e) {
            return null;
        }
        return property;
    }

    /**
     * Creates HashMaps for TransformerMappings for easy access.
     *
     * @param transformerMappings which are put into Maps transformerMapping or transformerMappingOverviewSection
     */
    private void createTransformerMappingMap(List<TransformerMapping> transformerMappings) {
        transformerMapping = new HashMap<>();
        transformerMappingOverviewSection = new HashMap<>();

        // iterate all transformerMappings and add to respective list
        for (TransformerMapping tsMapping : transformerMappings) {
            String tsName = tsMapping.getName();
            if (tsName == null) {
                continue;
            }

            if (tsName.startsWith("overview_section")) {
                List<TransformerMapping> list = transformerMappingOverviewSection.get(tsName);
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(tsMapping);
                    transformerMappingOverviewSection.put(tsName, list);
                } else {
                    list.add(tsMapping);
                }
            } else {
                transformerMapping.put(tsMapping.getName(), tsMapping);
            }
        }
    }

    /**
     * Creates HashMap for TransformerMappings for easy access.
     * OverviewSections are ignored.
     *
     * @param transformerMappings which are put into Maps transformerMapping or transformerMappingOverviewSection
     * @return Map of Name of TransformerMapping -> TransformerMapping
     */
    public static Map<String, TransformerMapping> getTransformermappingMap(List<TransformerMapping> transformerMappings) {
        Map<String, TransformerMapping> transformerMappingMap = new HashMap<>();

        // iterate all transformerMappings and add to map
        for (TransformerMapping tsMapping : transformerMappings) {
            String tsName = tsMapping.getName();
            if (tsName == null) {
                continue;
            }

            if (tsName.startsWith("overview_section")) {
                continue;
            } else {
                transformerMappingMap.put(tsMapping.getName(), tsMapping);
            }
        }

        return transformerMappingMap;
    }

    /**
     * Removes whitespace from the start and end of a string
     * including special html characters like &nbsp;.
     * Method taken from: http://stackoverflow.com/a/31624585/5115653
     *
     * @param value string to trim
     * @return trimmed string
     */
    private String trimAdvanced(String value) {
        if (value == null) {
            return "";
        }

        int strLength = value.length();
        int len = value.length();
        int st = 0;
        char[] val = value.toCharArray();

        if (strLength == 0) {
            return "";
        }

        while ((st < len) && (val[st] <= ' ') || (val[st] == '\u00A0')) {
            st++;
            if (st == strLength) {
                break;
            }
        }
        while ((st < len) && (val[len - 1] <= ' ') || (val[len - 1] == '\u00A0')) {
            len--;
            if (len == 0) {
                break;
            }
        }

        return (st > len) ? "" : ((st > 0) || (len < strLength)) ? value.substring(st, len) : value;
    }
}
