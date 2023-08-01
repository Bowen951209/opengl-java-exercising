package engine.sceneComponents.textures;

import engine.ShaderProgram;
import engine.sceneComponents.models.FileModel;
import engine.sceneComponents.Camera;
import engine.util.ValuesContainer;

import static org.lwjgl.opengl.GL43.*;
public class Skydome extends FileModel {
    private static final ShaderProgram SHADER_PROGRAM = new ShaderProgram(
            "assets/shaders/skydome/vert.glsl",
            "assets/shaders/skydome/frag.glsl");
    private final Texture3D texture3D;
    private final Camera camera;

    public Skydome(Texture3D texture3D, Camera camera) {
        super("assets/models/dome.obj", true);
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
