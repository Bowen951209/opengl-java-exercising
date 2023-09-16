package chapter16_ray_tracing;

import engine.App;
import engine.GLFWWindow;
import engine.ShaderProgram;
import engine.gui.Checkbox;
import engine.gui.*;
import engine.raytrace.PixelManager;
import engine.raytrace.modelObjects.*;
import engine.sceneComponents.models.FullScreenQuad;
import engine.sceneComponents.models.Model;
import engine.sceneComponents.textures.Texture2D;
import engine.util.Destroyer;
import engine.util.Material;
import engine.util.ValuesContainer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import java.awt.*;
import java.util.Objects;

import static org.lwjgl.opengl.GL43.*;

public class Program16_2 extends App {
    private static final Color COLOR_DARK_BLUE = new Color(.0f, .0f, .05f);
    private ShaderProgram screenQuadShader, computeShader, rayComputeShader;
    private Texture2D screenQuadTexture, brickTexture;
    private Model fullScreenQuad;
    private float[] lightPosition, boxPosition, boxRotation;
    private final Matrix4f invBoxMMat = new Matrix4f();
    private final Matrix4f boxMMatRotate = new Matrix4f();
    private final Matrix4f invBoxMMatRotate = new Matrix4f();
    private final float[] resScaleSliderVal = {2f};
    private float resolutionScale = (float) Math.pow(2, -resScaleSliderVal[0]);
    private int numXPixel, numYPixel, screenSizeX, screenSizeY;
    private PixelManager pixelManager;
    private Checkbox clearScreenCheckbox;

    @Override
    protected void initGLFWWindow() {
        glfwWindow = new GLFWWindow(3000, 1500, "Ray Casting");
        GLFWVidMode glfwVidMode = Objects.requireNonNull(
                GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()));

