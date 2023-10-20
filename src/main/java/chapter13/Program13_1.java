package chapter13;

import engine.util.Destroyer;
import engine.util.Material;
import engine.util.ValuesContainer;
import org.joml.Vector3f;
import engine.*;
import engine.exceptions.InvalidMaterialException;
import engine.sceneComponents.models.Torus;
import engine.sceneComponents.PositionalLight;

import static org.lwjgl.opengl.GL43.*;

public class Program13_1 extends App {
    protected ShaderProgram shaderProgram;
    protected Torus torus;
    protected final PositionalLight light = new PositionalLight()
            .setGlobalAmbient(new float[]{.5f, .5f, .5f, 1f})
            .setLightAmbient(new float[]{.9f, .9f, .9f, 1f});
    protected Material material;
    protected float inflateValue, inflateOffSet = 0.05f;

    @Override
    protected void customizedInit() {
        glfwWindow = new GLFWWindow(1500, 1000, "Prog13.1 Geometry shader first try");

        shaderProgram = new ShaderProgram(
                "assets/shaders/program13_1/vert.glsl",
                "assets/shaders/program13_1/frag.glsl",
                "assets/shaders/program13_1/geo.glsl"
        );
        shaderProgram.use();
        torus = new Torus(.5f, .2f, 48, true, new Vector3f());
        try {
            material = new Material("gold");
        } catch (InvalidMaterialException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void drawScene() {
        torus.updateState(camera);
        glUniform4fv(shaderProgram.getUniformLoc("globalAmbient"), light.getGlobalAmbient());
        glUniform4fv(shaderProgram.getUniformLoc("light.ambient"), light.getLightAmbient());
        glUniform4fv(shaderProgram.getUniformLoc("light.specular"), light.getLightDiffuse());
        glUniform4fv(shaderProgram.getUniformLoc("light.diffuse"), light.getLightSpecular());
        glUniform3fv(shaderProgram.getUniformLoc("light.position"), light.getLightPosition());
        light.flipAll();

        glUniform4fv(shaderProgram.getUniformLoc("material.ambient"), material.getAmbient());
        glUniform4fv(shaderProgram.getUniformLoc("material.specular"), material.getDiffuse());
        glUniform4fv(shaderProgram.getUniformLoc("material.diffuse"), material.getSpecular());
        glUniform1fv(shaderProgram.getUniformLoc("material.shininess"), material.getShininess());
        material.flipAll();

        glUniformMatrix4fv(shaderProgram.getUniformLoc("m_matrix"), false, torus.getMMat().get(ValuesContainer.VALS_OF_16));
        glUniformMatrix4fv(shaderProgram.getUniformLoc("v_matrix"), false, camera.getVMat().get(ValuesContainer.VALS_OF_16));
        glUniformMatrix4fv(shaderProgram.getUniformLoc("mv_matrix"), false, torus.getMvMat().get(ValuesContainer.VALS_OF_16));
        glUniformMatrix4fv(shaderProgram.getUniformLoc("p_matrix"), false, camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        glUniformMatrix4fv(shaderProgram.getUniformLoc("norm_matrix"), false, torus.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        inflateValue += inflateOffSet;
        if (inflateValue >= 2f) {
            inflateOffSet = -0.05f;
        }
        if (inflateValue <= -0.5f) {
            inflateOffSet = 0.05f;
        }
        glUniform1f(shaderProgram.getUniformLoc("inflateValue"), inflateValue);

        // Front face, lighting enabled.
        glUniform1i(shaderProgram.getUniformLoc("isLighting"), 1);
        glFrontFace(GL_CCW);
        torus.draw(GL_TRIANGLES);

        // Back face, lighting disabled.
        glUniform1i(shaderProgram.getUniformLoc("isLighting"), 0);
        glFrontFace(GL_CW);
        torus.draw(GL_TRIANGLES);
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID());
    }

    public static void main(String[] args) {
        new Program13_1().run(true, false);
    }
}
