package ru.mvawork.gwt.widgets.client.window;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import ru.mvawork.gwt.widgets.client.events.WindowCloseEvent;

public class ApplicationWindow extends Composite implements WindowCloseEvent.HasWindowCloseHandler {


    public interface WindowStyle extends CssResource {
        String closeButton();
        String headerPanel();
        String contentPanel();
        String titlePanel();
        String windowPanel();
        String actionHeaderPanel();
        String dragHeaderPanel();

    }

    public interface Resources extends ClientBundle {
        @Source("../resources/headers/close.png")
        ImageResource closeButtonImage();
        @Source("../resources/headers/close_Over.png")
        ImageResource closeButtonOverImage();
        @Source("ApplicationWindow.css")
        WindowStyle style();
    }

    public static class Appearance {
        Resources resources;

        public Appearance() {
            this.resources = GWT.create(Resources.class);
            this.resources.style().ensureInjected();
        }
    }

    private static Appearance appearance = new Appearance();

    private static WindowUiBinder ourUiBinder = GWT.create(WindowUiBinder.class);

    interface WindowUiBinder extends UiBinder<FlowPanel, ApplicationWindow> {

    }

    private class DragHeaderPanel extends FlowPanel {

        private boolean isDraging = false;
        private int touchPositionX;
        private int touchPositionY;

        public DragHeaderPanel() {
            sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEMOVE | Event.ONMOUSEUP);
        }

        @Override
        public void onBrowserEvent(Event event) {

            super.onBrowserEvent(event);
            switch (event.getTypeInt()) {
                case Event.ONMOUSEDOWN:
                    event.preventDefault();
                    event.stopPropagation();
                    DOM.setCapture(getElement());
                    isDraging = true;
                    touchPositionX = event.getClientX();
                    touchPositionY = event.getClientY();
                    break;
                case Event.ONMOUSEMOVE:
                    if (isDraging) {
                        event.preventDefault();
                        event.stopPropagation();
                        int newTouchPositionX = event.getClientX();
                        newTouchPositionX = newTouchPositionX < 0 ? 0 : newTouchPositionX;
                        int newTouchPositionY = event.getClientY();
                        newTouchPositionY = newTouchPositionY < 0 ? 0 : newTouchPositionY;
                        int windowLeft =  windowPanel.getElement().getOffsetLeft() + newTouchPositionX - touchPositionX;
                        int windowTop = windowPanel.getElement().getOffsetTop() + newTouchPositionY - touchPositionY;
                        moveWindow(windowLeft, windowTop);
                        touchPositionX = newTouchPositionX;
                        touchPositionY = newTouchPositionY;
                    }
                    break;
                case Event.ONMOUSEUP:
                    event.preventDefault();
                    event.stopPropagation();
                    isDraging = false;
                    DOM.releaseCapture(getElement());
                    break;
            }
        }

    }


    @UiField(provided = true)
    WindowStyle style = appearance.resources.style();
    @UiField
    Label titleLabel;
    @UiField
    FlowPanel headerPanel;
    @UiField
    SimplePanel contentPanel;
    @UiField
    FlowPanel titlePanel;
    @UiField
    FlowPanel windowPanel;
    @UiField(provided = true)
    FlowPanel dragHeaderPanel;
    @UiField
    FlowPanel closeButton;
    @UiField
    FlowPanel actionHeaderPanel;

    public ApplicationWindow() {
        dragHeaderPanel = new DragHeaderPanel();
        initWidget(ourUiBinder.createAndBindUi(this));
        closeButton.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fireEvent(new WindowCloseEvent());
            }
        }, ClickEvent.getType());
    }

    private void moveWindow(int left, int top) {
        windowPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        windowPanel.getElement().getStyle().setLeft(left, Style.Unit.PX);
        windowPanel.getElement().getStyle().setTop(top, Style.Unit.PX);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public String getTitle(String title) {
        return  titleLabel.getText();
    }

    @Override
    public HandlerRegistration addWindowCloseHandler(WindowCloseEvent.WindowCloseHandler handler) {
        return addHandler(handler, WindowCloseEvent.getType());
    }

    private void updateDragHeaderWidth() {
        Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                dragHeaderPanel.getElement().getStyle().setWidth(actionHeaderPanel.getElement().getOffsetLeft() - dragHeaderPanel.getElement().getOffsetLeft(), Style.Unit.PX);
            }
        });
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        updateDragHeaderWidth();
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        updateDragHeaderWidth();
    }

    public HasOneWidget getContentDisplay() {
        return contentPanel;
    }
}