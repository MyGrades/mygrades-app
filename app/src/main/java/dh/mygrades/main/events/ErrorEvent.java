package dh.mygrades.main.events;

/**
 * Event to send an Error to subscribers, e.g. activities.
 */
public class ErrorEvent {
    private ErrorType type;
    private String msg;

    public ErrorEvent() {
    }

    public ErrorEvent(ErrorType type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public ErrorType getType() {
        return type;
    }

    public void setType(ErrorType type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public enum ErrorType {
        GENERAL, TIMEOUT, NO_NETWORK
    }
}
