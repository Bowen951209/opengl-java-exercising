package engine.util;

import static org.lwjgl.opengl.GL43.*;

import org.lwjgl.BufferUtils;
import engine.exceptions.InvalidMaterialException;

import java.nio.FloatBuffer;


/*
 * ADS value can be find in https://barradeau.com/nicoptere/dump/materials.html
 * */
public class Material {
    public static final float[] GOLD_AMBIENT = {0.2473f, 0.1995f, 0.0745f, 1f};
    public static final float[] GOLD_DIFFUSE = {0.7516f, 0.6065f, 0.2265f, 1f};
    public static final float[] GOLD_SPECULAR = {0.6283f, 0.5559f, 0.3661f, 1f};
    public static final float GOLD_SHININESS = 51.2f;

    public static final float[] SILVER_AMBIENT = {0.19225f, 0.19225f, 0.19225f, 1f};
    public static final float[] SILVER_DIFFUSE = {0.50754f, 0.50754f, 0.50754f, 1f};
    public static final float[] SILVER_SPECULAR = {0.508273f, 0.508273f, 0.508273f, 1f};
    public static final float SILVER_SHININESS = 51.2f;

    public static final float[] BRONZE_AMBIENT = {0.2125f, 0.1275f, 0.0540f, 1f};
    public static final float[] BRONZE_DIFFUSE = {0.7140f, 0.4284f, 0.1814f, 1f};
    public static final float[] BRONZE_SPECULAR = {0.3936f, 0.2719f, 0.1667f, 1f};
    public static final float BRONZE_SHININESS = 25.6f;
    public static final float[] JADE_AMBIENT = {0.135f, 0.2225f, 0.1575f, 0.95f};
    public static final float[] JADE_DIFFUSE = {0.54f, 0.89f, 0.63f, 0.95f};
    public static final float[] JADE_SPECULAR = {0.316228f, 0.316228f, 0.316228f, 0.95f};
    public static final float JADE_SHININESS = 12.8f;


    private static Material goldMaterialInstance;
    private static Material bronzeMaterialInstance;
    private static Material silverMaterialInstance;
    private static Material jadeMaterialInstance;

    private final FloatBuffer ambient = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer diffuse = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer specular = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer shininess = BufferUtils.createFloatBuffer(1);

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
                ambient.put(GOLD_AMBIENT);
                diffuse.put(GOLD_DIFFUSE);
                specular.put(GOLD_SPECULAR);
                shininess.put(GOLD_SHININESS);
            }
            case "BRONZE" -> {
                ambient.put(BRONZE_AMBIENT);
                diffuse.put(BRONZE_DIFFUSE);
                specular.put(BRONZE_SPECULAR);
                shininess.put(BRONZE_SHININESS);
            }
            case "SILVER" -> {
                ambient.put(SILVER_AMBIENT);
                diffuse.put(SILVER_DIFFUSE);
                specular.put(SILVER_SPECULAR);
                shininess.put(SILVER_SHININESS);
            }
            case "JADE" -> {
                ambient.put(JADE_AMBIENT);
                diffuse.put(JADE_DIFFUSE);
                specular.put(JADE_SPECULAR);
                shininess.put(JADE_SHININESS);
            }
            default -> throw new InvalidMaterialException("Undefined material is passed in.");
        }
        flipAll();
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
