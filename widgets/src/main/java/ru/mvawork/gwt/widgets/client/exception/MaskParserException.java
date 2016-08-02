package ru.mvawork.gwt.widgets.client.exception;

public class MaskParserException extends RuntimeException {

    private String mask;
    private int pos;
    private char inputChar;

    private MaskParserException() {
    }

    public MaskParserException(String mask, int pos, char inputChar) {
        this.mask = mask;
        this.pos = pos;
        this.inputChar = inputChar;
    }

    public String getMask() {
        return mask;
    }

    public int getPos() {
        return pos;
    }

    public char getInputChar() {
        return inputChar;
    }

}
