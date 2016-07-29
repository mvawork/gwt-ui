package ru.mvawork.gwt.client.widgets.exception;

public class CurrencyFormatException extends IllegalArgumentException {

    public CurrencyFormatException() {
        super();
    }

    public CurrencyFormatException(String s) {
        super(s);
    }

    static public CurrencyFormatException forInputString(String s) {
        return new CurrencyFormatException("For input string: \"" + s + "\"");
    }

}
