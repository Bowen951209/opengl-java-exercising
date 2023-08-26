package chapter16_ray_tracing;

import engine.App;
import engine.GLFWWindow;
import engine.ShaderProgram;
import engine.gui.*;
import engine.sceneComponents.models.*;
import engine.sceneComponents.textures.Texture2D;
import engine.util.Destroyer;
import engine.util.ValuesContainer;

import static org.lwjgl.opengl.GL43.*;

import java.awt.*;

public class Program16_2 extends App {
    private ShaderProgram screenQuadShader, computeShader, rayComputeShader;
    private Texture2D screenQuadTexture, earthTexture, brickTexture;
    private Model fullScreenQuad;
    private float[] boxPosition;
    private float[] lightPosition;
    private float[] boxRotation;
    private final float[] resScaleSliderVal = {0f};
    private float resolutionScale = (float) Math.pow(2, -resScaleSliderVal[0]);
    private int numPixel, numXPixel, numYPixel;
    private Texture2D xp, xn, yp, yn, zp, zn;

    @Override
    protected void initGLFWWindow() {
        glfwWindow = new GLFWWindow(3000, 1500, "Ray Casting");
        initSSBO();
    }

    private void initSSBO() {
        updateNumPixel();
        int bufferSize = numPixel * Float.BYTES * 3;

        int ssboRayStart = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboRayStart);
        glBufferData(GL_SHADER_STORAGE_BUFFER, bufferSize, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssboRayStart);

        int ssboRayDir = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboRayDir);
        glBufferData(GL_SHADER_STORAGE_BUFFER, bufferSize, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, ssboRayDir);
    }

    private void updateNumXPixel() {
        numXPixel = (int) (glfwWindow.getCurrentWidth() * resolutionScale);
    }
    private void updateNumYPixel() {
        numYPixel = (int) (glfwWindow.getCurrentHeight() * resolutionScale);
    }
    private void updateNumPixel() {
        updateNumXPixel();
        updateNumYPixel();
        numPixel = numXPixel * numYPixel;
    }

    @Override
    protected void addCallbacks() {
        // FrameBuffer resize
        this.getDefaultCallbacks().getDefaultFrameBufferResizeCB().addCallback(
                () -> {
                    computeRays();
                    screenQuadTexture.fill(numXPixel, numYPixel, null);
                }
        );
    }

    @Override
    protected void initShaderPrograms() {
        screenQuadShader = new ShaderProgram(
                "assets/shaders/ch16/16_2/screen_quad/vert.glsl",
                "assets/shaders/ch16/16_2/screen_quad/frag.glsl"
        );
        rayComputeShader = new ShaderProgram(
                "assets/shaders/ch16/16_2/compute/ray.glsl"
        );
        computeShader = new ShaderProgram(
                "assets/shaders/ch16/16_2/compute/compute.glsl"
        );
    }

    @Override
    protected void initModels() {
        camera.setPos(0f, 0f, 5f);
        camera.addCameraUpdateCallBack(this::computeRays);
        fullScreenQuad = new FullScreenQuad();

        // init rays
        computeRays();
    }

    @Override
    protected void initTextures() {
        screenQuadTexture = new Texture2D(0) {
            @Override
            public void config(int mipMapSampleMode) {
                // no extra mipmap
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            }
        };

        updateNumXPixel();
        updateNumYPixel();
        screenQuadTexture.fill(numXPixel, numYPixel, Color.PINK);

        brickTexture = new Texture2D(1, "assets/textures/imageTextures/brick1.jpg");
        earthTexture = new Texture2D(0, "assets/textures/imageTextures/earthmap1k.jpg");

        // skybox
        xp = new Texture2D(2, "assets/textures/skycubes/lakesIsland/xp.jpg");
        xn = new Texture2D(3, "assets/textures/skycubes/lakesIsland/xn.jpg");
        yp = new Texture2D(4, "assets/textures/skycubes/lakesIsland/yp.jpg");
        yn = new Texture2D(5, "assets/textures/skycubes/lakesIsland/yn.jpg");
        zp = new Texture2D(6, "assets/textures/skycubes/lakesIsland/zp.jpg");
        zn = new Texture2D(7, "assets/textures/skycubes/lakesIsland/zn.jpg");
    }

    @Override
    protected void initGUI() {
        gui = new GUI(glfwWindow, 3f);
        GuiWindow userWindow = new GuiWindow("Hello! User", false);
        userWindow.addChild(new Text(
                """
                        Use buttons below to play with things:
                                                
                        """
        ));
        boxPosition = new float[]{-1.0f, -.5f, 1.0f};
        boxRotation = new float[]{10f, 70f, 55f};
        lightPosition = new float[]{-4.0f, 1.0f, 8.0f};

        Slider resolutionSlider = new SliderFloat1("resolution_scale ((1/2) ^ x)", resScaleSliderVal,
                0f, 5f).enableMouseWheelControl().setWheelSpeed(0.5f)
                .addScrollCallBack(() -> {
                    resolutionScale = (float) Math.pow(2, -resScaleSliderVal[0]);

                    computeRays();
                    screenQuadTexture.fill(numXPixel, numYPixel, null);
                });
        Slider boxPositionSlider = new SliderFloat3("box_position", boxPosition,
                -10f, 10f).enableMouseWheelControl();
        Slider boxRotationSlider = new SliderFloat3("box_rotation", boxRotation,
                -180f, 180f).enableMouseWheelControl().setWheelSpeed(5f);
        Slider lightPositionSlider = new SliderFloat3("light_position", lightPosition,
                -10, 10).enableMouseWheelControl();
        userWindow.addChild(resolutionSlider).addChild(boxPositionSlider)
                .addChild(boxRotationSlider).addChild(lightPositionSlider);
        gui.addComponents(userWindow);
        gui.addComponents(new FpsDisplay(this));
    }

    @Override
    protected void drawScene() {
        computeShader.use();

        brickTexture.bind();
        earthTexture.bind();
        xp.bind();
        xn.bind();
        yp.bind();
        yn.bind();
        zp.bind();
        zn.bind();
        glBindImageTexture(0, screenQuadTexture.getTexID(), 0, false,
                0, GL_WRITE_ONLY, GL_RGBA8);

        computeShader.putUniform3f("box_position", boxPosition);
        computeShader.putUniform3f("box_rotation", boxRotation);
        computeShader.putUniform3f("light_position", lightPosition);

        updateNumXPixel();
        updateNumYPixel();
        glDispatchCompute(numXPixel, numYPixel, 1);
        glFinish();


        screenQuadShader.use();
        screenQuadTexture.bind();
        fullScreenQuad.draw(GL_TRIANGLES);
    }

    private void computeRays() {
        rayComputeShader.use();
        rayComputeShader.putUniform1f("camera_pos_x", camera.getPos().x);
        rayComputeShader.putUniform1f("camera_pos_y", camera.getPos().y);
        rayComputeShader.putUniform1f("camera_pos_z", camera.getPos().z);
        rayComputeShader.putUniformMatrix4f("cameraToWorld_matrix",
                camera.getInvVMat().get(ValuesContainer.VALS_OF_16));
        updateNumXPixel();
        updateNumYPixel();
        glDispatchCompute(numXPixel, numYPixel, 1);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID());
    }

    public static void main(String[] args) {
        new Program16_2().run(true);
    }
}
