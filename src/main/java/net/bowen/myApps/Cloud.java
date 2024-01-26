package net.bowen.myApps;

import net.bowen.engine.App;
import net.bowen.engine.GLFWWindow;
import net.bowen.engine.ShaderProgram;
import net.bowen.engine.ShaderProgramBuilder;
import net.bowen.engine.gui.*;
import net.bowen.engine.sceneComponents.PositionalLight;
import net.bowen.engine.sceneComponents.models.FileModel;
import net.bowen.engine.sceneComponents.textures.Texture2D;
import net.bowen.engine.util.Destroyer;
import net.bowen.engine.util.Material;
import net.bowen.engine.util.ValuesContainer;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL43.*;

public class Cloud extends App {
    private final int[] octaves = {3};
    private final float[] scale = {0.01f};
    private final float[] layer = {0};
    private final float[] persistence = {0.75f};
    private final float[] lacunarity = {1.47f};
    private final float[] boxMin = {-1f, 0.45f, -2f};
    private final float[] boxMax = {1.7f, 3.3f, 0.8f};
    private final float[] lightPos = new float[3];

    private FileModel terrainModel;
    private PositionalLight light;
    private ShaderProgram worleyNoiseShader;
    private ShaderProgram sceneShader;
    private ShaderProgram boxRaytraceShader;
    private Texture2D worleyDisplayTexture;
    private Texture2D raytraceTexture;
    int worleyNumWorkGroupX, worleyNumWorkGroupY;
    int raytraceNumWorkGroupX, raytraceNumWorkGroupY;

    @Override
    protected void initGLFWWindow() {
        glfwWindow = new GLFWWindow(1500, 1000, "Cloud");
    }

    @Override
    protected void initGUI() {
        gui = new GUI(glfwWindow, 2.5f);

        // Worley noise texture config window.
        GuiWindow texConfig = new GuiWindow("Texture Config", true);
        texConfig.show();
        texConfig.addChild(new Text("Worley noise"));

        ImageDisplay imageDisplay = new ImageDisplay(worleyDisplayTexture.getTexID(), 500);
        texConfig.addChild(imageDisplay);

        SliderFloat1 scaleSlider = new SliderFloat1("Scale", scale, 0.01f, 0.5f);
        texConfig.addChild(scaleSlider);

        SliderFloat1 layerSlider = new SliderFloat1("Layer", layer, 0, 200);
        texConfig.addChild(layerSlider);

        SliderInt1 octavesSlider = new SliderInt1("Octaves", octaves, 1, 10);
        texConfig.addChild(octavesSlider);

        SliderFloat1 persistenceSlider = new SliderFloat1("Persistence", persistence, 0.01f, 5f);
        texConfig.addChild(persistenceSlider);

        SliderFloat1 lacunaritySlider = new SliderFloat1("Lacunarity", lacunarity, 0.01f, 5f);
        texConfig.addChild(lacunaritySlider);


        // Cloud box Config Window.
        GuiWindow boxConfigWindow = new GuiWindow("Box Config", true);
        boxConfigWindow.show();

        boxConfigWindow.addChild(new Text("You can config the cloud box's min/max here."));

        SliderFloat3 boxMinSlider = new SliderFloat3("boxMin", boxMin, -5, 5);
        boxConfigWindow.addChild(boxMinSlider);

        SliderFloat3 boxMaxSlider = new SliderFloat3("boxMax", boxMax, -5, 5);
        boxConfigWindow.addChild(boxMaxSlider);

        // Test texture window.
        gui.addComponents(new ImageDisplay(raytraceTexture.getTexID(), 1000));

        // Light config window.
        GuiWindow lightConfigWindow = new GuiWindow("Config the light here", true);
        lightConfigWindow.show();

        SliderFloat3 lightPosSlider = new SliderFloat3("Light Position", lightPos, -5f, 5f);
        lightPosSlider.addScrollCallBack(() -> light.setPosition(lightPos[0], lightPos[1], lightPos[2]));
        lightConfigWindow.addChild(lightPosSlider);

        // Add the components to the GUI.
        gui.addComponents(new FpsDisplay(this));
        gui.addComponents(texConfig);
        gui.addComponents(boxConfigWindow);
        gui.addComponents(lightConfigWindow);
    }

    @Override
    protected void initShaderPrograms() {
        worleyNoiseShader = new ShaderProgramBuilder().addShader(GL_COMPUTE_SHADER,
                "assets/shaders/utils/worley3D.glsl",
                "assets/shaders/cloudSimulate/worleyCompute.glsl").getProgram();

        sceneShader = new ShaderProgram(
                "assets/shaders/cloudSimulate/vert.glsl",
                "assets/shaders/cloudSimulate/frag.glsl"
        );

        boxRaytraceShader = new ShaderProgram("assets/shaders/cloudSimulate/boxRayTracerCompute.glsl");
    }

