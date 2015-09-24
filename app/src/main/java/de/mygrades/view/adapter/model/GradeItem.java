package de.mygrades.view.adapter.model;

/**
 * GradeItem is used in GradesRecyclerViewAdapter.
 */
public class GradeItem implements GradesAdapterItem {
    private String name;
    private float grade;
    private float creditPoints;

    public GradeItem() {}

    public GradeItem(String name, float grade, float creditPoints) {
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

    public float getGrade() {
        return grade;
    }

    public void setGrade(float grade) {
        this.grade = grade;
    }

    public float getCreditPoints() {
        return creditPoints;
    }

    public void setCreditPoints(float creditPoints) {
        this.creditPoints = creditPoints;
    }
}
