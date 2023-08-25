package engine.gui;

import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Math;

public abstract class Slider implements GuiComponents {
    private final float[] values;

    private final float minValue, maxValue;
    private final String label;
    private final float labelLength;
    private boolean isMouseWheelControlAble = false;
    private float wheelSpeed = 1f;

    public Slider setWheelSpeed(float wheelSpeed) {
        this.wheelSpeed = wheelSpeed;
        return this;
    }

    private final ImVec2 rectMin = new ImVec2();
    private final ImVec2 rectMax = new ImVec2();

    public Slider enableMouseWheelControl() {
        isMouseWheelControlAble = true;
        return this;
    }
    public Slider(String label, float[] values, float minValue, float maxValue) {
        this.label = label;
        this.values = values;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.labelLength = ImGui.getIO().getFontGlobalScale() * label.length() * 7f; // 7 is the number I tested out
    }

    @Override
    public void render() {
        if (getClass().equals(SliderFloat1.class)) {
            ImGui.sliderFloat(label, values, minValue, maxValue);
        } else if (getClass().equals(SliderFloat3.class)) {
            ImGui.sliderFloat3(label, values, minValue, maxValue);
        } else if (getClass().equals(SliderFloat4.class)) {
            ImGui.sliderFloat4(label, values, minValue, maxValue);
        }

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
                    values[section] += Math.clamp(-1f, 1f, ImGui.getIO().getMouseWheel()) * wheelSpeed;
                }
            }
        }
    }

    protected abstract int detectHoveredSection(ImVec2 rectMin, ImVec2 rectMax);
}
