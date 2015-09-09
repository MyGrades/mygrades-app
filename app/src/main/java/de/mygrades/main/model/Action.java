package de.mygrades.main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO for action.
 */
public class Action {
    private int actionId;
    private int ruleId;
    private int position;
    private String method;
    private String url;
    private String parseExpression;
    private String parseType;
    private List<ActionParam> actionParams;

    public Action() {
        actionParams = new ArrayList<>();
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public List<ActionParam> getActionParams() {
        return actionParams;
    }

    public void setActionParams(List<ActionParam> actionParams) {
        this.actionParams = actionParams;
    }

    @Override
    public String toString() {
        return "Action{" +
                "actionId=" + actionId +
                ", ruleId=" + ruleId +
                ", position=" + position +
                ", method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", parseExpression='" + parseExpression + '\'' +
                ", parseType='" + parseType + '\'' +
                ", actionParams=" + actionParams +
                '}';
    }
}
