package ru.mvawork.gwt.widgets.client.window;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import ru.mvawork.gwt.widgets.client.events.WindowCloseEvent;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public class ApplicationWindow extends Composite implements WindowCloseEvent.HasWindowCloseHandler {

    private static Logger log = Logger.getLogger(ApplicationWindow.class.getName());


    public interface WindowStyle extends CssResource {
        String closeButton();
        String headerPanel();
        String contentPanel();
        String titlePanel();
        String windowPanel();
        String actionHeaderPanel();
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

    private class ClickPanel extends FlowPanel implements HasClickHandlers {

        ClickPanel() {
            addDomHandler(new MouseDownHandler() {
                @Override
                public void onMouseDown(MouseDownEvent event) {
                    event.stopPropagation();
                    event.preventDefault();
                }
            }, MouseDownEvent.getType());
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }

    }

    private class DragHeaderPanel extends FlowPanel {

        private int touchPositionX;
        private int touchPositionY;

        DragHeaderPanel() {
            sinkEvents(Event.ONMOUSEDOWN);
        }

        @Override
        public void onBrowserEvent(Event event) {

            super.onBrowserEvent(event);
            switch (event.getTypeInt()) {
                case Event.ONMOUSEDOWN:
                    event.preventDefault();
                    event.stopPropagation();
                    DOM.setCapture(getElement());
                    touchPositionX = event.getClientX();
                    touchPositionY = event.getClientY();
                    break;
                case Event.ONMOUSEMOVE:
                    int newTouchPositionX = event.getClientX();
                    newTouchPositionX = newTouchPositionX < 0 ? 0 : newTouchPositionX;
                    int newTouchPositionY = event.getClientY();
                    newTouchPositionY = newTouchPositionY < 0 ? 0 : newTouchPositionY;
                    int windowLeft =  windowPanel.getElement().getOffsetLeft() + newTouchPositionX - touchPositionX;
                    int windowTop = windowPanel.getElement().getOffsetTop() + newTouchPositionY - touchPositionY;
                    moveWindow(windowLeft, windowTop);
                    touchPositionX = newTouchPositionX;
                    touchPositionY = newTouchPositionY;
                    break;
                case Event.ONMOUSEUP:
                    DOM.releaseCapture(getElement());
                    break;
            }
        }

    }

    @UiField(provided = true)
    WindowStyle style = appearance.resources.style();
    @UiField
    Label titleLabel;
    @UiField(provided = true)
    FlowPanel headerPanel;
    @UiField
    SimplePanel contentPanel;
    @UiField
    FlowPanel titlePanel;
    @UiField
    FlowPanel windowPanel;
    @UiField
    FlowPanel actionHeaderPanel;

    private ClickPanel closeButton;


    public ApplicationWindow() {
        headerPanel = new DragHeaderPanel();
        initWidget(ourUiBinder.createAndBindUi(this));
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
        if (closeButton == null) {
            closeButton = new ClickPanel();
            closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    fireEvent(new WindowCloseEvent());
                }
            });
            closeButton.setStyleName(style.closeButton());
            actionHeaderPanel.add(closeButton);
        }
        return addHandler(handler, WindowCloseEvent.getType());
    }

    @UiChild(tagname = "content", limit = 1)
    public void addContent(Widget widget) {
        contentPanel.setWidget(widget);
    }
}