package chapter12.program23_3.launcher;

import imgui.ImGui;
import utilities.GLFWWindow;
import utilities.GUI;
import utilities.Program;

public class Program12_3 extends Program {
    @Override
    protected void init() {
        glfwWindow = new GLFWWindow(1500, 1000, "Prog12.3");
        gui = new GUI(glfwWindow, 3f) {
            @Override
            protected void drawFrame() {
                ImGui.newFrame(); // start frame
                ImGui.begin("Description"); // window
                ImGui.text("Program12.3: simple tessellated terrain.");
                ImGui.end();
                ImGui.render(); // end frame
            }
        };
    }

    @Override
    protected void getAllUniformLocs() {

    }

    @Override
    protected void drawScene() {

    }

    @Override
    protected void destroy() {

    }

    public static void main(String[] args) {
        new Program12_3().run(false);
    }
}
