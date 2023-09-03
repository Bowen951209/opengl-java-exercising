package engine.gui;

import imgui.ImGui;
import imgui.type.ImBoolean;

public class Checkbox implements GuiComponents {
    private final String label;
    private final ImBoolean isActive;

    public Checkbox(String label, boolean isActive) {
        this.label = label;
        this.isActive = new ImBoolean(isActive);
    }

    public boolean getIsActive() {
        return isActive.get();
    }

    @Override
    public void render() {
        ImGui.checkbox(label, isActive);
    }
}
