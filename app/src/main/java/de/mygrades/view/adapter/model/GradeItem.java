package de.mygrades.view.adapter.model;

/**
 * GradeItem is used in GradesRecyclerViewAdapter.
 */
public class GradeItem implements GradesAdapterItem {
    private String name;
    private Float grade;
    private Float creditPoints;
    private String hash;
    private int seen;

    public GradeItem() {}

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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Checks if two GradeItems are equal, according to their credit points and grade.
     *
     * @param gradeItem - other grade item
     * @return true, if credit points and grade are equal
     */
    public boolean equals(GradeItem gradeItem) {
        // check if credit points has changed
        if ((gradeItem.getCreditPoints() != null && creditPoints != null) &&
            (!gradeItem.getCreditPoints().equals(creditPoints))) {
            return false;
        }

        if ((gradeItem.getCreditPoints() == null && creditPoints != null) ||
            (gradeItem.getCreditPoints() != null && creditPoints == null)) {
            return false;
        }

        // check if grade has changed
        if ((gradeItem.getGrade() != null && grade != null) &&
            (!gradeItem.getGrade().equals(grade))) {
            return false;
        }

        if ((gradeItem.getGrade() == null && grade != null) ||
            (gradeItem.getGrade() != null && grade == null)) {
            return false;
        }

        return true;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public int getSeen() {
        return seen;
    }
}
