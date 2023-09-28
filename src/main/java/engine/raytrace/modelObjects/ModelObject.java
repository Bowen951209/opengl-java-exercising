package engine.raytrace.modelObjects;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL43.*;

public abstract class ModelObject {
    /**
     * The value is calculated according to the "std140 layout" rule.
     * It is ONLY compatible with the structure described in the package-info.
     *
     * @see <a href="https://learnopengl.com/Advanced-OpenGL/Advanced-GLSL">LearnOpenGL Page</a>
     */
    public static final int STRUCT_MEMORY_SPACE = 92;
    private static final int OBJ_TYPE_ROOMBOX = 0;
    private static final int OBJ_TYPE_SPHERE = 1;
    private static final int OBJ_TYPE_BOX = 2;
    private static final int OBJ_TYPE_PLANE = 3;
    protected static final int UTIL_GL_UNIFORM_BUFFER = glGenBuffers();


    protected final Vector3f position = new Vector3f(), color = new Vector3f(),
            mins = new Vector3f(), maxs = new Vector3f(), rotation = new Vector3f();

    /**
     * A matrix for computing convenience.(Not passing into shaders)
     */
    protected final Matrix4f localToWorldR = new Matrix4f();
    /**
     * Only useful for Plane class.
     */
    protected final Matrix4f invTrLocalToWorldR = new Matrix4f();
    /**
     * Only useful for Box / Plane class.
     */
    protected final Matrix4f worldToLocalR = new Matrix4f();
    /**
     * Only useful for Box / Plane class.
     */
    protected final Matrix4f worldToLocalTR = new Matrix4f();

    protected final float[] ambient, diffuse, specular;

    protected final float shininess;

    protected boolean hasColor, hasTexture, isReflective, isTransparent;
    protected float reflectivity, refractivity, ior, radius;

    public ModelObject setColor(float r, float g, float b) {
        hasColor = true;
        this.color.set(r, g, b);
        return this;
    }

