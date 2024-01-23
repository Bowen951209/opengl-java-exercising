package net.bowen.engine.gui;

import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Math;

import java.util.HashSet;

public abstract class Slider implements GuiComponents {
    private final int numOfVals;
    private final float minValue, maxValue;
    private final float labelLength;
    private final float[] values;
    private final String label;

    private boolean isMouseWheelControlAble = false;
    private float wheelSpeed = 1f;

    public Slider setWheelSpeed(float wheelSpeed) {
        this.wheelSpeed = wheelSpeed;
        return this;
    }

    private final ImVec2 rectMin = new ImVec2();
    private final ImVec2 rectMax = new ImVec2();
    private final HashSet<Runnable> scrollCallbacks = new HashSet<>();

    public Slider addScrollCallBack(Runnable e) {
        scrollCallbacks.add(e);
        return this;
    }

    public void runCallbacks() {
        scrollCallbacks.forEach(Runnable::run);
    }

    public Slider enableMouseWheelControl() {
        isMouseWheelControlAble = true;
        return this;
    }

    public Slider(String label, float[] values, float minValue, float maxValue, int numOfVals) {
        this.label = label;
        this.values = values;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.numOfVals = numOfVals;
        this.labelLength = ImGui.getIO().getFontGlobalScale() * label.length() * 7f; // 7 is the number I tested out
    }

    @Override
    public void render() {
        boolean isSlide = false;

        if (getClass().equals(SliderFloat1.class)) {
            isSlide = ImGui.sliderFloat(label, values, minValue, maxValue);
        } else if (getClass().equals(SliderFloat3.class)) {
            isSlide = ImGui.sliderFloat3(label, values, minValue, maxValue);
        } else if (getClass().equals(SliderFloat4.class)) {
            isSlide = ImGui.sliderFloat4(label, values, minValue, maxValue);
        }

        if (isSlide) {
            scrollCallbacks.forEach(Runnable::run);
        }


        // For mouse wheel control
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
                int section = detectHoveredSection(rectMin, rectMax, numOfVals);

                // clamping
                if ((ImGui.getIO().getMouseWheel() > 0f && values[section] < maxValue) ||
                        (ImGui.getIO().getMouseWheel() < 0f && values[section] > minValue)) {
                    values[section] += ImGui.getIO().getMouseWheel() * wheelSpeed;
                    values[section] = Math.clamp(minValue, maxValue, values[section]);
                    scrollCallbacks.forEach(Runnable::run);
                }
            }

        }
    }

    private static int detectHoveredSection(ImVec2 rectMin, ImVec2 rectMax, int numOfVals) {
        if (numOfVals < 1) throw new IllegalArgumentException("numOfVals should >= 1");
        float sectionWidth = (rectMax.x - rectMin.x) / numOfVals;

        for (int i = 0; i < numOfVals; i++) {
            float offset = sectionWidth * i;
            float sectionMinX = rectMin.x + offset;
            float sectionMaxX = sectionMinX + sectionWidth;

            if (ImGui.isMouseHoveringRect(sectionMinX, rectMin.y, sectionMaxX, rectMax.y)) {
                System.out.println("Covering: " + i);
                return i;
            }

        }

        return -1;
    }
}
