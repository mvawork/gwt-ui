package ru.mvawork.gwt.widgets.client.exception;

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
