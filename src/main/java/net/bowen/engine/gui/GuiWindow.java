package net.bowen.engine.gui;

import imgui.flag.ImGuiCond;
import imgui.internal.ImGui;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;

public class GuiWindow implements GuiComponents {
    private final String title;
    private final boolean isCloseable;
    private final ImBoolean isShow = new ImBoolean(false);

    private float initWidth, initHeight;

    public void setInitWidth(float initWidth) {
        this.initWidth = initWidth;
    }

    public void setInitHeight(float initHeight) {
        this.initHeight = initHeight;
    }

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

    /**
     * @param isCloseable whether the window is closeable. If set to true, you have to manually call {@link #show()} to show it.
     * */
    public GuiWindow(String title, boolean isCloseable) {
        this.title = title;
        this.isCloseable = isCloseable;
    }

    @Override
    public void render() {
        if (isCloseable) {
            if (isShow.get()) {
                if (initWidth != 0 || initHeight != 0)
                    ImGui.setNextWindowSize(initWidth, initHeight, ImGuiCond.Appearing);
                ImGui.begin(title, isShow);
                for (GuiComponents i : childComponents) {
                    i.render();
                }
                ImGui.end();
            }
        } else {
            if (initWidth != 0 || initHeight != 0)
                ImGui.setNextWindowSize(initWidth, initHeight);
            ImGui.begin(title);
            for (GuiComponents i : childComponents) {
                i.render();
            }
            ImGui.end();
        }
    }
}
