package ru.mvawork.gwt.client.ui.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class DateFormatErrorEvent extends GwtEvent<DateFormatErrorEvent.DateFormatErrorHandler> {

    public interface DateFormatErrorHandler extends EventHandler {
        public void onDateFormatError(DateFormatErrorEvent event);
    }

    public interface HasCurrencyFormatErrorHandler {
        HandlerRegistration addCurrencyFormatErrorHandler(DateFormatErrorHandler handler);
    }

    private static Type<DateFormatErrorHandler> type = new Type<>();

    private final String text;

    public DateFormatErrorEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static Type<DateFormatErrorHandler> getType() {
        return type;
    }

    @Override
    public Type<DateFormatErrorHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(DateFormatErrorHandler handler) {
        handler.onDateFormatError(this);
    }

}
