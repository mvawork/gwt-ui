package ru.mvawork.gwt.showcase.client.window;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import ru.mvawork.gwt.widgets.client.events.WindowCloseEvent;
import ru.mvawork.gwt.widgets.client.window.ApplicationWindow;

/**
 * Created by MenshevVA on 15.08.2016.
 */
public class TestWindow extends Composite {

    interface TestWindowUiBinder extends UiBinder<ApplicationWindow, TestWindow> {
    }

    private static TestWindowUiBinder ourUiBinder = GWT.create(TestWindowUiBinder.class);
    @UiField
    ApplicationWindow testWindow;

    public TestWindow() {
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    @UiHandler("testWindow")
    void onCloseAction(WindowCloseEvent event) {
        Window.alert("Close");
    }


}