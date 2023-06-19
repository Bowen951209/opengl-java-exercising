package utilities;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public abstract class GUI {
    final ImGuiIO io;
    final ImGuiImplGl3 imGuiImplGl3;
    final long windowId;

    public Map<String, Object> getElementStates() {
        return elementStates;
    }
    private final Map<String, Object> elementStates = new HashMap<>();
    public GUI(int initWindowWidth, int initWindowHeight, long windowID, float fontScale) {
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
    public void update() {

        drawFrame();
        // Cursor
        // left button down
        boolean isMouse1Down = GLFW.glfwGetMouseButton(windowId, GLFW.GLFW_MOUSE_BUTTON_1) == 1;
        io.setMouseDown(0, isMouse1Down);

        imGuiImplGl3.renderDrawData(ImGui.getDrawData());
    }
    protected abstract void drawFrame();
    protected abstract void initElementStates();
    public void destroy() {
        imGuiImplGl3.dispose();
        System.out.println("ImGuiGL3 disposed");
        ImGui.destroyContext();
        System.out.println("ImGui destroyed");
    }
}
