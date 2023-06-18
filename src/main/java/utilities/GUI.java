package utilities;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import org.lwjgl.glfw.GLFW;

public abstract class GUI {
    final ImGuiIO IO;
    final ImGuiImplGl3 IM_GUI_GL3;
    final long WINDOW_ID;
    public GUI(int initWindowWidth, int initWindowHeight, long windowID, float fontScale) {
        WINDOW_ID = windowID;

        ImGui.createContext();
        ImGui.styleColorsLight();
        IM_GUI_GL3 = new ImGuiImplGl3();
        IM_GUI_GL3.init("#version 430 core");

        IO = ImGui.getIO();
        IO.setIniFilename(null);
        IO.setDisplaySize(initWindowWidth, initWindowHeight);
        IO.setFontGlobalScale(fontScale);
    }
    public void update() {

        drawFrame();
        // Cursor
        // left button down
        boolean isMouse1Down = GLFW.glfwGetMouseButton(WINDOW_ID, GLFW.GLFW_MOUSE_BUTTON_1) == 1;
        IO.setMouseDown(0, isMouse1Down);

        IM_GUI_GL3.renderDrawData(ImGui.getDrawData());
    }
    protected abstract void drawFrame();

    public void destroy() {
        IM_GUI_GL3.dispose();
        System.out.println("ImGuiGL3 disposed");
        ImGui.destroyContext();
        System.out.println("ImGui destroyed");
    }
}
