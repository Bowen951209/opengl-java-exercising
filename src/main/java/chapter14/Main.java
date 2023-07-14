package chapter14;

import engine.*;
import engine.models.FileModel;
import engine.models.TessGrid;
import engine.models.Torus;
import engine.sceneComponents.PositionalLight;
import imgui.ImGui;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL43.*;

public class Main extends App {
    private TessGrid grid;
    private Torus torus;
    private FileModel pyramid;
    private PositionalLight light;
    private ShaderProgram transparencyProgram;
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

        // Models
        grid = new TessGrid(
                "assets/textures/imageTextures/greenMountain.jpg",
                "assets/textures/heightMaps/greenMountain.jpg",
                new Vector3f(0f, -.9f, 0f)
                );
        grid.setDrawMode(GL_FILL);

        // light
        light = new PositionalLight().brightLight();

        torus = new Torus(.5f, .2f, 48, true, new Vector3f(2f, 0.4f, -2f));
        pyramid = new FileModel("assets/models/pyr.obj", new Vector3f(0f, 0.6f, 0f));


        // GUI
        gui = new GUI(glfwWindow, 3f) {
            @Override
            protected void drawFrame() {
                ImGui.newFrame(); // start frame
                ImGui.begin("Description"); // window
                ImGui.text("This is a all in one program in chapter14.");
                ImGui.text("1 - fog");
                ImGui.text("2 - transparency");
                ImGui.end();
                ImGui.render(); // end frame
            }
        };
    }
    @Override
    protected void drawScene() {
        // Grid
        TessGrid.useTessProgram();
        grid.updateState(camera);
        grid.draw(0);

        // Transparency
        glEnable(GL_BLEND);
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
        Materials.getMaterial("gold").putToUniforms(
                transparencyProgram.getUniformLoc("material.ambient"),
                transparencyProgram.getUniformLoc("material.diffuse"),
                transparencyProgram.getUniformLoc("material.specular"),
                transparencyProgram.getUniformLoc("material.shininess")
        );
        transparencyProgram.putUniformMatrix4f("mv_matrix", torus.getMvMat().get(ValuesContainer.VALS_OF_16));
        transparencyProgram.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        transparencyProgram.putUniformMatrix4f("norm_matrix", torus.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        //I. render back face first
        glCullFace(GL_FRONT);
        transparencyProgram.putUniform1f("alpha", 0.7f);
        transparencyProgram.putUniform1f("flipNormal", 1f); // don't flip normals
        torus.updateState(camera);
        torus.draw(GL_TRIANGLES);

        //II. then render front face
        glCullFace(GL_BACK);
        transparencyProgram.putUniform1f("alpha", 0.3f);
        transparencyProgram.putUniform1f("flipNormal", -1f); // flip normals to inside(back face)
        torus.updateState(camera);
        torus.draw(GL_TRIANGLES);

        // pyramid
        Materials.getMaterial("bronze").putToUniforms(
                transparencyProgram.getUniformLoc("material.ambient"),
                transparencyProgram.getUniformLoc("material.diffuse"),
                transparencyProgram.getUniformLoc("material.specular"),
                transparencyProgram.getUniformLoc("material.shininess")
        );
        transparencyProgram.putUniformMatrix4f("mv_matrix", pyramid.getMvMat().get(ValuesContainer.VALS_OF_16));
        transparencyProgram.putUniformMatrix4f("norm_matrix", pyramid.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        //I. render back face first
        glCullFace(GL_FRONT);
        transparencyProgram.putUniform1f("alpha", 0.7f);
        transparencyProgram.putUniform1f("flipNormal", 1f); // don't flip normals
        pyramid.updateState(camera);
        pyramid.draw(GL_TRIANGLES);

        //II. then render front face
        glCullFace(GL_BACK);
        transparencyProgram.putUniform1f("alpha", 0.3f);
        transparencyProgram.putUniform1f("flipNormal", -1f); // flip normals to inside(back face)
        pyramid.updateState(camera);
        pyramid.draw(GL_TRIANGLES);

        glDisable(GL_BLEND);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID(), gui);
    }

    public static void main(String[] args) {
        new Main().run(false, true);
    }
}
