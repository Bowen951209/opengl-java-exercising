package net.bowen.engine.gui;

import imgui.ImGui;

public class SliderFloat extends Slider {
    public SliderFloat(String label, float[] values, float minValue, float maxValue, int numOfVals) {
        super(label, values, minValue, maxValue, numOfVals);
    }

    @Override
    protected boolean invokeImGUI() {
        if (numOfVals == 1) {
            return ImGui.sliderFloat(label, valuesFloat, minValue, maxValue);
        } else if (numOfVals == 3) {
            return ImGui.sliderFloat3(label, valuesFloat, minValue, maxValue);
        } else if (numOfVals == 4) {
            return ImGui.sliderFloat4(label, valuesFloat, minValue, maxValue);
        } else throw new RuntimeException("Wrong numOfVals.");
    }
}
