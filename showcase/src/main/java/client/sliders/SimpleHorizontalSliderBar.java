package client.sliders;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import ru.mvawork.gwt.client.ui.sliders.HorizontalSliderBar;

public class SimpleHorizontalSliderBar extends HorizontalSliderBar<Integer> {

    public interface Resources extends ClientBundle {
        @Source("polzunok.png")
        ImageResource polzunok();
        @Source("SimpleHorizontalSliderBar.css")
        Style style();
    }

    private static class Appearance {

        Resources resources;

        public Appearance() {
            resources = GWT.create(Resources.class);
            resources.style().ensureInjected();
        }

        public Resources getResources() {
            return resources;
        }
    }

    private static Appearance appearance = new Appearance();


    @Override
    public Style getStyle() {
        return appearance.getResources().style();
    }


}
