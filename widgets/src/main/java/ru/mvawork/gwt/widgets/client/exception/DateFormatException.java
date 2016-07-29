package ru.mvawork.gwt.widgets.client.exception;

public class DateFormatException extends IllegalArgumentException {

    public DateFormatException() {
        super();
    }

    public DateFormatException(String s) {
        super(s);
    }

    static public DateFormatException forInputString(String s) {
        return new DateFormatException("For input string: \"" + s + "\"");
    }

}