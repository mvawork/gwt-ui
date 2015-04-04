package ru.mvawork.gwt.client.ui.textbox;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBox;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.logging.Logger;

public class CurrencyBox extends ValueBox<BigDecimal> implements ChangeHandler, KeyPressHandler, KeyDownHandler {

    RegExp regExp = RegExp.compile("^\\d*(\\.\\d*)?$");


    private static final Logger log = Logger.getLogger(CurrencyBox.class.getName());

    public static class CurrencyFormatException extends IllegalArgumentException {

        public CurrencyFormatException() {
            super();
        }

        public CurrencyFormatException(String s) {
            super(s);
        }

        static CurrencyFormatException forInputString(String s) {
            return new CurrencyFormatException("For input string: \"" + s + "\"");
        }

    }


    private static String getClearInput(CharSequence text) {
        StringBuilder sb = new StringBuilder();
        for (int pos = 0; pos < text.length(); pos++) {
            Character c = text.charAt(pos);
            if (c != ' ')
                sb.append(c);
        }
        return sb.toString();
    }


    private static class MoneyRender extends AbstractRenderer<BigDecimal> {
        @Override
        public String render(BigDecimal object) {
            if (object == null)
                return "";
            String s = object.setScale(2, BigDecimal.ROUND_FLOOR).toPlainString();
            int l = s.length() - 3;
            StringBuilder sb = new StringBuilder();
            int z = l % 3;
            sb.append(s, 0, z);
            while (z < l) {
                if (z > 0)
                    sb.append(" ");
                sb.append(s, z, z + 3);
                z += 3;
            }
            /* Добавить дробную часть */
            s = s.substring(z+1, z+3);
            sb.append(".");
            sb.append(s);

            return sb.toString();
        }
    }

    private static class MoneyParser implements Parser<BigDecimal> {

        @Override
        public BigDecimal parse(CharSequence text) throws ParseException {
            try {
                return text == null ? null : new BigDecimal(getClearInput(text));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    private static MoneyRender moneyRender = new MoneyRender();
    private static MoneyParser moneyParser = new MoneyParser();

    public CurrencyBox() {
        super(Document.get().createTextInputElement(), moneyRender, moneyParser);
        addDomHandler(this, ChangeEvent.getType());
        addDomHandler(this, KeyPressEvent.getType());
        addDomHandler(this, KeyDownEvent.getType());
        sinkEvents(Event.ONPASTE);
    }

    private boolean checkInputText(String text) {
        log.fine(text);
        text = getClearInput(text);
        MatchResult matchResult = regExp.exec(text);
        return matchResult != null && matchResult.getInput().equals(text);
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONPASTE:
                final String text = getText();
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (!checkInputText(getClearInput(getText())))
                            setText(text);
                    }
                });
                break;
        }
    }


    @Override
    public void onChange(ChangeEvent event) {
        setValue(getValue());
    }

    private void setText(String text, int cursorPos) {
        StringBuilder sb = new StringBuilder();
        boolean allowDecimalSeparator = true;
        char c;
        int l = text.length();
        if (cursorPos > l)
            cursorPos = l;
        if (cursorPos < 0)
            cursorPos = 0;

        int digitNum = 0, spaceNum = 0;
        for (int i = l; i > 0; i--) {
            c = text.charAt(i-1);
            switch (c) {
                case ' ':
                    if (cursorPos >=i)
                        cursorPos--;
                    break;
                case '.':
                    if (!allowDecimalSeparator)
                        throw CurrencyFormatException.forInputString(text);
                    allowDecimalSeparator = false;

                    for (int j = sb.length(); j > 0; j--) {
                        if (sb.charAt(j-1) == ' ') {
                            sb.delete(j-1, j);
                        }
                    }
                    spaceNum = 0;
                    digitNum = 0;
                    break;
                case '0':case '1':case '2':case '3':case '4':
                case '5':case '6':case '7':case '8':case '9':
                    digitNum++;
                    if (digitNum > 3 && digitNum%3 == 1) {
                        sb.append(' ');
                        if (cursorPos > i) {
                            spaceNum++;
                        }
                    }
                    break;
                default:
                    throw CurrencyFormatException.forInputString(text);
            }
            if (c != ' ')
                sb.append(c);

        }
        cursorPos += spaceNum;
        sb.reverse();
        setText(sb.toString());
        setCursorPos(cursorPos);
    }


    @Override
    public void onKeyPress(KeyPressEvent event) {
        try {
            char c = event.getCharCode();
            if (Character.isDigit(c) || c == '.') {
                String text = getText();
                StringBuilder sb = new StringBuilder();
                int pos = getCursorPos();
                if (text != null && !text.isEmpty()) {
                    sb.append(text, 0, pos);
                    sb.append(c);
                    sb.append(text, pos + getSelectionLength(), text.length());
                } else {
                    if (c == '.') {
                        sb.append("0");
                        pos++;
                    }
                    sb.append(c);
                }
                setText(sb.toString(), pos + 1);
            }
        } catch (CurrencyFormatException e) {
            //ToDo fire event InvalidInput
        } finally {
            cancelKey();
        }

    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        StringBuilder sb = new StringBuilder();
        NativeEvent ne = event.getNativeEvent();
        final String text = getText();
        switch (event.getNativeKeyCode()) {
            case KeyCodes.KEY_Z:
            case KeyCodes.KEY_Y:
                if (ne.getCtrlKey()) {
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            if (!checkInputText(getClearInput(getText())))
                                setText(text);
                        }
                    });
                    break;
                }
            case KeyCodes.KEY_DELETE:
                if (text != null && !text.isEmpty()) {
                    int pos = getCursorPos();
                    if (pos >= 0) {
                        int l = text.length();
                        if (pos < l) {
                            sb.append(text, 0, pos);
                            int s = getSelectionLength();
                            if  (s == 0) {
                                sb.append(text, pos + 1, l);
                            } else {
                                sb.append(text, pos + s, l);
                            }
                            if (!checkInputText(sb.toString()))
                                cancelKey();
                        }
                    }
                }
                break;
            case KeyCodes.KEY_BACKSPACE:
                if (text != null && !text.isEmpty()) {
                    int pos = getCursorPos();
                    if (pos >= 0) {
                        int s = getSelectionLength();
                        int l = text.length();
                        if (s > 0) {
                            sb.append(text, 0, pos);
                            sb.append(text, pos + s, l);
                        } else {
                            sb.append(text, 0, pos -1);
                            sb.append(text, pos, l);
                        }
                        if (!checkInputText(sb.toString()))
                            cancelKey();
                    }
                }
                break;
            default:
                break;
        }

    }
}
