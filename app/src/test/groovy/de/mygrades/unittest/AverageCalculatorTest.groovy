package de.mygrades.unittest

import de.mygrades.util.AverageCalculator
import de.mygrades.view.adapter.model.GradeItem
import spock.lang.Specification
import spock.lang.Unroll
import static spock.util.matcher.HamcrestMatchers.closeTo

class AverageCalculatorTest extends Specification {

    @Unroll
    @SuppressWarnings("GroovyAssignabilityCheck") // hide annoying hint at 'closeTo' method
    def "test average calculation and credit point sum, expect average #expected_average and cp sum #expected_cp_sum"() {
        when:
        def averageCalculator = new AverageCalculator(simple_weighting)
        def items = new ArrayList<GradeItem>()
        items.add(createGradeItem(grade_1, cp_1))
        items.add(createGradeItem(grade_2, cp_2))
        items.add(createGradeItem(grade_3, cp_3))
        averageCalculator.calculate(items)
        def average = averageCalculator.getAverage()
        def creditPoints = averageCalculator.getCreditPointsSum()

        then:
        average closeTo(expected_average, 0.0001)
        creditPoints closeTo(expected_cp_sum, 0.0001)

        where:
        grade_1          << [1.0   , 1.0  , 1.3]
        cp_1             << [5.0   , 5.0  , 5.0]

        grade_2          << [2.3   , 5.0  , null]
        cp_2             << [7.0   , 7.0  , 7.0]

        grade_3          << [null  , null , 1.7]
        cp_3             << [null  , null , null]

        simple_weighting << [false , true , false]
        expected_average << [1.7583, 1.0 , 1.3]
        expected_cp_sum  << [12.0  , 5.0 , 12]
    }

    def createGradeItem(grade, cp) {
        def gradeItem = new GradeItem()
        gradeItem.setGrade((Float) grade)
        gradeItem.setCreditPoints((Float) cp)
        gradeItem
    }
}