package chapter12.program12_3.launcher;

import imgui.ImGui;
import org.joml.Matrix4f;
import utilities.*;
import utilities.sceneComponents.Texture;

import static org.lwjgl.opengl.GL43.*;

public class Program12_3 extends App {
    private Program program;
    private Texture texture;

    @Override
    protected void init() {
        glfwWindow = new GLFWWindow(1500, 1000, "Prog12.3");

        program = new Program(
                "assets/shaders/program12_3/vertex.glsl",
                "assets/shaders/program12_3/fragment.glsl",
                "assets/shaders/program12_3/TCS.glsl",
                "assets/shaders/program12_3/TES.glsl"
                );
        program.use();

        // VAO *every program must have a VAO*.
        int vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Texture
        texture = new Texture(0, "assets/textures/imageTextures/moon.jpg");

        // GUI
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

    private int mvpLoc;
    @Override
    protected void getAllUniformLocs() {
        // Only 1 location to get here.
        mvpLoc = glGetUniformLocation(program.getID(), "mvp");
    }

    private final Matrix4f mMat = new Matrix4f().scale(3f);
    private final Matrix4f mvpMat = new Matrix4f();
    @Override
    protected void drawScene() {
        // Update mvp mat every frame.
        mvpMat.set(camera.getProjMat()).mul(camera.getVMat()).mul(mMat); // Remember to make multiplication order, or it'll fuck up.
        glUniformMatrix4fv(mvpLoc, false, mvpMat.get(ValuesContainer.VALS_OF_16));

        // Render the patch.

        texture.bind();
        glPatchParameteri(GL_PATCH_VERTICES, 1);
        glDrawArrays(GL_PATCHES, 0, 1);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID(), gui);
    }

    public static void main(String[] args) {
        new Program12_3().run(false);
    }
}
