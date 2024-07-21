package net.bowen;

import net.bowen.engine.App;
import net.bowen.engine.GLFWWindow;
import net.bowen.engine.ShaderProgram;
import net.bowen.engine.sceneComponents.models.FullScreenQuad;
import net.bowen.engine.sceneComponents.models.Model;
import net.bowen.engine.sceneComponents.textures.Texture2D;
import net.bowen.engine.util.Destroyer;
import net.bowen.engine.util.ValuesContainer;

import static org.lwjgl.opengl.GL43.*;

public class Cloud extends App {
    private final int[] localGroupSize = new int[2];

    private ShaderProgram computeShaderProgram;
    private ShaderProgram screenShaderProgram;
    private Model screenQuad;
    private Texture2D computeTexture;
    private Texture2D noiseTexture;

    @Override
    protected void initGLFWWindow() {
        glfwWindow = new GLFWWindow(1920, 1080, "Cloud");
    }

    @Override
    protected void addCallbacks() {
        getDefaultCallbacks().getDefaultFrameBufferResizeCB().addCallback(
                () -> computeTexture.fill(glfwWindow.getCurrentWidth(), glfwWindow.getCurrentHeight(), null)
        );
    }

    @Override
    protected void initShaderPrograms() {
        computeShaderProgram = new ShaderProgram("assets/shaders/cloud/compute.glsl");
        screenShaderProgram = new ShaderProgram("assets/shaders/cloud/screenVert.glsl", "assets/shaders/cloud/screenFrag.glsl");

        // Get the local group size from the compute shader.
        glGetProgramiv(computeShaderProgram.getID(), GL_COMPUTE_WORK_GROUP_SIZE, localGroupSize);
    }

    @Override
    protected void initModels() {
        screenQuad = new FullScreenQuad();
    }

    @Override
    protected void initTextures() {
        computeTexture = new Texture2D(0);
        computeTexture.bind();
        computeTexture.fill(glfwWindow.getInitWidth(), glfwWindow.getInitHeight(), null);
        glBindImageTexture(0, computeTexture.getTexID(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA8);

        noiseTexture = new Texture2D(1, "assets/textures/noises/noise1.jpg");
        computeTexture.bind();
    }

    @Override
    protected void drawScene() {
        // Update the texture with the compute shader.
        computeShaderProgram.use();
        computeShaderProgram.putUniformMatrix4f("invViewMat", camera.getInvVMat().get(ValuesContainer.VALS_OF_16));
        computeShaderProgram.putUniform3f("camPos", camera.getPos().get(ValuesContainer.VALS_OF_3));

        // Pass in time in seconds.
        float time = (float) (System.currentTimeMillis() % 1000000) / 1000f;
        computeShaderProgram.putUniform1f("uTime", time);

        noiseTexture.bind();
        glDispatchCompute(glfwWindow.getCurrentWidth() / localGroupSize[0],
                glfwWindow.getCurrentHeight() / localGroupSize[1],
                1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        // Draw the texture to the screen.
        screenShaderProgram.use();
        screenQuad.draw(GL_TRIANGLES);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID());
    }

    public static void main(String[] args) {
        new Cloud().run();
    }
}
