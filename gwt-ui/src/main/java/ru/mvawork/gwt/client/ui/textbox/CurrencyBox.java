package ru.mvawork.gwt.client.ui.textbox;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBox;
import ru.mvawork.gwt.client.ui.exception.CurrencyFormatException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.logging.Logger;

public class CurrencyBox extends ValueBox<BigDecimal> implements ChangeHandler, KeyPressHandler, KeyDownHandler {

    private static final Logger log = Logger.getLogger(CurrencyBox.class.getName());

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
            return new ParsedText(object.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()).getText();
        }
    }

    private static class MoneyParser implements Parser<BigDecimal> {

        @Override
        public BigDecimal parse(CharSequence text) throws ParseException {
            try {
                if (text == null)
                    return null;
                BigDecimal decimal = new BigDecimal(getClearInput(text));
                decimal.setScale(2,  BigDecimal.ROUND_HALF_UP);
                return decimal;
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

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONPASTE:
                final String text = getText();
                final int prevPos = getCursorPos();
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {


                        String s = getText();

                        if (!s.isEmpty()) {
                            int pos;
                            log.fine(String.valueOf(prevPos));
                            if (text.length() >= s.length()) {
                                pos = s.length();
                            } else {
                                pos = prevPos + s.length() - text.length();
                            }
                            log.fine(s);
                            log.fine(String.valueOf(pos));
                            if (!trySetText(s, pos)) {
                                setText(text);
                                setCursorPos(prevPos);
                            }
                        }
                    }
                });
                break;
            case Event.ONBLUR:
                setValue(getValue());
                break;

        }
    }


    @Override
    public void onChange(ChangeEvent event) {
        setValue(getValue());
    }

    private static class ParsedText {
        private final int cursorPos;
        private final String text;

        public ParsedText(String text) {
            this(text, text.length());
        }

        public ParsedText(String text, int cursorPos) {
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
            this.text = sb.toString();
            this.cursorPos = cursorPos;
        }

        public int getCursorPos() {
            return cursorPos;
        }

        public String getText() {
            return text;
        }

    }

    private boolean trySetText(String text, int cursorPos) {
        try {
            ParsedText parsedText = new ParsedText(text, cursorPos);
            setText(parsedText.getText());
            setCursorPos(parsedText.getCursorPos());
            return true;
        } catch (CurrencyFormatException e) {
            //ToDo fire event InvalidInput
            return false;
        }
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {

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
            trySetText(sb.toString(), pos + 1);
        }
        if (c != 0)
            cancelKey();

    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        StringBuilder sb = new StringBuilder();
        NativeEvent ne = event.getNativeEvent();
        final String text = getText();
        final int prevPos = getCursorPos();;
        switch (event.getNativeKeyCode()) {
            case KeyCodes.KEY_Z:
            case KeyCodes.KEY_Y:
                if (ne.getCtrlKey()) {
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            String s = getText();
                            if (!trySetText(s, s.length())) {
                                setText(text);
                                setCursorPos(prevPos);
                            }
                        }
                    });
                    break;
                }
            case KeyCodes.KEY_DELETE:
                if (text != null && !text.isEmpty()) {
                    int l = text.length();
                    if (prevPos < l) {
                        sb.append(text, 0, prevPos);
                        int s = getSelectionLength();
                        if (s == 0) {
                            if (text.charAt(prevPos) == ' ' && prevPos < l - 1) {
                                sb.append(text, prevPos + 2, l);
                            } else {
                                sb.append(text, prevPos + 1, l);
                            }
                        } else {
                            sb.append(text, prevPos + s, l);
                        }
                        trySetText(sb.toString(), prevPos);
                        cancelKey();
                    }
                }

                break;
            case KeyCodes.KEY_BACKSPACE:
                if (text != null && !text.isEmpty()) {
                    int pos = prevPos;
                    int s = getSelectionLength();
                    int l = text.length();
                    if (s > 0) {
                        sb.append(text, 0, pos);
                        sb.append(text, pos + s, l);
                    } else {
                        if (pos > 0) {
                            if (pos > 1 && text.charAt(pos - 1) == ' ')
                                pos--;
                            sb.append(text, 0, pos - 1);
                            sb.append(text, pos, l);
                        }
                    }
                    trySetText(sb.toString(), pos - 1);
                    cancelKey();
                }
                break;
        }

    }
}
