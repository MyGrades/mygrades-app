package de.mygrades.main.events;

/**
 * This event posts the selected university and the username to subscribers.
 * This is used, if an error occurs during the initial scraping and the user goes back to
 * the LoginActivity to change the password or username.
 */
public class LoginDataEvent {
    private String username;
    private long universityId;
    private String universityName;

    public LoginDataEvent(String username, long universityId, String universityName) {
        this.username = username;
        this.universityId = universityId;
        this.universityName = universityName;
    }

    public String getUsername() {
        return username;
    }

    public long getUniversityId() {
        return universityId;
    }

    public String getUniversityName() {
        return universityName;
    }
}
