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
        items.add(createGradeItem(grade_1, modified_grade_1, cp_1, modified_cp_1, weight_1, hidden_1))
        items.add(createGradeItem(grade_2, modified_grade_2, cp_2, modified_cp_2, weight_2, hidden_2))
        items.add(createGradeItem(grade_3, modified_grade_3, cp_3, modified_cp_3, weight_3, hidden_3))
        averageCalculator.calculate(items)
        def average = averageCalculator.getAverage()
        def creditPoints = averageCalculator.getCreditPointsSum()

        then:
        average closeTo(expected_average, 0.0001)
        creditPoints closeTo(expected_cp_sum, 0.0001)

        where:
        grade_1          << [1.0   , 1.0  , 1.0   , 1.0   , 1.0   , 0.0  , 1.7]
        modified_grade_1 << [null  , null , 1.3   , 1.3   , null  , null , null]
        cp_1             << [5.0   , 5.0  , 5.0   , 5.0   , 5.0   , 15.0 , 10.0]
        modified_cp_1    << [null  , null , 13.0  , 13.0  , null  , null , null]
        weight_1         << [null  , null , null  , null  , 3.0   , null , null]
        hidden_1         << [false , false, false , false , false , false, true]

        grade_2          << [2.3   , 2.3  , 2.3   , 2.3   , 2.3   , 5.0  , 2.0]
        modified_grade_2 << [null  , null , null  , null  , null  , null , null]
        cp_2             << [7.0   , 7.0  , 7.0   , 7.0   , 7.0   , 3.5  , 3.5]
        modified_cp_2    << [null  , null , null  , null  , null  , null , null]
        weight_2         << [null  , null , 3.7   , 3.7   , 2.5   , null , 2.0]
        hidden_2         << [false , false, false , false , false , false, false]

        grade_3          << [null  , null , null  , null  , null  , null , null]
        modified_grade_3 << [null  , null , null  , 1.7   , null  , 1.7  , null]
        cp_3             << [null  , null , null  , 3.5   , null  , null , null]
        modified_cp_3    << [null  , null , null  , 5.0   , null  , 5.0  , null]
        weight_3         << [null  , null , null  , 2.0   , null  , 2.0  , null]
        hidden_3         << [false , false, false , false , false , false, false]

        simple_weighting << [false , true , false , false , true  , false, false]
        expected_average << [1.7583, 1.65 , 1.9658, 1.9114, 1.5909, 1.7  , 2.0]
        expected_cp_sum  << [12.0  , 12.0 , 20.0  , 25.0  , 12.0  , 20.0 , 3.5]
    }

    def createGradeItem(grade, modifiedGrade, cp, modifiedCp, weight, hidden) {
        def gradeItem = new GradeItem()
        gradeItem.setGrade((Double) grade)
        gradeItem.setModifiedGrade((Double) modifiedGrade)
        gradeItem.setCreditPoints((Double) cp)
        gradeItem.setModifiedCreditPoints((Double) modifiedCp)
        gradeItem.setWeight((Double) weight)
        gradeItem.setHidden((Boolean) hidden)
        gradeItem
    }
}