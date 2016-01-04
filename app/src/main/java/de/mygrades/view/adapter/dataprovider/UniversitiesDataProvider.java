package de.mygrades.view.adapter.dataprovider;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.mygrades.view.adapter.model.RuleItem;
import de.mygrades.view.adapter.model.UniversityFooter;
import de.mygrades.view.adapter.model.UniversityGroupItem;
import de.mygrades.view.adapter.model.UniversityHeader;
import de.mygrades.view.adapter.model.UniversityItem;

/**
 * UniversitiesDataProvider provides access to the underlying university items.
 */
public class UniversitiesDataProvider {
    private List<UniversityGroupItem> items;
    private UniversityHeader header;
    private UniversityFooter footer;

    public UniversitiesDataProvider() {
        header = new UniversityHeader();
        footer = new UniversityFooter();

        items = new ArrayList<>();
        items.add(header);
        items.add(footer);
    }

    /**
     * Adds new universities.
     *
     * @param expandableItemManager RecyclerViewExpandableItemManager
     * @param universityItems list of UniversityItem objects
     */
    public void addUniversities(RecyclerViewExpandableItemManager expandableItemManager, List<UniversityItem> universityItems) {
        for (UniversityItem universityItem : universityItems) {
            if (!checkUpdateUniversity(expandableItemManager, universityItem)) {
                addUniversity(expandableItemManager, universityItem);
            }
        }

        updateSections(expandableItemManager);
    }

    /**
     * Checks if an university must be updated.
     * In addition, it is also checked if any rules have changed.
     *
     * @param expandableItemManager RecyclerViewExpandableItemManager
     * @param newUniversity university to update
     * @return true, if university was updated
     */
    private boolean checkUpdateUniversity(RecyclerViewExpandableItemManager expandableItemManager, UniversityItem newUniversity) {
        for(int i = 1; i < items.size() - 1; i++) {
            UniversityItem universityItem = (UniversityItem) items.get(i);

            if (universityItem.getUniversityId() == newUniversity.getUniversityId()) {
                if (!universityItem.getName().equals(newUniversity.getName())) {
                    updateUniversity(expandableItemManager, newUniversity, i);
                    return true;
                }

                // compare size of rules
                if (newUniversity.getRules().size() != universityItem.getRules().size()) {
                    updateUniversity(expandableItemManager, newUniversity, i);
                    return true;
                }

                // check if any rule name has changed
                Collections.sort(newUniversity.getRules());
                Collections.sort(universityItem.getRules());
                for (int j = 0; j < newUniversity.getRules().size(); j++) {
                    if (newUniversity.getRules().get(j).compareTo(universityItem.getRules().get(j)) != 0) {
                        updateUniversity(expandableItemManager, newUniversity, i);
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Updates an university if it exists already.
     *
     * @param expandableItemManager RecyclerViewExpandableItemManager
     * @param newUniversity university to update
     */
    private void updateUniversity(RecyclerViewExpandableItemManager expandableItemManager, UniversityItem newUniversity, int index) {
        // remove old university
        items.remove(index);
        expandableItemManager.notifyGroupItemRemoved(index);

        // add new university
        addUniversity(expandableItemManager, newUniversity);
    }

    /**
     * Adds a new university.
     *
     * @param expandableItemManager RecyclerViewExpandableItemManager
     * @param newUniversity UniversityItem to add
     */
    private void addUniversity(RecyclerViewExpandableItemManager expandableItemManager, UniversityItem newUniversity) {
        int newUniversityIndex = items.size() - 1; // add to end, if no proper index was found

        for(int i = 1; i < items.size() - 1; i++) {
            UniversityItem universityItem = (UniversityItem) items.get(i);

            if (newUniversity.getName().compareToIgnoreCase(universityItem.getName()) <= 0) {
                newUniversityIndex = i;
                break;
            }
        }

        // sort rules by name
        Collections.sort(newUniversity.getRules());
        items.add(newUniversityIndex, newUniversity);
        expandableItemManager.notifyGroupItemInserted(newUniversityIndex);
    }

    /**
     * Iteratively updates all sections.
     *
     * @param expandableItemManager RecyclerViewExpandableItemManager
     */
    private void updateSections(RecyclerViewExpandableItemManager expandableItemManager) {
        // iterate backwards over list, ignores footer and header
        for(int i = items.size() - 2; i >= 1; i--) {
            UniversityItem universityItem = (UniversityItem) items.get(i);

            if (i == 1) {
                universityItem.setIsSectionHeader(true);
                expandableItemManager.notifyGroupItemChanged(i);
                break;
            }

            char current = universityItem.getName().charAt(0);
            char previous = ((UniversityItem) items.get(i - 1)).getName().charAt(0);

            boolean isAlreadySectionHeader = universityItem.isSectionHeader();
            boolean isNewSection = current != previous; // => true means current item is section

            if (isAlreadySectionHeader != isNewSection) {
                universityItem.setIsSectionHeader(isNewSection);
                expandableItemManager.notifyGroupItemChanged(i);
            }
        }
    }

    public int getGroupCount() {
        return items.size();
    }

    public int getChildCount(int groupPosition) {
        if (groupPosition > 0 && groupPosition < getGroupCount() - 1)
            return ((UniversityItem) items.get(groupPosition)).getRules().size();
        else
            return 0; // header and footer have no children
    }

    public UniversityGroupItem getGroupItem(int groupPosition) {
        return items.get(groupPosition);
    }

    public RuleItem getChildItem(int groupPosition, int childPosition) {
        if (groupPosition > 0 && groupPosition < getGroupCount() - 1)
            return ((UniversityItem) items.get(groupPosition)).getRules().get(childPosition);
        else {
            return null; // header and footer have no children
        }
    }

    public UniversityHeader getHeader() {
        return header;
    }

    public UniversityFooter getFooter() {
        return footer;
    }
}
