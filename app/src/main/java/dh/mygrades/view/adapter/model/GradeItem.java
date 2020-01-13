package dh.mygrades.view.adapter.model;

import dh.mygrades.database.dao.GradeEntry;

/**
 * GradeItem is used in GradesRecyclerViewAdapter.
 */
public class GradeItem implements GradesAdapterItem {
    private String name;
    private String modifiedName;
    private Double grade;
    private String state;
    private Double modifiedGrade;
    private String modifiedState;
    private Double creditPoints;
    private Double modifiedCreditPoints;
    private Double weight;
    private String hash;
    private String semester;
    private String modifiedSemester;
    private int seen;
    private boolean hidden;

    public GradeItem() {}

    /**
     * Create GradeItem from GradeEntry.
     *
     * @param gradeEntry - grade entry
     */
    public GradeItem(GradeEntry gradeEntry) {
        setName(gradeEntry.getName());
        setModifiedName(gradeEntry.getModifiedName());
        setHash(gradeEntry.getHash());

        setCreditPoints(gradeEntry.getCreditPoints());
        setModifiedCreditPoints(gradeEntry.getModifiedCreditPoints());

        setGrade(gradeEntry.getGrade());
        setModifiedGrade(gradeEntry.getModifiedGrade());

        setState(gradeEntry.getState());
        setModifiedState(gradeEntry.getModifiedState());

        setSemester(gradeEntry.getSemester());
        setModifiedSemester(gradeEntry.getModifiedSemester());

        setWeight(gradeEntry.getWeight());
        setHidden(gradeEntry.isHidden());

        if (gradeEntry.getSeen() != null) {
            setSeen(gradeEntry.getSeen());
        }
    }

    /**
     * Determines whether the weight should be shown in the overview.
     *
     * @return true, if weight should be shown
     */
    public boolean showWeight() {
        return weight != null && weight != 1.0;
    }

    /**
     * Determines whether the modified badge should be shown in the overview.
     *
     * @return true, if modified badge should be shown
     */
    public boolean showModifiedBadge() {
        return modifiedGrade != null || modifiedState != null || modifiedCreditPoints != null ||
                modifiedName != null || modifiedSemester != null;
    }

    /**
     * Get the name which should be shown to the user.
     *
     * @return name to show
     */
    public String getShownName() {
        return modifiedName == null ? name : modifiedName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GradeItem gradeItem = (GradeItem) o;

        if (seen != gradeItem.seen) return false;
        if (hidden != gradeItem.hidden) return false;
        if (name != null ? !name.equals(gradeItem.name) : gradeItem.name != null) return false;
        if (modifiedName != null ? !modifiedName.equals(gradeItem.modifiedName) : gradeItem.modifiedName != null)
            return false;
        if (grade != null ? !grade.equals(gradeItem.grade) : gradeItem.grade != null) return false;
        if (modifiedGrade != null ? !modifiedGrade.equals(gradeItem.modifiedGrade) : gradeItem.modifiedGrade != null)
            return false;
        if (state != null ? !state.equals(gradeItem.state) : gradeItem.state != null) return false;
        if (modifiedState != null ? !modifiedState.equals(gradeItem.modifiedState) : gradeItem.modifiedState != null)
            return false;
        if (creditPoints != null ? !creditPoints.equals(gradeItem.creditPoints) : gradeItem.creditPoints != null)
            return false;
        if (modifiedCreditPoints != null ? !modifiedCreditPoints.equals(gradeItem.modifiedCreditPoints) : gradeItem.modifiedCreditPoints != null)
            return false;
        if (weight != null ? !weight.equals(gradeItem.weight) : gradeItem.weight != null)
            return false;
        if (hash != null ? !hash.equals(gradeItem.hash) : gradeItem.hash != null) return false;
        if (semester != null ? !semester.equals(gradeItem.semester) : gradeItem.semester != null)
            return false;
        return modifiedSemester != null ? modifiedSemester.equals(gradeItem.modifiedSemester) : gradeItem.modifiedSemester == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (modifiedName != null ? modifiedName.hashCode() : 0);
        result = 31 * result + (grade != null ? grade.hashCode() : 0);
        result = 31 * result + (modifiedGrade != null ? modifiedGrade.hashCode() : 0);
        result = 31 * result + (creditPoints != null ? creditPoints.hashCode() : 0);
        result = 31 * result + (modifiedCreditPoints != null ? modifiedCreditPoints.hashCode() : 0);
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        result = 31 * result + (semester != null ? semester.hashCode() : 0);
        result = 31 * result + (modifiedSemester != null ? modifiedSemester.hashCode() : 0);
        result = 31 * result + seen;
        result = 31 * result + (hidden ? 1 : 0);
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getCreditPoints() {
        return creditPoints;
    }

    public void setCreditPoints(Double creditPoints) {
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

    public Double getModifiedGrade() {
        return modifiedGrade;
    }

    public void setModifiedGrade(Double modifiedGrade) {
        this.modifiedGrade = modifiedGrade;
    }

    public String getModifiedState(){
        return modifiedState;
    }

    public void setModifiedState(String modifiedState) {
        this.modifiedState = modifiedState;
    }

    public Double getModifiedCreditPoints() {
        return modifiedCreditPoints;
    }

    public void setModifiedCreditPoints(Double modifiedCreditPoints) {
        this.modifiedCreditPoints = modifiedCreditPoints;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getModifiedSemester() {
        return modifiedSemester;
    }

    public void setModifiedSemester(String modifiedSemester) {
        this.modifiedSemester = modifiedSemester;
    }

    public String getModifiedName() {
        return modifiedName;
    }

    public void setModifiedName(String modifiedName) {
        this.modifiedName = modifiedName;
    }

    public String getCurrentSemester() {
        return modifiedSemester == null ? semester : modifiedSemester;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
