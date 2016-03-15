package de.mygrades.view.adapter.model;

import de.mygrades.database.dao.GradeEntry;

/**
 * GradeItem is used in GradesRecyclerViewAdapter.
 */
public class GradeItem implements GradesAdapterItem {
    private String name;
    private Float grade;
    private Float modifiedGrade;
    private Float creditPoints;
    private Float modifiedCreditPoints;
    private Double weight;
    private String hash;
    private int seen;

    public GradeItem() {}

    /**
     * Create GradeItem from GradeEntry.
     *
     * @param gradeEntry - grade entry
     */
    public GradeItem(GradeEntry gradeEntry) {
        setName(gradeEntry.getName());
        setHash(gradeEntry.getHash());

        Double creditPoints = gradeEntry.getCreditPoints();
        setCreditPoints(creditPoints == null ? null : creditPoints.floatValue());

        Double modifiedCreditPoints = gradeEntry.getModifiedCreditPoints();
        setModifiedCreditPoints(modifiedCreditPoints == null ? null : modifiedCreditPoints.floatValue());

        Double grade = gradeEntry.getGrade();
        setGrade(grade == null ? null : grade.floatValue());

        Double modifiedGrade = gradeEntry.getModifiedGrade();
        setModifiedGrade(modifiedGrade == null ? null : modifiedGrade.floatValue());

        Double weight = gradeEntry.getWeight();
        setWeight(weight == null ? null : weight);

        if (gradeEntry.getSeen() != null) {
            setSeen(gradeEntry.getSeen());
        }
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

        // check if modified credit points has changed
        if ((gradeItem.getModifiedCreditPoints() != null && modifiedCreditPoints != null) &&
            (!gradeItem.getModifiedCreditPoints().equals(modifiedCreditPoints))) {
            return false;
        }

        if ((gradeItem.getModifiedCreditPoints() == null && modifiedCreditPoints != null) ||
            (gradeItem.getModifiedCreditPoints() != null && modifiedCreditPoints == null)) {
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

        // check if modified grade has changed
        if ((gradeItem.getModifiedGrade() != null && modifiedGrade != null) &&
            (!gradeItem.getModifiedGrade().equals(modifiedGrade))) {
            return false;
        }

        if ((gradeItem.getModifiedGrade() == null && modifiedGrade != null) ||
            (gradeItem.getModifiedGrade() != null && modifiedGrade == null)) {
            return false;
        }

        // check if weight differs
        if ((gradeItem.getWeight() != null && weight != null) &&
            (!gradeItem.getWeight().equals(weight))) {
            return false;
        }

        if ((gradeItem.getWeight() == null && weight != null) ||
            (gradeItem.getWeight() != null && weight == null)) {
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

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Float getModifiedGrade() {
        return modifiedGrade;
    }

    public void setModifiedGrade(Float modifiedGrade) {
        this.modifiedGrade = modifiedGrade;
    }

    public Float getModifiedCreditPoints() {
        return modifiedCreditPoints;
    }

    public void setModifiedCreditPoints(Float modifiedCreditPoints) {
        this.modifiedCreditPoints = modifiedCreditPoints;
    }
}
