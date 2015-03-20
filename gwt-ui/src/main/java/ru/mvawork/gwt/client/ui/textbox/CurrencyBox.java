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

    RegExp regExp = RegExp.compile("^\\d*(\\.\\d{0,2})?$");


    private static final Logger log = Logger.getLogger(CurrencyBox.class.getName());

    private static String getFormatedInput(CharSequence text) {
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
            String s = object.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
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
            if (!s.equals("00")) {
                sb.append(".");
                sb.append(s);
            }

            return sb.toString();
        }
    }

    private static class MoneyParser implements Parser<BigDecimal> {

        @Override
        public BigDecimal parse(CharSequence text) throws ParseException {
            try {
                return text == null ? null : new BigDecimal(getFormatedInput(text));
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
        text = getFormatedInput(text);
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
                        if (!checkInputText(getFormatedInput(getText())))
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

    @Override
    public void onKeyPress(KeyPressEvent event) {
        Character c = event.getCharCode();
        if (Character.isDigit(c) || c.equals('.')) {
            String text = getText();
            if (text != null && !text.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int pos = getCursorPos();
                sb.append(text, 0, pos);
                sb.append(c);
                sb.append(text, pos + getSelectionLength(), text.length());
                if (!checkInputText(sb.toString()))
                    cancelKey();
            }
        } else {
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
                            if (!checkInputText(getFormatedInput(getText())))
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
        }
    }
}
