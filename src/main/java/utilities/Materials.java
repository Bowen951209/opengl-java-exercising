package utilities;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class Materials {
    public FloatBuffer getAMBIENT() {
        return AMBIENT;
    }

    public FloatBuffer getDIFFUSE() {
        return DIFFUSE;
    }

    public FloatBuffer getSPECULAR() {
        return SPECULAR;
    }

    public FloatBuffer getSHININESS() {
        return SHININESS;
    }

    private final FloatBuffer AMBIENT = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer DIFFUSE = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer SPECULAR = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer SHININESS = BufferUtils.createFloatBuffer(1);

    public Materials(String material) {
        switch (material) {
            case "gold" -> {
                AMBIENT.put(goldAmbient());
                DIFFUSE.put(goldDiffuse());
                SPECULAR.put(goldSpecular());
                SHININESS.put(goldShininess());
            }
            case "bronze" -> {
                AMBIENT.put(bronzeAmbient());
                DIFFUSE.put(bronzeDiffuse());
                SPECULAR.put(bronzeSpecular());
                SHININESS.put(bronzeShininess());
            }
            case "silver" -> {
                AMBIENT.put(silverAmbient());
                DIFFUSE.put(silverDiffuse());
                SPECULAR.put(silverSpecular());
                SHININESS.put(silverShininess());
            }
            default -> throw new RuntimeException("Undefined material is passed in.");
        }
    }


    public static float[] goldAmbient() {
        return new float[]{0.2473f, 0.1995f, 0.0745f, 1f};
    }

    public static float[] goldDiffuse() {
        return new float[]{0.7516f, 0.6065f, 0.2265f, 1};
    }

    public static float[] goldSpecular() {
        return new float[]{0.6283f, 0.5559f, 0.3661f, 1};
    }

    public static float goldShininess() {
        return 51.2f;
    }

    public static float[] bronzeAmbient() {
        return new float[]{0.2125f, 0.1275f, 0.0540f, 1f};
    }

    public static float[] bronzeDiffuse() {
        return new float[]{0.7140f, 0.4284f, 0.1814f, 1};
    }

    public static float[] bronzeSpecular() {
        return new float[]{0.3936f, 0.2719f, 0.1667f, 1};
    }

    public static float bronzeShininess() {
        return 25.6f;
    }

    public static float[] silverAmbient() {
        return new float[]{0.1923f, 0.1923f, 0.1923f, 1};
    }

    public static float[] silverDiffuse() {
        return new float[]{0.5075f, 0.5075f, 0.5075f, 1};
    }

    public static float[] silverSpecular() {
        return new float[]{0.5083f, 0.5083f, 0.5083f, 1};
    }

    public static float silverShininess() {
        return 51.2f;
    }

    public void flipAll() {
        AMBIENT.flip();
        DIFFUSE.flip();
        SPECULAR.flip();
        SHININESS.flip();
    }
}