    public ModelObject setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        return this;
    }

    public ModelObject setPosition(float[] position) {
        if (position.length != 3)
            throw new RuntimeException("Passed in array length is not 3.");
        this.position.set(position);
        return this;
    }

    public ModelObject setRotation(float x, float y, float z) {
        this.rotation.set(x, y, z);
        return this;
    }

    public ModelObject setRotation(float[] rotation) {
        if (rotation.length != 3)
            throw new RuntimeException("Passed in array length is not 3.");
        this.rotation.set(rotation);
        return this;
    }

    public ModelObject setReflectivity(float reflectivity) {
        isReflective = true;
        this.reflectivity = reflectivity;
        return this;
    }

    public ModelObject setRefraction(float refractivity, float ior) {
        isTransparent = true;
        this.refractivity = refractivity;
        this.ior = ior;
        return this;
    }

    protected ModelObject(float[] ambient, float[] diffuse,
                          float[] specular, float shininess) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
    }

    private static void putData(FloatBuffer dataBuffer, ModelObject modelObject) {
        // ***(0 are pads)***
        // type
        dataBuffer.put(getObjType(modelObject));
        // radius
        dataBuffer.put(modelObject.radius);
        dataBuffer.put(0);
        dataBuffer.put(0);
        // mins
        dataBuffer.put(modelObject.mins.x);
        dataBuffer.put(modelObject.mins.y);
        dataBuffer.put(modelObject.mins.z);
        dataBuffer.put(0);
        // maxs
        dataBuffer.put(modelObject.maxs.x);
        dataBuffer.put(modelObject.maxs.y);
        dataBuffer.put(modelObject.maxs.z);
        dataBuffer.put(0);
        // rotation
        dataBuffer.put(modelObject.rotation.x);
        dataBuffer.put(modelObject.rotation.y);
        dataBuffer.put(modelObject.rotation.z);
        dataBuffer.put(0);
        // position
        dataBuffer.put(modelObject.position.x);
        dataBuffer.put(modelObject.position.y);
        dataBuffer.put(modelObject.position.z);
        dataBuffer.put(0);
        // hasColor
        dataBuffer.put(modelObject.hasColor ? 1 : 0);
        // hasTexture
        dataBuffer.put(modelObject.hasTexture ? 1 : 0);
        // isReflective
        dataBuffer.put(modelObject.isReflective ? 1 : 0);
        // isTransparent
        dataBuffer.put(modelObject.isTransparent ? 1 : 0);
        // color
        dataBuffer.put(modelObject.color.x);
        dataBuffer.put(modelObject.color.y);
        dataBuffer.put(modelObject.color.z);
        dataBuffer.put(0);
        // reflectivity
        dataBuffer.put(modelObject.reflectivity);
        // refractivity
        dataBuffer.put(modelObject.refractivity);
        // IOR
        dataBuffer.put(modelObject.ior);
        // shininess
        dataBuffer.put(modelObject.shininess);
        // ambient
        dataBuffer.put(modelObject.ambient[0]);
        dataBuffer.put(modelObject.ambient[1]);
        dataBuffer.put(modelObject.ambient[2]);
        dataBuffer.put(modelObject.ambient[3]);
        // diffuse
        dataBuffer.put(modelObject.diffuse[0]);
        dataBuffer.put(modelObject.diffuse[1]);
        dataBuffer.put(modelObject.diffuse[2]);
        dataBuffer.put(modelObject.diffuse[3]);
        // specular
        dataBuffer.put(modelObject.specular[0]);
        dataBuffer.put(modelObject.specular[1]);
        dataBuffer.put(modelObject.specular[2]);
        dataBuffer.put(modelObject.specular[3]);

        // invTrLocalToWorldR
        addMatrixToBuffer(dataBuffer, modelObject.invTrLocalToWorldR);
        // worldToLocalR
        addMatrixToBuffer(dataBuffer, modelObject.worldToLocalR);
        // worldToLocalTR
        addMatrixToBuffer(dataBuffer, modelObject.worldToLocalTR);
    }

    public static void putToShader(int uniformBinding, ModelObject[] modelObjects,
                                   FloatBuffer buffer) {
        for (ModelObject modelObject : modelObjects) {
            modelObject.updateMatrices();
            putData(buffer, modelObject);
        }

        buffer.flip();
        glBindBuffer(GL_UNIFORM_BUFFER, UTIL_GL_UNIFORM_BUFFER);
        glBufferData(GL_UNIFORM_BUFFER, buffer, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, uniformBinding, UTIL_GL_UNIFORM_BUFFER);
    }

    /**
     * Create a new buffer, and use it to put into UBO.
     * This is just for convenience and not recommended because each time you call it, it'll
     * create a new instance, resource-consuming.
     * @see ModelObject#putToShader(int, ModelObject[], FloatBuffer)
     * */
    public static void putToShader(int uniformBinding, ModelObject[] modelObjects) {
        FloatBuffer dataBuffer = BufferUtils.
                createFloatBuffer(STRUCT_MEMORY_SPACE * modelObjects.length);
        putToShader(uniformBinding, modelObjects, dataBuffer);
    }

    private void updateMatrices() {
        if (this instanceof Plane) {
            localToWorldR.identity().rotateY(rotation.y);

            worldToLocalR.set(localToWorldR).invert();
            invTrLocalToWorldR.set(worldToLocalR).transpose();
            worldToLocalTR.set(localToWorldR).translate(0, mins.y, 0).invert();
        } else if (this instanceof Box) {
            localToWorldR.identity().rotateX(rotation.x).rotateY(rotation.y).rotateZ(rotation.z);

            worldToLocalR.set(localToWorldR).invert();
            worldToLocalTR.identity().translate(position).mul(localToWorldR).invert();
        }
    }

    private static void addMatrixToBuffer(FloatBuffer buffer, Matrix4f matrix) {
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                buffer.put(matrix.get(column, row));
            }
        }
    }

    private static int getObjType(ModelObject object) {
        if (object instanceof Box) {
            return OBJ_TYPE_BOX;
        } else if (object instanceof Plane) {
            return OBJ_TYPE_PLANE;
        } else if (object instanceof Sphere) {
            return OBJ_TYPE_SPHERE;
        } else if (object instanceof RoomBox) {
            return OBJ_TYPE_ROOMBOX;
        } else {
            throw new RuntimeException("Unpredicted class!");
        }
    }
}
