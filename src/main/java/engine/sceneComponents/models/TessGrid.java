package engine.sceneComponents.models;

import engine.ShaderProgram;
import engine.util.ValuesContainer;
import engine.sceneComponents.Camera;
import engine.sceneComponents.textures.Texture2D;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL43.*;

public class TessGrid extends Model {
    private static ShaderProgram tessRenderProgram;
    private final Texture2D imageTexture, heightMap;
    private int drawMode = GL_FILL;

    public static void useTessProgram() {
        tessRenderProgram.use();
    }

    public TessGrid(String imageTexturePath, String heightMapPath) {
        this(imageTexturePath, heightMapPath, new Vector3f());
    }

    public TessGrid(String imageTexturePath, String heightMapPath, Vector3f position) {
        super(position, false, true, false);
        imageTexture = new Texture2D(0, imageTexturePath);
        heightMap = new Texture2D(1, heightMapPath);
        if (tessRenderProgram == null) {
            tessRenderProgram = new ShaderProgram(
                    "assets/shaders/tessGrid/vert.glsl",
                    "assets/shaders/tessGrid/frag.glsl",
                    "assets/shaders/tessGrid/tcs.glsl",
                    "assets/shaders/tessGrid/tes.glsl"
            );
        }

        // default mMat
        super.mMat.identity()
                .translate(position)
                .scale(10f);
    }

    @Override
    protected void updateMMat() {}

    @Override
    public void draw(int mode) {
        // mode is no use here

        bindVAO();

        imageTexture.bind();
        heightMap.bind();

        glPatchParameteri(GL_PATCH_VERTICES, 4);
        glPolygonMode(GL_FRONT_AND_BACK, drawMode);
        glDrawArraysInstanced(GL_PATCHES, 0, 4, 64 * 64);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); // for gui to render
    }

    public void putUniform(Camera camera) {
        // mv
        glUniformMatrix4fv(tessRenderProgram.getUniformLoc("mv_matrix"),
                false, mvMat.get(ValuesContainer.VALS_OF_16));

        // proj
        glUniformMatrix4fv(tessRenderProgram.getUniformLoc("p_matrix"),
                false, camera.getProjMat().get(ValuesContainer.VALS_OF_16));
    }

    @Override
    public void updateState(Camera camera) {
        super.updateState(camera);
        putUniform(camera);
    }

    public void setDrawMode(int mode) {
        this.drawMode = mode;
    }
}
