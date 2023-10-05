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
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL43.*;

public class Program16_2 extends App {
    private static final Color COLOR_DARK_BLUE = new Color(.0f, .0f, .05f);
    private ShaderProgram screenQuadShader, computeShader, rayComputeShader;
    private Texture2D screenQuadTexture, brickTexture;
    private Model fullScreenQuad;
    private final float[] lightPosition = {-4.0f, 1.0f, 6.0f},
            boxPosition = {-1.0f, -.5f, 1.0f}, boxRotation = {10f, 70f, 55f};
    private final float[] resScaleSliderVal = {2f};
    private float resolutionScale = (float) Math.pow(2, -resScaleSliderVal[0]);
    private int numXPixel, numYPixel, screenSizeX, screenSizeY;
    private PixelManager pixelManager;
    private Checkbox clearScreenCheckbox;
    private ModelObject box;
    private ModelObject[] modelObjects;
    private FloatBuffer structUniformBuffer;

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

        pixelManager = new PixelManager(computeShader, "numXPixel", "numRenderedPixel",  3, 2000);
    }

    @Override
    protected void initModels() {
        camera.setPos(0f, 0f, 5f);
        camera.addCameraUpdateCallBack(this::refresh);
        fullScreenQuad = new FullScreenQuad();


        box = new Box(Material.bronzeAmbient(), Material.bronzeDiffuse(), Material.bronzeSpecular(),
                Material.bronzeShininess(), -.5f, -.5f, -1, .5f,
                .5f, 1)
                .setRotation(boxRotation)
                .setPosition(boxPosition);

        modelObjects = new ModelObject[]{
                new RoomBox(Material.goldAmbient(), Material.goldDiffuse(),
                        Material.goldSpecular(), Material.goldShininess(), true,
                        20).setColor(1, .5f, .5f),

                new Plane(Material.jadeAmbient(), Material.jadeDiffuse(), Material.jadeSpecular(),
                        Material.jadeShininess(), -2.5f, 12, 12)
                        .setRotation(0, (float) (Math.PI / 4f), 0), // 45deg on Y

                new Sphere(Material.silverAmbient(), Material.silverDiffuse(),
                        Material.silverSpecular(), Material.silverShininess(), 2.5f
                ).setPosition(.5f, 0, -3).setRefraction(.8f, 1.5f),

                box
        };
        structUniformBuffer =
                BufferUtils.createFloatBuffer(modelObjects.length * ModelObject.STRUCT_MEMORY_SPACE);
        ModelObject.putToShader(2, modelObjects, structUniformBuffer);

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

    private void refresh() {
        computeRays();
        pixelManager.zeroNumRendered();
        // FIXME: 2023/10/5 pixel order list should only regenerate when resolution is changed, but
        //  this method is also called when the camera moves
        pixelManager.putListToShader(pixelManager.generateList(numXPixel, numYPixel));
        pixelManager.putNumXToShader();
        updateModels();
    }

    private void updateModels() {
        box.setRotation(boxRotation);
        box.setPosition(boxPosition);
        ModelObject.putToShader(2, modelObjects, structUniformBuffer);

        computeShader.use();
    }

    @Override
    protected void drawScene() {
        computeShader.use();

        brickTexture.bind();
        glBindImageTexture(0, screenQuadTexture.getTexID(), 0, false,
                0, GL_WRITE_ONLY, GL_RGBA8);

        computeShader.putUniform3f("lightPosition", lightPosition);
        computeShader.putUniform3f("cameraPosition", camera.getPos()
                .get(ValuesContainer.VALS_OF_3));

        updateNumPixelXY();
        glDispatchCompute(pixelManager.getNumDispatchCall(), 1, 1);
        pixelManager.addNumRendered(pixelManager.getNumDispatchCall());
        glFinish();

        screenQuadShader.use();
        screenQuadTexture.bind();
        fullScreenQuad.draw(GL_TRIANGLES);
    }

    /**
     * Compute rays based on camera.
     */
    private void computeRays() {
        // TODO: 2023/10/5 this method should also use the pixel order list,  or when at high
        //  resolution, fps drops.
        rayComputeShader.use();
        rayComputeShader.putUniform3f("cameraPosition", camera.getPos().get(ValuesContainer.VALS_OF_3));
        rayComputeShader.putUniformMatrix4f("cameraToWorldMatrix",
                camera.getInvVMat().get(ValuesContainer.VALS_OF_16));
        updateNumPixelXY();
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
