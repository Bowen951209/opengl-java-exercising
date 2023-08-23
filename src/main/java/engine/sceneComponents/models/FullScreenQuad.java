package engine.sceneComponents.models;

import static org.lwjgl.opengl.GL43.glDrawArrays;

public class FullScreenQuad extends Model {
    private static final float[] VERTICES = {
            -1.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f
    };
    private static final float[] TEXCOORDS = {
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f
    };

    public FullScreenQuad() {
        super(null, false, true, false);

        storeDataToVBOs(VERTICES, null, TEXCOORDS);
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