package ru.mvawork.gwt.widgets.client.textbox;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.mvawork.gwt.widgets.client.events.MaskFormatErrorEvent;
import ru.mvawork.gwt.widgets.client.events.MaskValueChangeEvent;
import ru.mvawork.gwt.widgets.client.exception.MaskParserException;

@SuppressWarnings("WeakerAccess")
public class MaskBox extends ValueBox<String> implements KeyPressHandler, KeyDownHandler,
        MaskFormatErrorEvent.HasMaskFormatErrorHandler, MaskValueChangeEvent.HasMaskValueChangeHandler {

    private final MaskParser maskParser;

    public MaskBox(String mask) {
        this(new MaskParser(mask));
    }

    private MaskBox(MaskParser maskParser) {
        super(Document.get().createTextInputElement(), maskParser, maskParser);
        this.maskParser = maskParser;
        addDomHandler(this, KeyPressEvent.getType());
        addDomHandler(this, KeyDownEvent.getType());
        sinkEvents(Event.ONPASTE | Event.FOCUSEVENTS | Event.MOUSEEVENTS);
    }

    @Override
    public void onBrowserEvent(final Event event) {
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
                        setText(maskParser.applyMask(sb.toString(), false));
                    }
                });
                break;
            case Event.ONFOCUS:
                final String v = maskParser.clearMask(getText());
                setText(maskParser.applyMask(v, false));
                Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        setCursorPos(maskParser.getMaskPos(v, v.length()));
                    }
                });
                break;
            case Event.ONBLUR:
                String value = getValue();
                if (value.isEmpty())
                    setText("");
                fireEvent(new MaskValueChangeEvent(value));
                break;
            case Event.ONMOUSEDOWN:
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        int curPos = getCursorPos();
                        String ut = maskParser.clearMask(getText());
                        if (ut != null) {
                            int maxPos = maskParser.getMaskPos(ut, ut.length());
                            if (curPos > maxPos)
                                curPos = maxPos;
                        }
                        if (curPos < maskParser.getMinInputPos())
                            curPos = maskParser.getMinInputPos();
                        setCursorPos(curPos);
                    }
                });
                break;

        }
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
                    String ut = maskParser.clearMask(mt);
                    int ul = ut.length();
                    int n1 = maskParser.getTextPos(mt, prevPos);
                    sb.append(ut, 0, n1);
                    int s = getSelectionLength();
                    if (s == 0)
                        sb.append(ut, n1 + 1, ul);
                    else
                        sb.append(ut, maskParser.getTextPos(mt, prevPos + s), l);
                    setText(maskParser.applyMask(sb.toString(), false));
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
                final String ut = maskParser.clearMask(mt);
                int ul = ut.length();
                final int n1 = maskParser.getTextPos(mt, prevPos);
                final int maxPos = maskParser.getMaskPos(ut, ut.length());
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
                        sb.append(ut, maskParser.getTextPos(mt, prevPos + s), ul);
                        Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                            @Override
                            public void execute() {
                                setCursorPos(maskParser.getMaskPos(ut, n1));
                            }
                        });
                    } else {
                        if (n1 > 0) {
                            maskParser.getMaskPos(ut, n1 - 1);
                            sb.append(ut, 0, n1 - 1);
                            sb.append(ut, n1, ul);
                            Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                                @Override
                                public void execute() {
                                    setCursorPos(n1 - 1 > 0 ? maskParser.getMaskPos(ut, n1 - 1) : maskParser.getMinInputPos());
                                }
                            });

                        } else {
                            Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                                @Override
                                public void execute() {
                                    setCursorPos(maskParser.getMinInputPos());
                                }
                            });
                        }
                    }
                    setText(maskParser.applyMask(sb.toString(), false));
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
            int pos = getCursorPos();
            final String text = getText();
            String value = maskParser.clearMask(text);
            final int n = maskParser.getTextPos(text, pos);
            StringBuilder sb = new StringBuilder();
            sb.append(value, 0, n);
            sb.append(c);
            sb.append(value, n, value.length());
            try {
                final String m = sb.toString();
                setText(maskParser.applyMask(m, false));
                Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        setCursorPos(maskParser.getMaskPos(m, n + 1));
                    }
                });
            } catch (MaskParserException e) {
                fireEvent(new MaskFormatErrorEvent(e));
            }
        }
    }

    @Override
    public HandlerRegistration addMaskFormatErrorHandler(MaskFormatErrorEvent.MaskFormatErrorHandler handler) {
        return addHandler(handler, MaskFormatErrorEvent.getType());
    }

    @Override
    public com.google.gwt.event.shared.HandlerRegistration addMaskValueChangeHandler(MaskValueChangeEvent.MaskValueChangeHandler handler) {
        return addHandler(handler, MaskValueChangeEvent.getType());
    }
}
