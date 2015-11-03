package de.mygrades.main.events;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.database.dao.University;

/**
 * UniversityEvent is used to send a list of universities to subscribers, e.g. activities.
 */
public class UniversityEvent {
    private List<University> universities;

    public UniversityEvent() {
        universities = new ArrayList<>();
    }

    /**
     * Get all universities from this event.
     * If publishedOnly is set, a new list will be created.
     *
     * @param publishedOnly - get publishedOnly universities
     * @return list of universities
     */
    public List<University> getUniversities(boolean publishedOnly) {
        if (publishedOnly) {
            List<University> publishedUniversities = new ArrayList<>();
            for(University u : universities) {
                if (u.getPublished()) {
                    publishedUniversities.add(u);
                }
            }
            return publishedUniversities;
        }

        return universities;
    }

    public void setUniversities(List<University> universities) {
        this.universities = new ArrayList<>(universities);
    }
}
