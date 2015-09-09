package de.mygrades.main.model;

/**
 * POJO for TransformerMapping
 */
public class TransformerMapping {
    private int transformerId;
    private int ruleId;
    private String name;
    private String parseExpression;
    private String parseType;

    public TransformerMapping() {}

    public int getTransformerId() {
        return transformerId;
    }

    public void setTransformerId(int transformerId) {
        this.transformerId = transformerId;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParseExpression() {
        return parseExpression;
    }

    public void setParseExpression(String parseExpression) {
        this.parseExpression = parseExpression;
    }

    public String getParseType() {
        return parseType;
    }

    public void setParseType(String parseType) {
        this.parseType = parseType;
    }

    @Override
    public String toString() {
        return "TransformerMapping{" +
                "transformerId=" + transformerId +
                ", ruleId=" + ruleId +
                ", name='" + name + '\'' +
                ", parseExpression='" + parseExpression + '\'' +
                ", parseType='" + parseType + '\'' +
                '}';
    }
}
