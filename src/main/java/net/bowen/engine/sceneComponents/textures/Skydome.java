package net.bowen.engine.sceneComponents.textures;

import net.bowen.engine.ShaderProgram;
import net.bowen.engine.sceneComponents.Camera;
import net.bowen.engine.sceneComponents.models.HalfSphere;
import net.bowen.engine.util.ValuesContainer;

import static org.lwjgl.opengl.GL43.*;
/**
 * ps. The skydome idea in the book is really not perfect, or even acceptable.
 * There's always a flaw, either mine and the book's.
 * So, after implementing all the book's topics, I'll probably redo this topic, but use another cloud simulating method.
 * So that will be in the future.
 * */
public class Skydome extends HalfSphere {
    private static final ShaderProgram SHADER_PROGRAM = new ShaderProgram(
            "assets/shaders/skydome/vert.glsl",
            "assets/shaders/skydome/frag.glsl");
    private final Texture3D texture3D;
    private final Camera camera;

    public Skydome(Texture3D texture3D, Camera camera) {
        super();
        this.texture3D = texture3D;
        this.camera = camera;
    }

    @Override
    public void draw(int mode) {
        SHADER_PROGRAM.use();
        SHADER_PROGRAM.putUniformMatrix4f("mv_matrix", super.mvMat.get(ValuesContainer.VALS_OF_16));
        SHADER_PROGRAM.putUniformMatrix4f("proj_matrix", camera.getProjMat().get(ValuesContainer.VALS_OF_16));
        this.texture3D.bind();
        glDisable(GL_DEPTH_TEST);
        glFrontFace(GL_CW);
        super.draw(mode);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
    }
}
