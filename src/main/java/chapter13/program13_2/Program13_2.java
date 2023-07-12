package chapter13.program13_2;

import chapter13.program13_1.Program13_1;
import org.joml.Vector3f;
import utilities.GLFWWindow;
import utilities.Materials;
import utilities.ShaderProgram;
import utilities.ValuesContainer;
import utilities.exceptions.InvalidMaterialException;
import utilities.models.Torus;

import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL11.GL_CW;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform1i;

public class Program13_2 extends Program13_1 {
    public Program13_2() {
        super();
    }

    @Override
    protected void init() {
        glfwWindow = new GLFWWindow(1500, 1000, "Prog13.2 Deleting Primitives");

        shaderProgram = new ShaderProgram(
                "assets/shaders/program13_1/vert.glsl",
                "assets/shaders/program13_1/frag.glsl",
                "assets/shaders/program13_2/geo.glsl"
        );
        shaderProgram.use();
        torus = new Torus(.5f, .2f, 48, true, new Vector3f());
        try {
            material = new Materials("gold");
        } catch (InvalidMaterialException e) {
            throw new RuntimeException(e);
        }

        inflateValue = 1f;
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

    public static void main(String[] args) {
        new Program13_2().run(true, false);
    }
}
