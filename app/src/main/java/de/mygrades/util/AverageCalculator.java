package de.mygrades.util;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.database.dao.GradeEntry;
import de.mygrades.view.adapter.model.GradeItem;
import de.mygrades.view.adapter.model.GradesAdapterItem;

/**
 * Calculates the average grade and credit points sum for a list of GradeItems.
 * If simpleWeighting is set to true, credit points have no effect on the calculated average grade.
 */
public class AverageCalculator {
    private float average;
    private float creditPointsSum;
    private boolean simpleWeighting;

    public AverageCalculator(boolean simpleWeighting) {
        this.simpleWeighting = simpleWeighting;
    }

    /**
     * Calculates the average grade and credit points sum for a list of GradeItems.
     *
     * @param items - list of GradeAdapterItems
     */
    public void calculate(List<? extends GradesAdapterItem> items) {
        average = 0f;
        creditPointsSum = 0f;
        float creditPointsSumForAverage = 0f; // some grade_entries may have credit points, but no grade
        int passedGradesCounter = 0; // used, if simpleWeighting is true

        // iterate over items, count credit points and calculate average
        for(GradesAdapterItem item : items) {
            if (!(item instanceof GradeItem))
                continue;

            GradeItem gradeItem = (GradeItem) item;
            int weight = gradeItem.getWeight() == null ? 1 : gradeItem.getWeight();

            float actCreditPoints = (gradeItem.getCreditPoints() == null ? 0f : gradeItem.getCreditPoints());
            Float modifiedCreditPoints = gradeItem.getModifiedCreditPoints();
            actCreditPoints = modifiedCreditPoints == null ? actCreditPoints : modifiedCreditPoints;

            creditPointsSum += actCreditPoints;
            if (gradeItem.getGrade() != null && gradeItem.getGrade() > 0) {
                creditPointsSumForAverage += (actCreditPoints * weight);
            }

            float grade = (gradeItem.getGrade() == null ? 0f : gradeItem.getGrade());
            Float modifiedGrade = gradeItem.getModifiedGrade();
            grade = modifiedGrade == null ? grade : modifiedGrade;

            if (simpleWeighting) {
                if (grade > 0 && grade < 5) {
                    passedGradesCounter += weight;
                    average += (grade * weight);
                }
            } else {
                average += grade * actCreditPoints * weight;
            }
        }

        if (simpleWeighting) {
            average = passedGradesCounter > 0 ? average / passedGradesCounter : 0f;
        } else {
            average = creditPointsSumForAverage > 0 ? average / creditPointsSumForAverage : 0f;
        }
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
