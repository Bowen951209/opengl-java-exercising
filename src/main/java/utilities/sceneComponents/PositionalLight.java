package utilities.sceneComponents;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class PositionalLight {
    private final FloatBuffer GLOBAL_AMBIENT = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer LIGHT_AMBIENT = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer LIGHT_DIFFUSE = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer LIGHT_SPECULAR = BufferUtils.createFloatBuffer(4);


    private final FloatBuffer LIGHT_POSITION = BufferUtils.createFloatBuffer(3);
    private final Vector3f LIGHT_POSITION_IN_VECTOR;

    public FloatBuffer getLIGHT_POSITION() {
        LIGHT_POSITION_IN_VECTOR.get(LIGHT_POSITION);
        return LIGHT_POSITION;
    }
    public FloatBuffer getGLOBAL_AMBIENT() {
        return GLOBAL_AMBIENT;
    }

    public FloatBuffer getLIGHT_AMBIENT() {
        return LIGHT_AMBIENT;
    }

    public FloatBuffer getLIGHT_DIFFUSE() {
        return LIGHT_DIFFUSE;
    }

    public FloatBuffer getLIGHT_SPECULAR() {
        return LIGHT_SPECULAR;
    }

    public PositionalLight() {
        this(
                new float[] {0.7f, 0.7f, 0.7f, 1.0f},
                new float[] {0.0f, 0.0f, 0.0f, 1.0f},
                new float[] {1.0f, 1.0f, 1.0f, 1.0f},
                new float[] {1.0f, 1.0f, 1.0f, 1.0f},
                new Vector3f(5.0f, 2.0f, 2.0f)
        );
    }

    public void flipAll() {
        GLOBAL_AMBIENT.flip();
        LIGHT_AMBIENT.flip();
        LIGHT_DIFFUSE.flip();
        LIGHT_SPECULAR.flip();
        LIGHT_POSITION.flip();
    }
    public PositionalLight(float[] globalAmbient, float[] lightAmbient, float[] lightDiffuse, float[] lightSpecular, Vector3f lightPosition) {
        GLOBAL_AMBIENT.put(globalAmbient);
        LIGHT_AMBIENT.put(lightAmbient);
        LIGHT_DIFFUSE.put(lightDiffuse);
        LIGHT_SPECULAR.put(lightSpecular);
        LIGHT_POSITION_IN_VECTOR = new Vector3f(lightPosition);
    }
}
