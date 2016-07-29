package ru.mvawork.gwt.client.widgets.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import ru.mvawork.gwt.client.widgets.events.CurrencyFormatErrorEvent.CurrencyFormatErrorHandler;

public class CurrencyFormatErrorEvent extends GwtEvent<CurrencyFormatErrorHandler> {

    public interface CurrencyFormatErrorHandler extends EventHandler {
        public void onCurrencyFormatError(CurrencyFormatErrorEvent event);
    }

    public interface HasCurrencyFormatErrorHandler {
        HandlerRegistration addCurrencyFormatErrorHandler(CurrencyFormatErrorHandler handler);
    }

    private static Type<CurrencyFormatErrorHandler> type = new Type<>();

    private final String text;

    public CurrencyFormatErrorEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static Type<CurrencyFormatErrorHandler> getType() {
        return type;
    }

    @Override
    public Type<CurrencyFormatErrorHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(CurrencyFormatErrorHandler handler) {
        handler.onCurrencyFormatError(this);
    }

}
