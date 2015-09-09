package de.mygrades.main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO for rule.
 */
public class Rule {
    private int ruleId;
    private String type;
    private List<TransformerMapping> transformerMappings;
    private String updatedAt;

    public Rule() {
        transformerMappings = new ArrayList<>();
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TransformerMapping> getTransformerMappings() {
        return transformerMappings;
    }

    public void setTransformerMappings(List<TransformerMapping> transformerMappings) {
        this.transformerMappings = transformerMappings;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "ruleId=" + ruleId +
                ", type='" + type + '\'' +
                ", transformerMappings=" + transformerMappings +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
