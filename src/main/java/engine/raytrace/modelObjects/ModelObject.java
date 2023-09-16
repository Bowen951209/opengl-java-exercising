package engine.raytrace.modelObjects;

import engine.ShaderProgram;
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
     * */
    private static final int STRUCT_MEMORY_SPACE = 44;
    private static final int OBJ_TYPE_ROOMBOX = 0, OBJ_TYPE_SPHERE = 1,
            OBJ_TYPE_BOX = 2, OBJ_TYPE_PLANE = 3;
    protected static final int UTIL_GL_UNIFORM_BUFFER = glGenBuffers();


    protected final Vector3f position = new Vector3f(), color = new Vector3f(),
            mins = new Vector3f(), maxs = new Vector3f(), rotation = new Vector3f();
    protected final float[] ambient, diffuse, specular;
    protected final float shininess;


    // TODO: 2023/9/16 For hasTexture, add a texture binding element in struct.
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

    public ModelObject setRotation(Vector3f rotation) {
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

    public static void putToShader(ShaderProgram shaderProgram, int uniformBinding,
                                   ModelObject[] modelObjects) {
        FloatBuffer dataBuffer = BufferUtils.
                createFloatBuffer(STRUCT_MEMORY_SPACE * modelObjects.length);

        for (ModelObject modelObject : modelObjects) {
            // (0 are pads)

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
        }

        dataBuffer.flip();
        glBindBuffer(GL_UNIFORM_BUFFER, UTIL_GL_UNIFORM_BUFFER);
        glBufferData(GL_UNIFORM_BUFFER, dataBuffer, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, uniformBinding, UTIL_GL_UNIFORM_BUFFER);
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
