package de.mygrades.util;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.database.dao.GradeEntry;
import de.mygrades.view.adapter.model.GradeItem;
import de.mygrades.view.adapter.model.GradesAdapterItem;

/**
 * Calculates the average grade and credit points sum for a list of GradeItems.
 */
public class AverageCalculator {
    private float average;
    private float creditPointsSum;

    /**
     * Calculates the average grade and credit points sum for a list of GradeItems.
     *
     * @param items - list of GradeAdapterItems
     */
    public void calculate(List<? extends GradesAdapterItem> items) {
        average = 0f;
        creditPointsSum = 0f;
        float creditPointsSumForAverage = 0f; // some grade_entries may have credit points, but no grade

        // iterate over items, count credit points and calculate average
        for(GradesAdapterItem item : items) {
            if (!(item instanceof GradeItem))
                continue;

            GradeItem grade = (GradeItem) item;
            float actCreditPoints = (grade.getCreditPoints() == null ? 0f : grade.getCreditPoints());
            creditPointsSum += actCreditPoints;
            if (grade.getGrade() != null && grade.getGrade() > 0) {
                creditPointsSumForAverage += actCreditPoints;
            }
            average += (grade.getGrade() == null ? 0f : grade.getGrade() * actCreditPoints);
        }
        average = creditPointsSumForAverage > 0 ? average/creditPointsSumForAverage : 0f;
    }

    /**
     * Converts grade entries to grade items and calculates the average and credit points sum.
     *
     * @param gradeEntries - list of grade entries
     */
    public void calculateFromGradeEntries(List<GradeEntry> gradeEntries) {
        List<GradeItem> gradeItems = new ArrayList<>();
        for(GradeEntry entry : gradeEntries) {
            gradeItems.add(new GradeItem(entry));
        }
        calculate(gradeItems);
    }

    public float getAverage() {
        return average;
    }

    public float getCreditPointsSum() {
        return creditPointsSum;
    }
}
