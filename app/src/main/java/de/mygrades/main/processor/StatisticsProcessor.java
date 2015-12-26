package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.main.events.StatisticsEvent;
import de.mygrades.util.AverageCalculator;

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

        EventBus.getDefault().post(statisticsEvent);
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
