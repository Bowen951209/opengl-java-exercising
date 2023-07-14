package chapter14;

import engine.*;
import engine.models.TessGrid;
import imgui.ImGui;
import static org.lwjgl.opengl.GL43.*;

public class Main extends App {
    private TessGrid grid;
    @Override
    protected void init() {
        // p.s most are copied from Program12_4.

        // Window
        glfwWindow = new GLFWWindow(1500, 1000, "Chapter14");

        // Models
        grid = new TessGrid(
                "assets/textures/imageTextures/greenMountain.jpg",
                "assets/textures/heightMaps/greenMountain.jpg"
                );
        grid.setDrawMode(GL_FILL);

        // GUI
        gui = new GUI(glfwWindow, 3f) {
            @Override
            protected void drawFrame() {
                ImGui.newFrame(); // start frame
                ImGui.begin("Description"); // window
                ImGui.text("TODO: type descriptions here.");
                ImGui.end();
                ImGui.render(); // end frame
            }
        };
    }
    @Override
    protected void drawScene() {
        grid.updateState(camera);
        grid.draw(0);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID(), gui);
    }

    public static void main(String[] args) {
        new Main().run(false, true);
    }
}
