package chapter10.program10_3.launcher;

import chapter10.program10_2.launcher.Program10_2;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.lwjgl.glfw.GLFW;
import utilities.*;
import utilities.callbacks.DefaultCallbacks;
import utilities.models.Sphere;
import utilities.sceneComponents.PositionalLight;
import utilities.sceneComponents.Texture;

import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static utilities.ValuesContainer.VALS_OF_16;

public class Program10_3 extends Program10_2 {
    protected int program;
    private Sphere sphere;
    private PositionalLight positionalLight;
    private Materials material;

    private int globalAmbientLoc, mv_matrixLoc, proj_matrixLoc, norm_matrixLoc, materialShininessLoc,
            lightAmbientLoc, lightDiffuseLoc, lightSpecularLoc, lightPosition, materialAmbientLoc,
            materialDiffuseLoc, materialSpecularLoc, isUsingNormalMapLoc, isUsingImageTextureLoc;
    private Texture imageTexture, normalMapTexture;
    private GUI gui;
    private ImBoolean isUsingNormalMap, isUsingImageTexture;

    public Program10_3(String title) {
        super(title);
    }

    public static void main(String[] args) {
        new Program10_3("Texture + Normal Mapping");
    }

    @Override
    protected void init(String title) {
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        CAMERA.setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, title);
        windowID = glfwWindow.getWindowHandle();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));

        new DefaultCallbacks(windowID, CAMERA, true).bindToGLFW();

        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        program = new ShaderProgramSetter(Path.of("assets/shaders/program10_3/vertex.glsl")
                , Path.of("assets/shaders/program10_3/fragment.glsl"))
                .getProgram();

        getAllUniformsLoc();
        sphere = new Sphere() {
            @Override
            protected void updateMMat() {
                float rotateAngle = (float) GLFW.glfwGetTime() / 3f;
                M_MAT.identity().scale(2f).translate(POSITION).rotateY(rotateAngle).rotateX(rotateAngle / 2f);
            }
        };
        positionalLight = new PositionalLight();
        material = new Materials("gold");

        normalMapTexture = new Texture(0, "assets/textures/normalMaps/moonNORMAL.jpg");
        imageTexture = new Texture(1, "assets/textures/imageTextures/moon.jpg");

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

            @Override
            protected void initElementStates() {}
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
        glUniform4fv(globalAmbientLoc, positionalLight.getGLOBAL_AMBIENT());
        glUniform4fv(lightAmbientLoc, positionalLight.getLIGHT_AMBIENT());
        glUniform4fv(lightDiffuseLoc, positionalLight.getLIGHT_DIFFUSE());
        glUniform4fv(lightSpecularLoc, positionalLight.getLIGHT_SPECULAR());
        glUniform3fv(lightPosition, positionalLight.getLIGHT_POSITION());
        positionalLight.flipAll();

        // material
        glUniform4fv(materialAmbientLoc, material.getAMBIENT());
        glUniform4fv(materialDiffuseLoc, material.getDIFFUSE());
        glUniform4fv(materialSpecularLoc, material.getSPECULAR());
        glUniform1fv(materialShininessLoc, material.getSHININESS());
        material.flipAll();

        // matrices
        glUniformMatrix4fv(mv_matrixLoc, false, sphere.getMV_MAT().get(VALS_OF_16));
        glUniformMatrix4fv(norm_matrixLoc, false, sphere.getINV_TR_MAT().get(VALS_OF_16));
        glUniformMatrix4fv(proj_matrixLoc, false, CAMERA.getProjMat().get(VALS_OF_16));

        // boolean
        int isUsingNormalMapInInt = isUsingNormalMap.get() ? 1 : 0;
        int isUsingImageTextureInInt = isUsingImageTexture.get() ? 1 : 0;
        glUniform1i(isUsingNormalMapLoc, isUsingNormalMapInInt);
        glUniform1i(isUsingImageTextureLoc, isUsingImageTextureInInt);

        sphere.updateState(CAMERA);
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
