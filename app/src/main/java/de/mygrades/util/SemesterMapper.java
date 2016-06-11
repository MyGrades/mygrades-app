package de.mygrades.util;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mygrades.database.dao.GradeEntry;
import de.mygrades.main.core.SemesterTransformer;

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
     * It also appends a future semester and prepends a semester in the past (used in edit mode).
     *
     * @param gradeEntries list of grade entries
     * @return map semester->semesterNumber
     */
    public Map<String, Integer> getSemesterToNumberMap(List<GradeEntry> gradeEntries) {
        Set<String> semesterSet = new HashSet<>();

        // create set of semester strings
        for (GradeEntry gradeEntry : gradeEntries) {
            if (gradeEntry.getSemester() != null) {
                semesterSet.add(gradeEntry.getSemester());
            }
            if (gradeEntry.getModifiedSemester() != null) {
                semesterSet.add(gradeEntry.getModifiedSemester());
            }
        }

        // get sorted semester list
        List<String> sortedSemester = createConsecutiveSemesterList(semesterSet);

        // add a future semester at the end and a past semester to the beginning
        if (sortedSemester.size() > 0) {
            sortedSemester.add(getNextSemester(sortedSemester.get(sortedSemester.size() - 1)));
            sortedSemester.add(0, getPreviousSemester(sortedSemester.get(0)));
        } else {
            sortedSemester.add(createCurrentSemester());
        }

        return getGradeEntrySemesterMap(sortedSemester);
    }

    /**
     * Creates a consecutive list of semester strings based on an already existing semester set.
     * This set may have gaps between semesters, which are filled by this method.
     * The returned list is sorted (ascending).
     *
     * @param semesterSet set of semester strings
     * @return sorted, gapless list of semester strings
     */
    public List<String> createConsecutiveSemesterList(Set<String> semesterSet) {
        List<String> sortedSemester = new LinkedList<>(semesterSet);
        sortSemesterList(sortedSemester, true);

        ListIterator<String> it = sortedSemester.listIterator();
        while(it.hasNext()) {
            String currentSemester = it.next();
            String nextSemester = it.hasNext() ? it.next() : null;
            it.previous(); // go back

            if (nextSemester == null) break; // end of list

            if(!isNextSemester(currentSemester, nextSemester)) {
                String actualNextSemester = getNextSemester(currentSemester);
                it.add(actualNextSemester);
                it.previous(); // go back
            }
        }

        return sortedSemester;
    }

    /**
     * Checks if two semester are directly consecutive.
     *
     * @param currentSemester - current semester as string
     * @param nextSemester - next semester as string
     * @return true, if semester are consecutive
     */
    public boolean isNextSemester(String currentSemester, String nextSemester) {
        if (currentSemester == null || nextSemester == null) return false;

        Integer currentYear;
        Matcher matcher = semesterPattern.matcher(currentSemester);
        currentYear = matcher.find() ? parseInt(matcher.group(2)) : 0;

        Integer nextYear;
        matcher = semesterPattern.matcher(nextSemester);
        nextYear = matcher.find() ? parseInt(matcher.group(2)) : 0;

        // expect summer semester as next
        if (currentSemester.toLowerCase().startsWith("w")) {
            if (!nextSemester.toLowerCase().startsWith("s") || nextYear - currentYear != 1) {
                return false;
            }
        }

        // expect winter semester as next
        if (currentSemester.toLowerCase().startsWith("s")) {
            if (!nextSemester.toLowerCase().startsWith("w") || !nextYear.equals(currentYear)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the next semester after the given semester.
     *
     * @param currentSemester - current semester as string
     * @return next semester as string
     */
    public String getNextSemester(String currentSemester) {
        String newSemester;

        // get semester and year from current semester
        Matcher matcher = semesterPattern.matcher(currentSemester);

        String semester = "";
        Integer year = 0;
        if (matcher.find()) { // Find first match
            semester = matcher.group(1);
            year = parseInt(matcher.group(2));
        }

        // if extractedSemester starts with w -> Wintersemester, next is Sommersemester
        if (semester.toLowerCase().startsWith("w")) {
            newSemester = SemesterTransformer.SEMESTER_SS + (year + 1);
        } else {
            newSemester = SemesterTransformer.SEMESTER_WS + year + "/" + (year+1);
        }

        return newSemester;
    }

    /**
     * Get the previous semester before the given semester.
     *
     * @param currentSemester - current semester as string
     * @return previous semester as string
     */
    public String getPreviousSemester(String currentSemester) {
        String newSemester;

        // get semester and year from current semester
        Matcher matcher = semesterPattern.matcher(currentSemester);

        String semester = "";
        Integer year = 0;
        if (matcher.find()) {
            semester = matcher.group(1);
            year = parseInt(matcher.group(2));
        }

        // if extractedSemester starts with w -> Wintersemester, previous is Sommersemester
        if (semester.toLowerCase().startsWith("w")) {
            newSemester = SemesterTransformer.SEMESTER_SS + (year);
        } else {
            newSemester = SemesterTransformer.SEMESTER_WS + (year - 1) + "/" + (year);
        }

        return newSemester;
    }

    /**
     * Creates a map semester->semesterNumber based on a list of semester strings.
     *
     * @param semesterList list of semester strings
     * @return map semester->semesterNumber
     */
    private Map<String, Integer> getGradeEntrySemesterMap(List<String> semesterList) {
        sortSemesterList(semesterList, true);

        // create Map Semester -> SemesterNumber for easy adding to GradeEntry
        Map<String, Integer> semesterSemesterNumberMap = new HashMap<>();
        for (int i = 0; i < semesterList.size(); i++) {
            semesterSemesterNumberMap.put(semesterList.get(i), i+1);
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
                    year1 = parseInt(matcher.group(2));
                }

                // get Semester and Year from second String
                matcher = semesterPattern.matcher(s2);
                if (matcher.find()) { // Find first match
                    sem2 = matcher.group(1);
                    year2 = parseInt(matcher.group(2));
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
     * Returns the string of the actual first semester, based on the list of grade entries
     * and the semester->semesterNumber map.
     * This is necessary, because its possible that the map contains semester in the past,
     * without actual grade entries.
     *
     * @param gradeEntries - list of grade entries
     * @param semesterNumberMap - semester to semesterNumber map
     * @return string of actual first semester
     */
    public String getActualFirstSemester(List<GradeEntry> gradeEntries, Map<String, Integer> semesterNumberMap) {
        String actualFirstSemester = null;
        Integer actualFirstSemesterNumber = null;

        // find grade entry with lowest semester
        for(GradeEntry gradeEntry : gradeEntries) {
            String currentSemester = gradeEntry.getModifiedSemester() == null ? gradeEntry.getSemester() : gradeEntry.getModifiedSemester();
            int currentSemesterNumber = semesterNumberMap.get(currentSemester);
            if (actualFirstSemesterNumber == null) {
                actualFirstSemesterNumber = currentSemesterNumber;
                actualFirstSemester = currentSemester;
            } else if (currentSemesterNumber < actualFirstSemesterNumber) {
                actualFirstSemester = currentSemester;
                actualFirstSemesterNumber = currentSemesterNumber;
            }
        }

        return actualFirstSemester;
    }

    /**
     * Creates a semester string based on the current year and month.
     * April to september is a summer semester, and october till march winter semester.
     *
     * @return semester string
     */
    private String createCurrentSemester() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        return getSemesterByYearAndMonth(year, month);
    }

    /**
     * Creates a semester string based on the current year and month.
     * April to september is a summer semester, and october till march winter semester.
     *
     * @param year current year
     * @param month current month
     * @return semester string
     */
    public String getSemesterByYearAndMonth(int year, int month) {
        if (month >= 0 && month <= 3) {
            return "Wintersemester " + (year - 1) + "/" + year;
        } else if (month >= 4 && month <= 9) {
            return "Sommersemester " + year;
        } else if (month >= 10 && month <= 12) {
            return "Wintersemester " + year + "/" + (year + 1);
        }

        return null;
    }

    private Integer parseInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
