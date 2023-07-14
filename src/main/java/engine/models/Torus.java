package engine.models;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.joml.Math.toRadians;
import static org.lwjgl.opengl.GL43.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL43.glDrawElements;

public class Torus extends Model {
    private int numVertices;
    private final int PREC;
    private int[] indices;
    private Vector3f[] vertices;
    private Vector2f[] texCoords;
    private Vector3f[] normals;
    private final float INNER;
    private final float OUTER;


    private int numIndices;
    private final FloatBuffer VERTICES_IN_BUF;
    private final FloatBuffer NORMALS_IN_BUF;
    private IntBuffer indicesInBuffer;

    public Torus(float innerRadius, float outerRadius, int precision, boolean usingBuffer) {
        this(innerRadius, outerRadius, precision, usingBuffer, new Vector3f());
    }

    public Torus(float innerRadius, float outerRadius, int precision, boolean usingBuffer, Vector3f position) {
        super(position, true, false, false);

        INNER = innerRadius;
        OUTER = outerRadius;
        PREC = precision;
        initTorus(usingBuffer);

        VERTICES_IN_BUF = BufferUtils.createFloatBuffer(vertices.length * 3);
        NORMALS_IN_BUF = BufferUtils.createFloatBuffer(normals.length * 3);
        if (usingBuffer) {
            putDataIntoBuffer(); // put into IntBuffer or FloatBuffer alike.
            storeIndicesToEBO(indicesInBuffer); // if using IntBuffer, then store it to ebo, else not.
        }
        storeDataToVBOs(VERTICES_IN_BUF, NORMALS_IN_BUF);
    }

    private void putDataIntoBuffer() {
        for (int i = 0; i < numVertices; i++) {
            VERTICES_IN_BUF.put(vertices[i].x());         // vertex position
            VERTICES_IN_BUF.put(vertices[i].y());
            VERTICES_IN_BUF.put(vertices[i].z());

            NORMALS_IN_BUF.put(normals[i].x());         // normal vector
            NORMALS_IN_BUF.put(normals[i].y());
            NORMALS_IN_BUF.put(normals[i].z());
        }

        VERTICES_IN_BUF.flip(); // 此行非常必要!
        NORMALS_IN_BUF.flip();
        indicesInBuffer.flip();
    }

    private void initTorus(boolean usingBuffer) {
        numVertices = (PREC + 1) * (PREC + 1);
        numIndices = PREC * PREC * 6;

        vertices = new Vector3f[numVertices];
        texCoords = new Vector2f[numVertices];
        normals = new Vector3f[numVertices];
        Vector3f[] sTangents = new Vector3f[numVertices];
        Vector3f[] tTangents = new Vector3f[numVertices];
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = new Vector3f();
            texCoords[i] = new Vector2f();
            normals[i] = new Vector3f();
            sTangents[i] = new Vector3f();
            tTangents[i] = new Vector3f();
        }
        // 計算第一個環.
        for (int i = 0; i < PREC + 1; i++) {
            float amt = toRadians(i * 360.0f / PREC);
            // build the ring by rotating points around the origin, then moving them outward
            Vector3f ringPos = new Vector3f(0.0f, OUTER, 0.0f);
            ringPos.rotateZ(amt);
            ringPos.add(INNER, 0f, 0f);
            vertices[i].set(ringPos);

            // 為環上的每個點計算紋理座標
            texCoords[i].set(0.0f, ((float) i) / ((float) PREC));

            // compute tangents and normal vectors for each vertex in the ring
            tTangents[i] = new Vector3f(0.0f, -1.0f, 0.0f);     // The first tangent vector starts as the -Y axis,
            tTangents[i].rotateAxis(amt + (3.14159f / 2.0f), 0.0f, 0.0f, 1.0f);
            // and is then rotated around the Z axis.
            sTangents[i].set(0.0f, 0.0f, -1.0f);     // The second tangent is -Z in each case.
            normals[i] = tTangents[i].cross(sTangents[i]); // The cross product produces the normal
        }

