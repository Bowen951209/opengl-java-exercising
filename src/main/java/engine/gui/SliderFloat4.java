package engine.gui;

import imgui.ImGui;

public class SliderFloat4 implements GuiComponents{
    private final float[] values;

    private final float minValue, maxValue;
    private final String label;
    public SliderFloat4(String label, float[] values, float minValue, float maxValue) {
        this.label = label;
        this.values = values;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    @Override
    public void render() {
        ImGui.sliderFloat4(label, values, minValue, maxValue);
    }
}
