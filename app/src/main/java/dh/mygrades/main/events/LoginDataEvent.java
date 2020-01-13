package dh.mygrades.main.events;

/**
 * This event posts the selected university and the username to subscribers.
 * This is used, if an error occurs during the initial scraping and the user goes back to
 * the LoginActivity to change the password or username.
 */
public class LoginDataEvent {
    private String username;
    private long universityId;
    private long ruleId;
    private String universityName;

    public LoginDataEvent(String username, long universityId, long ruleId, String universityName) {
        this.username = username;
        this.universityId = universityId;
        this.ruleId = ruleId;
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

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }
}
