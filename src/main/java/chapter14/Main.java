package chapter14;

import engine.*;
import engine.models.FileModel;
import engine.models.TessGrid;
import engine.models.Torus;
import engine.sceneComponents.PositionalLight;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL43.*;

public class Main extends App {
    private TessGrid grid;
    private Torus torus0, torus1;
    private FileModel pyramid;
    private PositionalLight light;
    private ShaderProgram transparencyProgram, clippingPlaneProgram;
    @Override
    protected void init() {
        // p.s most are copied from Program12_4.

        // Window
        glfwWindow = new GLFWWindow(1500, 1000, "Chapter14");

        // Shader Programs
        transparencyProgram = new ShaderProgram(
                "assets/shaders/transparency/vert.glsl",
                "assets/shaders/transparency/frag.glsl"
        );
        clippingPlaneProgram = new ShaderProgram(
                "assets/shaders/clippingPlane/vert.glsl",
                "assets/shaders/clippingPlane/frag.glsl"
        );

        // Models
        grid = new TessGrid(
                "assets/textures/imageTextures/greenMountain.jpg",
                "assets/textures/heightMaps/greenMountain.jpg",
                new Vector3f(0f, -.9f, 0f)
        );
        grid.setDrawMode(GL_FILL);

        // light
        light = new PositionalLight().brightLight();

        // Models
        torus0 = new Torus(.5f, .2f, 48, true, new Vector3f(2f, 0.4f, -2f));
        torus1 = new Torus(.5f, .2f, 48, true, new Vector3f(-2f, 0.4f, 0f));
        pyramid = new FileModel("assets/models/pyr.obj", new Vector3f(0f, 0.6f, 0f));


        // GUI
        gui = new GUI(glfwWindow, 3f) {
            private final ImBoolean planeControl = new ImBoolean();
            @Override
            protected void drawFrame() {
                ImGui.newFrame(); // start frame
                ImGui.begin("Description"); // description window
                ImGui.text("This is a all in one program in chapter14.");
                ImGui.text("1 - fog");
                ImGui.text("2 - transparency");
                ImGui.text("3 - user-defined clipping planes");
                if (!planeControl.get())
                    planeControl.set(ImGui.button("plane control"));
                ImGui.end();
                if (planeControl.get()) {
                    ImGui.begin("Plane Control Panel", planeControl);
                    ImGui.text("ax + by + cz + d = 0                ");
                    ImGui.sliderFloat4("a / b / c / d", (float[]) getElementStates().get("planeEquation"), -10f, 10f);
                    ImGui.end();
                }

                ImGui.render();
            }

            @Override
            protected void initElementStates() {
                getElementStates().put("planeEquation", new float[] {0f, 0f, -1f, 0f});
            }
        };
    }
    @Override
    protected void drawScene() {
        // ------------------- Grid ( Fog ) --------------------
        TessGrid.useTessProgram();
        grid.updateState(camera);
        grid.draw(0);

        // ------------------ Transparency ---------------------
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
        Materials.getMaterial("bronze").putToUniforms(
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
        Materials.getMaterial("gold").putToUniforms(
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

        glDisable(GL_BLEND);

        // -----------------About clipping planes-----------------------
        glEnable(GL_CLIP_DISTANCE0);
        clippingPlaneProgram.use();
        clippingPlaneProgram.putUniform4f("clipPlane", (float[])gui.getElementStates().get("planeEquation"));
        light.putToUniforms(
                clippingPlaneProgram.getUniformLoc("globalAmbient"),
                clippingPlaneProgram.getUniformLoc("light.ambient"),
                clippingPlaneProgram.getUniformLoc("light.diffuse"),
                clippingPlaneProgram.getUniformLoc("light.specular"),
                clippingPlaneProgram.getUniformLoc("light.position")
        );
        Materials.getMaterial("silver").putToUniforms(
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

        glDisable(GL_CULL_FACE);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID(), gui);
    }

    public static void main(String[] args) {
        new Main().run(false, true);
    }
}
