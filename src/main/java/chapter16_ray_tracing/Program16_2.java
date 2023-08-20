package chapter16_ray_tracing;

import engine.App;
import engine.GLFWWindow;
import engine.ShaderProgram;
import engine.sceneComponents.models.*;
import engine.sceneComponents.textures.Texture2D;
import engine.util.Destroyer;

import static org.lwjgl.opengl.GL43.*;
import java.awt.*;

public class Program16_2 extends App {
    private ShaderProgram screenQuadShader;
    private Texture2D screenQuadTexture;
    private Model fullScreenQuad;

    @Override
    protected void initGLFWWindow() {
        glfwWindow = new GLFWWindow(3000, 1500, "Ray Casting");
    }

    @Override
    protected void initShaderPrograms() {
        screenQuadShader = new ShaderProgram(
                "assets/shaders/ch16/16_2/screen_quad/vert.glsl",
                "assets/shaders/ch16/16_2/screen_quad/frag.glsl"
        );
    }

    @Override
    protected void initModels() {
        fullScreenQuad = new FullScreenQuad();
    }

    @Override
    protected void initTextures() {
        screenQuadTexture= new Texture2D(0);
        screenQuadTexture.fill(
                glfwWindow.getCurrentWidth(),
                glfwWindow.getCurrentHeight(),
                Color.PINK
        );
    }

    @Override
    protected void drawScene() {
        screenQuadShader.use();
        screenQuadTexture.bind();
        fullScreenQuad.draw(GL_TRIANGLES);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID());
    }

    public static void main(String[] args) {
        new Program16_2().run(false);
    }
}
