package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.main.events.StatisticsEvent;
import de.mygrades.util.AverageCalculator;
import de.mygrades.view.adapter.model.GradeItem;
import de.mygrades.view.adapter.model.SemesterItem;

/**
 * StatisticsProcessor creates relevant statistic data and posts an event to subscribers.
 */
public class StatisticsProcessor extends BaseProcessor {
    private SharedPreferences prefs;

    public StatisticsProcessor(Context context) {
        super(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    /**
     * Creates all statistics data and posts a StatisticsEvent afterwards.
     */
    public void getStatistics() {
        List<GradeEntry> gradeEntries = daoSession.getGradeEntryDao().loadAll();
        StatisticsEvent statisticsEvent = new StatisticsEvent();

        // set average and credit points sum
        AverageCalculator averageCalculator = new AverageCalculator();
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

        EventBus.getDefault().post(statisticsEvent);
    }

    /**
     * Creates a list of SemesterItems with its grades, sorted by semester number.
     *
     * @param gradeEntries - list of grade entries
     * @return list of SemesterItems
     */
    private List<SemesterItem> getSemesterItems(List<GradeEntry> gradeEntries) {
        Map<Integer, SemesterItem> semesterItemMap = new HashMap<>();

        // build map
        for(GradeEntry gradeEntry : gradeEntries) {
            int semesterNumber = gradeEntry.getSemesterNumber();
            GradeItem gradeItem = new GradeItem(gradeEntry);

            if (!semesterItemMap.containsKey(semesterNumber)) {
                semesterItemMap.put(semesterNumber, new SemesterItem());
            }

            SemesterItem item = semesterItemMap.get(semesterNumber);
            item.setSemesterNumber(semesterNumber);
            item.addGrade(gradeItem, false);
        }

        // convert to list and sort
        List<SemesterItem> semesterItems = new ArrayList<>(semesterItemMap.values());
        Collections.sort(semesterItems);

        // calculate averages and credit points per semester
        for (SemesterItem semesterItem : semesterItems) {
            semesterItem.update();
        }

        return semesterItems;
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
