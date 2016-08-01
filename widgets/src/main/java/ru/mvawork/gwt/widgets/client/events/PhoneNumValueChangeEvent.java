package ru.mvawork.gwt.widgets.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class PhoneNumValueChangeEvent extends GwtEvent<PhoneNumValueChangeEvent.PhoneNumValueChangeHandler> {

    public interface PhoneNumValueChangeHandler extends EventHandler {
        void onPhoneNumValueChange(PhoneNumValueChangeEvent event);
    }

    public interface HasPhoneNumValueChangeHandler {
        HandlerRegistration addPhoneNumValueChangeHandler(PhoneNumValueChangeEvent.PhoneNumValueChangeHandler handler);
    }

    private static Type<PhoneNumValueChangeEvent.PhoneNumValueChangeHandler> type = new Type<>();

    private final String value;

    public PhoneNumValueChangeEvent(String value) {
        this.value = value;
    }

    public static Type<PhoneNumValueChangeEvent.PhoneNumValueChangeHandler> getType() {
        return type;
    }

    @Override
    public Type<PhoneNumValueChangeEvent.PhoneNumValueChangeHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(PhoneNumValueChangeEvent.PhoneNumValueChangeHandler handler) {
        handler.onPhoneNumValueChange(this);
    }

}
