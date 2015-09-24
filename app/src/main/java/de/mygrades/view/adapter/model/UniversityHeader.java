package de.mygrades.view.adapter.model;

/**
 * University header item used in UniversitiesRecyclerViewAdapter.
 */
public class UniversityHeader implements UniversityAdapterItem {
    private String header;

    public UniversityHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
