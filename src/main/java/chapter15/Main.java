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
import engine.sceneComponents.models.FileModel;
import engine.sceneComponents.models.Grid;
import engine.sceneComponents.textures.Texture2D;
import engine.sceneComponents.textures.Texture3D;
import engine.sceneComponents.textures.WaterSurfaceTexture;
import engine.util.Material;
import engine.util.ValuesContainer;
import engine.util.WaterFrameBuffers;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.opengl.GL43.*;

/*This chapter will simulate water*/
public class Main extends App {
    private Skybox skybox;
    private Grid floor, waterSurface;
    private FileModel wordWall;
    private ShaderProgram floorProgram, waterSurfaceProgram, fathersDayProgram;
    private PositionalLight light;
    private WaterFrameBuffers waterFrameBuffers;

    @Override
    protected void initGLFWWindow() {
        super.glfwWindow = new GLFWWindow(2000, 1500, "Water Simulation");
    }

    @Override
    protected void addCallbacks() {
        getDefaultCallbacks().getDefaultFrameBufferResizeCB().addCallback(
                ()-> {
                    final int[] width = new int[1];
                    final int[] height = new int[1];
                    GLFW.glfwGetFramebufferSize(glfwWindow.getID(), width, height);
                    waterFrameBuffers.resizeTo(width[0], height[0]);
                }
                );
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

        fathersDayProgram = new ShaderProgram(
                "assets/shaders/waterSimulate/fathersDay/vert.glsl",
                "assets/shaders/waterSimulate/fathersDay/frag.glsl"
        );
    }

    @Override
    protected void initFrameBuffers() {
        waterFrameBuffers = new WaterFrameBuffers(glfwWindow.getInitWidth(), glfwWindow.getInitHeight());
    }

    @Override
    protected void initModels() {
        camera.step(0.7f);
        camera.setPos(0f, 14f, 15f);

        wordWall = new FileModel("assets/models/fathersDayWordWall.obj", new Vector3f(0f, 50f, -50f), false) {
            @Override
            protected void updateMMat() {
                mMat.identity().translate(position).scale(0.5f).rotateY((float) GLFW.glfwGetTime() / 3f);
            }
        };
        fileModelList.add(wordWall);

        skybox = new Skybox(camera, "assets/textures/skycubes/fluffyClouds");
        floor = new Grid(new Vector3f(0f, -0.4f, 0f));
        waterSurface = new Grid(new Vector3f(0f, 10f, 0f));

        light = new PositionalLight();
    }

    @Override
    protected void initTextures() {
        Texture3D waterNormalMap3D = new WaterSurfaceTexture(2);
        waterNormalMap3D.setZoom(32);
        texture3DList.add(waterNormalMap3D);
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
        // Render from refraction camera
        glBindFramebuffer(GL_FRAMEBUFFER, waterFrameBuffers.getRefractionFrameBuffer());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        boolean camIsAboveWater = camera.getPos().y > waterSurface.getPos().y;
        if (camIsAboveWater) {
            drawObjectsBelowWater();
        } else {
            drawObjectsAboveWater();
        }

        // Render from reflection camera
        glBindFramebuffer(GL_FRAMEBUFFER, waterFrameBuffers.getReflectionFrameBuffer());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // reflect camera
        camera.reflect(waterSurface.getPos().y);
        camera.updateVMat();
        if (camIsAboveWater) {
            drawObjectsAboveWater();
        }

        // Render from default camera and to screen
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // restore camera position
        camera.reflect(waterSurface.getPos().y);
        camera.updateVMat();
        drawObjectsAboveWater();
        drawObjectsBelowWater();
        drawWaterSurface();
    }

    private void drawObjectsAboveWater() {
        skybox.draw();
        drawWordWall();
    }

    private void drawObjectsBelowWater() {
        drawFloor();
    }

    private void drawWordWall() {
        fathersDayProgram.use();

        // put light's uniforms
        light.putToUniforms(
                fathersDayProgram.getUniformLoc("globalAmbient"),
                fathersDayProgram.getUniformLoc("light.ambient"),
                fathersDayProgram.getUniformLoc("light.diffuse"),
                fathersDayProgram.getUniformLoc("light.specular"),
                fathersDayProgram.getUniformLoc("light.position")
        );

        // put material's uniform
        Material.getMaterial("gold").putToUniforms(fathersDayProgram.getUniformLoc("material.shininess"));

        // update surface state
        wordWall.updateState(camera);

        // put states to uniform
        fathersDayProgram.putUniformMatrix4f("mv_matrix", wordWall.getMvMat().get(ValuesContainer.VALS_OF_16));
        fathersDayProgram.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        fathersDayProgram.putUniformMatrix4f("norm_matrix", wordWall.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        // draw
        wordWall.draw(GL_TRIANGLES);
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
        waterSurfaceProgram.putUniform1f("texture3DSampleY", (float) GLFW.glfwGetTime() / 30);

        Texture2D.putToUniform(0, waterFrameBuffers.getReflectionImageTexture());
        Texture2D.putToUniform(1, waterFrameBuffers.getRefractionImageTexture());

        // if camera is above -> render up surface
        // if camera is below -> render down surface
        if (camera.getPos().y < waterSurface.getPos().y) { // below
            glFrontFace(GL_CW);
            waterSurfaceProgram.putUniform1i("isAbove", 0);
        } else {
            waterSurfaceProgram.putUniform1i("isAbove", 1);
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
