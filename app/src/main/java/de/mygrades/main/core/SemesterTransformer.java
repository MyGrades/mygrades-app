package de.mygrades.main.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mygrades.database.dao.Rule;

/**
 * Class to transform from html extracted semester strings to an actual semester string
 * with multiple patterns.
 */
public class SemesterTransformer {
    public static final String SEMESTER_FORMAT_SEMESTER = "semester";
    public static final String SEMESTER_FORMAT_SEMESTER_REVERSED = "semester_reversed";
    public static final String SEMESTER_FORMAT_DATE = "date";

    private static final String SEMESTER_WS = "Wintersemester ";
    private static final String SEMESTER_SS = "Sommersemester ";

    /**
     * Compile Pattern to extract the Semester and Year of field extracted from html.
     */
    private Pattern semesterPattern;
    private Rule rule;

    public SemesterTransformer(Rule rule) {
        this.rule = rule;
        this.semesterPattern = Pattern.compile(rule.getSemesterPattern());
    }

    /**
     * Calculates the GradeEntry semester property to "Wintersemester" or "Sommersemester".
     * Different types are possible which is determined through the rule.
     *
     * @param origSemester original string extracted out of html
     * @return formatted semester string
     */
    public String calculateGradeEntrySemester(String origSemester) {
        // if origSemester is null -> return null so this entry will get ignored
        if (origSemester == null) {
            return null;
        }

        if (rule.getSemesterFormat().equals(SEMESTER_FORMAT_SEMESTER)) {
            return calculateFromSemesterFormat(origSemester);
        } else if (rule.getSemesterFormat().equals(SEMESTER_FORMAT_SEMESTER_REVERSED)) {
            return calculateFromSemesterReversedFormat(origSemester);
        } else if (rule.getSemesterFormat().equals(SEMESTER_FORMAT_DATE)) {
            return calculateFromDateFormat(origSemester);
        }

        return "";
    }

    /**
     * Calculates the semester from a semester format, eg. 'WiSe 12/13'.
     *
     * @param origSemester original string extracted out of html
     * @return formatted semester string
     */
    private String calculateFromSemesterFormat(String origSemester) {
        String extractedSemester = "";
        Integer extractedYear = 0;
        String resultSemester = "";

        // match pattern to origSemester and get Year and Semester String
        Matcher matcher = semesterPattern.matcher(origSemester);
        if (matcher.find()) { // Find first match
            extractedSemester = matcher.group(1);

            try {
                extractedYear = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                extractedYear = 0;
            }
        }

        extractedYear = correctYear(extractedYear);

        // if extractedSemester starts with w -> Wintersemester
        if (extractedSemester.toLowerCase().startsWith("w")) {
            resultSemester += SEMESTER_WS + extractedYear + "/" + (extractedYear+1);
        } else {
            resultSemester += SEMESTER_SS + extractedYear;
        }

        return resultSemester;
    }

    /**
     * Calculates the semester from a reversed semester format, eg. '2015WS'.
     *
     * @param origSemester original string extracted out of html
     * @return formatted semester string
     */
    private String calculateFromSemesterReversedFormat(String origSemester) {
        String extractedSemester = "";
        Integer extractedYear = 0;
        String resultSemester = "";

        // match pattern to origSemester and get Year and Semester String
        Matcher matcher = semesterPattern.matcher(origSemester);
        if (matcher.find()) { // Find first match
            extractedSemester = matcher.group(2);

            try {
                extractedYear = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                extractedYear = 0;
            }
        }

        // get year in correct format
        extractedYear = correctYear(extractedYear);

        // if extractedSemester starts with w -> Wintersemester
        if (extractedSemester.toLowerCase().startsWith("w")) {
            resultSemester += SEMESTER_WS + extractedYear + "/" + (extractedYear+1);
        } else {
            resultSemester += SEMESTER_SS + extractedYear;
        }

        return resultSemester;
    }

    /**
     * Calculates the semester from a date format, eg. '27.07.2015'.
     *
     * @param origSemester original string extracted out of html
     * @return formatted semester string
     */
    private String calculateFromDateFormat(String origSemester) {
        Integer extractedYear = 0;
        Integer extractedMonth = 0;
        String resultSemester = "";

        // match pattern to origSemester and get Year and Month
        Matcher matcher = semesterPattern.matcher(origSemester);
        if (matcher.find()) { // Find first match
            try {
                extractedMonth = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                extractedMonth = 0;
            }
            try {
                extractedYear = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                extractedYear = 0;
            }
        }

        extractedYear = correctYear(extractedYear);

        // calculate Semester string depending on month and year
        if (extractedMonth >= rule.getSemesterStartSummer() && extractedMonth < rule.getSemesterStartWinter()) { // Sommersemester (04-09)
            resultSemester += SEMESTER_SS + extractedYear;
        } else { // Wintersemester
            if (extractedMonth >= rule.getSemesterStartWinter()) { // first part of Wintersemester (10-12)
                resultSemester += SEMESTER_WS + extractedYear + "/" + (extractedYear+1);
            } else { // second part of Wintersemester (01-03)
                resultSemester += SEMESTER_WS + (extractedYear-1) + "/" + extractedYear;
            }
        }

        return resultSemester;
    }

    /**
     * Corrects a given year if its not in 20xx notation.
     *
     * @param extractedYear - extracted year
     * @return corrected year
     */
    private Integer correctYear(Integer extractedYear) {
        if (extractedYear.toString().length() < 4) {
            extractedYear = extractedYear + 2000;
        }
        return extractedYear;
    }
}
