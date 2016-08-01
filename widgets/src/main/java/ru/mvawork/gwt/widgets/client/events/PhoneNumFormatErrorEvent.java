package ru.mvawork.gwt.widgets.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class PhoneNumFormatErrorEvent extends GwtEvent<PhoneNumFormatErrorEvent.PhoneNumFormatErrorHandler> {


        public interface PhoneNumFormatErrorHandler extends EventHandler {
            public void onPhoneNumFormatError(PhoneNumFormatErrorEvent event);
        }

        public interface HasPhoneNumFormatErrorHandler {
            HandlerRegistration addPhoneNumFormatErrorHandler(PhoneNumFormatErrorHandler handler);
        }

        private static Type<PhoneNumFormatErrorHandler> type = new Type<>();

        private final String text;

        public PhoneNumFormatErrorEvent(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public static Type<PhoneNumFormatErrorHandler> getType() {
            return type;
        }

        @Override
        public Type<PhoneNumFormatErrorHandler> getAssociatedType() {
            return type;
        }

        @Override
        protected void dispatch(PhoneNumFormatErrorHandler handler) {
            handler.onPhoneNumFormatError(this);
        }

}
