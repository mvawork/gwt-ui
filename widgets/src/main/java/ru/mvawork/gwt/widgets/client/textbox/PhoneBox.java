package ru.mvawork.gwt.widgets.client.textbox;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBox;

import java.text.ParseException;

public class PhoneBox extends ValueBox<String> implements KeyPressHandler, KeyDownHandler {

    static final private String phoneMask = "+7(___)___-__-__";
    static final private int phoneMaskLengh = phoneMask.length();

    private static class DateRender extends AbstractRenderer<String> {

        @Override
        public String render(String object) {
            if (object == null)
                return null;
            return applyMask(object, false);
        }

    }

    private static class DateParser implements Parser<String> {
        @Override
        public String parse(CharSequence text) throws ParseException {
            String unmaskedText = clearMask(text.toString());
            if (unmaskedText.length() == 0)
                return null;
            return unmaskedText;
        }
    }

    private static DateRender dateRender = new DateRender();
    private static DateParser dateParser = new DateParser();

    public PhoneBox() {
        super(Document.get().createTextInputElement(), dateRender, dateParser);
        addDomHandler(this, KeyPressEvent.getType());
        addDomHandler(this, KeyDownEvent.getType());
        sinkEvents(Event.ONPASTE | Event.FOCUSEVENTS);

    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONPASTE:
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        String text = getText();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < text.length(); i++) {
                            char c = text.charAt(i);
                            switch (c) {
                                case '0':case '1':case '2':
                                case '3':case '4':case '5':
                                case '6':case '7':case '8':
                                case '9':
                                    sb.append(c);
                                    break;
                            }
                        }
                        setText(applyMask(sb.toString(), false));
                    }
                });
                break;
            case Event.ONFOCUS:
                String s = getText();
                if (s == null || s.isEmpty()) {
                    setText(phoneMask);
                    Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            setCursorPos(0);
                        }
                    });
                }
                break;
            case Event.ONBLUR:
                try {
                    String phoneNum = getValue();
                    // TODO: 29.07.2016 вызвать событие на валидный номер
                } catch (Exception e) {
                    // TODO: 29.07.2016  Вызвать событие инвалидный номер
                }
                break;
        }
    }


    private static String clearMask(String maskedText) {
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < maskedText.length(); i ++) {
            char c = maskedText.charAt(i);
            switch (c) {
                case '0':case '1':case '2':case '3':case '4':case '5': case '6':case '7':case '8':case '9':
                    sb.append(c);
                    break;
                case '_':
                    sb.append('_');
                    break;
            }
        }
        return sb.toString().trim();
    }

    private static String applyMask(String value, boolean trimMask) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        int len = value.length();
        sb.append(phoneMask.substring(0, 3));
        for (int i = 3; i < phoneMaskLengh; i++) {
            if (trimMask && pos == len)
                break;
            char c = phoneMask.charAt(i);
            switch (c) {
                case ')':case '-':
                    sb.append(c);
                    break;
                default:
                    sb.append(pos < len ? value.charAt(pos++) : c);
                    break;
            }
        }
        return sb.toString();
    }

    private int getTextPos(String maskedText, int pos) {
        return clearMask(maskedText.substring(0, pos)).length();
    }

    private int getMaskPos(String text, int pos) {
        return applyMask(text.substring(0, pos), true).length();
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        StringBuilder sb = new StringBuilder();
        final String mt = getText();
        final int prevPos = getCursorPos();
        switch (event.getNativeKeyCode()) {
            case KeyCodes.KEY_Z:
            case KeyCodes.KEY_Y:
                cancelKey();
                break;
            case KeyCodes.KEY_DELETE:
                    int l = mt.length();
                    if (prevPos < l) {
                        String ut = clearMask(mt);
                        int ul = ut.length();
                        int n1 = getTextPos(mt, prevPos);
                        sb.append(ut, 0, n1);
                        int s = getSelectionLength();
                        if (s == 0)
                            sb.append(ut, n1 + 1, ul);
                        else
                            sb.append(ut, getTextPos(mt, prevPos + s), l);
                        setText(applyMask(sb.toString(), false));
                        cancelKey();
                        Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                            @Override
                            public void execute() {
                                setCursorPos(prevPos);
                            }
                        });
                    }
                break;
            case KeyCodes.KEY_BACKSPACE:
                int s = getSelectionLength();
                final String ut = clearMask(mt);
                int ul = ut.length();
                final int n1 = getTextPos(mt, prevPos);
                if (s > 0) {
                    sb.append(ut, 0, n1);
                    sb.append(ut, getTextPos(mt, prevPos + s), ul);
                    Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            setCursorPos(getMaskPos(ut, n1));
                        }
                    });
                } else {
                    if (n1 > 0) {
                        getMaskPos(ut, n1-1);
                        sb.append(ut, 0, n1-1);
                        sb.append(ut, n1, ul);
                        Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                            @Override
                            public void execute() {
                                setCursorPos(n1 - 1> 0 ? getMaskPos(ut, n1-1) : 0);
                            }
                        });
                    } else {
                        break;
                    }
                }
                setText(applyMask(sb.toString(), false));
                cancelKey();
                break;
        }
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {
        char c = event.getCharCode();
        if (c != 0) {
            try {
                switch (c) {
                    case '0': case '1': case '2': case '3': case '4': case '5':
                    case '6': case '7': case '8':
                    case '9':
                        int pos = getCursorPos();
                        //Если не на последей позиции
                        if (pos < phoneMaskLengh) {
                            final String text = getText();
                            // +7(
                            if (pos < 3)
                                pos = 3;
                            char p = text.charAt(pos);
                            if (p == ')' || p == '-')
                                pos++;
                            StringBuilder sb = new StringBuilder();
                            sb.append(text, 0, pos);
                            sb.append(c);
                            sb.append(text, pos + 1, phoneMaskLengh);
                            setText(sb.toString());
                            final int fpos = pos + 1;
                            Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                                @Override
                                public void execute() {
                                    setCursorPos(fpos + (text.charAt(fpos) == '.' ? 1 : 0));
                                }
                            });
                        }
                        break;
                }
            } finally {
                cancelKey();
            }
        }
    }
}