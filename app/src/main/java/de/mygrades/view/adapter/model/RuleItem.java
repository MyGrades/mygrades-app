package de.mygrades.view.adapter.model;

import android.content.Context;
import android.content.Intent;

import de.mygrades.view.activity.LoginActivity;

/**
 * POJO for selectable rule items in UniversitiesAdapter.
 */
public class RuleItem implements Comparable<RuleItem> {
    private long childId;
    private String name;
    private long ruleId;

    public RuleItem(long childId) {
        this.childId = childId;
    }

    public long getChildId() {
        return childId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * Starts an intent to go to the LoginActivity.
     *
     * @param context Context
     * @param universityData UniversityData used in intent data
     */
    public void goToLogin(Context context, UniversityItem universityData) {
        final Intent intent = new Intent(context, LoginActivity.class);

        intent.putExtra(LoginActivity.EXTRA_UNIVERSITY_NAME, universityData.getName());
        intent.putExtra(LoginActivity.EXTRA_UNIVERSITY_ID, universityData.getUniversityId());
        intent.putExtra(LoginActivity.EXTRA_RULE_ID, ruleId);
        context.startActivity(intent);
    }

    @Override
    public int compareTo(RuleItem another) {
        return name.compareToIgnoreCase(another.getName());
    }
}
