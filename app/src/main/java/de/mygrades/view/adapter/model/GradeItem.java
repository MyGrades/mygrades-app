package de.mygrades.view.adapter.model;

/**
 * GradeItem is used in GradesRecyclerViewAdapter.
 */
public class GradeItem implements GradesAdapterItem {
    private String name;
    private Float grade;
    private Float creditPoints;

    public GradeItem() {}

    public GradeItem(String name, Float grade, Float creditPoints) {
        this.name = name;
        this.grade = grade;
        this.creditPoints = creditPoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getGrade() {
        return grade;
    }

    public void setGrade(Float grade) {
        this.grade = grade;
    }

    public Float getCreditPoints() {
        return creditPoints;
    }

    public void setCreditPoints(Float creditPoints) {
        this.creditPoints = creditPoints;
    }
}