    @Override
    protected void initTextures() {
        // Get the compute shaders local work group sizes.
        IntBuffer worleyShaderWGSize = getWorleyShaderWGSize();
        IntBuffer raytraceShaderWGSize = getRaytraceShaderWGSize();

        // The texture for displaying worley noise in a GUI window,
        worleyDisplayTexture = new Texture2D(0);
        worleyDisplayTexture.bind();
        glBindImageTexture(0, worleyDisplayTexture.getTexID(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA8);

        worleyNumWorkGroupX = 500 / worleyShaderWGSize.get(0);
        worleyNumWorkGroupY = 500 / worleyShaderWGSize.get(1);
        worleyDisplayTexture.fill(500, 500, Color.BLACK); // init the texture size.


        // The texture for displaying raytrace box in a GUI window.
        raytraceTexture = new Texture2D(1);
        raytraceTexture.bind();
        glBindImageTexture(1, raytraceTexture.getTexID(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA8);

        raytraceNumWorkGroupX = 1000 / raytraceShaderWGSize.get(0);
        raytraceNumWorkGroupY = 1000 / raytraceShaderWGSize.get(1);
        raytraceTexture.fill(1000, 1000, Color.BLACK); // init the texture size.

        // The texture for the terrain model.
        Texture2D terrainTexture = new Texture2D(0, "assets/textures/imageTextures/terrain.jpg");
        terrainTexture.bind();
    }

    @Override
    protected void initModels() {
        terrainModel = new FileModel("assets/models/terrain.obj", new Vector3f(0f, -30f, 0f), true) {
            @Override
            protected void updateMMat() {
                super.updateMMat();
                mMat.scale(100);
            }
        };
        addFileModel(terrainModel);

        light = new PositionalLight();
        light.setDrawable(camera);
    }

    @Override
    protected void drawScene() {
        // *** Noise texture
        worleyNoiseShader.use();

        // Put Uniforms
        worleyNoiseShader.putUniform1f("scale", scale[0]);
        worleyNoiseShader.putUniform1f("layer", layer[0]);
        worleyNoiseShader.putUniform1i("octaves", octaves[0]);
        worleyNoiseShader.putUniform1f("persistence", persistence[0]);
        worleyNoiseShader.putUniform1f("lacunarity", lacunarity[0]);

        // Call the compute shader to generate noise to texture.
        glDispatchCompute(worleyNumWorkGroupX, worleyNumWorkGroupY, 1);
        // Make sure writing to image has finished before read.
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);


        // *** Box raytracing
        boxRaytraceShader.use();

        // Put Uniforms
        boxRaytraceShader.putUniform3f("boxMin", boxMin);
        boxRaytraceShader.putUniform3f("boxMax", boxMax);
        boxRaytraceShader.putUniformMatrix4f("invVMat", camera.getInvVMat().get(ValuesContainer.VALS_OF_16));

        // Call the compute shader to generate noise to texture.
        glDispatchCompute(1000 / 8, 1000 / 4, 1);
        // Make sure writing to image has finished before read.
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);


        // *** Scene
        // Update objects states.
        camera.updateVMat();
        camera.handle();
        terrainModel.updateState(camera);
        light.getSphere().updateState(camera);

        sceneShader.use();
        // Put Uniforms
        light.putToUniforms(
                sceneShader.getUniformLoc("globalAmbient"),
                sceneShader.getUniformLoc("light.ambient"),
                sceneShader.getUniformLoc("light.diffuse"),
                sceneShader.getUniformLoc("light.specular"),
                sceneShader.getUniformLoc("light.position")
        );
        Material.getMaterial("GOLD").putToUniforms(
                sceneShader.getUniformLoc("material.shininess")
        );
        sceneShader.putUniformMatrix4f("normMat", terrainModel.getInvTrMat().get(ValuesContainer.VALS_OF_16));
        sceneShader.putUniformMatrix4f("mvMat", terrainModel.getMvMat().get(ValuesContainer.VALS_OF_16));
        sceneShader.putUniformMatrix4f("projMat", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        // The draw call.
        terrainModel.draw(GL_TRIANGLES);

        light.getSphere().draw(GL_TRIANGLES);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID(), gui);
    }

    private IntBuffer getWorleyShaderWGSize() {
        IntBuffer buffer = BufferUtils.createIntBuffer(3);
        glGetProgramiv(worleyNoiseShader.getID(), GL_COMPUTE_WORK_GROUP_SIZE, buffer);
        return buffer;
    }

    private IntBuffer getRaytraceShaderWGSize() {
        IntBuffer buffer = BufferUtils.createIntBuffer(3);
        glGetProgramiv(boxRaytraceShader.getID(), GL_COMPUTE_WORK_GROUP_SIZE, buffer);
        return buffer;
    }

    public static void main(String[] args) {
        new Cloud().run(true, true);
    }
}
