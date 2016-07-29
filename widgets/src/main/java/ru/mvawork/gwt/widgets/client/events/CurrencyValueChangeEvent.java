package ru.mvawork.gwt.widgets.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import ru.mvawork.gwt.widgets.client.events.CurrencyValueChangeEvent.CurrencyValueChangeHandler;

public class CurrencyValueChangeEvent extends GwtEvent<CurrencyValueChangeHandler> {

    public interface CurrencyValueChangeHandler extends EventHandler {
        public void onCurrencyValueChange(CurrencyValueChangeEvent event);
    }

    public interface HasCurrencyValueChangeHandler {
        HandlerRegistration addCurrencyValueChangeHandler(CurrencyValueChangeHandler handler);
    }

    private static Type<CurrencyValueChangeHandler> type = new Type<>();

    private final String text;

    public CurrencyValueChangeEvent(String text) {
        this.text = text;
    }

    public static Type<CurrencyValueChangeHandler> getType() {
        return type;
    }

    @Override
    public Type<CurrencyValueChangeHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(CurrencyValueChangeHandler handler) {
        handler.onCurrencyValueChange(this);

    }


}
