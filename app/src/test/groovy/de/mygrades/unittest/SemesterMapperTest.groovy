package de.mygrades.unittest

import de.mygrades.util.SemesterMapper
import spock.lang.Specification
import spock.lang.Unroll;

class SemesterMapperTest extends Specification {

    @Unroll
    def "test if #next_semester is directly after #current_semester, should be #result"() {
        given:
        def semesterMapper = new SemesterMapper()

        expect:
        semesterMapper.isNextSemester(current_semester, next_semester) == result

        where:
        current_semester           | next_semester              | result
        "Sommersemester 2015"      | "Wintersemester 2015/2016" | true
        "Sommersemester 2015"      | "Sommersemester 2016"      | false
        "sommersemester 2015"      | "Wintersemester 2016/2017" | false
        "Wintersemester 2015/2016" | "Sommersemester 2016"      | true
        "Wintersemester 2015/2016" | "Sommersemester 2017"      | false
        "Wintersemester 2015/2016" | "Wintersemester 2015/16"   | false
        "Wintersemester 2015/1016" | "Wintersemester 2016/17"   | false
    }

    @Unroll
    def "get semester after #current_semester, expect #expected_next_semester"() {
        given:
        def semesterMapper = new SemesterMapper()

        expect:
        semesterMapper.getNextSemester(current_semester) == expected_next_semester

        where:
        current_semester           | expected_next_semester
        "Sommersemester 2015"      | "Wintersemester 2015/2016"
        "Wintersemester 2014/2015" | "Sommersemester 2015"
    }

    @Unroll
    def "get previous semester before #current_semester, expect #expected_previous_semester"() {
        given:
        def semesterMapper = new SemesterMapper()

        expect:
        semesterMapper.getPreviousSemester(current_semester) == expected_previous_semester

        where:
        current_semester           | expected_previous_semester
        "Sommersemester 2015"      | "Wintersemester 2014/2015"
        "Wintersemester 2014/2015" | "Sommersemester 2014"
    }

    @Unroll
    def "create consecutive semester list, expect #expected_list_size consecutive semester"() {
        when:
        def semesterMapper = new SemesterMapper()
        def semesterList = semesterMapper.createConsecutiveSemesterList(semester_set as Set)

        then:
        semesterList.size() == expected_list_size

        where:
        semester_set                                                                    | expected_list_size
        ["Sommersemester 2015", "Wintersemester 2015/2016"]                             | 2
        ["Sommersemester 2015", "Sommersemester 2016"]                                  | 3
        ["Wintersemester 2015/2016", "Sommersemester 2017"]                             | 4
        ["Wintersemester 2015/2016", "Wintersemester 2017/18"]                          | 5
        ["Sommersemester 2015"]                                                         | 1
        []                                                                              | 0
        ["Wintersemester 2014/2015", "Wintersemester 2015/2016", "Sommersemester 2017"] | 6
    }
}