package ru.mvawork.gwt.widgets.client.textbox;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.mvawork.gwt.widgets.client.events.PhoneNumFormatErrorEvent;
import ru.mvawork.gwt.widgets.client.events.PhoneNumValueChangeEvent;

import java.text.ParseException;

public class PhoneBox extends ValueBox<String> implements KeyPressHandler, KeyDownHandler,
        PhoneNumFormatErrorEvent.HasPhoneNumFormatErrorHandler, PhoneNumValueChangeEvent.HasPhoneNumValueChangeHandler {

    static final private String phoneMask = "+7(___)___-__-__";
    static final private int phoneMaskLengh = phoneMask.length();

    @Override
    public HandlerRegistration addPhoneNumFormatErrorHandler(PhoneNumFormatErrorEvent.PhoneNumFormatErrorHandler handler) {
        return addHandler(handler, PhoneNumFormatErrorEvent.getType());
    }

    @Override
    public com.google.gwt.event.shared.HandlerRegistration addPhoneNumValueChangeHandler(PhoneNumValueChangeEvent.PhoneNumValueChangeHandler handler) {
        return addHandler(handler, PhoneNumValueChangeEvent.getType());
    }

    private static class PhoneNumRender extends AbstractRenderer<String> {

        @Override
        public String render(String object) {
            if (object != null && object.substring(0, 2) == "+7")
                object = object.substring(2);
            if (object == null)
                return null;
            return applyMask(object, false);
        }

    }

    private static class PhoneNumParser implements Parser<String> {
        @Override
        public String parse(CharSequence text) throws ParseException {
            String unmaskedText = clearMask(text.toString());
            if (unmaskedText.length() == 0)
                return null;
            return "+7" + unmaskedText;
        }
    }

    private static PhoneNumRender PhoneNumRender = new PhoneNumRender();
    private static PhoneNumParser PhoneNumParser = new PhoneNumParser();

    public PhoneBox() {
        super(Document.get().createTextInputElement(), PhoneNumRender, PhoneNumParser);
        addDomHandler(this, KeyPressEvent.getType());
        addDomHandler(this, KeyDownEvent.getType());
        sinkEvents(Event.ONPASTE | Event.FOCUSEVENTS | Event.MOUSEEVENTS);
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
                            if (Character.isDigit(c))
                                    sb.append(c);
                        }
                        setText(applyMask(sb.toString(), false));
                    }
                });
                break;
            case Event.ONFOCUS:
                String s = getText();
                if (s.isEmpty()) {
                    setText(phoneMask);
                    Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            setCursorPos(3);
                        }
                    });
                } else {
                    final String v = clearMask(s);
                    Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            setCursorPos(getMaskPos(v, v.length()));
                        }
                    });
                }
                break;
            case Event.ONBLUR:
                String phoneNum = getValue();
                if (phoneNum != null && phoneNum.length() != 12)
                    fireEvent(new PhoneNumFormatErrorEvent(phoneNum));
                else {
                    setValue(phoneNum);
                    fireEvent(new PhoneNumValueChangeEvent(phoneNum));
                }
                break;
            case Event.ONMOUSEDOWN:
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        int curPos = getCursorPos();
                        String ut = clearMask(getText());
                        if (ut != null) {
                            int maxPos = getMaskPos(ut, ut.length());
                            if (curPos > maxPos)
                                curPos = maxPos;
                        }
                        if (curPos < 3)
                            curPos = 3;
                        setCursorPos(curPos);
                    }
                });
                break;

        }
    }

    private static String clearMask(String maskedText) {
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < maskedText.length(); i ++) {
            char c = maskedText.charAt(i);
            if (Character.isDigit(c))
                sb.append(c);
        }
        return sb.toString();
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
        int maskPos = applyMask(text.substring(0, pos), true).length();
        while (maskPos < phoneMaskLengh && phoneMask.charAt(maskPos) != '_')
            maskPos++;
        return maskPos;
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
                final int maxPos = getMaskPos(ut, ut.length());
                if (maxPos < prevPos) {
                    Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            setCursorPos(maxPos);
                        }
                    });
                } else {
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
                            getMaskPos(ut, n1 - 1);
                            sb.append(ut, 0, n1 - 1);
                            sb.append(ut, n1, ul);
                            Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                                @Override
                                public void execute() {
                                    setCursorPos(n1 - 1 > 0 ? getMaskPos(ut, n1 - 1) : 3);
                                }
                            });

                        } else {
                            Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                                @Override
                                public void execute() {
                                    setCursorPos(3);
                                }
                            });
                        }
                    }
                    setText(applyMask(sb.toString(), false));
                }
                cancelKey();
                break;
        }
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {
        char c = event.getCharCode();
        if (c != 0) {
            cancelKey();
            if (Character.isDigit(c) && getSelectionLength() == 0) {
                int pos = getCursorPos();
                final String text = getText();
                String value = clearMask(text);
                final int n = getTextPos(text, pos);
                StringBuilder sb = new StringBuilder();
                sb.append(value, 0, n);
                sb.append(c);
                sb.append(value, n, value.length());
                final String m = applyMask(sb.toString(), false);
                setText(m);
                Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        setCursorPos(getMaskPos(m, n+1));
                    }
                });
            }
        }
    }
}