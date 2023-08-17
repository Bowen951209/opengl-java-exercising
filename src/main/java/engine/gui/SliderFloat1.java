package engine.gui;

import imgui.ImGui;

public class SliderFloat1 implements GuiComponents{
    private final float[] value;

    private final float minValue, maxValue;
    private final String label;
    public SliderFloat1(String label, float[] value, float minValue, float maxValue) {
        this.label = label;
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    @Override
    public void render() {
        ImGui.sliderFloat(label, value, minValue, maxValue);
    }
}
