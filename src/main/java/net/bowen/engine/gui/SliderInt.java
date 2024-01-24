package net.bowen.engine.gui;

import imgui.ImGui;

public abstract class SliderInt extends Slider {
    public SliderInt(String label, int[] values, float minValue, float maxValue, int numOfVals) {
        super(label, values, minValue, maxValue, numOfVals);
    }

    @Override
    public boolean invokeImGUI() {
        if (numOfVals == 1) {
            return ImGui.sliderInt(label, valuesInt, (int) minValue, (int) maxValue);
        } else throw new RuntimeException("Wrong numOfVals.");
    }
}
