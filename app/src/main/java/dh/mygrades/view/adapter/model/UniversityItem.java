package dh.mygrades.view.adapter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO for expandable university items.
 */
public class UniversityItem extends UniversityGroupItem {
    private long groupId;
    private String name;
    private long universityId;
    private boolean isSectionHeader;
    private String sectionTitle;
    private boolean showRuleHint;

    private List<RuleItem> rules;

    public UniversityItem(long groupId) {
        this.groupId = groupId;
        rules = new ArrayList<>();
        showRuleHint = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.sectionTitle = String.valueOf(name.toUpperCase().charAt(0));
    }

    public long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(long universityId) {
        this.universityId = universityId;
    }

    public boolean isSectionHeader() {
        return isSectionHeader;
    }

    public void setIsSectionHeader(boolean isSectionHeader) {
        this.isSectionHeader = isSectionHeader;
    }

    public List<RuleItem> getRules() {
        return rules;
    }

    public void addRuleData(RuleItem ruleItem) {
        this.rules.add(ruleItem);
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    @Override
    public long getGroupId() {
        return groupId;
    }

    public boolean showRuleHint() {
        return showRuleHint;
    }

    public void setShowRuleHint(boolean showRuleHint) {
        this.showRuleHint = showRuleHint;
    }
}
