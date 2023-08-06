package chapter15;

import engine.App;
import engine.GLFWWindow;
import engine.ShaderProgram;
import engine.gui.FpsDisplay;
import engine.gui.GUI;
import engine.gui.GuiWindow;
import engine.gui.Text;
import engine.sceneComponents.PositionalLight;
import engine.sceneComponents.Skybox;
import engine.sceneComponents.models.Grid;
import engine.util.Material;
import engine.util.ValuesContainer;
import engine.util.WaterFrameBuffers;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL43.*;

/*This chapter will simulate water*/
public class Main extends App {
    private Skybox skybox;
    private Grid floor, waterSurface;
    private ShaderProgram floorProgram, waterSurfaceProgram;
    private PositionalLight light;
    private WaterFrameBuffers waterFrameBuffers;

    @Override
    protected void initGLFWWindow() {
        super.glfwWindow = new GLFWWindow(2000, 1500, "Water Simulation");
    }

    @Override
    protected void initShaderPrograms() {
        floorProgram = new ShaderProgram(
                "assets/shaders/waterSimulate/floorShaders/vert.glsl",
                "assets/shaders/waterSimulate/floorShaders/frag.glsl"
        );

        waterSurfaceProgram = new ShaderProgram(
                "assets/shaders/waterSimulate/waterSurfaceShaders/vert.glsl",
                "assets/shaders/waterSimulate/waterSurfaceShaders/frag.glsl"
        );
    }

    @Override
    protected void initFrameBuffers() {
        waterFrameBuffers = new WaterFrameBuffers(glfwWindow.getWidth(), glfwWindow.getHeight());
    }

    @Override
    protected void initModels() {
        camera.setPos(0f, 4f, 10f);

        skybox = new Skybox(camera, "assets/textures/skycubes/fluffyClouds");
        floor = new Grid(new Vector3f(0f, -0.4f, 0f));
        waterSurface = new Grid(new Vector3f(0f, 10f, 0f));

        light = new PositionalLight();
    }

    @Override
    protected void initGUI() {
        gui = new GUI(glfwWindow, 3.0f);
        GuiWindow descriptionWindow = new GuiWindow("Description", false);
        descriptionWindow.addChild(new Text("Water simulating"));
        gui.addComponents(new FpsDisplay(this));
        gui.addComponents(descriptionWindow);
    }

    @Override
    protected void drawScene() {

        // default frame buffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // skybox
        skybox.draw();
        // floor
        drawFloor();
        // water surface
        drawWaterSurface();

    }

    private void drawFloor() {
        floorProgram.use();

        // put light's uniforms
        light.putToUniforms(
                floorProgram.getUniformLoc("globalAmbient"),
                floorProgram.getUniformLoc("light.ambient"),
                floorProgram.getUniformLoc("light.diffuse"),
                floorProgram.getUniformLoc("light.specular"),
                floorProgram.getUniformLoc("light.position")
        );

        // put material's uniform
        Material.getMaterial("gold").putToUniforms(floorProgram.getUniformLoc("material.shininess"));

        // update floor states
        floor.updateState(camera);

        // put states to uniform
        floorProgram.putUniformMatrix4f("mv_matrix", floor.getMvMat().get(ValuesContainer.VALS_OF_16));
        floorProgram.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        floorProgram.putUniformMatrix4f("norm_matrix", floor.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        // draw
        floor.draw(GL_TRIANGLES);
    }

    private void drawWaterSurface() {
        waterSurfaceProgram.use();

        // put light's uniforms
        light.putToUniforms(
                waterSurfaceProgram.getUniformLoc("globalAmbient"),
                waterSurfaceProgram.getUniformLoc("light.ambient"),
                waterSurfaceProgram.getUniformLoc("light.diffuse"),
                waterSurfaceProgram.getUniformLoc("light.specular"),
                waterSurfaceProgram.getUniformLoc("light.position")
        );

        // put material's uniform
        Material.getMaterial("gold").putToUniforms(waterSurfaceProgram.getUniformLoc("material.shininess"));

        // update surface state
        waterSurface.updateState(camera);

        // put states to uniform
        waterSurfaceProgram.putUniformMatrix4f("mv_matrix", waterSurface.getMvMat().get(ValuesContainer.VALS_OF_16));
        waterSurfaceProgram.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        waterSurfaceProgram.putUniformMatrix4f("norm_matrix", waterSurface.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        // if camera is above -> render up surface
        // if camera is below -> render down surface
        if (camera.getPos().y < waterSurface.getPos().y) {
            glFrontFace(GL_CW);
        }

        // draw
        waterSurface.draw(GL_TRIANGLES);

        // restore glFrontFace
        glFrontFace(GL_CCW);
    }

    @Override
    protected void destroy() {

    }

    public static void main(String[] args) {
        new Main().run(true, true);
    }
}
