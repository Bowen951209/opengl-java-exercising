package chapter12.program12_5;

import chapter12.program12_4.launcher.Program12_4;
import imgui.ImGui;
import utilities.*;
import utilities.exceptions.InvalidMaterialException;
import utilities.sceneComponents.Texture;

import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL40.*;

public class Program12_5 extends Program12_4 {
    public static void main(String[] args) {
        new Program12_5().run(false);
    }
    @Override
    protected void init() {
        // p.s most are copied from Program12_4.

        // Window
        glfwWindow = new GLFWWindow(1500, 1000, "Prog12.5");

        // Program
        super.program = new Program(
                "assets/shaders/program12_5/vertShader.glsl",
                "assets/shaders/program12_5/fragShader.glsl",
                "assets/shaders/program12_5/tessCShader.glsl",
                "assets/shaders/program12_5/tessEShader.glsl"
        );
        super.program.use();

        // VAO *every program must have a VAO*.
        int vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Texture
        imageTexture = new Texture(0, "assets/textures/imageTextures/squareMoonMap.jpg");
        heightMap = new Texture(1, "assets/textures/heightMaps/squareMoonBump.jpg");
        normalMap = new Texture(2, "assets/textures/normalMaps/squareMoonNormal.jpg");

        // Model's material
        try {
            material = new Materials("gold");
        } catch (InvalidMaterialException e) {
            throw new RuntimeException(e);
        }

        // GUI
        gui = new GUI(glfwWindow, 3f) {
            @Override
            protected void drawFrame() {
                ImGui.newFrame(); // start frame
                ImGui.begin("Description"); // window
                ImGui.text("Program12.5: LOD(Level Of Detail)");
                ImGui.end();
                ImGui.render(); // end frame
            }
        };
    }

    @Override
    protected void drawScene() {
        // In this program I want to use GL_LINE
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

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
                program.getUniformLoc("mv_matrix"), false,
                mvMat.get(ValuesContainer.VALS_OF_16)
        );
        glUniformMatrix4fv(
                program.getUniformLoc("mvp_matrix"), false,
                mvpMat.get(ValuesContainer.VALS_OF_16)
        );

        // Render the patch.

        imageTexture.bind();
        heightMap.bind();
        normalMap.bind();

        glPatchParameteri(GL_PATCH_VERTICES, 4);
        glDrawArraysInstanced(GL_PATCHES, 0, 4, 64 * 64);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }
}
