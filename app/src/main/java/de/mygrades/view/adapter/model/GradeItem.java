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
    private Integer semesterNumber;
    private Integer modifiedSemesterNumber;
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
        setWeight(weight);

        Integer semesterNumber = gradeEntry.getSemesterNumber();
        setSemesterNumber(semesterNumber);

        Integer modifiedSemesterNumber = gradeEntry.getModifiedSemesterNumber();
        setModifiedSemesterNumber(modifiedSemesterNumber);

        if (gradeEntry.getSeen() != null) {
            setSeen(gradeEntry.getSeen());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GradeItem gradeItem = (GradeItem) o;

        if (name != null ? !name.equals(gradeItem.name) : gradeItem.name != null) return false;
        if (grade != null ? !grade.equals(gradeItem.grade) : gradeItem.grade != null) return false;
        if (modifiedGrade != null ? !modifiedGrade.equals(gradeItem.modifiedGrade) : gradeItem.modifiedGrade != null)
            return false;
        if (creditPoints != null ? !creditPoints.equals(gradeItem.creditPoints) : gradeItem.creditPoints != null)
            return false;
        if (modifiedCreditPoints != null ? !modifiedCreditPoints.equals(gradeItem.modifiedCreditPoints) : gradeItem.modifiedCreditPoints != null)
            return false;
        if (weight != null ? !weight.equals(gradeItem.weight) : gradeItem.weight != null)
            return false;
        if (hash != null ? !hash.equals(gradeItem.hash) : gradeItem.hash != null) return false;
        if (semesterNumber != null ? !semesterNumber.equals(gradeItem.semesterNumber) : gradeItem.semesterNumber != null)
            return false;
        return !(modifiedSemesterNumber != null ? !modifiedSemesterNumber.equals(gradeItem.modifiedSemesterNumber) : gradeItem.modifiedSemesterNumber != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (grade != null ? grade.hashCode() : 0);
        result = 31 * result + (modifiedGrade != null ? modifiedGrade.hashCode() : 0);
        result = 31 * result + (creditPoints != null ? creditPoints.hashCode() : 0);
        result = 31 * result + (modifiedCreditPoints != null ? modifiedCreditPoints.hashCode() : 0);
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        result = 31 * result + (semesterNumber != null ? semesterNumber.hashCode() : 0);
        result = 31 * result + (modifiedSemesterNumber != null ? modifiedSemesterNumber.hashCode() : 0);
        return result;
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

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public Integer getModifiedSemesterNumber() {
        return modifiedSemesterNumber;
    }

    public void setModifiedSemesterNumber(Integer modifiedSemesterNumber) {
        this.modifiedSemesterNumber = modifiedSemesterNumber;
    }
}
