package engine.sceneComponents.models;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL43.*;

public class FullScreenQuad extends Model{
    private static final float[] VERTICES = new float[] {
            -1.0f, 1.0f, 0.0f,  -1.0f,-1.0f, 0.0f,  1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,  1.0f,  1.0f, 0.0f,  -1.0f,  1.0f, 0.0f
    };

    private static final float[] TCS = new float[] {
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f
    };
    protected FullScreenQuad() {
        super(null, false, true, false);

        verticesInBuf = BufferUtils.createFloatBuffer(VERTICES.length);
        verticesInBuf.put(VERTICES);
        verticesInBuf.flip();
        tcInBuf = BufferUtils.createFloatBuffer(TCS.length);
        tcInBuf.put(TCS);
        tcInBuf.flip();

        storeDataToVBOs(verticesInBuf, null, tcInBuf);
    }

    @Override
    protected void updateMMat() {

    }

    @Override
    public void draw(int mode) {
        bindVAO();
        glDrawArrays(mode, 0, VERTICES.length);
    }
}