        // 繞y軸旋轉最初那個還，形成其他環
        for (int ring = 1; ring < PREC + 1; ring++) {
            for (int vert = 0; vert < PREC + 1; vert++) {

                // 繞y軸旋轉最初那個環的頂點座標
                float amt = toRadians((float) ring * 360.0f / PREC);
                Vector3f vp = new Vector3f(vertices[vert]);
                vp.rotateY(amt);
                vertices[ring * (PREC + 1) + vert].set(vp);

                // 計算新環點的紋理座標
                texCoords[ring * (PREC + 1) + vert].set((float) ring * 2.0f / (float) PREC, texCoords[vert].y());

                // rotate the tangent and bitangent vectors around the Y axis
                sTangents[ring * (PREC + 1) + vert].set(sTangents[vert])
                        .rotateAxis(amt, 0.0f, 1.0f, 0.0f);
                tTangents[ring * (PREC + 1) + vert].set(tTangents[vert])
                        .rotateAxis(amt, 0.0f, 1.0f, 0.0f);
                // rotate the normal vector around the Y axis
                normals[ring * (PREC + 1) + vert].set(normals[vert])
                        .rotateAxis(amt, 0.0f, 1.0f, 0.0f);
            }
        }
        // calculate triangle indices corresponding to the two triangles built per vertex
        if (!usingBuffer) {
            indices = new int[numIndices];

            for (int ring = 0; ring < PREC; ring++) {
                for (int vert = 0; vert < PREC; vert++) {
                    indices[((ring * PREC + vert) * 2) * 3] = ring * (PREC + 1) + vert;
                    indices[((ring * PREC + vert) * 2) * 3 + 1] = (ring + 1) * (PREC + 1) + vert;
                    indices[((ring * PREC + vert) * 2) * 3 + 2] = ring * (PREC + 1) + vert + 1;
                    indices[((ring * PREC + vert) * 2 + 1) * 3] = ring * (PREC + 1) + vert + 1;
                    indices[((ring * PREC + vert) * 2 + 1) * 3 + 1] = (ring + 1) * (PREC + 1) + vert;
                    indices[((ring * PREC + vert) * 2 + 1) * 3 + 2] = (ring + 1) * (PREC + 1) + vert + 1;
                }
            }
        } else {
            indicesInBuffer = BufferUtils.createIntBuffer(numIndices);
            for (int ring = 0; ring < PREC; ring++) {
                for (int vert = 0; vert < PREC; vert++) {
                    indicesInBuffer.put(ring * (PREC + 1) + vert);
                    indicesInBuffer.put((ring + 1) * (PREC + 1) + vert);
                    indicesInBuffer.put(ring * (PREC + 1) + vert + 1);
                    indicesInBuffer.put(ring * (PREC + 1) + vert + 1);
                    indicesInBuffer.put((ring + 1) * (PREC + 1) + vert);
                    indicesInBuffer.put((ring + 1) * (PREC + 1) + vert + 1);
                }
            }
        }
    }

    // ===============================Numbers===============================
    public int getNumVertices() {
        return numVertices;
    }

    public int getNumIndices() {
        return numIndices;
    }

    // ===============================Values===============================
    public Vector3f[] getVertices() {
        return vertices;
    }

    public Vector2f[] getTexCoords() {
        return texCoords;
    }

    public Vector3f[] getNormals() {
        return normals;
    }

    public int[] getIndicesInArray() {
        return indices;
    }

    public IntBuffer getIndicesInBuffer() {
        return indicesInBuffer;
    }

    @Override
    protected void updateMMat() {
        mMat.identity()
                .translate(position)
                .scale(2.5f)
                .rotateX(.5f);
    }

    @Override
    public void draw(int mode) {
        bindVAO();
        glDrawElements(mode, numIndices, GL_UNSIGNED_INT, 0);
    }
}
