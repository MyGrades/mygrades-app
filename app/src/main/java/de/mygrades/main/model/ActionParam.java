package de.mygrades.main.model;

/**
 * POJO for ActionParam.
 */
public class ActionParam {
    private int actionParamId;
    private int actionId;
    private String key;
    private String value;

    public ActionParam() {}

    public int getActionParamId() {
        return actionParamId;
    }

    public void setActionParamId(int actionParamId) {
        this.actionParamId = actionParamId;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ActionParam{" +
                "actionParamId=" + actionParamId +
                ", actionId=" + actionId +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
