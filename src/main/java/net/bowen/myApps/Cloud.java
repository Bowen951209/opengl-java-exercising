package net.bowen.myApps;

import net.bowen.engine.App;
import net.bowen.engine.GLFWWindow;
import net.bowen.engine.ShaderProgram;
import net.bowen.engine.ShaderProgramBuilder;
import net.bowen.engine.gui.*;
import net.bowen.engine.sceneComponents.textures.Texture2D;
import net.bowen.engine.util.Destroyer;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL43.*;

public class Cloud extends App {
    private final float[] scale = {0.01f};
    private final float[] layer = {0};

    private ShaderProgram worleyNoiseShader;
    private Texture2D displayTexture;
    private IntBuffer workGroupSize;
    int numWorkGroupX, numWorkGroupY;

    @Override
    protected void initGLFWWindow() {
        glfwWindow = new GLFWWindow(1500, 1000, "Cloud");
    }

    @Override
    protected void initGUI() {
        gui = new GUI(glfwWindow, 2.5f);

        GuiWindow debugWindow = new GuiWindow("Debugger", true);
        debugWindow.show();
        debugWindow.addChild(new Text("Worley noise"));

        ImageDisplay imageDisplay = new ImageDisplay(displayTexture.getTexID(), 500);
        debugWindow.addChild(imageDisplay);

        SliderFloat1 scaleSlider = new SliderFloat1("Scale", scale, 0.01f, 0.5f);
        debugWindow.addChild(scaleSlider);

        SliderFloat1 layerSlider = new SliderFloat1("Layer", layer, 0, 200);
        debugWindow.addChild(layerSlider);

        gui.addComponents(new FpsDisplay(this));
        gui.addComponents(debugWindow);
    }

    @Override
    protected void initShaderPrograms() {
        worleyNoiseShader = new ShaderProgramBuilder().addShader(GL_COMPUTE_SHADER,
                "assets/shaders/utils/worley3D.glsl",
                "assets/shaders/cloudSimulate/worleyCompute.glsl").getProgram();
        worleyNoiseShader.use();

        workGroupSize = BufferUtils.createIntBuffer(3);
        glGetProgramiv(worleyNoiseShader.getID(), GL_COMPUTE_WORK_GROUP_SIZE, workGroupSize);
    }

    @Override
    protected void initTextures() {
        displayTexture = new Texture2D(0);
        displayTexture.bind();
        glBindImageTexture(0, displayTexture.getTexID(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA8);

        // Generate the worley noise texture with compute shader.
        numWorkGroupX = 500 / workGroupSize.get(0);
        numWorkGroupY = 500 / workGroupSize.get(1);
        displayTexture.fill(numWorkGroupX, numWorkGroupY, Color.BLACK); // init the texture size.


        System.out.println("Dispatch compute called.");
    }

    @Override
    protected void drawScene() {
        worleyNoiseShader.putUniform1f("scale", scale[0]);
        worleyNoiseShader.putUniform1f("layer", layer[0]);

        glDispatchCompute(numWorkGroupX, numWorkGroupY, 1);
        // Make sure writing to image has finished before read.
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID(), gui);
    }

    public static void main(String[] args) {
        new Cloud().run(true, true);
    }
}
