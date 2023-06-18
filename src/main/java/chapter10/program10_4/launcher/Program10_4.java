package chapter10.program10_4.launcher;

import chapter10.program10_3.launcher.Program10_3;
import org.joml.Vector3f;
import utilities.Color;
import utilities.GLFWWindow;
import utilities.ShaderProgramSetter;
import utilities.ValuesContainer;
import utilities.callbacks.DefaultCallbacks;
import utilities.models.Grid;
import utilities.sceneComponents.Texture;

import java.nio.file.Path;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

public class Program10_4 extends Program10_3 {
    private Texture imageTexture, heightMap;
    private Grid grid;

    public Program10_4(String title) {
        super(title);
    }

    @Override
    protected void init(String title) {
        // Basics
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        CAMERA.step(.01f).setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, title);
        windowID = glfwWindow.getWindowHandle();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));

        // Callbacks
        new DefaultCallbacks(windowID, CAMERA, false).bindToGLFW();

        // GL settings
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        // Programs
        program = new ShaderProgramSetter((Path.of("assets/shaders/program10_4/vertex.glsl"))
                , Path.of("assets/shaders/program10_4/fragment.glsl"))
                .getProgram();
        getAllUniformsLoc();
        glUseProgram(program);

        // Textures
        imageTexture = new Texture(0, "assets/textures/imageTextures/heightTexture.jpg");
        heightMap = new Texture(1, "assets/textures/heightMaps/height.jpg");

        // Models
        grid = new Grid(new Vector3f(0f, -.1f, 4f));
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        // Hints
        System.out.println("Hint: You can press F1 to look around.");
    }

    @Override
    protected void drawScene() {
        imageTexture.bind();
        heightMap.bind();

        glUniformMatrix4fv(mvMatLoc, false, grid.getMV_MAT().get(ValuesContainer.VALS_OF_16));
        glUniformMatrix4fv(projMatLoc, false, CAMERA.getProjMat().get(ValuesContainer.VALS_OF_16));

        grid.updateState(CAMERA);
        grid.draw(GL_TRIANGLES);
    }

    public static void main(String[] args) {
        new Program10_4("Height Mapping");
    }

    private int mvMatLoc, projMatLoc;

    @Override
    protected void getAllUniformsLoc() {
        mvMatLoc = glGetUniformLocation(program, "mv_matrix");
        projMatLoc = glGetUniformLocation(program, "proj_matrix");
    }

    @Override
    protected void destroy() {
        // overrideable
        glfwDestroyWindow(windowID);
        System.out.println("GLFW window destroyed");
        glfwTerminate();
        System.out.println("GLFW terminated");
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        System.out.println("GLFW error callback freed");
    }
}
