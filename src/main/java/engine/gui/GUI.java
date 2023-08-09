package engine.gui;

import engine.GLFWWindow;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUI {
    private final ImGuiIO io;
    private final ImGuiImplGl3 imGuiImplGl3;
    private final long windowId;
    private final float fontScale;

    public float getFontScale() {
        return fontScale;
    }

    private final List<GuiComponents> componentsList = new ArrayList<>();
    public Map<String, Object> getElementStates() {
        return elementStates;
    }
    private final Map<String, Object> elementStates = new HashMap<>();
    public GUI(int initWindowWidth, int initWindowHeight, long windowID, float fontScale) {
        this.fontScale = fontScale;
        windowId = windowID;

        ImGui.createContext();
        ImGui.styleColorsLight();
        imGuiImplGl3 = new ImGuiImplGl3();
        imGuiImplGl3.init("#version 430 core");

        io = ImGui.getIO();
        io.setIniFilename(null);
        io.setDisplaySize(initWindowWidth, initWindowHeight);
        io.setFontGlobalScale(fontScale);

        initElementStates();
    }
    public GUI(GLFWWindow glfwWindow, float fontScale) {
        this(glfwWindow.getInitWidth(), glfwWindow.getInitHeight(), glfwWindow.getID(), fontScale);
    }
    public void update() {


        drawFrame();// old method

        for (GuiComponents i : componentsList) {
            i.render();
        }
        ImGui.render();


        // Cursor
        // left button down
        boolean isMouse1Down = GLFW.glfwGetMouseButton(windowId, GLFW.GLFW_MOUSE_BUTTON_1) == 1;
        io.setMouseDown(0, isMouse1Down);

        imGuiImplGl3.renderDrawData(ImGui.getDrawData());
    }
    @Deprecated
    protected void drawFrame() {ImGui.newFrame();}

    public GUI addComponents(GuiComponents components) {
        componentsList.add(components);
        return this;
    }

    protected void initElementStates() {}
    public void destroy() {
        imGuiImplGl3.dispose();
        System.out.println("ImGuiGL3 disposed");
        ImGui.destroyContext();
        System.out.println("ImGui destroyed");
    }
}
