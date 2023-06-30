package chapter12.program12_1.launcher;

import chapter10.program10_4.launcher.Program10_4;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import utilities.Color;
import utilities.GLFWWindow;
import utilities.ShaderProgramSetter;
import utilities.ValuesContainer;
import utilities.callbacks.DefaultCallbacks;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

public class Program12_1 extends Program10_4 {

    public Program12_1(String title) {
        super(title);
    }

    @Override
    protected void init(String title) {
        // Basics
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        camera.step(.01f).setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, title);
        windowID = glfwWindow.getWindowHandle();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));

        // Callbacks
        new DefaultCallbacks(windowID, camera, false).bindToGLFW();

        // GL settings
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        // Programs
        program = new ShaderProgramSetter(
                "assets/shaders/program12_1/vertex.glsl",
                "assets/shaders/program12_1/fragment.glsl",
                "assets/shaders/program12_1/tcs.glsl",
                "assets/shaders/program12_1/tes.glsl"
        ).getProgram();

        // Camera
        camera.setPos(.5f, -.2f, 2f);

        // VAO
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Hints
        System.out.println("Hint: You can press F1 to look around.");
    }

    @Override
    protected void loop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.updateVMat();

        // Draw scene
        glUseProgram(program);
        int mvpLoc = glGetUniformLocation(program, "mvp_matrix");

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

        glPatchParameteri(GL_PATCH_VERTICES, 4);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);  // FILL or LINE
        glDrawArrays(GL_PATCHES, 0, 4);

        // small stuff
        camera.handle();

        glfwSwapBuffers(windowID);
        glfwPollEvents();
    }
    public static void main(String[] args) {
        new Program12_1("Basic Tessellation Mesh");
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
