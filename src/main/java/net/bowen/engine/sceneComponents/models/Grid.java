package net.bowen.engine.sceneComponents.models;

import org.joml.Vector3f;

import static org.lwjgl.opengl.GL43.*;

public class Grid extends Model {
    private static final float[] VERTICES = {
            -120.0f, 0.0f, -240.0f, -120.0f, 0.0f, 0.0f, 120.0f, 0.0f, -240.0f,
            120.0f, 0.0f, -240.0f, -120.0f, 0.0f, 0.0f, 120.0f, 0.0f, 0.0f
    };
    private static final float[] TEXCOORDS = {
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
            1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f
    };
    private static final float[] NORMALS = {
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    public Grid() {
        this(new Vector3f());
    }

    public Grid(Vector3f position) {
        super(position, true, true, false);

        storeDataToVBOs(VERTICES, NORMALS, TEXCOORDS);
    }

    @Override
    protected void updateMMat() {
        mMat.identity().translate(position);
    }

    @Override
    public void draw(int mode) {
        bindVAO();
        glDrawArrays(mode, 0, VERTICES.length);
    }
}