package client;

import client.sliders.SimpleHorizontalSliderBar;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import ru.mvawork.gwt.client.ui.events.BarValueChangeEvent;
import ru.mvawork.gwt.client.ui.sliders.HorizontalSliderBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainEntryPoint implements EntryPoint {



    public void onModuleLoad() {
        List<Integer> l = Arrays.asList(3, 6, 12, 18, 24);
        final ArrayList<Integer> srokList = new ArrayList<>(l);
        int defSrok = srokList.size() - 1;
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

    }

}
