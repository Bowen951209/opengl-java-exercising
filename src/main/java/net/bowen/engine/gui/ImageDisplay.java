package net.bowen.engine.gui;

import imgui.ImGui;
import net.bowen.engine.sceneComponents.textures.Texture2D;

public class ImageDisplay implements GuiComponents{
    private final int displaySize;
    private final int texID;

    public ImageDisplay(int texID, int displaySize) {
        this.texID = texID;
        this.displaySize = displaySize;
    }

    public ImageDisplay(int usingUnit, String filepath, int displaySize) {
        this.texID = new Texture2D(usingUnit, filepath).getTexID();
        this.displaySize = displaySize;
    }

    @Override
    public void render() {
        // Render the full texture size.
        ImGui.image(texID, displaySize, displaySize);
    }
}
