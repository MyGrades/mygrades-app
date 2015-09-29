package de.mygrades.view.adapter.model;

/**
 * University item used in UniversitiesRecyclerViewAdapter.
 */
public class UniversityItem implements UniversityAdapterItem {
    private String name;
    private long universityId;

    public UniversityItem(String name, long universityId) {
        this.name = name;
        this.universityId = universityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(long universityId) {
        this.universityId = universityId;
    }
}
