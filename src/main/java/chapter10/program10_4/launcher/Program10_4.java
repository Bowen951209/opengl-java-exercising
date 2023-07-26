package chapter10.program10_4.launcher;

import chapter10.program10_3.launcher.Program10_3;
import engine.gui.GUI;
import engine.sceneComponents.textures.Texture2D;
import engine.util.Color;
import engine.util.ValuesContainer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImInt;
import org.joml.Vector3f;
import engine.*;
import engine.callbacks.DefaultCallbacks;
import engine.models.RawGrid;

import java.nio.file.Path;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

public class Program10_4 extends Program10_3 {
    private Texture2D imageTexture, heightMap;
    private RawGrid grid;
    private GUI gui;

    public Program10_4(String title) {
        super(title);
    }

    @Override
    protected void init(String title) {
        // Basics
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        camera.step(.01f).setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, title);
        windowID = glfwWindow.getID();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));

        // Callbacks
        new DefaultCallbacks(windowID, camera, true).bindToGLFW();

        // GL settings
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        // Programs
        program = new ShaderProgram((Path.of("assets/shaders/program10_4/vertex.glsl"))
                , Path.of("assets/shaders/program10_4/fragment.glsl"))
                .getID();
        getAllUniformsLoc();
        glUseProgram(program);

        // Textures
        imageTexture = new Texture2D(0, "assets/textures/imageTextures/heightTexture.jpg");
        heightMap = new Texture2D(1, "assets/textures/heightMaps/height.jpg");

        // Models
        grid = new RawGrid(new Vector3f(0f, -.1f, 4f));

        // GUI
        gui = new GUI(WINDOW_INIT_W, WINDOW_INIT_H, windowID, 2.5f) {
            final ImInt SELECTION = new ImInt();
            final String[] POLYGON_MODES = {"GL_LINE", "GL_FILL"};
            final ImGuiIO io = ImGui.getIO();
            final float comboWidth = io.getDisplaySizeX() / 2f;
            final float comboHeight = io.getDisplaySizeY() / 7f;

            @Override
            protected void drawFrame() {
                ImGui.newFrame(); // start frame

                ImGui.begin("Debugger"); // window

                ImGui.setWindowSize(comboWidth, comboHeight);
                ImGui.combo("Polygon mode", SELECTION, POLYGON_MODES);
                Integer decidedPolygonMode = SELECTION.get() == 0 ? GL_LINE : GL_FILL;
                getElementStates().put("Polygon mode", decidedPolygonMode);

                ImGui.end();

                ImGui.render(); // end frame
            }

            @Override
            protected void initElementStates() {
                getElementStates().put("Polygon mode", GL_LINE);
            }
        };

        // Hints
        System.out.println("Hint: You can press F1 to look around.");
    }

    @Override
    protected void loop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.updateVMat();

        glPolygonMode(GL_FRONT_AND_BACK, (int)gui.getElementStates().get("Polygon mode")); // This is for gui to choose mode
        drawScene();

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        gui.update();

        camera.handle();

        glfwSwapBuffers(windowID);
        glfwPollEvents();
    }

    @Override
    protected void drawScene() {
        imageTexture.bind();
        heightMap.bind();

        glUniformMatrix4fv(mvMatLoc, false, grid.getMvMat().get(ValuesContainer.VALS_OF_16));
        glUniformMatrix4fv(projMatLoc, false, camera.getProjMat().get(ValuesContainer.VALS_OF_16));

        grid.updateState(camera);
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
        gui.destroy();
    }
}
