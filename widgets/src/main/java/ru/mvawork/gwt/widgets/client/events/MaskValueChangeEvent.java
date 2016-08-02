package ru.mvawork.gwt.widgets.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class MaskValueChangeEvent extends GwtEvent<MaskValueChangeEvent.MaskValueChangeHandler> {

    public interface MaskValueChangeHandler extends EventHandler {
        void onPhoneNumValueChange(MaskValueChangeEvent event);
    }

    public interface HasMaskValueChangeHandler {
        HandlerRegistration addPhoneNumValueChangeHandler(MaskValueChangeHandler handler);
    }

    private static Type<MaskValueChangeHandler> type = new Type<>();

    private final String value;

    public MaskValueChangeEvent(String value) {
        this.value = value;
    }

    public static Type<MaskValueChangeHandler> getType() {
        return type;
    }

    @Override
    public Type<MaskValueChangeHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(MaskValueChangeHandler handler) {
        handler.onPhoneNumValueChange(this);
    }

}
