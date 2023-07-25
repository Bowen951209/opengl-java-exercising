package engine.util;

import static org.lwjgl.opengl.GL43.*;

import org.lwjgl.BufferUtils;
import engine.exceptions.InvalidMaterialException;

import java.nio.FloatBuffer;


/*
 * ADS value can be find in https://barradeau.com/nicoptere/dump/materials.html
 * */
public class Material {
    private static Material goldMaterialInstance;
    private static Material bronzeMaterialInstance;
    private static Material silverMaterialInstance;
    private static Material jadeMaterialInstance;

    public static Material getMaterial(String material) {
        try {
            switch (material.toUpperCase()) {
                case "GOLD" -> {
                    if (goldMaterialInstance == null) {
                        goldMaterialInstance = new Material("gold");
                    }
                    return goldMaterialInstance;
                }
                case "BRONZE" -> {
                    if (bronzeMaterialInstance == null) {
                        bronzeMaterialInstance = new Material("bronze");
                    }
                    return bronzeMaterialInstance;
                }
                case "SILVER" -> {
                    if (silverMaterialInstance == null) {
                        silverMaterialInstance = new Material("silver");
                    }
                    return silverMaterialInstance;
                }
                case "JADE" -> {
                    if (jadeMaterialInstance == null) {
                        jadeMaterialInstance = new Material("jade");
                    }
                    return jadeMaterialInstance;
                }
            }

            return new Material(material);
        } catch (InvalidMaterialException e) {
            throw new RuntimeException(e);
        }
    }

    private final FloatBuffer ambient = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer diffuse = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer specular = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer shininess = BufferUtils.createFloatBuffer(1);

    public FloatBuffer getAmbient() {
        return ambient;
    }

    public FloatBuffer getDiffuse() {
        return diffuse;
    }

    public FloatBuffer getSpecular() {
        return specular;
    }

    public FloatBuffer getShininess() {
        return shininess;
    }

    public Material(String material) throws InvalidMaterialException {
        switch (material.toUpperCase()) {
            case "GOLD" -> {
                ambient.put(goldAmbient());
                diffuse.put(goldDiffuse());
                specular.put(goldSpecular());
                shininess.put(goldShininess());
            }
            case "BRONZE" -> {
                ambient.put(bronzeAmbient());
                diffuse.put(bronzeDiffuse());
                specular.put(bronzeSpecular());
                shininess.put(bronzeShininess());
            }
            case "SILVER" -> {
                ambient.put(silverAmbient());
                diffuse.put(silverDiffuse());
                specular.put(silverSpecular());
                shininess.put(silverShininess());
            }
            case "JADE" -> {
                ambient.put(jadeAmbient());
                diffuse.put(jadeDiffuse());
                specular.put(jadeSpecular());
                shininess.put(jadeShininess());
            }
            default -> throw new InvalidMaterialException("Undefined material is passed in.");
        }
        flipAll();
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

    public static float[] jadeAmbient() {
        return new float[]{0.135f, 0.2225f, 0.1575f, 0.95f};
    }

    public static float[] jadeDiffuse() {
        return new float[]{0.54f, 0.89f, 0.63f, 0.95f};
    }

    public static float[] jadeSpecular() {
        return new float[]{0.316228f, 0.316228f, 0.316228f, 0.95f};
    }

    public static float jadeShininess() {
        return 12.8f;
    }

    public void flipAll() {
        ambient.flip();
        diffuse.flip();
        specular.flip();
        shininess.flip();
    }

    public void putToUniforms(int ambLoc, int diffLoc, int specLoc, int shineLoc) {
        glUniform4fv(ambLoc, this.ambient);
        glUniform4fv(diffLoc, this.diffuse);
        glUniform4fv(specLoc, this.specular);
        glUniform1fv(shineLoc, shininess);
    }

    public void putToUniforms(int shineLoc) {
        glUniform1fv(shineLoc, shininess);
    }
}
