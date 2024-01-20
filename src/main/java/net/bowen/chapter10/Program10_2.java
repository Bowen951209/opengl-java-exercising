package net.bowen.chapter10;

import net.bowen.engine.GLFWWindow;
import net.bowen.engine.ShaderProgram;
import net.bowen.engine.callbacks.DefaultCallbacks;
import net.bowen.engine.exceptions.InvalidMaterialException;
import net.bowen.engine.sceneComponents.PositionalLight;
import net.bowen.engine.sceneComponents.models.Sphere;
import net.bowen.engine.sceneComponents.textures.Texture2D;
import net.bowen.engine.util.Color;
import net.bowen.engine.util.Material;

import static net.bowen.engine.util.ValuesContainer.VALS_OF_16;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL43.*;

/*Normal Mapping*/
public class Program10_2 extends Program10_1 {
    private int globalAmbientLoc, mv_matrixLoc, proj_matrixLoc, norm_matrixLoc, materialShininessLoc,
            lightAmbientLoc, lightDiffuseLoc, lightSpecularLoc, lightPosition, materialAmbientLoc,
            materialDiffuseLoc, materialSpecularLoc;
    private PositionalLight positionalLight;
    private Sphere sphere;
    private int defaultProgram;
    private Material material;

    public Program10_2(String title) {
        super(title);
    }

    @Override
    protected void init(String title) {
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        camera.setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, title);
        windowID = glfwWindow.getID();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));

        new DefaultCallbacks(windowID, camera).bindToGLFW();

        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glActiveTexture(GL_TEXTURE0);

        defaultProgram = new ShaderProgram("assets/shaders/program10_2/vert.glsl"
                , "assets/shaders/program10_2/frag.glsl")
                .getID();

        getAllUniformsLoc();
        sphere = new Sphere();
        positionalLight = new PositionalLight();
        try {
            material = new Material("gold");
        } catch (InvalidMaterialException e) {
            throw new RuntimeException(e);
        }
        Texture2D normalMapTexture = new Texture2D(0, "assets/textures/normalMaps/castleroofNORMAL.jpg");
        normalMapTexture.bind();

        System.out.println("Hint: You can press F1 to look around.");
    }


    @Override
    protected void getAllUniformsLoc() {
        globalAmbientLoc = glGetUniformLocation(defaultProgram, "globalAmbient");
        mv_matrixLoc = glGetUniformLocation(defaultProgram, "mv_matrix");
        proj_matrixLoc = glGetUniformLocation(defaultProgram, "proj_matrix");
        norm_matrixLoc = glGetUniformLocation(defaultProgram, "norm_matrix");
        lightAmbientLoc = glGetUniformLocation(defaultProgram, "light.ambient");
        lightDiffuseLoc = glGetUniformLocation(defaultProgram, "light.diffuse");
        lightSpecularLoc = glGetUniformLocation(defaultProgram, "light.specular");
        lightPosition = glGetUniformLocation(defaultProgram, "light.position");
        materialAmbientLoc = glGetUniformLocation(defaultProgram, "material.ambient");
        materialDiffuseLoc = glGetUniformLocation(defaultProgram, "material.diffuse");
        materialSpecularLoc = glGetUniformLocation(defaultProgram, "material.specular");
        materialShininessLoc = glGetUniformLocation(defaultProgram, "material.shininess");
    }

    @Override
    protected void loop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.updateVMat();
        drawScene();
        camera.handle();

        glfwSwapBuffers(windowID);
        glfwPollEvents();
    }

    protected void drawScene() {
        glUseProgram(defaultProgram);

        //繪製sphere

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

        sphere.updateState(camera);
        sphere.draw(GL_TRIANGLES);
    }

    public static void main(String[] args) {
        new Program10_2("Normal Mapping");
    }
}
