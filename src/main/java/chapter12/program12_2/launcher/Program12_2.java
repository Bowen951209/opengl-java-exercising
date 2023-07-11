package chapter12.program12_2.launcher;

import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import utilities.*;
import utilities.sceneComponents.Texture;

import static org.lwjgl.opengl.GL43.*;

public class Program12_2 extends App {
    private int program;

    public static void main(String[] args) {
        new Program12_2().run(false);
        // I don't know why when you enable cull face, you just view nothing in the case you use tess,
        // so I just disabled it.
    }
    @Override
    protected void init() {
        // give window.
        glfwWindow = new GLFWWindow(1500, 1000, "Prog12.2");

        // Programs
        program = new ShaderProgram(
                "assets/shaders/program12_2/vertex.glsl",
                "assets/shaders/program12_2/fragment.glsl",
                "assets/shaders/program12_2/TCS.glsl",
                "assets/shaders/program12_2/TES.glsl"
                ).use(); // use method returns ID.

        // VAO
        int vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Textures
        Texture texture = new Texture(0, "assets/textures/imageTextures/tiles.jpg");
        texture.bind();

        // GUI
        gui = new GUI(glfwWindow.getWidth(), glfwWindow.getHeight(), glfwWindow.getID(), 3f) {
            @Override
            protected void drawFrame() {
                ImGui.newFrame(); // start frame

                ImGui.begin("Description"); // window
                ImGui.text("Program12.1: tessellation with a Bezier surface.");

                ImGui.end();

                ImGui.render(); // end frame
            }
        };

        // Hints
        System.out.println("Hint: You can press F1 to look around.");
    }

    @Override
    protected void drawScene() {

        glUseProgram(program);
        Matrix4f vMat = new Matrix4f();
        Matrix4f mMat = new Matrix4f();
        Matrix4f mvpMat = new Matrix4f();

        vMat.set(camera.getVMat());

        Vector3f terLoc = new Vector3f(0.0f, 0.0f, 0.0f);
        mMat.identity().setTranslation(terLoc.x(), terLoc.y(), terLoc.z());
        mMat.rotateX((float) Math.toRadians(35.0f));

        mvpMat.set(camera.getProjMat());
        mvpMat.mul(vMat);
        mvpMat.mul(mMat);

        glUniformMatrix4fv(mvpLoc, false, mvpMat.get(ValuesContainer.VALS_OF_16));

        glFrontFace(GL_CCW);

        glPatchParameteri(GL_PATCH_VERTICES, 16);
        glDrawArrays(GL_PATCHES, 0, 16);

    }

    private int mvpLoc;
    @Override
    protected void getAllUniformLocs() {
        mvpLoc = glGetUniformLocation(program, "mvp_matrix");
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID(), gui);
    }
}
