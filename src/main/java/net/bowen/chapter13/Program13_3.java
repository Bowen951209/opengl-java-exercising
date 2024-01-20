package net.bowen.chapter13;

import org.joml.Vector3f;
import net.bowen.engine.GLFWWindow;
import net.bowen.engine.util.Material;
import net.bowen.engine.ShaderProgram;
import net.bowen.engine.util.ValuesContainer;
import net.bowen.engine.exceptions.InvalidMaterialException;
import net.bowen.engine.sceneComponents.models.Torus;

import static org.lwjgl.opengl.GL20.*;

public class Program13_3 extends Program13_2 {
    @Override
    protected void customizedInit() {
        glfwWindow = new GLFWWindow(1500, 1000, "Prog13.3 Adding Primitives");

        shaderProgram = new ShaderProgram(
                "assets/shaders/program13_3/vertShader.glsl",
                "assets/shaders/program13_3/fragShader.glsl",
                "assets/shaders/program13_3/geomShader.glsl"
        );
        shaderProgram.use();
        torus = new Torus(.5f, .2f, 48, true, new Vector3f());
        try {
            material = new Material("gold");
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

        glUniformMatrix4fv(shaderProgram.getUniformLoc("mv_matrix"), false, torus.getMvMat().get(ValuesContainer.VALS_OF_16));
        glUniformMatrix4fv(shaderProgram.getUniformLoc("proj_matrix"), false, camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        glUniformMatrix4fv(shaderProgram.getUniformLoc("norm_matrix"), false, torus.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        glUniform1f(shaderProgram.getUniformLoc("inflateValue"), inflateValue);

        // Front face, lighting enabled.
        glFrontFace(GL_CCW);
        torus.draw(GL_TRIANGLES);
    }



    private Program13_3() {
        super();
    }
    public static void main(String[] args) {
        new Program13_3().run(true, false);
    }
}
