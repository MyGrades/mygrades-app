package de.mygrades.view.adapter.model;

/**
 * University header item used in UniversitiesRecyclerViewAdapter.
 */
public class UniversitySection implements UniversityAdapterItem {
    private String section;

    public UniversitySection(String section) {
        this.section = section;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
}
