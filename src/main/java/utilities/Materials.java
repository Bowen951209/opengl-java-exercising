package utilities;

public class Materials {
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
}
