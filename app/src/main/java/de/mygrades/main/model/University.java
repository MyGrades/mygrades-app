package de.mygrades.main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO for university.
 */
public class University {
    private int universityId;
    private String shortName;
    private String name;
    private String updatedAtServer;
    private List<Rule> rules;

    public University() {
        this.rules = new ArrayList<>();
    }

    public int getUniversityId() {
        return universityId;
    }

    public void setUniversityId(int universityId) {
        this.universityId = universityId;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdatedAtServer() {
        return updatedAtServer;
    }

    public void setUpdatedAtServer(String updatedAtServer) {
        this.updatedAtServer = updatedAtServer;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "University{" +
                "universityId=" + universityId +
                ", shortName='" + shortName + '\'' +
                ", name='" + name + '\'' +
                ", updatedAtServer='" + updatedAtServer + '\'' +
                ", rules=" + rules +
                '}';
    }
}
