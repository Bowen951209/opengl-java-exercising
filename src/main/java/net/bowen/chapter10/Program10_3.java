package net.bowen.chapter10;

import net.bowen.engine.GLFWWindow;
import net.bowen.engine.ShaderProgram;
import net.bowen.engine.gui.GUI;
import net.bowen.engine.sceneComponents.textures.Texture2D;
import net.bowen.engine.util.Color;
import net.bowen.engine.util.Material;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.lwjgl.glfw.GLFW;
import net.bowen.engine.callbacks.DefaultCallbacks;
import net.bowen.engine.exceptions.InvalidMaterialException;
import net.bowen.engine.sceneComponents.models.Sphere;
import net.bowen.engine.sceneComponents.PositionalLight;

import java.nio.file.Path;

import static org.lwjgl.opengl.GL20.*;
import static net.bowen.engine.util.ValuesContainer.VALS_OF_16;

public class Program10_3 extends Program10_2 {
    protected int program;
    private Sphere sphere;
    private PositionalLight positionalLight;
    private Material material;

    private int globalAmbientLoc, mv_matrixLoc, proj_matrixLoc, norm_matrixLoc, materialShininessLoc,
            lightAmbientLoc, lightDiffuseLoc, lightSpecularLoc, lightPosition, materialAmbientLoc,
            materialDiffuseLoc, materialSpecularLoc, isUsingNormalMapLoc, isUsingImageTextureLoc;
    private Texture2D imageTexture, normalMapTexture;
    private GUI gui;
    private ImBoolean isUsingNormalMap, isUsingImageTexture;

    public Program10_3(String title) {
        super(title);
    }

    public static void main(String[] args) {
        new Program10_3("Texture2D + Normal Mapping");
    }

    @Override
    protected void init(String title) {
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        camera.setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, title);
        windowID = glfwWindow.getID();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));

        new DefaultCallbacks(windowID, camera, true).bindToGLFW();

        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        program = new ShaderProgram("assets/shaders/program10_3/vertex.glsl"
                , "assets/shaders/program10_3/fragment.glsl")
                .getID();

        getAllUniformsLoc();
        sphere = new Sphere() {
            @Override
            protected void updateMMat() {
                float rotateAngle = (float) GLFW.glfwGetTime() / 3f;
                mMat.identity().scale(2f).translate(position).rotateY(rotateAngle).rotateX(rotateAngle / 2f);
            }
        };
        positionalLight = new PositionalLight();
        try {
            material = new Material("gold");
        } catch (InvalidMaterialException e) {
            throw new RuntimeException(e);
        }

        normalMapTexture = new Texture2D(0, "assets/textures/normalMaps/moonNORMAL.jpg");
        imageTexture = new Texture2D(1, "assets/textures/imageTextures/moon.jpg");

        // ImGUI

        isUsingNormalMap = new ImBoolean();
        isUsingImageTexture = new ImBoolean();
        gui = new GUI(WINDOW_INIT_W, WINDOW_INIT_H, windowID, 2.5f) {
            @Override
            protected void drawFrame() {

                ImGui.newFrame(); // start frame

                ImGui.begin("Bowen's Debugger");
                ImGui.text("You can change some settings here.");
                ImGui.checkbox("Use normal map", isUsingNormalMap);
                ImGui.checkbox("Use image texture", isUsingImageTexture);
                ImGui.end();

                ImGui.render(); // end frame
            }

        };


        System.out.println("Hint: You can press F1 to look around.");
    }

    @Override
    protected void drawScene() {
        glUseProgram(program);

        //繪製sphere
        // These should be called here.It seems like you can only activate 1 texture at a time.
        normalMapTexture.bind();
        imageTexture.bind();

        // light
        glUniform4fv(globalAmbientLoc, positionalLight.getGlobalAmbient());
        glUniform4fv(lightAmbientLoc, positionalLight.getLightAmbient());
        glUniform4fv(lightDiffuseLoc, positionalLight.getLightDiffuse());
        glUniform4fv(lightSpecularLoc, positionalLight.getLightSpecular());
        glUniform3fv(lightPosition, positionalLight.getLightPosition());
        positionalLight.flipAll();

        // material
        glUniform4fv(materialAmbientLoc, material.getAmbient());
        glUniform4fv(materialDiffuseLoc, material.getDiffuse());
        glUniform4fv(materialSpecularLoc, material.getSpecular());
        glUniform1fv(materialShininessLoc, material.getShininess());
        material.flipAll();

        // matrices
        glUniformMatrix4fv(mv_matrixLoc, false, sphere.getMvMat().get(VALS_OF_16));
        glUniformMatrix4fv(norm_matrixLoc, false, sphere.getInvTrMat().get(VALS_OF_16));
        glUniformMatrix4fv(proj_matrixLoc, false, camera.getProjMat().get(VALS_OF_16));

        // boolean
        int isUsingNormalMapInInt = isUsingNormalMap.get() ? 1 : 0;
        int isUsingImageTextureInInt = isUsingImageTexture.get() ? 1 : 0;
        glUniform1i(isUsingNormalMapLoc, isUsingNormalMapInInt);
        glUniform1i(isUsingImageTextureLoc, isUsingImageTextureInInt);

        sphere.updateState(camera);
        sphere.draw(GL_TRIANGLES);

        gui.update();
    }

    @Override
    protected void getAllUniformsLoc() {
        globalAmbientLoc = glGetUniformLocation(program, "globalAmbient");
        mv_matrixLoc = glGetUniformLocation(program, "mv_matrix");
        proj_matrixLoc = glGetUniformLocation(program, "proj_matrix");
        norm_matrixLoc = glGetUniformLocation(program, "norm_matrix");
        lightAmbientLoc = glGetUniformLocation(program, "light.ambient");
        lightDiffuseLoc = glGetUniformLocation(program, "light.diffuse");
        lightSpecularLoc = glGetUniformLocation(program, "light.specular");
        lightPosition = glGetUniformLocation(program, "light.position");
        materialAmbientLoc = glGetUniformLocation(program, "material.ambient");
        materialDiffuseLoc = glGetUniformLocation(program, "material.diffuse");
        materialSpecularLoc = glGetUniformLocation(program, "material.specular");
        materialShininessLoc = glGetUniformLocation(program, "material.shininess");

        isUsingNormalMapLoc = glGetUniformLocation(program, "isUsingNormalMapInInt");
        isUsingImageTextureLoc = glGetUniformLocation(program, "isUsingImageTextureInInt");
    }

    @Override
    protected void destroy() {
        super.destroy();
        gui.destroy();
    }
}
