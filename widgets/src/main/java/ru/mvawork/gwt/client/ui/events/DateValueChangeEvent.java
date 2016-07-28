package ru.mvawork.gwt.client.ui.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class DateValueChangeEvent extends GwtEvent<DateValueChangeEvent.DateValueChangeHandler> {

    public interface DateValueChangeHandler extends EventHandler {
        public void onDateValueChange(DateValueChangeEvent event);
    }

    public interface HasDateValueChangeHandler {
        HandlerRegistration addDateValueChangeHandler(DateValueChangeEvent.DateValueChangeHandler handler);
    }

    private static Type<DateValueChangeEvent.DateValueChangeHandler> type = new Type<>();

    private final String text;

    public DateValueChangeEvent(String text) {
        this.text = text;
    }

    public static Type<DateValueChangeEvent.DateValueChangeHandler> getType() {
        return type;
    }

    @Override
    public Type<DateValueChangeEvent.DateValueChangeHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(DateValueChangeEvent.DateValueChangeHandler handler) {
        handler.onDateValueChange(this);

    }

}
