package ru.mvawork.gwt.client.ui.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;


public class BarValueChangeEvent <T> extends GwtEvent<BarValueChangeEvent.BarValueChangeHandler> {

    public interface BarValueChangeHandler extends EventHandler{
        void onBarValueChanged(BarValueChangeEvent event);
    }

    public interface HasBarValueChangeHandler {
        HandlerRegistration addBarValueChangeHandler(BarValueChangeHandler handler);
    }

    public static Type<BarValueChangeHandler> type = new Type<>();

    private final T value;

    public BarValueChangeEvent(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }


    public static Type<BarValueChangeHandler> getType() {
        return type;
    }

    @Override
    public Type<BarValueChangeHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(BarValueChangeHandler handler) {
        handler.onBarValueChanged(this);
    }


}
