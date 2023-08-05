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
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL43.GL_TRIANGLES;

/*This chapter will simulate water*/
public class Main extends App {
    // TODO: 2023/8/3 plane with ADS lighting (for water, no texture) if camera is higher than plane, render up face, else render bottom face.

    private Skybox skybox;
    private Grid floor;
    private ShaderProgram floorProgram;
    private PositionalLight light;

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
    }

    @Override
    protected void initModels() {
        camera.setPos(0f, 4f, 10f);

        skybox = new Skybox(camera, "assets/textures/skycubes/fluffyClouds");
        floor = new Grid(new Vector3f(0f, -0.4f, 0f));

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
        // skybox
        skybox.draw();

        // floor
        drawFloor();

    }

    private void drawFloor() {
        floorProgram.use();

        // light
        light.putToUniforms(
                floorProgram.getUniformLoc("globalAmbient"),
                floorProgram.getUniformLoc("light.ambient"),
                floorProgram.getUniformLoc("light.diffuse"),
                floorProgram.getUniformLoc("light.specular"),
                floorProgram.getUniformLoc("light.position")
        );

        // material
        Material.getMaterial("gold").putToUniforms(floorProgram.getUniformLoc("material.shininess"));

        // floor
        floor.updateState(camera);
        floorProgram.putUniformMatrix4f("mv_matrix", floor.getMvMat().get(ValuesContainer.VALS_OF_16));
        floorProgram.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        floorProgram.putUniformMatrix4f("norm_matrix", floor.getInvTrMat().get(ValuesContainer.VALS_OF_16));
        floor.draw(GL_TRIANGLES);
    }

    @Override
    protected void destroy() {

    }

    public static void main(String[] args) {
        new Main().run(true, true);
    }
}
