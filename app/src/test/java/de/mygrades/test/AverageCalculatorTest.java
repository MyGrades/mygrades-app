package de.mygrades.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.util.AverageCalculator;
import de.mygrades.view.adapter.model.GradeItem;

import static org.junit.Assert.assertEquals;


/**
 * Unit tests for the AverageCalculator.
 */
public class AverageCalculatorTest {

    /**
     * Test the AverageCalculator with basic grade items.
     * No modifications or special settings are used.
     */
    @Test
    public void calculateAverage_withoutModification() {
        AverageCalculator averageCalculator = new AverageCalculator(false);

        GradeItem gradeItem1 = new GradeItem();
        gradeItem1.setGrade(1.0);
        gradeItem1.setCreditPoints(5.0);

        GradeItem gradeItem2 = new GradeItem();
        gradeItem2.setGrade(2.3);
        gradeItem2.setCreditPoints(7.0);

        List<GradeItem> items = new ArrayList<>();
        items.add(gradeItem1);
        items.add(gradeItem2);

        averageCalculator.calculate(items);
        assertEquals(1.7583, averageCalculator.getAverage(), 0.0001);
        assertEquals(12.0, averageCalculator.getCreditPointsSum(), 0.0000001);
    }


    /**
     * Test the AverageCalculator with simpleWeighting set to true. CreditPoints must be ignored.
     */
    @Test
    public void calculateAverage_withoutModification_simpleWeighting() {
        AverageCalculator averageCalculator = new AverageCalculator(true);

        GradeItem gradeItem1 = new GradeItem();
        gradeItem1.setGrade(1.0);
        gradeItem1.setCreditPoints(5.0);

        GradeItem gradeItem2 = new GradeItem();
        gradeItem2.setGrade(2.3);
        gradeItem2.setCreditPoints(7.0);

        List<GradeItem> items = new ArrayList<>();
        items.add(gradeItem1);
        items.add(gradeItem2);

        averageCalculator.calculate(items);

        assertEquals(1.65, averageCalculator.getAverage(), 0.0000001);
        assertEquals(12, averageCalculator.getCreditPointsSum(), 0.000000001);
    }

    /**
     * Test the AverageCalculator with modified grades and credit points.
     */
    @Test
    public void calculateAverage_withModification() {
        AverageCalculator averageCalculator = new AverageCalculator(false);

        GradeItem gradeItem1 = new GradeItem();
        gradeItem1.setGrade(1.0);
        gradeItem1.setModifiedGrade(1.3);
        gradeItem1.setCreditPoints(5.0);
        gradeItem1.setModifiedCreditPoints(13.0);

        GradeItem gradeItem2 = new GradeItem();
        gradeItem2.setGrade(2.3);
        gradeItem2.setCreditPoints(7.0);
        gradeItem2.setWeight(3.7);

        List<GradeItem> items = new ArrayList<>();
        items.add(gradeItem1);
        items.add(gradeItem2);

        averageCalculator.calculate(items);
        averageCalculator.calculate(items);
        assertEquals(1.9658, averageCalculator.getAverage(), 0.00001);
        assertEquals(20.0, averageCalculator.getCreditPointsSum(), 0.0000001);
    }

    /**
     * Test the AverageCalculator with modified and null values.
     */
    @Test
    public void calculateAverage_withModificationAndNullValues() {
        AverageCalculator averageCalculator = new AverageCalculator(false);

        GradeItem gradeItem1 = new GradeItem();
        gradeItem1.setGrade(1.0);
        gradeItem1.setModifiedGrade(1.3);
        gradeItem1.setCreditPoints(5.0);
        gradeItem1.setModifiedCreditPoints(13.0);

        GradeItem gradeItem2 = new GradeItem();
        gradeItem2.setGrade(2.3);
        gradeItem2.setCreditPoints(7.0);
        gradeItem2.setWeight(3.7);

        GradeItem gradeItem3 = new GradeItem();
        gradeItem3.setGrade(null);
        gradeItem3.setCreditPoints(3.5);
        gradeItem3.setModifiedGrade(1.7);
        gradeItem3.setModifiedCreditPoints(5.0);
        gradeItem3.setWeight(2.0);

        List<GradeItem> items = new ArrayList<>();
        items.add(gradeItem1);
        items.add(gradeItem2);
        items.add(gradeItem3);

        averageCalculator.calculate(items);
        averageCalculator.calculate(items);
        assertEquals(1.9114519, averageCalculator.getAverage(), 0.0000001);
        assertEquals(25.0, averageCalculator.getCreditPointsSum(), 0.0000001);
    }

    /**
     * Test the AverageCalculator with simpleWeighting set to true
     * and additional specific weight values.
     */
    @Test
    public void calculateAverage_simpleWeighting_withWeight() {
        AverageCalculator averageCalculator = new AverageCalculator(true);

        GradeItem gradeItem1 = new GradeItem();
        gradeItem1.setGrade(1.0);
        gradeItem1.setCreditPoints(5.0);
        gradeItem1.setWeight(3.0);

        GradeItem gradeItem2 = new GradeItem();
        gradeItem2.setGrade(2.3);
        gradeItem2.setCreditPoints(7.0);
        gradeItem2.setWeight(2.5);

        List<GradeItem> items = new ArrayList<>();
        items.add(gradeItem1);
        items.add(gradeItem2);

        averageCalculator.calculate(items);

        assertEquals(1.590909, averageCalculator.getAverage(), 0.000001);
        assertEquals(12, averageCalculator.getCreditPointsSum(), 0.000000001);
    }
}
