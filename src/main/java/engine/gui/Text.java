package engine.gui;

import imgui.internal.ImGui;

public class Text implements GuiComponents {
    private final String content;

    public Text(String content) {
        this.content = content;
    }
    @Override
    public void render() {
        ImGui.text(content);
    }
}
