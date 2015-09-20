package de.mygrades.util.exceptions;

/**
 * Exception for errors while parsing.
 */
@SuppressWarnings("unused")
public class ParseException extends Exception {

    public ParseException(){

    }

    public ParseException(String detailMessage) {
        super(detailMessage);
    }

    public ParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ParseException(Throwable throwable) {
        super(throwable);
    }
}
