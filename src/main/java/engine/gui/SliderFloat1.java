package engine.gui;

import imgui.ImGui;
import imgui.ImVec2;

public class SliderFloat1 extends Slider{
    public SliderFloat1(String label, float[] values, float minValue, float maxValue) {
        super(label, values, minValue, maxValue);
    }

    @Override
    protected int detectHoveredSection(ImVec2 rectMin, ImVec2 rectMax) {
        final float sectionWidth = (rectMax.x - rectMin.x);

        final float section0minX = rectMin.x;
        final float section0maxX = section0minX + sectionWidth;

        int section = -1;

        if (ImGui.isMouseHoveringRect(section0minX, rectMin.y, section0maxX, rectMax.y)) {
            // Section 0
            section = 0;
        }

        return section;
    }
}
