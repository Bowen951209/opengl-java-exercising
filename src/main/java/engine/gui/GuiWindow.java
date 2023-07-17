package engine.gui;

import imgui.internal.ImGui;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;

public class GuiWindow implements GuiComponents {
    private final String title;
    private final boolean isCloseable;
    private final ImBoolean isShow = new ImBoolean(false);

    public ImBoolean isShow() {
        return isShow;
    }

    public void show() {
        isShow.set(true);
    }

    private final List<GuiComponents> childComponents = new ArrayList<>();

    public GuiWindow addChild(GuiComponents child) {
        childComponents.add(child);
        return this;
    }

    public GuiWindow(String title, boolean isCloseable) {
        this.title = title;
        this.isCloseable = isCloseable;
    }

    @Override
    public void render() {
        if (isCloseable) {
            if (isShow.get()) {
                ImGui.begin(title, isShow);
                for (GuiComponents i : childComponents) {
                    i.render();
                }
                ImGui.end();
            }
        } else {
            ImGui.begin(title);
            for (GuiComponents i : childComponents) {
                i.render();
            }
            ImGui.end();
        }
    }
}
