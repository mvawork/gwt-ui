package ru.mvawork.gwt.widgets.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class WindowCloseEvent extends GwtEvent<WindowCloseEvent.WindowCloseHandler> {

    public interface WindowCloseHandler extends EventHandler {
        void onWindowClose(WindowCloseEvent event);
    }

    public interface HasWindowCloseHandler {
        HandlerRegistration addWindowCloseHandler(WindowCloseHandler handler);
    }

    public WindowCloseEvent() {
    }

    private static Type<WindowCloseHandler> type = new Type<>();

    public static Type<WindowCloseHandler> getType() {
        return type;
    }

    @Override
    public Type<WindowCloseHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(WindowCloseHandler handler) {
        handler.onWindowClose(this);
    }

}
