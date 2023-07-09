package chapter12.program12_4.launcher;

import imgui.ImGui;
import org.joml.Matrix4f;
import utilities.*;
import utilities.exceptions.InvalidMaterialException;
import utilities.exceptions.UniformLocNotFoundException;
import utilities.sceneComponents.PositionalLight;
import utilities.sceneComponents.Texture;

import static org.lwjgl.opengl.GL43.*;

public class Program12_4 extends App {
    private Program program;
    private Texture imageTexture, heightMap, normalMap;
    private final PositionalLight light = new PositionalLight().setPosition(0f, .1f, .2f);
    private Materials material;


    @Override
    protected void init() {
        // Window
        glfwWindow = new GLFWWindow(1500, 1000, "Prog12.4");

        // Program
        program = new Program(
                "assets/shaders/program12_4/vertShader.glsl",
                "assets/shaders/program12_4/fragShader.glsl",
                "assets/shaders/program12_4/tessCShader.glsl",
                "assets/shaders/program12_4/tessEShader.glsl"
        );
        program.use();


        // VAO *every program must have a VAO*.
        int vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Texture
        imageTexture = new Texture(0, "assets/textures/imageTextures/squareMoonMap.jpg");
        heightMap = new Texture(1, "assets/textures/heightMaps/squareMoonBump.jpg");
        normalMap = new Texture(2, "assets/textures/normalMaps/squareMoonNormal.jpg");

        // Model's material
        try {
            material = new Materials("silver");
        } catch (InvalidMaterialException e) {
            throw new RuntimeException(e);
        }

        // GUI
        gui = new GUI(glfwWindow, 3f) {
            @Override
            protected void drawFrame() {
                ImGui.newFrame(); // start frame
                ImGui.begin("Description"); // window
                ImGui.text("Program12.4: instanced tessellated terrain.");
                ImGui.end();
                ImGui.render(); // end frame
            }
        };
    }

    @Override
    protected void getAllUniformLocs() {
        try {
            program.getAllUniformLocs(new String[]{
                    "globalAmbient",
                    "light.ambient",
                    "light.diffuse",
                    "light.specular",
                    "light.position",

                    "material.ambient",
                    "material.diffuse",
                    "material.specular",
                    "material.shininess",

                    "mvp_matrix",
                    "mv_matrix"
            });
        } catch (UniformLocNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final Matrix4f mMat = new Matrix4f()
            .translate(0f, -.5f, 0f)
            .scale(10f)
            .rotateX((float) Math.toRadians(0f));
    private final Matrix4f mvMat = new Matrix4f();
    private final Matrix4f mvpMat = new Matrix4f();

    @Override
    protected void drawScene() {
        // Update mv & mvp mat every frame.
        mvMat.set(camera.getVMat()).mul(mMat);
        mvpMat.set(camera.getProjMat()).mul(mvMat); // Remember to make sure multiplication order, or it'll fuck up.
        // Pass in uniforms
        light.putToUniforms(
                program.getUniformLoc("globalAmbient"),
                program.getUniformLoc("light.ambient"),
                program.getUniformLoc("light.diffuse"),
                program.getUniformLoc("light.specular"),
                program.getUniformLoc("light.position")
        );
        material.putToUniforms(
                program.getUniformLoc("material.ambient"),
                program.getUniformLoc("material.diffuse"),
                program.getUniformLoc("material.specular"),
                program.getUniformLoc("material.shininess")
        );
        glUniformMatrix4fv(
                program.getUniformLoc("mv_matrix"),false,
                mvMat.get(ValuesContainer.VALS_OF_16)
        );
        glUniformMatrix4fv(
                program.getUniformLoc("mvp_matrix"),false,
                mvpMat.get(ValuesContainer.VALS_OF_16)
        );

        // Render the patch.

        imageTexture.bind();
        heightMap.bind();
        normalMap.bind();

        glPatchParameteri(GL_PATCH_VERTICES, 4);
        glDrawArraysInstanced(GL_PATCHES, 0, 4, 64 * 64);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID(), gui);
    }

    public static void main(String[] args) {
        new Program12_4().run(false);
    }
}
