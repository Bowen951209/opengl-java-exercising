package utilities.models;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import utilities.sceneComponents.Camera;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.joml.Math.*;
import static org.lwjgl.opengl.GL43.*;

public class Sphere extends Model {
    private int numIndices;
    private final int prec; // prec = precision
    private int[] indices;
    private Vector3f[] vertices, tangents;
    private Vector2f[] texCoords;
    private Vector3f[] normals;
    private int numVertices;
    private final FloatBuffer VERTICES_IN_BUF;
    private final FloatBuffer NORMALS_IN_BUF;
    private final FloatBuffer TC_IN_BUF;
    private final FloatBuffer TANGENTS_IN_BUF;
    private final IntBuffer INDICES_IN_BUFFER;

    public Sphere() {
        this(100, new Vector3f());
    }

    public Sphere(int p) {
        this(p, new Vector3f());
    }

    public Sphere(int p, Vector3f position) {
        super(position, true, true, true);
        prec = p;
        initSphere();

        VERTICES_IN_BUF = BufferUtils.createFloatBuffer(numVertices * 3);
        NORMALS_IN_BUF = BufferUtils.createFloatBuffer(numVertices * 3);
        TC_IN_BUF = BufferUtils.createFloatBuffer(numVertices * 2);
        TANGENTS_IN_BUF = BufferUtils.createFloatBuffer(numVertices * 3);

        INDICES_IN_BUFFER = BufferUtils.createIntBuffer(numIndices);
        INDICES_IN_BUFFER.put(indices);

        putDataIntoBuffer();
        storeDataToVBOs(VERTICES_IN_BUF, NORMALS_IN_BUF, TC_IN_BUF, TANGENTS_IN_BUF);
        storeIndicesToEBO(INDICES_IN_BUFFER);
    }

    private void putDataIntoBuffer() {
        for (int i = 0; i < numVertices; i++) {
            VERTICES_IN_BUF.put(vertices[i].x());         // vertex position
            VERTICES_IN_BUF.put(vertices[i].y());
            VERTICES_IN_BUF.put(vertices[i].z());

            NORMALS_IN_BUF.put(normals[i].x());         // normal vector
            NORMALS_IN_BUF.put(normals[i].y());
            NORMALS_IN_BUF.put(normals[i].z());

            TC_IN_BUF.put(texCoords[i].x());            // texture coords
            TC_IN_BUF.put(texCoords[i].y());

            TANGENTS_IN_BUF.put(tangents[i].x());       // tangents
            TANGENTS_IN_BUF.put(tangents[i].y());
            TANGENTS_IN_BUF.put(tangents[i].z());
        }

        VERTICES_IN_BUF.flip(); // 此行非常必要!
        NORMALS_IN_BUF.flip();
        INDICES_IN_BUFFER.flip();
        TC_IN_BUF.flip();
        TANGENTS_IN_BUF.flip();
    }

    private void initSphere() {
        numVertices = (prec + 1) * (prec + 1);
        numIndices = prec * prec * 6;
        indices = new int[numIndices];
        vertices = new Vector3f[numVertices];
        tangents = new Vector3f[numVertices];
        texCoords = new Vector2f[numVertices];
        normals = new Vector3f[numVertices];
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = new Vector3f();
            texCoords[i] = new Vector2f();
            normals[i] = new Vector3f();
        }
        // calculate triangle vertices
        for (int i = 0; i <= prec; i++) {
            for (int j = 0; j <= prec; j++) {
                float y = cos(toRadians((float) (180 - i * 180 / prec)));
                float x = -(float) cos(toRadians(j * 360 / (float) prec)) * abs(cos(asin(y)));
                float z = sin(toRadians(j * 360 / (float) prec)) * abs(cos(asin(y)));
                vertices[i * (prec + 1) + j].set(x, y, z);
                texCoords[i * (prec + 1) + j].set((float) j / prec, (float) i / prec);
                normals[i * (prec + 1) + j].set(x, y, z);

                // calculate tangent vector

                // if North or South Pole
                if (((x == 0) && (y == 1) && (z == 0)) || ((x == 0) && (y == -1) && (z == 0))) {
                    tangents[i * (prec + 1) + j].set(0.0f, 0.0f, -1.0f);
                } else {
                    tangents[i * (prec + 1) + j] = (new Vector3f(0, 1, 0)).cross(new Vector3f(x, y, z));
                }
            }
        }
        // calculate triangle indices
        for (int i = 0; i < prec; i++) {
            for (int j = 0; j < prec; j++) {
                indices[6 * (i * prec + j)] = i * (prec + 1) + j;
                indices[6 * (i * prec + j) + 1] = i * (prec + 1) + j + 1;
                indices[6 * (i * prec + j) + 2] = (i + 1) * (prec + 1) + j;
                indices[6 * (i * prec + j) + 3] = i * (prec + 1) + j + 1;
                indices[6 * (i * prec + j) + 4] = (i + 1) * (prec + 1) + j + 1;
                indices[6 * (i * prec + j) + 5] = (i + 1) * (prec + 1) + j;
            }
        }
    }

    public int getNumIndices() {
        return numIndices;
    }

    public int[] getIndices() {
        return indices;
    }

    public Vector3f[] getVertices() {
        return vertices;
    }

    public Vector2f[] getTexCoords() {
        return texCoords;
    }

    public Vector3f[] getNormals() {
        return normals;
    }

    @Override
    protected void updateMMat() {
        M_MAT.identity()
                .translate(POSITION);
    }

    @Override
    public void updateState(Camera camera) {
        super.updateState(camera);
    }

    @Override
    public void draw(int mode) {
        bindVAO();
        glDrawElements(mode, numIndices, GL_UNSIGNED_INT, 0);
    }
}

