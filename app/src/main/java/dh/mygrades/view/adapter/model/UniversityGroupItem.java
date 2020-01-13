package dh.mygrades.view.adapter.model;

/**
 * Abstract class for group items in UniversitiesAdapter.
 */
public abstract class UniversityGroupItem {

    /**
     * Get the group id.
     * ExpandableItemAdapter requires stable IDs for each group item.
     *
     * @return group id
     */
    public abstract long getGroupId();
}
