package chapter13;

import org.joml.Vector3f;
import engine.GLFWWindow;
import engine.util.Material;
import engine.ShaderProgram;
import engine.util.ValuesContainer;
import engine.exceptions.InvalidMaterialException;
import engine.sceneComponents.models.Torus;

import static org.lwjgl.opengl.GL20.*;

public class Program13_4 extends Program13_2 {
    private Program13_4() {
        super();
    }

    @Override
    protected void customizedInit() {
        glfwWindow = new GLFWWindow(1500, 1000, "Prog13.4 Changing Primitive type");

        shaderProgram = new ShaderProgram(
                "assets/shaders/program13_4/vert.glsl",
                "assets/shaders/program13_4/frag.glsl",
                "assets/shaders/program13_4/geo.glsl"
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

        glUniformMatrix4fv(shaderProgram.getUniformLoc("mv_matrix"), false, torus.getMvMat().get(ValuesContainer.VALS_OF_16));
        glUniformMatrix4fv(shaderProgram.getUniformLoc("proj_matrix"), false, camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        glUniformMatrix4fv(shaderProgram.getUniformLoc("norm_matrix"), false, torus.getInvTrMat().get(ValuesContainer.VALS_OF_16));

        glFrontFace(GL_CCW);
        torus.draw(GL_TRIANGLES);
    }

    public static void main(String[] args) {
        new Program13_4().run(false, false);
    }
}
