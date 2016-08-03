package ru.mvawork.gwt.showcase.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import ru.mvawork.gwt.showcase.client.sliders.SimpleHorizontalSliderBar;
import ru.mvawork.gwt.widgets.client.events.BarValueChangeEvent;
import ru.mvawork.gwt.widgets.client.events.CurrencyFormatErrorEvent;
import ru.mvawork.gwt.widgets.client.events.CurrencyValueChangeEvent;
import ru.mvawork.gwt.widgets.client.sliders.HorizontalSliderBar;
import ru.mvawork.gwt.widgets.client.textbox.CurrencyBox;
import ru.mvawork.gwt.widgets.client.textbox.MaskBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainEntryPoint implements EntryPoint {



    public void onModuleLoad() {
        List<Integer> l = Arrays.asList(3, 6, 12, 18, 24);
        final ArrayList<Integer> srokList = new ArrayList<>(l);
        final ListBox srokListBox = new ListBox();
        for (Integer srok : srokList) {
            srokListBox.addItem(srok + "мес.");
        }

        RootPanel.get().add(srokListBox);

        final HorizontalSliderBar<Integer> sliderBar = new SimpleHorizontalSliderBar();
        sliderBar.setPixelSize(400, 50);
        sliderBar.setValues(srokList);

        RootPanel.get().add(sliderBar);

        srokListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                sliderBar.setSelectedIndex(srokListBox.getSelectedIndex());

            }
        });

        sliderBar.addBarValueChangeHandler(new BarValueChangeEvent.BarValueChangeHandler() {
            @Override
            public void onBarValueChanged(BarValueChangeEvent event) {
                srokListBox.setSelectedIndex(sliderBar.getSelectedIndex());
            }
        });

        sliderBar.setSelectedIndex(srokList.size() - 1);

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        CurrencyBox moneyInputBox = new CurrencyBox();
        moneyInputBox.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                Window.alert(event.getValue().toString());
            }
        });
        horizontalPanel.add(moneyInputBox);
        final Label moneyErrorLabel = new Label();
        horizontalPanel.add(moneyErrorLabel);

        moneyInputBox.addCurrencyFormatErrorHandler(new CurrencyFormatErrorEvent.CurrencyFormatErrorHandler() {
            @Override
            public void onCurrencyFormatError(CurrencyFormatErrorEvent event) {
                moneyErrorLabel.setText("Ошибка ввода");
            }
        });
        moneyInputBox.addCurrencyValueChangeHandler(new CurrencyValueChangeEvent.CurrencyValueChangeHandler() {
            @Override
            public void onCurrencyValueChange(CurrencyValueChangeEvent event) {
                moneyErrorLabel.setText(null);
            }
        });
        RootPanel.get().add(horizontalPanel);
        //
        HorizontalPanel dateTestPanel = new HorizontalPanel();
        final MaskBox dateBox = new MaskBox("99.99.9999");
        dateTestPanel.add(dateBox);
        final Label dateErrorLabel = new Label();
        dateTestPanel.add(dateErrorLabel);
        RootPanel.get().add(dateTestPanel);

        // Телефон
        HorizontalPanel phoneTestPanel = new HorizontalPanel();
        final MaskBox phoneBox = new MaskBox("+7(999)999-99-99");
        phoneTestPanel.add(phoneBox);
        final Label phoneErrorLabel = new Label();
        phoneTestPanel.add(phoneErrorLabel);
        RootPanel.get().add(phoneTestPanel);


    }

}
