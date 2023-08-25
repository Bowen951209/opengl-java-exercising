package chapter16_ray_tracing;

import engine.App;
import engine.GLFWWindow;
import engine.ShaderProgram;
import engine.gui.*;
import engine.sceneComponents.models.*;
import engine.sceneComponents.textures.Texture2D;
import engine.util.Destroyer;

import static org.lwjgl.opengl.GL43.*;

import java.awt.*;

public class Program16_2 extends App {
    private ShaderProgram screenQuadShader, computeShader;
    private Texture2D screenQuadTexture, earthTexture, brickTexture;
    private Model fullScreenQuad;
    private float[] boxPosition, lightPosition, boxRotation;
    @Override
    protected void initGLFWWindow() {
        glfwWindow = new GLFWWindow(3000, 1500, "Ray Casting");
    }

    @Override
    protected void addCallbacks() {
        this.getDefaultCallbacks().getDefaultFrameBufferResizeCB().addCallback(
                () -> screenQuadTexture.fill(glfwWindow.getCurrentWidth(), glfwWindow.getCurrentHeight(), null)
        );
    }

    @Override
    protected void initShaderPrograms() {
        screenQuadShader = new ShaderProgram(
                "assets/shaders/ch16/16_2/screen_quad/vert.glsl",
                "assets/shaders/ch16/16_2/screen_quad/frag.glsl"
        );
        computeShader = new ShaderProgram(
                "assets/shaders/ch16/16_2/compute/compute.glsl"
        );
    }

    @Override
    protected void initModels() {
        camera.setPos(0f, 0f, 5f);
        fullScreenQuad = new FullScreenQuad();
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
        screenQuadTexture.fill(
                glfwWindow.getCurrentWidth(),
                glfwWindow.getCurrentHeight(),
                Color.PINK
        );

        brickTexture = new Texture2D(1, "assets/textures/imageTextures/brick1.jpg");
        earthTexture = new Texture2D(0, "assets/textures/imageTextures/earthmap1k.jpg");
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
        boxPosition = new float[] {-1.0f, -.5f, 1.0f};
        boxRotation = new float[] {10f, 70f, 55f};
        lightPosition = new float[] {-4.0f, 1.0f, 8.0f};
        SliderFloat3 boxPositionSlider = new SliderFloat3("box_position", boxPosition,
                -10f, 10f).enableMouseWheelControl();
        SliderFloat3 boxRotationSlider = new SliderFloat3("box_rotation", boxRotation,
                -180f, 180f).enableMouseWheelControl().setWheelSpeed(5f);
        SliderFloat3 lightPositionSlider = new SliderFloat3("light_position", lightPosition,
                -10, 10).enableMouseWheelControl();
        userWindow.addChild(boxPositionSlider).addChild(boxRotationSlider).addChild(lightPositionSlider);
        gui.addComponents(userWindow);
        gui.addComponents(new FpsDisplay(this));
    }

    @Override
    protected void drawScene() {
        computeShader.use();

        brickTexture.bind();
        earthTexture.bind();
        glBindImageTexture(0, screenQuadTexture.getTexID(), 0, false,
                0, GL_WRITE_ONLY, GL_RGBA8);

        computeShader.putUniform3f("box_position", boxPosition);
        computeShader.putUniform3f("box_rotation", boxRotation);
        computeShader.putUniform3f("light_position", lightPosition);

        glDispatchCompute(glfwWindow.getCurrentWidth(), glfwWindow.getCurrentHeight(), 1);
        glFinish();


        screenQuadShader.use();
        screenQuadTexture.bind();
        fullScreenQuad.draw(GL_TRIANGLES);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID());
    }

    public static void main(String[] args) {
        new Program16_2().run(true);
    }
}
