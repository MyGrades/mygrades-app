package de.mygrades.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mygrades.database.dao.GradeEntry;

/**
 * Helper class to create a map for semester->semesterNumber.
 */
public class SemesterMapper {

    /**
     * The pattern is used to select the semester string and year,
     * e.g. "Wintersemester 2013/2014" is splitted into "Wintersemester" and "2013".
     */
    private Pattern semesterPattern;

    public SemesterMapper() {
        semesterPattern = Pattern.compile("(^\\w+)\\s*([0-9]+)");
    }

    /**
     * Creates a map semester->semesterNumber based on a list of grade entries.
     * TODO: also adds an additional semester for the edit mode.
     *
     * @param gradeEntries list of grade entries
     * @return map semester->semesterNumber
     */
    public Map<String, Integer> getGradeEntrySemesterMapForEditMode(List<GradeEntry> gradeEntries) {
        Set<String> semesterSet = new HashSet<>();

        // create semester set
        for (GradeEntry gradeEntry : gradeEntries) {
            semesterSet.add(gradeEntry.getSemester());
        }

        return getGradeEntrySemesterMap(semesterSet);
    }

    /**
     * Creates a map semester->semesterNumber based on a set of semester strings.
     *
     * @param semestersSet set of semester strings
     * @return map semester->semesterNumber
     */
    public Map<String, Integer> getGradeEntrySemesterMap(Set<String> semestersSet) {
        // create List out of set to sort it
        List<String> semestersList = new ArrayList<>(semestersSet);

        // sort the list
        sortSemesterList(semestersList, true);

        // create Map Semester -> SemesterNumber for easy adding to GradeEntry
        Map<String, Integer> semesterSemesterNumberMap = new HashMap<>();
        for (int i = 0; i < semestersList.size(); i++) {
            semesterSemesterNumberMap.put(semestersList.get(i), i+1);
        }

        return semesterSemesterNumberMap;
    }

    /**
     * Sorts a given list of semester strings.
     *
     * @param semesterList list of semester strings
     * @param asc ascending or descending
     */
    public void sortSemesterList(List<String> semesterList, final boolean asc) {
        // sort semester to get the correct semester number
        Collections.sort(semesterList, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                Matcher matcher;
                String sem1 = "";
                String sem2 = "";
                Integer year1 = 0;
                Integer year2 = 0;

                // get Semester and Year from first String
                matcher = semesterPattern.matcher(s1);
                if (matcher.find()) { // Find first match
                    sem1 = matcher.group(1);
                    try {
                        year1 = Integer.parseInt(matcher.group(2));
                    } catch (NumberFormatException e) {
                        year1 = 0;
                    }
                }

                // get Semester and Year from second String
                matcher = semesterPattern.matcher(s2);
                if (matcher.find()) { // Find first match
                    sem2 = matcher.group(1);
                    try {
                        year2 = Integer.parseInt(matcher.group(2));
                    } catch (NumberFormatException e) {
                        year2 = 0;
                    }
                }

                // compare years -> if equal sem1 and sem2 (SoSe and WiSe) have to be compared
                if (asc) {
                    int compYears = year1.compareTo(year2);
                    if (compYears == 0) {
                        return sem1.compareTo(sem2);
                    }
                    return compYears;
                }  else {
                    int compYears = year2.compareTo(year1);
                    if (compYears == 0) {
                        return sem2.compareTo(sem1);
                    }
                    return compYears;
                }
            }
        });
    }

    /**
     * Updates each GradeEntry in gradeEntries with SemesterNumber.
     * This is calculated by the available Semester Strings given in semestersSet.
     *
     * @param gradeEntries GradeEntries which should get a SemesterNumber
     * @param semestersSet available
     */
    public void setGradeEntrySemesterNumber(List<GradeEntry> gradeEntries, Set<String> semestersSet) {
        Map<String, Integer> semesterSemesterNumberMap = getGradeEntrySemesterMap(semestersSet);

        for (GradeEntry gradeEntry : gradeEntries) {
            gradeEntry.setSemesterNumber(semesterSemesterNumberMap.get(gradeEntry.getSemester()));
        }
    }
}
