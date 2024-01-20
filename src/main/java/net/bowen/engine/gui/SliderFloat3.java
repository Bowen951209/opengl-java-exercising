package net.bowen.engine.gui;

import imgui.ImGui;
import imgui.ImVec2;

public class SliderFloat3 extends Slider {
    public SliderFloat3(String label, float[] values, float minValue, float maxValue) {
        super(label, values, minValue, maxValue);
    }

    @Override
    protected int detectHoveredSection(ImVec2 rectMin, ImVec2 rectMax) {
        final float sectionWidth = (rectMax.x - rectMin.x) / 3f;

        final float section0minX = rectMin.x;
        final float section0maxX = section0minX + sectionWidth;

        final float section1maxX = section0maxX + sectionWidth;

        int section;

        if (ImGui.isMouseHoveringRect(section0minX, rectMin.y, section0maxX, rectMax.y)) {
            // Section 0
            section = 0;
        } else if (ImGui.isMouseHoveringRect(section0maxX, rectMin.y, section1maxX, rectMax.y)) {
            // Section 1
            section = 1;
        } else {
            // Section 2
            section = 2;
        }

        return section;
    }
}
