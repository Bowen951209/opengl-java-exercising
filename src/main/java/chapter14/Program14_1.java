package chapter14;

import engine.App;
import engine.GLFWWindow;
import engine.ShaderProgram;
import engine.gui.*;
import engine.sceneComponents.models.FileModel;
import engine.sceneComponents.models.TessGrid;
import engine.sceneComponents.models.Torus;
import engine.sceneComponents.PositionalLight;
import engine.sceneComponents.textures.*;
import engine.util.Destroyer;
import engine.util.Material;
import engine.util.ValuesContainer;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL43.*;

public class Program14_1 extends App {
    private TessGrid grid;
    private Torus torus0, torus1;
    private PositionalLight light;
    private ShaderProgram transparencyProgram, clippingPlaneProgram, texture3DProgram;
    private Texture3D stripe3D;
    private Texture3D marble3D;
    private Texture3D wood3D;
    private FileModel dragon, cube0, cube1, pyramid;
    private Skydome skydome;
    private CloudTexture cloud3D;

    @Override
    protected void initGLFWWindow() {
        glfwWindow = new GLFWWindow(1500, 1000, "Chapter14");
    }

    @Override
    protected void initShaderPrograms() {
        transparencyProgram = new ShaderProgram(
                "assets/shaders/transparency/vert.glsl",
                "assets/shaders/transparency/frag.glsl"
        );
        clippingPlaneProgram = new ShaderProgram(
                "assets/shaders/clippingPlane/vert.glsl",
                "assets/shaders/clippingPlane/frag.glsl"
        );
        texture3DProgram = new ShaderProgram(
                "assets/shaders/3DTextureShader/vert.glsl",
                "assets/shaders/3DTextureShader/frag.glsl"
        );
    }

    @Override
    protected void initTextures() {
        // Thread pre-prepare
        stripe3D = new StripeTexture(0);
        marble3D = new MarbleTexture(0);
        marble3D.setZoom(64);
        wood3D = new WoodTexture(0);
        wood3D.setZoom(64);
        cloud3D = new CloudTexture(0);

        addTexture3D(stripe3D);
        addTexture3D(marble3D);
        addTexture3D(wood3D);
        addTexture3D(cloud3D);
    }

    @Override
    protected void initModels() {
        torus0 = new Torus(.5f, .2f, 48, true, new Vector3f(2f, 0.4f, -2f));
        torus1 = new Torus(.5f, .2f, 48, true, new Vector3f(-2f, 0.4f, 0f));

        // model file
        pyramid = new FileModel("assets/models/pyr.obj", true);
        cube0 = new FileModel("assets/models/bigCube.obj", new Vector3f(1f, -0.7f, 1f), false);
        cube1 = new FileModel("assets/models/bigCube.obj", new Vector3f(2.5f, -0.7f, 1f), false);
        dragon = new FileModel("assets/models/simplify-scaled-stanford-dragon.obj", new Vector3f(0f, 1.5f, 0f), false) {
            @Override
            protected void updateMMat() {
                super.updateMMat();
            }
        };
        skydome = new Skydome(cloud3D, camera);

        addFileModel(pyramid);
        addFileModel(cube0);
        addFileModel(cube1);
        addFileModel(dragon);

        grid = new TessGrid(
                "assets/textures/imageTextures/greenMountain.jpg",
                "assets/textures/heightMaps/greenMountain.jpg",
                new Vector3f(0f, -.9f, 0f)
        );
        grid.setDrawMode(GL_FILL);


        // light
        light = new PositionalLight().brightLight();
    }

    @Override
    protected void initGUI() {
        gui = new GUI(glfwWindow, 3f);
        gui.getElementStates().put("planeEquation", new float[]{0f, 0f, 1f, 0f});

        GuiWindow planeControlPanel = new GuiWindow("Plane Control Panel", true)
                .addChild(new Text("ax + by + cz + d = 0                "))
                .addChild(new SliderFloat4("a / b / c / d", (float[]) gui.getElementStates().get("planeEquation"), -10f, 10f));

        GuiWindow descriptionWindow = new GuiWindow("Description", false)
                .addChild(new Text(
                        """
                                This is a all in one program in chapter14.
                                1 - fog
                                2 - transparency
                                3 - user-defined clipping planes
                                4 - 3D textures(stripe and marble)
                                5 - skydome (not good effect. I will remake clouds with another method in the future.)
                                """))
                .addChild(new WindowCallerButton("plane control", planeControlPanel));

        gui.addComponents(descriptionWindow)
                .addComponents(planeControlPanel)
                .addComponents(new FpsDisplay(this));
    }

    @Override
    protected void drawScene() {
        drawCloudSkydome();
        drawGrid();
        drawTransparency();
        glDisable(GL_BLEND);
        drawClippingPlane();
        glFrontFace(GL_CCW);
        draw3DTextures();
        glDisable(GL_CULL_FACE);
    }

    private void drawGrid() {
        TessGrid.useTessProgram();
        grid.updateState(camera);
        grid.draw(0);
    }

