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
        gradeItem1.setGrade(1.0f);
        gradeItem1.setCreditPoints(5.0f);

        GradeItem gradeItem2 = new GradeItem();
        gradeItem2.setGrade(2.3f);
        gradeItem2.setCreditPoints(7.0f);

        List<GradeItem> items = new ArrayList<>();
        items.add(gradeItem1);
        items.add(gradeItem2);

        averageCalculator.calculate(items);
        assertEquals(1.7583f, averageCalculator.getAverage(), 0.0001f);
        assertEquals(12.0f, averageCalculator.getCreditPointsSum(), 0.0000001f);
    }


    /**
     * Test the AverageCalculator with simpleWeighting set to true. CreditPoints must be ignored.
     */
    @Test
    public void calculateAverage_withoutModification_simpleWeighting() {
        AverageCalculator averageCalculator = new AverageCalculator(true);

        GradeItem gradeItem1 = new GradeItem();
        gradeItem1.setGrade(1.0f);
        gradeItem1.setCreditPoints(5.0f);

        GradeItem gradeItem2 = new GradeItem();
        gradeItem2.setGrade(2.3f);
        gradeItem2.setCreditPoints(7.0f);

        List<GradeItem> items = new ArrayList<>();
        items.add(gradeItem1);
        items.add(gradeItem2);

        averageCalculator.calculate(items);

        assertEquals(1.65f, averageCalculator.getAverage(), 0.000000001f);
        assertEquals(12f, averageCalculator.getCreditPointsSum(), 0.000000001f);
    }

    /**
     * Test the AverageCalculator with modified grades and credit points.
     */
    @Test
    public void calculateAverage_withModification() {
        AverageCalculator averageCalculator = new AverageCalculator(false);

        GradeItem gradeItem1 = new GradeItem();
        gradeItem1.setGrade(1.0f);
        gradeItem1.setModifiedGrade(1.3f);
        gradeItem1.setCreditPoints(5.0f);
        gradeItem1.setModifiedCreditPoints(13.0f);

        GradeItem gradeItem2 = new GradeItem();
        gradeItem2.setGrade(2.3f);
        gradeItem2.setCreditPoints(7.0f);
        gradeItem2.setWeight(3.7);

        List<GradeItem> items = new ArrayList<>();
        items.add(gradeItem1);
        items.add(gradeItem2);

        averageCalculator.calculate(items);
        averageCalculator.calculate(items);
        assertEquals(1.9658f, averageCalculator.getAverage(), 0.00001f);
        assertEquals(20.0f, averageCalculator.getCreditPointsSum(), 0.0000001f);
    }

    /**
     * Test the AverageCalculator with modified and null values.
     */
    @Test
    public void calculateAverage_withModificationAndNullValues() {
        AverageCalculator averageCalculator = new AverageCalculator(false);

        GradeItem gradeItem1 = new GradeItem();
        gradeItem1.setGrade(1.0f);
        gradeItem1.setModifiedGrade(1.3f);
        gradeItem1.setCreditPoints(5.0f);
        gradeItem1.setModifiedCreditPoints(13.0f);

        GradeItem gradeItem2 = new GradeItem();
        gradeItem2.setGrade(2.3f);
        gradeItem2.setCreditPoints(7.0f);
        gradeItem2.setWeight(3.7);

        GradeItem gradeItem3 = new GradeItem();
        gradeItem3.setGrade(null);
        gradeItem3.setCreditPoints(3.5f);
        gradeItem3.setModifiedGrade(1.7f);
        gradeItem3.setModifiedCreditPoints(5f);
        gradeItem3.setWeight(2.0);

        List<GradeItem> items = new ArrayList<>();
        items.add(gradeItem1);
        items.add(gradeItem2);
        items.add(gradeItem3);

        averageCalculator.calculate(items);
        averageCalculator.calculate(items);
        assertEquals(1.9114519f, averageCalculator.getAverage(), 0.0000001f);
        assertEquals(25.0f, averageCalculator.getCreditPointsSum(), 0.0000001f);
    }

    /**
     * Test the AverageCalculator with simpleWeighting set to true
     * and additional specific weight values.
     */
    @Test
    public void calculateAverage_simpleWeighting_withWeight() {
        AverageCalculator averageCalculator = new AverageCalculator(true);

        GradeItem gradeItem1 = new GradeItem();
        gradeItem1.setGrade(1.0f);
        gradeItem1.setCreditPoints(5.0f);
        gradeItem1.setWeight(3.0);

        GradeItem gradeItem2 = new GradeItem();
        gradeItem2.setGrade(2.3f);
        gradeItem2.setCreditPoints(7.0f);
        gradeItem2.setWeight(2.5);

        List<GradeItem> items = new ArrayList<>();
        items.add(gradeItem1);
        items.add(gradeItem2);

        averageCalculator.calculate(items);

        assertEquals(1.590909f, averageCalculator.getAverage(), 0.000001f);
        assertEquals(12f, averageCalculator.getCreditPointsSum(), 0.000000001f);
    }
}