        screenSizeX = glfwVidMode.width();
        screenSizeY = glfwVidMode.height();
        initSSBO();
    }

    private void initSSBO() {
        int bufferSize = screenSizeX * screenSizeY * Float.BYTES * 3;

        int ssboRayStart = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboRayStart);
        glBufferData(GL_SHADER_STORAGE_BUFFER, bufferSize, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssboRayStart);

        int ssboRayDir = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboRayDir);
        glBufferData(GL_SHADER_STORAGE_BUFFER, bufferSize, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, ssboRayDir);

        pixelManager = new PixelManager(2);
        updateNumPixelXY();
        pixelManager.fill(numXPixel * numYPixel);
    }

    private void updateNumPixelXY() {
        numXPixel = (int) (glfwWindow.getCurrentWidth() * resolutionScale);
        numYPixel = (int) (glfwWindow.getCurrentHeight() * resolutionScale);
    }

    @Override
    protected void addCallbacks() {
        // FrameBuffer resize
        this.getDefaultCallbacks().getDefaultFrameBufferResizeCB().addCallback(this::refresh);
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


        ModelObject[] modelObjects = {
                new RoomBox(Material.goldAmbient(), Material.goldDiffuse(),
                        Material.goldSpecular(), Material.goldShininess(), true)
                        .setColor(1, .5f, .5f),
                new Plane(Material.jadeAmbient(), Material.jadeDiffuse(), Material.jadeSpecular(),
                        Material.jadeShininess(), .5f, 12, 12),
                new Sphere(Material.silverAmbient(), Material.silverDiffuse(),
                        Material.silverSpecular(), Material.silverShininess(), 2.5f
                ).setPosition(.5f, 0, -3).setRefraction(.8f, 1.5f),
                new Box(Material.bronzeAmbient(), Material.bronzeDiffuse(), Material.bronzeSpecular(),
                        Material.bronzeShininess(), new Vector3f(-.5f, -.5f, -1),
                        new Vector3f(.5f, .5f, 1))
        };
        ModelObject.putToShader(computeShader, 2, modelObjects);


        final Vector3f PLANE_POSITION = new Vector3f(0f, -2.5f, -2.0f);
        final float PLANE_ROTATION_Y = (float) (Math.PI * 0.25); // 45 deg
        final Matrix4f PLANE_MMAT_TRANS = new Matrix4f().translate(PLANE_POSITION);
        final Matrix4f PLANE_MMAT_ROT = new Matrix4f().rotateY(PLANE_ROTATION_Y);
        final Matrix4f INV_PLANE_MMAT_ROT = new Matrix4f(PLANE_MMAT_ROT).invert();
        final Matrix4f PLANE_MMAT = new Matrix4f(PLANE_MMAT_ROT).mul(PLANE_MMAT_TRANS);
        final Matrix4f INV_PLANE_MMAT = new Matrix4f(PLANE_MMAT).invert();

        computeShader.use();
        computeShader.putUniformMatrix4f("invPlaneMMat",
                INV_PLANE_MMAT.get(ValuesContainer.VALS_OF_16));
        computeShader.putUniformMatrix4f("planeMMatRotate",
                PLANE_MMAT_ROT.get(ValuesContainer.VALS_OF_16));
        computeShader.putUniformMatrix4f("invPlaneMMatRotate",
                INV_PLANE_MMAT_ROT.get(ValuesContainer.VALS_OF_16));

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

        updateNumPixelXY();
        screenQuadTexture.fill(numXPixel, numYPixel, COLOR_DARK_BLUE);

        brickTexture = new Texture2D(1, "assets/textures/imageTextures/marble.jfif");
    }

    @Override
    protected void initGUI() {
        gui = new GUI(glfwWindow, screenSizeX * screenSizeY * .0000004f);
        GuiWindow userWindow = new GuiWindow("Hello! User", false);
        userWindow.addChild(new Text(
                """
                        Use buttons below to play with things:
                                                
                        """
        ));
        boxPosition = new float[]{-1.0f, -.5f, 1.0f};
        boxRotation = new float[]{10f, 70f, 55f};
        lightPosition = new float[]{-4.0f, 1.0f, 8.0f};

        clearScreenCheckbox = new Checkbox("Clear screen when camera moves"
                , true);
        Slider resolutionSlider = new SliderFloat1("resolution_scale ((1/2) ^ x)", resScaleSliderVal,
                0f, 5f).enableMouseWheelControl().setWheelSpeed(0.5f)
                .addScrollCallBack(() -> {
                    resolutionScale = (float) Math.pow(2, -resScaleSliderVal[0]);
                    refresh();
                });
        Slider boxPositionSlider = new SliderFloat3("box_position", boxPosition,
                -10f, 10f).enableMouseWheelControl().addScrollCallBack(this::refresh);
        Slider boxRotationSlider = new SliderFloat3("box_rotation", boxRotation,
                -180f, 180f).enableMouseWheelControl().setWheelSpeed(5f).addScrollCallBack(this::refresh);
        Slider lightPositionSlider = new SliderFloat3("light_position", lightPosition,
                -10, 10).enableMouseWheelControl().addScrollCallBack(this::refresh);
        userWindow.addChild(clearScreenCheckbox);
        userWindow.addChild(resolutionSlider);
        userWindow.addChild(boxPositionSlider);
        userWindow.addChild(boxRotationSlider);
        userWindow.addChild(lightPositionSlider);
        gui.addComponents(userWindow);
        gui.addComponents(new FpsDisplay(this));

        refresh();
    }

    /**
     * This method is called when you want to update the state.
     */
    private void refresh() {
        computeRays();
        pixelManager.resizeTurnOnOrder(numXPixel * numYPixel);
        updateBoxMatrices();
    }

    private void updateBoxMatrices() {
        boxMMatRotate.identity().rotateX(boxRotation[0]).rotateY(boxRotation[1])
                .rotateZ(boxRotation[2]);
        invBoxMMatRotate.set(boxMMatRotate).invert();

        invBoxMMat.identity().translate(boxPosition[0], boxPosition[1], boxPosition[2]).mul(boxMMatRotate).invert();


        computeShader.use();
        computeShader.putUniformMatrix4f("invBoxMMat",
                invBoxMMat.get(ValuesContainer.VALS_OF_16));
        computeShader.putUniformMatrix4f("invBoxMMatRotate",
                invBoxMMatRotate.get(ValuesContainer.VALS_OF_16));

    }

    @Override
    protected void drawScene() {
        computeShader.use();
        if (getFps() > 0f) {
            pixelManager.turnOn((int) (getFps() * 200));
        }

        pixelManager.putPixelArrayToSSBO();

        brickTexture.bind();
        glBindImageTexture(0, screenQuadTexture.getTexID(), 0, false,
                0, GL_WRITE_ONLY, GL_RGBA8);

        computeShader.putUniform3f("lightPosition", lightPosition);
        computeShader.putUniform3f("cameraPosition", camera.getPos()
                .get(ValuesContainer.VALS_OF_3));

        updateNumPixelXY();
        // TODO: 2023/9/16 Use the new method mentioned in my notebook.
        glDispatchCompute(numXPixel, numYPixel, 1);
        glFinish();

        pixelManager.updateBuffer();

        screenQuadShader.use();
        screenQuadTexture.bind();
        fullScreenQuad.draw(GL_TRIANGLES);
    }

    /**
     * This method is fast.
     */
    private void computeRays() {
        rayComputeShader.use();
        rayComputeShader.putUniform3f("cameraPosition", camera.getPos().get(ValuesContainer.VALS_OF_3));
        rayComputeShader.putUniformMatrix4f("cameraToWorldMatrix",
                camera.getInvVMat().get(ValuesContainer.VALS_OF_16));
        updateNumPixelXY();
        pixelManager.fill(numXPixel * numYPixel);
        if (clearScreenCheckbox != null && clearScreenCheckbox.getIsActive()) {
            screenQuadTexture.fill(numXPixel, numYPixel, COLOR_DARK_BLUE);
        }
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
