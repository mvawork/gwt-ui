package ru.mvawork.gwt.widgets.client.textbox;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBox;
import ru.mvawork.gwt.widgets.client.events.DateFormatErrorEvent;
import ru.mvawork.gwt.widgets.client.events.DateValueChangeEvent;

import java.text.ParseException;
import java.util.Date;

public class DateBox extends ValueBox<Date> implements KeyPressHandler, KeyDownHandler,
        DateFormatErrorEvent.HasDateFormatErrorHandler, DateValueChangeEvent.HasDateValueChangeHandler {

    static final private DateTimeFormat df = DateTimeFormat.getFormat("ddMMyyyy");
    static final private String dateMask = "__.__.____";
    static final private int dateMaskLengh = dateMask.length();

    @Override
    public HandlerRegistration addDateFormatErrorHandler(DateFormatErrorEvent.DateFormatErrorHandler handler) {
        return addHandler(handler, DateFormatErrorEvent.getType());
    }

    @Override
    public HandlerRegistration addDateValueChangeHandler(DateValueChangeEvent.DateValueChangeHandler handler) {
        return addHandler(handler, DateValueChangeEvent.getType());
    }

    private static class DateRender extends AbstractRenderer<Date> {
        @Override
        public String render(Date object) {
            if (object == null)
                return null;
            return applyMask(df.format(object), false);
        }
    }

    private static class DateParser implements Parser<Date> {
        @Override
        public Date parse(CharSequence text) throws ParseException {
            String unmaskedText = clearMask(text.toString());
            if (unmaskedText.length() == 0)
                return null;
            return df.parse(unmaskedText);
        }
    }

    private static DateRender dateRender = new DateRender();
    private static DateParser dateParser = new DateParser();

    public DateBox() {
        super(Document.get().createTextInputElement(), dateRender, new DateParser());
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
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
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
                    setText(dateMask);
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
                    Date d = getValue();
                    setValue(d);
                    fireEvent(new DateValueChangeEvent(d));
                } catch (Exception e) {
                    fireEvent(new DateFormatErrorEvent(getText()));
                }
                break;
        }
    }

    private static String clearMask(String text) {
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        if (len > dateMaskLengh)
            len = dateMaskLengh;
        for (int i = 0; i < len; i++) {
            char c = dateMask.charAt(i);
            char t = text.charAt(i);
            switch (c) {
                case '.':
                    break;
                default:
                    sb.append(t == c ? ' ' : t);
                    break;
            }
        }
        return sb.toString().trim();
    }

    private static String applyMask(String text, boolean trimMask) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        int len = text.length();
        for (int i = 0; i < dateMaskLengh; i++) {
            if (trimMask && pos == len)
                break;
            char c = dateMask.charAt(i);
            switch (c) {
                case '.':
                    sb.append(c);
                    break;
                default:
                    sb.append(pos < len ? text.charAt(pos++) : c);
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
    public void onKeyPress(KeyPressEvent event) {
        char c = event.getCharCode();
        if (c != 0) {
            try {
                switch (c) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        int pos = getCursorPos();
                        if (pos < dateMaskLengh) {
                            final String text = getText();
                            if (text.charAt(pos) == '.')
                                pos++;
                            StringBuilder sb = new StringBuilder();
                            sb.append(text, 0, pos);
                            sb.append(c);
                            sb.append(text, pos + 1, dateMaskLengh);
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

    @Override
    public void onKeyDown(KeyDownEvent event) {
        StringBuilder sb = new StringBuilder();
        final String text = getText();
        final int prevPos = getCursorPos();
        switch (event.getNativeKeyCode()) {
            case KeyCodes.KEY_Z:
            case KeyCodes.KEY_Y:
                cancelKey();
                break;
            case KeyCodes.KEY_DELETE:
                if (text != null && !text.isEmpty()) {
                    int l = text.length();
                    if (prevPos < l) {
                        String ut = clearMask(text);
                        int ul = ut.length();
                        int n1 = getTextPos(text, prevPos);
                        sb.append(ut, 0, n1);
                        int s = getSelectionLength();
                        if (s == 0)
                            sb.append(ut, n1 + 1, ul);
                        else
                            sb.append(ut, getTextPos(text, prevPos + s), l);
                        setText(applyMask(sb.toString(), false));
                        cancelKey();
                        Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                            @Override
                            public void execute() {
                                setCursorPos(prevPos);
                            }
                        });
                    }
                }
                break;
            case KeyCodes.KEY_BACKSPACE:
                if (text != null && !text.isEmpty()) {
                    int s = getSelectionLength();
                    final String ut = clearMask(text);
                    int ul = ut.length();
                    final int n1 = getTextPos(text, prevPos);
                    if (s > 0) {
                        sb.append(ut, 0, n1);
                        sb.append(ut, getTextPos(text, prevPos + s), ul);
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
                }
                break;
        }
    }
}
