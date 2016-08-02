package ru.mvawork.gwt.widgets.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.mvawork.gwt.widgets.client.exception.MaskParserException;

public class MaskFormatErrorEvent extends GwtEvent<MaskFormatErrorEvent.MaskFormatErrorHandler> {


    public interface MaskFormatErrorHandler extends EventHandler {
        void onMaskFormatError(MaskFormatErrorEvent event);
    }

    public interface HasMaskFormatErrorHandler {
        HandlerRegistration addMaskFormatErrorHandler(MaskFormatErrorHandler handler);
    }

    private static Type<MaskFormatErrorHandler> type = new Type<>();

    private MaskParserException exception;

    public MaskFormatErrorEvent(MaskParserException exception) {
        this.exception = exception;
    }

    public MaskParserException getException() {
        return exception;
    }

    public static Type<MaskFormatErrorHandler> getType() {
            return type;
        }

        @Override
        public Type<MaskFormatErrorHandler> getAssociatedType() {
            return type;
        }

        @Override
        protected void dispatch(MaskFormatErrorHandler handler) {
            handler.onMaskFormatError(this);
        }

}
