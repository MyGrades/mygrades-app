package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.main.events.StatisticsEvent;
import de.mygrades.util.AverageCalculator;
import de.mygrades.util.SemesterMapper;
import de.mygrades.view.adapter.model.GradeItem;
import de.mygrades.view.adapter.model.SemesterItem;

/**
 * StatisticsProcessor creates relevant statistic data and posts an event to subscribers.
 */
public class StatisticsProcessor extends BaseProcessor {
    private SharedPreferences prefs;
    private SemesterMapper semesterMapper;
    private Map<String, Integer> semesterNumberMap;

    public StatisticsProcessor(Context context) {
        super(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        semesterMapper = new SemesterMapper();
    }

    /**
     * Creates all statistics data and posts a StatisticsEvent afterwards.
     */
    public void getStatistics() {
        List<GradeEntry> gradeEntries = daoSession.getGradeEntryDao().loadAll();
        semesterNumberMap = semesterMapper.getSemesterToNumberMap(gradeEntries);
        String actualFirstSemester = semesterMapper.getActualFirstSemester(gradeEntries, semesterNumberMap);
        StatisticsEvent statisticsEvent = new StatisticsEvent(semesterNumberMap, actualFirstSemester);

        // set average and credit points sum
        boolean simpleWeighting = prefs.getBoolean(context.getString(R.string.pref_key_simple_weighting), false);
        AverageCalculator averageCalculator = new AverageCalculator(simpleWeighting);
        averageCalculator.calculateFromGradeEntries(gradeEntries);
        statisticsEvent.setAverage(averageCalculator.getAverage());
        statisticsEvent.setCreditPoints(averageCalculator.getCreditPointsSum());

        // set grade count
        statisticsEvent.setGradeCount(gradeEntries.size());

        // set study progress
        String maxCreditPointsAsString = prefs.getString(context.getString(R.string.pref_key_max_credit_points), "180");
        int maxCreditPoints = parseInt(maxCreditPointsAsString, 180);
        float studyProgress = (averageCalculator.getCreditPointsSum() / maxCreditPoints) * 100;
        statisticsEvent.setStudyProgress(studyProgress);

        // create map for diagrams
        List<SemesterItem> semesterItems = getSemesterItems(gradeEntries);
        statisticsEvent.setSemesterItems(semesterItems);

        // set credit points per semester
        statisticsEvent.setCreditPointsPerSemester(getCreditPointsPerSemester(semesterItems));

        // set grade distribution
        statisticsEvent.setGradeDistribution(getGradeDistribution(gradeEntries));

        EventBus.getDefault().post(statisticsEvent);
    }

    /**
     * Creates a list of SemesterItems with its grades, sorted by semester number.
     *
     * @param gradeEntries - list of grade entries
     * @return list of SemesterItems
     */
    private List<SemesterItem> getSemesterItems(List<GradeEntry> gradeEntries) {
        boolean simpleWeighting = prefs.getBoolean(context.getString(R.string.pref_key_simple_weighting), false);

        Map<String, SemesterItem> semesterItemMap = new HashMap<>();
        for(String semester : semesterNumberMap.keySet()) {
            SemesterItem item = new SemesterItem(simpleWeighting);
            item.setSemester(semester);
            semesterItemMap.put(semester, item);
        }

        for (GradeEntry gradeEntry : gradeEntries) {
            String semester = gradeEntry.getModifiedSemester() == null ? gradeEntry.getSemester() : gradeEntry.getModifiedSemester();
            GradeItem gradeItem = new GradeItem(gradeEntry);

            SemesterItem item = semesterItemMap.get(semester);
            item.setSemester(semester);
            item.addGrade(gradeItem, false);
        }

        List<SemesterItem> semesterItems = new ArrayList<>(semesterItemMap.values());
        Collections.sort(semesterItems, new Comparator<SemesterItem>() {
            @Override
            public int compare(SemesterItem first, SemesterItem second) {
                return semesterNumberMap.get(first.getSemester()) - semesterNumberMap.get(second.getSemester());
            }
        });

        // remove empty semester at beginning and end of list
        stripSemesterItems(semesterItems);

        // calculate averages and credit points per semester
        for (SemesterItem semesterItem : semesterItems) {
            semesterItem.update();
        }

        return semesterItems;
    }

    /**
     * Removes all empty semester items at the beginning and end of the list.
     *
     * @param semesterItems list of semester items
     */
    private void stripSemesterItems(List<SemesterItem> semesterItems) {
        ListIterator<SemesterItem> it = semesterItems.listIterator();
        while(it.hasNext()) {
            SemesterItem semesterItem = it.next();
            if (semesterItem.getGrades().size() == 0) {
                it.remove();
            } else {
                break;
            }
        }

        it = semesterItems.listIterator(semesterItems.size());
        while(it.hasPrevious()) {
            SemesterItem semesterItem = it.previous();
            if (semesterItem.getGrades().size() == 0) {
                it.remove();
            } else {
                break;
            }
        }
    }

    /**
     * Calculates the average credit points per semester.
     *
     * @param semesterItems list of semester items
     * @return average credit points per semester
     */
    private float getCreditPointsPerSemester(List<SemesterItem> semesterItems) {
        float creditPointsPerSemester = 0;
        for(SemesterItem item : semesterItems) {
            creditPointsPerSemester += item.getCreditPoints();
        }

        if (semesterItems.size() > 0) {
            creditPointsPerSemester /= semesterItems.size();
        }

        return creditPointsPerSemester;
    }

    /**
     * Creates an integer array with the grade distribution.
     * [1.0 -1.3, 1.7-2.3, 2.7-3.3, 3.7 - 4.0, 4.3 - 5.0, others]
     *
     * @param gradeEntries list of grade entries
     * @return integer array with grade distribution
     */
    private int[] getGradeDistribution(List<GradeEntry> gradeEntries) {
        int[] gradeDistribution = new int[6];

        for (GradeEntry gradeEntry : gradeEntries) {
            Double grade = gradeEntry.getGrade();
            grade = gradeEntry.getModifiedGrade() == null ? grade : gradeEntry.getModifiedGrade();

            if (grade != null && grade > 0) {
                int gradeAsInt = (int) Math.round(grade);
                if (gradeAsInt > 0) {
                    gradeDistribution[gradeAsInt - 1] += 1;
                }
            } else {
                gradeDistribution[5] += 1;
            }
        }

        return gradeDistribution;
    }

    /**
     * Safely cast string to integer and ignore NumberFormatException.
     * In case of an exception, the default value will be returned.
     *
     * @param value  string to cast
     * @param defaultValue - default value if an exception is raised
     * @return integer
     */
    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
