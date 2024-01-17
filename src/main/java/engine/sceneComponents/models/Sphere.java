package engine.sceneComponents.models;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.joml.Math.*;
import static org.lwjgl.opengl.GL43.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL43.glDrawElements;

public class Sphere extends Model {
    protected int numIndices;
    protected int prec; // prec = precision
    protected int[] indices;
    protected Vector3f[] vertices, tangents;
    protected Vector2f[] texCoords;
    protected Vector3f[] normals;
    protected int numVertices;
    private final FloatBuffer tangentsInBuf;
    protected IntBuffer indicesInBuffer;

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

        verticesInBuf = BufferUtils.createFloatBuffer(numVertices * 3);
        normalsInBuf = BufferUtils.createFloatBuffer(numVertices * 3);
        tcInBuf = BufferUtils.createFloatBuffer(numVertices * 2);
        tangentsInBuf = BufferUtils.createFloatBuffer(numVertices * 3);

        indicesInBuffer = BufferUtils.createIntBuffer(numIndices);
        indicesInBuffer.put(indices);

        putDataIntoBuffer();
        storeDataToVBOs(verticesInBuf, normalsInBuf, tcInBuf, tangentsInBuf);
        storeIndicesToEBO(indicesInBuffer);
    }

    protected void putDataIntoBuffer() {
        for (int i = 0; i < numVertices; i++) {
            verticesInBuf.put(vertices[i].x());         // vertex position
            verticesInBuf.put(vertices[i].y());
            verticesInBuf.put(vertices[i].z());

            normalsInBuf.put(normals[i].x());         // normal vector
            normalsInBuf.put(normals[i].y());
            normalsInBuf.put(normals[i].z());

            tcInBuf.put(texCoords[i].x());            // texture coords
            tcInBuf.put(texCoords[i].y());

            tangentsInBuf.put(tangents[i].x());       // tangents
            tangentsInBuf.put(tangents[i].y());
            tangentsInBuf.put(tangents[i].z());
        }

        verticesInBuf.flip(); // 此行非常必要!
        normalsInBuf.flip();
        indicesInBuffer.flip();
        tcInBuf.flip();
        tangentsInBuf.flip();
    }

    protected void initSphere() {
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

    @Override
    protected void updateMMat() {
        mMat.identity()
                .translate(position);
    }

    @Override
    public void draw(int mode) {
        bindVAO();
        glDrawElements(mode, numIndices, GL_UNSIGNED_INT, 0);
    }
}

