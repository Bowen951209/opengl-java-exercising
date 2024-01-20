package net.bowen.engine.gui;

import imgui.ImGui;
import imgui.type.ImBoolean;

public class Button implements GuiComponents {
    private final String content;
    protected final ImBoolean isClick = new ImBoolean();

    public Button(String content) {
        this.content = content;
    }

    @Override
    public void render() {
        isClick.set(ImGui.button(content));
    }
}
