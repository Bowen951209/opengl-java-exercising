package net.bowen.engine.sceneComponents;

import net.bowen.engine.ShaderProgram;
import net.bowen.engine.sceneComponents.models.Sphere;
import net.bowen.engine.util.ValuesContainer;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL43.glUniform3fv;
import static org.lwjgl.opengl.GL43.glUniform4fv;

public class PositionalLight {
    private final FloatBuffer globalAmbient = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer lightAmbient = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer lightDiffuse = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer lightSpecular = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer lightPosition = BufferUtils.createFloatBuffer(3);
    private final Vector3f lightPositionInVector;

    private Sphere sphere;
    private ShaderProgram drawProgram;
    private boolean isDrawInit;

    public void setDrawable(Camera camera) {

        drawProgram = new ShaderProgram(
                "assets/shaders/utils/light/vert.glsl",
                "assets/shaders/utils/light/frag.glsl"
        );

        sphere = new Sphere(50, lightPositionInVector) {
            /**
             * This method uses the program needed itself, update uniforms, and draw call.
             * */
            @Override
            public void draw(int mode) {
                drawProgram.use();
                putToUniforms(
                        drawProgram.getUniformLoc("globalAmbient"),
                        drawProgram.getUniformLoc("light.ambient"),
                        drawProgram.getUniformLoc("light.diffuse"),
                        drawProgram.getUniformLoc("light.specular"),
                        drawProgram.getUniformLoc("light.position")
                );
                drawProgram.putUniformMatrix4f("mvMat", sphere.getMvMat().get(ValuesContainer.VALS_OF_16));
                drawProgram.putUniformMatrix4f("projMat", camera.getProjMat().get(ValuesContainer.VALS_OF_16));

                super.draw(mode);
            }
        };

        isDrawInit = true;
    }

    public Sphere getSphere() {
        if (!isDrawInit) throw new RuntimeException("Object did not init to drawable, please init it first.");
        return sphere;
    }

    public PositionalLight setPosition(float x, float y, float z) {
        lightPositionInVector.set(x, y, z);
        lightPositionInVector.get(lightPosition);

        return this;
    }

    public FloatBuffer getLightPosition() {
        lightPositionInVector.get(lightPosition);
        return lightPosition;
    }

    public PositionalLight setGlobalAmbient(float[] value) {
        this.globalAmbient.clear();
        this.globalAmbient.put(value);
        this.globalAmbient.flip();
        System.out.print("global.ambient set to: ");
        ValuesContainer.printFloatBuffer(this.globalAmbient);
        return this;
    }

    public FloatBuffer getGlobalAmbient() {
        return globalAmbient;
    }

    public PositionalLight setLightAmbient(float[] value) {
        this.lightAmbient.clear();
        this.lightAmbient.put(value);
        this.lightAmbient.flip();
        System.out.print("light.ambient set to: ");
        ValuesContainer.printFloatBuffer(this.lightAmbient);
        return this;
    }

    public FloatBuffer getLightAmbient() {
        return lightAmbient;
    }

    public PositionalLight setLightDiffuse(float[] value) {
        this.lightDiffuse.clear();
        this.lightDiffuse.put(value);
        this.lightDiffuse.flip();
        System.out.print("light.diffuse set to: ");
        ValuesContainer.printFloatBuffer(this.lightDiffuse);
        return this;
    }

    public FloatBuffer getLightDiffuse() {
        return lightDiffuse;
    }

    public FloatBuffer getLightSpecular() {
        return lightSpecular;
    }

    public PositionalLight(float[] globalAmbient, float[] lightAmbient, float[] lightDiffuse, float[] lightSpecular, Vector3f lightPosition) {
        this.globalAmbient.put(globalAmbient);
        this.lightAmbient.put(lightAmbient);
        this.lightDiffuse.put(lightDiffuse);
        this.lightSpecular.put(lightSpecular);
        flipAll();
        lightPositionInVector = new Vector3f(lightPosition);
    }

    public PositionalLight() {
        this(
                new float[]{0.1f, 0.1f, 0.1f, 1.0f}, // global ambient
                new float[]{0.0f, 0.0f, 0.0f, 1.0f}, // light ambient
                new float[]{1.0f, 1.0f, 1.0f, 1.0f}, // light diffuse
                new float[]{1.0f, 1.0f, 1.0f, 1.0f}, // light specular
                new Vector3f(5.0f, 2.0f, 2.0f) // light position
        );
    }

    public PositionalLight brightLight() {
        return setGlobalAmbient(new float[]{0.5f, 0.5f, 0.5f, 1.0f});
    }

    public void flipAll() {
        globalAmbient.flip();
        lightAmbient.flip();
        lightDiffuse.flip();
        lightSpecular.flip();
        lightPosition.flip();
    }

    public void putToUniforms(int globalAmbLoc, int lightAmbLoc, int lightDiffLoc, int lightSpecLoc, int lightPosLoc) {
        // These are vec4
        glUniform4fv(globalAmbLoc, this.globalAmbient);
        glUniform4fv(lightAmbLoc, this.lightAmbient);
        glUniform4fv(lightDiffLoc, this.lightDiffuse);
        glUniform4fv(lightSpecLoc, this.lightSpecular);

        // This is vec3
        glUniform3fv(lightPosLoc, this.lightPosition);
    }
}
