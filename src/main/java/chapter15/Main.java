package chapter15;

import engine.App;
import engine.GLFWWindow;
import engine.gui.FpsDisplay;
import engine.gui.GUI;
import engine.gui.GuiWindow;
import engine.gui.Text;
import engine.sceneComponents.Skybox;

/*This chapter will simulate water*/
public class Main extends App {
    // TODO: 2023/8/3 Skybox
    // TODO: 2023/8/3 pool floor with ADS lighting (include checkerboard texture)
    // TODO: 2023/8/3 plane with ADS lighting (for water, no texture) if camera is higher than plane, render up face, else render bottom face.

    private Skybox skybox;

    @Override
    protected void initGLFWWindow() {
        super.glfwWindow = new GLFWWindow(2000, 1500, "Water Simulation");
    }

    @Override
    protected void initModels() {
        skybox = new Skybox(camera, "assets/textures/skycubes/fluffyClouds");
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
        skybox.draw();
    }

    @Override
    protected void destroy() {

    }

    public static void main(String[] args) {
        new Main().run(true, true);
    }
}
