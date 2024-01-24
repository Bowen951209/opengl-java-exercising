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
    private final int[] octaves = {1};
    private final float[] scale = {0.05f};
    private final float[] layer = {0};
    private final float[] persistence = {0};
    private final float[] lacunarity = {0};

    private FileModel model;
    private PositionalLight light;
    private ShaderProgram worleyNoiseShader;
    private ShaderProgram sceneShader;
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

        SliderInt1 octavesSlider = new SliderInt1("Octaves", octaves, 1, 10);
        debugWindow.addChild(octavesSlider);

        SliderFloat1 persistenceSlider = new SliderFloat1("Persistence", persistence, 0.01f, 5f);
        debugWindow.addChild(persistenceSlider);

        SliderFloat1 lacunaritySlider = new SliderFloat1("Lacunarity", lacunarity, 0.01f, 5f);
        debugWindow.addChild(lacunaritySlider);

        gui.addComponents(new FpsDisplay(this));
        gui.addComponents(debugWindow);
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

        Texture2D terrainTexture = new Texture2D(0, "assets/textures/imageTextures/terrain.jpg");
    }

    @Override
    protected void initModels() {
        model = new FileModel("assets/models/terrain.obj", new Vector3f(0f, -30f, 0f), true) {
            @Override
            protected void updateMMat() {
                super.updateMMat();
                mMat.scale(100);
            }
        };
        addFileModel(model);

        light = new PositionalLight();
    }

    @Override
    protected void drawScene() {
        // Noise texture
        worleyNoiseShader.use();
        worleyNoiseShader.putUniform1f("scale", scale[0]);
        worleyNoiseShader.putUniform1f("layer", layer[0]);
        worleyNoiseShader.putUniform1i("octaves", octaves[0]);
        worleyNoiseShader.putUniform1f("persistence", persistence[0]);
        worleyNoiseShader.putUniform1f("lacunarity", lacunarity[0]);

        glDispatchCompute(numWorkGroupX, numWorkGroupY, 1);
        // Make sure writing to image has finished before read.
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);


        // Scene
        camera.updateVMat();
        camera.handle();
        model.updateState(camera);

        sceneShader.use();
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
        sceneShader.putUniformMatrix4f("normMat", model.getInvTrMat().get(ValuesContainer.VALS_OF_16));
        sceneShader.putUniformMatrix4f("mvMat", model.getMvMat().get(ValuesContainer.VALS_OF_16));
        sceneShader.putUniformMatrix4f("projMat", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        model.draw(GL_TRIANGLES);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID(), gui);
    }

    public static void main(String[] args) {
        new Cloud().run(true, true);
    }
}
