package ru.mvawork.gwt.widgets.client.textbox;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import ru.mvawork.gwt.widgets.client.exception.MaskParserException;

import java.text.ParseException;
import java.util.ArrayList;

class MaskParser extends AbstractRenderer<String> implements Parser<String> {

    private enum AllowedInput {notAllowed, digit}

    private static class InputChar {
        private final AllowedInput allowedInput;
        private final char maskChar;

        InputChar(AllowedInput allowedInput, char maskChar) {
            this.allowedInput = allowedInput;
            this.maskChar = maskChar;
        }


        AllowedInput getAllowedInput() {
            return allowedInput;
        }

        char getMaskChar() {
            return maskChar;
        }
    }


    private ArrayList<InputChar> inputChars = new ArrayList<>();
    private int minInputPos = 0;
    private char emptyInputMaskChar = '_';
    private String mask;

    MaskParser(String mask) {
        this.mask = mask;
        int n = 0;
        boolean isEscaped = false;
        while (n < mask.length()) {
            char c = mask.charAt(n++);
            if (isEscaped) {
                isEscaped = false;
                inputChars.add(new InputChar(AllowedInput.notAllowed, c));
                continue;
            }
            switch (c) {
                case '\\':
                    isEscaped = true;
                    break;
                case '9':
                    inputChars.add(new InputChar(AllowedInput.digit, emptyInputMaskChar));
                    break;
                default:
                    inputChars.add(new InputChar(AllowedInput.notAllowed, c));
                    break;
            }
        }
        for (InputChar i: inputChars) {
            if (i.getAllowedInput() != AllowedInput.notAllowed)
                break;
            minInputPos++;
        }


    }


    String applyMask(String value, boolean trimMask) {
        if (value == null)
            value = "";
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (int i = 0; i < inputChars.size(); i++){
            InputChar inputChar = inputChars.get(i);
            if (inputChar.getAllowedInput() == AllowedInput.notAllowed) {
                sb.append(inputChar.maskChar);
            } else {
                char c;
                if (n < value.length()) {
                    c = value.charAt(n++);
                    switch (inputChar.getAllowedInput()) {
                        case digit:
                            if (!Character.isDigit(c))
                                throw new MaskParserException(mask, i, c);
                            break;
                    }
                } else {
                    if (trimMask)
                        break;
                    c = inputChar.getMaskChar();
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    String clearMask(String maskedValue) {
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (int i = 0; i < inputChars.size(); i++) {
            InputChar inputChar = inputChars.get(i);
            if (n >= maskedValue.length())
                break;
            if (inputChar.getAllowedInput() == AllowedInput.notAllowed) {
                n++;
            } else {
                char c = maskedValue.charAt(n++);
                if (c == emptyInputMaskChar)
                    break;
                switch (inputChar.getAllowedInput()) {
                    case digit:
                        if (!Character.isDigit(c))
                            throw new MaskParserException(mask, i, c);
                        break;
                }
                sb.append(c);
            }
        }
        return  sb.toString();
    }

    int getMaskPos(String value, int pos) {
        int n = applyMask(value.substring(0, pos), true).length();
        while (n < inputChars.size() && inputChars.get(n).getAllowedInput() == AllowedInput.notAllowed)
            n++;
        return n;
    }

    int getTextPos(String maskedText, int pos) {
        return clearMask(maskedText.substring(0, pos)).length();
    }

    int getMinInputPos() {
        return minInputPos;
    }

    @Override
    public String parse(CharSequence text) throws ParseException {
        return clearMask(text.toString());
    }

    @Override
    public String render(String object) {
        return applyMask(object, false);
    }

}
