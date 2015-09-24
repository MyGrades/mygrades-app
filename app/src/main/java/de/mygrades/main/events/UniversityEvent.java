package de.mygrades.main.events;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.database.dao.University;

/**
 * UniversityEvent is used to send new universities to subscribers, e.g. activities.
 */
public class UniversityEvent {
    private List<University> newUniversities;

    public UniversityEvent() {
        newUniversities = new ArrayList<>();
    }

    public List<University> getNewUniversities(boolean publishedOnly) {
        if (publishedOnly) {
            List<University> publishedUniversities = new ArrayList<>();
            for(University u : newUniversities) {
                if (u.getPublished()) {
                    publishedUniversities.add(u);
                }
            }
            return publishedUniversities;
        }

        return newUniversities;
    }

    public void setNewUniversities(List<University> newUniversities) {
        this.newUniversities = new ArrayList<>(newUniversities);
    }
}
