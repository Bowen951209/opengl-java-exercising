package net.bowen.engine.gui;

import imgui.internal.ImGui;

public class Text implements GuiComponents {
    private String content;

    public void setContent(String content) {
        this.content = content;
    }

    public Text(String content) {
        this.content = content;
    }
    @Override
    public void render() {
        ImGui.text(content);
    }
}
