package studio.robotmonkey.model_tools.gui.widgets;

import java.util.ArrayList;

public class ToggleGroup {
    ArrayList<ToggleItemWidget> widgets;

    public ToggleGroup() {
        widgets = new ArrayList<>();
    }

    public void AddWidget(ToggleItemWidget widget)
    {
        widget.setToggleGroup(this);
        widgets.add(widget);
    }

    public void WidgetPressed(ToggleItemWidget widget){
        widget.active = false;
        for(ToggleItemWidget toggle:widgets)
        {
            if(toggle != widget)
            {
                toggle.active = true;
            }
        }
    }
}
