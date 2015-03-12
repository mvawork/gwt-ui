package ru.mvawork.gwt.client.ui.sliders;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import ru.mvawork.gwt.client.ui.events.BarValueChangeEvent;

import java.util.ArrayList;

public class HorizontalSliderBar<T> extends Composite implements BarValueChangeEvent.HasBarValueChangeHandler {


    public interface Resources extends ClientBundle {
        @Source("polzunok.png")
        ImageResource polzunok();
        @Source("HorizontalSliderBar.css")
        Style style();
    }

    public interface Style extends CssResource {
        String slider();
        String scale();
        String drag();
        String progress();
        String marks();
        String markTic();
        String markLabel();
    }

    public static class Appearance {

        private Resources resources;

        public Appearance() {
            this.resources = GWT. create(Resources.class);
            resources.style().ensureInjected();
        }

        public Resources getResources() {
            return resources;
        }
    }

    private class DragPanel extends FlowPanel {

        private boolean inDrag = true;
        private int touchPosition, minTouchPosition, maxTouchPosition ;

        public DragPanel() {
            setStyleName(style.drag());
            sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEMOVE | Event.ONMOUSEUP);
        }

        private int getDragPosition() {
            return getElement().getOffsetLeft();
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            switch (event.getTypeInt()) {
                case Event.ONMOUSEDOWN:
                    event.preventDefault();
                    event.stopPropagation();
                    DOM.setCapture(getElement());
                    inDrag = true;
                    touchPosition = event.getClientX();
                    minTouchPosition = touchPosition - getDragPosition() + minDragPos;
                    maxTouchPosition = minTouchPosition + maxDragPos - minDragPos;
                    break;
                case Event.ONMOUSEMOVE:
                    if (inDrag) {
                        event.preventDefault();
                        event.stopPropagation();
                        int newTochPosition = checkValue(event.getClientX(), minTouchPosition, maxTouchPosition);
                        setDragPosition(getDragPosition() + newTochPosition - touchPosition);
                        touchPosition = newTochPosition;
                    }

                    break;
                case Event.ONMOUSEUP:
                    event.preventDefault();
                    event.stopPropagation();
                    inDrag = false;
                    DOM.releaseCapture(drag.getElement());
                    setIntValue(getIntValueFromPosition(getDragPosition()));
                    break;
            }
        }
    }

    private static final Appearance appearance = new Appearance();

    private Style style = appearance.getResources().style();

    private AbsolutePanel rootPanel;
    private AbsolutePanel scale;
    private DragPanel drag;
    private FlowPanel progress;
    private AbsolutePanel marks;


    private int maxVal, curVal;
    private int minDragPos, maxDragPos;

    private int barWidth;
    private int dragTop;
    private int step;

    private ArrayList<T> values;

    public HorizontalSliderBar() {
        rootPanel = new AbsolutePanel();
        rootPanel.setStyleName(style.slider());
        /* Шкала */
        scale = new AbsolutePanel();
        scale.setStyleName(style.scale());
        scale.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                event.stopPropagation();
                event.preventDefault();
                setIntValue(getIntValueFromPosition(event.getRelativeX(scale.getElement())));
            }
        }, MouseDownEvent.getType());

        progress = new FlowPanel();
        progress.setStyleName(style.progress());
        scale.add(progress);
        rootPanel.add(scale);
        drag = new DragPanel();
        rootPanel.add(drag);
        marks = new AbsolutePanel();
        marks.setStyleName(style.marks());
        rootPanel.add(marks);
        initWidget(rootPanel);
    }


    @Override
    protected void onLoad() {
        super.onLoad();
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                HorizontalSliderBar.this.barWidth = rootPanel.getOffsetWidth();
                drawScrollBar();
            }
        });
    }

    private int checkValue(int value, int minValue, int maxValue) {
        if (minValue > maxValue)
            throw new RuntimeException("MinValue is more than MaxValue");
        value = value < minValue ? minValue : value;
        return value > maxValue ? maxValue: value;
    }

    private int getIntValueFromPosition(int position) {
        return Math.round((float)(position - minDragPos)/step);
    }

    private void setDragPosition(int position) {
        position = checkValue(position, minDragPos, maxDragPos);
        rootPanel.setWidgetPosition(drag, position, dragTop);
        progress.setWidth(position - minDragPos + "px");

    }

    private void setIntValue(int value){
        value = (value > maxVal) ? maxVal : value;
        curVal = (value < 0) ? 0 : value;
        fireEvent(new BarValueChangeEvent<>(values.get(value)));
        if (!isAttached())
            return;
        setDragPosition(curVal * step);
    }

    private void drawScrollBar() {
        if (!isAttached())
            return;
        if (values == null)
            throw new RuntimeException("Values is not assigned");
        /* Отступ сверху шкалы и ползунка исходя из высоты */
        int h1, h2, h;
        h1 = drag.getOffsetHeight();
        h2 = scale.getOffsetHeight();
        h = Math.max(h1, h2);
        dragTop = (h-h1)/2;
        int scaleTop = (h-h2)/2;
        /* Левый край позунка 0 */
        minDragPos = 0;
        /* Отступ слева шкалы исходя из ширины ползунка */
        int scaleLeft = (int) Math.floor(drag.getOffsetWidth() / 2 + 0.5);
        int scaleWidth = barWidth - scaleLeft * 2;
        /* Ширина шкалы пропрционально количеству значений */
        scaleWidth = scaleWidth - scaleWidth % maxVal;
        scale.setWidth(scaleWidth + "px");
        /* Максимальная позиция левого края ползунка */
        maxDragPos = minDragPos + scaleWidth;
        /* Шаг перемещения ползунка */
        step = scaleWidth / maxVal;
        /* Разместить шкалу */
        rootPanel.setWidgetPosition(scale, scaleLeft, scaleTop);
        /* Разместить ползунок */
        setIntValue(curVal);
        /* Разместить линейку шкалы */
        marks.clear();
        rootPanel.setWidgetPosition(marks, scaleLeft, scaleTop + h2);
        int markLeft = 0;
        int labelRight, labelWidth;
        for (Object value : values) {
            /* Отметки шкалы */
            InlineLabel m = new InlineLabel();
            m.setStyleName(style.markTic());
            marks.add(m);
            marks.setWidgetPosition(m, markLeft, 0);
            /* Надписи на шкале */
            InlineLabel l = new InlineLabel(value.toString());
            l.setStyleName(style.markLabel());
            marks.add(l);
            labelWidth = l.getOffsetWidth();
            labelRight = Math.min(markLeft + labelWidth, scaleWidth);
            marks.setWidgetPosition(l, labelRight - labelWidth, m.getOffsetHeight() + 1);
            /* Следущая позиция */
            markLeft = Math.min(step + markLeft, scaleWidth - 1);

        }
        marks.setWidth(scaleWidth + "px");
    }

    public void setValues(ArrayList<T> values) {
        this.values = values;
        maxVal = values.size() - 1;
        if (maxVal == 0)
            throw new RuntimeException("Quantity of values less than 2");

        if (isAttached())
            drawScrollBar();
    }

    public void setValue(T value) {
        setIntValue(values.indexOf(value));
    }

    @Override
    public HandlerRegistration addBarValueChangeHandler(BarValueChangeEvent.BarValueChangeHandler handler) {
        return addHandler(handler, BarValueChangeEvent.getType());
    }

}