    private void drawTransparency() {
        glEnable(GL_BLEND);
        glEnable(GL_CULL_FACE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBlendEquation(GL_FUNC_ADD);

        transparencyProgram.use();
        // torus
        light.putToUniforms(
                transparencyProgram.getUniformLoc("globalAmbient"),
                transparencyProgram.getUniformLoc("light.ambient"),
                transparencyProgram.getUniformLoc("light.diffuse"),
                transparencyProgram.getUniformLoc("light.specular"),
                transparencyProgram.getUniformLoc("light.position")
        );
        Material.getMaterial("bronze").putToUniforms(
                transparencyProgram.getUniformLoc("material.ambient"),
                transparencyProgram.getUniformLoc("material.diffuse"),
                transparencyProgram.getUniformLoc("material.specular"),
                transparencyProgram.getUniformLoc("material.shininess")
        );
        transparencyProgram.putUniformMatrix4f("mv_matrix", torus0.getMvMat().get(ValuesContainer.VALS_OF_16));
        transparencyProgram.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        transparencyProgram.putUniformMatrix4f("norm_matrix", torus0.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        glFrontFace(GL_CCW);
        transparencyProgram.putUniform1f("alpha", 1f);
        torus0.updateState(camera);
        torus0.draw(GL_TRIANGLES);

        // pyramid
        Material.getMaterial("gold").putToUniforms(
                transparencyProgram.getUniformLoc("material.ambient"),
                transparencyProgram.getUniformLoc("material.diffuse"),
                transparencyProgram.getUniformLoc("material.specular"),
                transparencyProgram.getUniformLoc("material.shininess")
        );
        transparencyProgram.putUniformMatrix4f("mv_matrix", pyramid.getMvMat().get(ValuesContainer.VALS_OF_16));
        transparencyProgram.putUniformMatrix4f("norm_matrix", pyramid.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        //I. render back face first
        glCullFace(GL_FRONT);
        transparencyProgram.putUniform1f("alpha", 0.3f);
        transparencyProgram.putUniform1f("flipNormal", -1f); // don't flip normals
        pyramid.updateState(camera);
        pyramid.draw(GL_TRIANGLES);

        //II. then render front face
        glCullFace(GL_BACK);
        transparencyProgram.putUniform1f("alpha", 0.7f);
        transparencyProgram.putUniform1f("flipNormal", 1f); // flip normals to inside(back face)
        pyramid.updateState(camera);
        pyramid.draw(GL_TRIANGLES);
    }

    private void drawClippingPlane() {
        glEnable(GL_CLIP_DISTANCE0);
        clippingPlaneProgram.use();
        clippingPlaneProgram.putUniform4f("clipPlane", (float[]) gui.getElementStates().get("planeEquation"));
        light.putToUniforms(
                clippingPlaneProgram.getUniformLoc("globalAmbient"),
                clippingPlaneProgram.getUniformLoc("light.ambient"),
                clippingPlaneProgram.getUniformLoc("light.diffuse"),
                clippingPlaneProgram.getUniformLoc("light.specular"),
                clippingPlaneProgram.getUniformLoc("light.position")
        );
        Material.getMaterial("silver").putToUniforms(
                clippingPlaneProgram.getUniformLoc("material.ambient"),
                clippingPlaneProgram.getUniformLoc("material.diffuse"),
                clippingPlaneProgram.getUniformLoc("material.specular"),
                clippingPlaneProgram.getUniformLoc("material.shininess")
        );
        clippingPlaneProgram.putUniformMatrix4f("mv_matrix", torus1.getMvMat().get(ValuesContainer.VALS_OF_16));
        clippingPlaneProgram.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        clippingPlaneProgram.putUniformMatrix4f("norm_matrix", torus1.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        glFrontFace(GL_CCW);
        clippingPlaneProgram.putUniform1f("flipNormal", 1f);
        torus1.updateState(camera);
        torus1.draw(GL_TRIANGLES);

        glFrontFace(GL_CW);
        clippingPlaneProgram.putUniform1f("flipNormal", -1f);
        torus1.draw(GL_TRIANGLES);
    }

    private void draw3DTextures() {
        texture3DProgram.use();
        dragon.updateState(camera);
        light.putToUniforms(
                texture3DProgram.getUniformLoc("globalAmbient"),
                texture3DProgram.getUniformLoc("light.ambient"),
                texture3DProgram.getUniformLoc("light.diffuse"),
                texture3DProgram.getUniformLoc("light.specular"),
                texture3DProgram.getUniformLoc("light.position")
        );
        Material.getMaterial("GOLD").putToUniforms(
                texture3DProgram.getUniformLoc("material.shininess")
        );
        texture3DProgram.putUniformMatrix4f("norm_matrix", dragon.getInvTrMat().get(ValuesContainer.VALS_OF_16));
        texture3DProgram.putUniformMatrix4f("mv_matrix", dragon.getMvMat().get(ValuesContainer.VALS_OF_16));
        texture3DProgram.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        stripe3D.bind();
        dragon.draw(GL_TRIANGLES);

        // Displaying marble 3D texture
        cube0.updateState(camera);
        Material.getMaterial("GOLD").putToUniforms(
                texture3DProgram.getUniformLoc("material.shininess")
        );
        texture3DProgram.putUniformMatrix4f("norm_matrix", cube0.getInvTrMat().get(ValuesContainer.VALS_OF_16));
        texture3DProgram.putUniformMatrix4f("mv_matrix", cube0.getMvMat().get(ValuesContainer.VALS_OF_16));
        texture3DProgram.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        marble3D.bind();
        cube0.draw(GL_TRIANGLES);

        cube1.updateState(camera);
        Material.getMaterial("GOLD").putToUniforms(
                texture3DProgram.getUniformLoc("material.shininess")
        );
        texture3DProgram.putUniformMatrix4f("norm_matrix", cube1.getInvTrMat().get(ValuesContainer.VALS_OF_16));
        texture3DProgram.putUniformMatrix4f("mv_matrix", cube1.getMvMat().get(ValuesContainer.VALS_OF_16));
        texture3DProgram.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        wood3D.bind();
        cube1.draw(GL_TRIANGLES);
    }

    private void drawCloudSkydome() {
        skydome.updateState(camera);

        skydome.draw(GL_TRIANGLES);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID(), gui);
    }

    public static void main(String[] args) {
        new Program14_1().run(false, true);
    }
}
