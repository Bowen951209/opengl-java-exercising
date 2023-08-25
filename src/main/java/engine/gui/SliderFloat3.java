package engine.gui;

import imgui.ImGui;
import imgui.ImVec2;

public class SliderFloat3 implements GuiComponents {
    private final float[] values;

    private final float minValue, maxValue;
    private final String label;
    private final float labelLength;
    private boolean isMouseWheelControlAble = false;
    private float wheelSpeed = 1f;

    public SliderFloat3 setWheelSpeed(float wheelSpeed) {
        this.wheelSpeed = wheelSpeed;
        return this;
    }

    private final ImVec2 rectMin = new ImVec2();
    private final ImVec2 rectMax = new ImVec2();

    public SliderFloat3 enableMouseWheelControl() {
        isMouseWheelControlAble = true;
        return this;
    }

    public SliderFloat3(String label, float[] values, float minValue, float maxValue) {
        this.label = label;
        this.values = values;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.labelLength = ImGui.getIO().getFontGlobalScale() * label.length() * 7f; // 7 is the number I tested out
    }

    @Override
    public void render() {
        ImGui.sliderFloat3(label, values, minValue, maxValue);

        ImGui.getItemRectMax(rectMax);
        rectMax.x -= labelLength;
        ImGui.getItemRectMin(rectMin);

        if (isMouseWheelControlAble) {
            handleWheelControl();
        }
    }

    private void handleWheelControl() {
        if (ImGui.getIO().getMouseWheel() != 0f) {
            if (ImGui.isMouseHoveringRect(rectMin.x, rectMin.y, rectMax.x, rectMax.y)) {
                int section = detectHoveredSection(rectMin, rectMax);

                if ((ImGui.getIO().getMouseWheel() > 0f && values[section] < maxValue) ||
                        (ImGui.getIO().getMouseWheel() < 0f && values[section] > minValue)) {
                        values[section] += ImGui.getIO().getMouseWheel() * wheelSpeed;
                }
            }
        }
    }

    private static int detectHoveredSection(ImVec2 rectMin, ImVec2 rectMax) {
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
