package chapter6;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;

import static org.joml.Math.toRadians;

public class Torus {
    private int numVertices;
    private final int prec;
    private int[] indices;
    private Vector3f[] vertices;
    private Vector2f[] texCoords;
    private Vector3f[] normals;
    private final float inner;
    private final float outer;


    private int numIndices;
    private IntBuffer indicesBuffer;

    public Torus(float innerRadius, float outerRadius, int precision, boolean usingBuffer) {
        inner = innerRadius;
        outer = outerRadius;
        prec = precision;
        initTorus(usingBuffer);
    }

    private void initTorus(boolean usingBuffer) {
        numVertices = (prec + 1) * (prec + 1);
        numIndices = prec * prec * 6;

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
        for (int i = 0; i < prec + 1; i++) {
            float amt = toRadians(i * 360.0f / prec);
            // build the ring by rotating points around the origin, then moving them outward
            Vector3f ringPos = new Vector3f(0.0f, outer, 0.0f);
            ringPos.rotateZ(amt);
            ringPos.add(inner, 0f, 0f);
            vertices[i].set(ringPos);

            // 為環上的每個點計算紋理座標
            texCoords[i].set(0.0f, ((float) i) / ((float) prec));

            // compute tangents and normal vectors for each vertex in the ring
            tTangents[i] = new Vector3f(0.0f, -1.0f, 0.0f);     // The first tangent vector starts as the -Y axis,
            tTangents[i].rotateAxis(amt + (3.14159f / 2.0f), 0.0f, 0.0f, 1.0f);
            // and is then rotated around the Z axis.
            sTangents[i].set(0.0f, 0.0f, -1.0f);     // The second tangent is -Z in each case.
            normals[i] = tTangents[i].cross(sTangents[i]); // The cross product produces the normal
        }

        // 繞y軸旋轉最初那個還，形成其他環
        for (int ring = 1; ring < prec + 1; ring++) {
            for (int vert = 0; vert < prec + 1; vert++) {

                // 繞y軸旋轉最初那個環的頂點座標
                float amt = toRadians((float) ring * 360.0f / prec);
                Vector3f vp = new Vector3f(vertices[vert]);
                vp.rotateY(amt);
                vertices[ring * (prec + 1) + vert].set(vp);

                // 計算新環點的紋理座標
                texCoords[ring * (prec + 1) + vert].set((float) ring * 2.0f / (float) prec, texCoords[vert].y());

                // rotate the tangent and bitangent vectors around the Y axis
                sTangents[ring * (prec + 1) + vert].set(sTangents[vert])
                        .rotateAxis(amt, 0.0f, 1.0f, 0.0f);
                tTangents[ring * (prec + 1) + vert].set(tTangents[vert])
                        .rotateAxis(amt, 0.0f, 1.0f, 0.0f);
                // rotate the normal vector around the Y axis
                normals[ring * (prec + 1) + vert].set(normals[vert])
                        .rotateAxis(amt, 0.0f, 1.0f, 0.0f);
            }
        }
        // calculate triangle indices corresponding to the two triangles built per vertex
        if (!usingBuffer) {
            indices = new int[numIndices];

            for (int ring = 0; ring < prec; ring++) {
                for (int vert = 0; vert < prec; vert++) {
                    indices[((ring * prec + vert) * 2) * 3] = ring * (prec + 1) + vert;
                    indices[((ring * prec + vert) * 2) * 3 + 1] = (ring + 1) * (prec + 1) + vert;
                    indices[((ring * prec + vert) * 2) * 3 + 2] = ring * (prec + 1) + vert + 1;
                    indices[((ring * prec + vert) * 2 + 1) * 3] = ring * (prec + 1) + vert + 1;
                    indices[((ring * prec + vert) * 2 + 1) * 3 + 1] = (ring + 1) * (prec + 1) + vert;
                    indices[((ring * prec + vert) * 2 + 1) * 3 + 2] = (ring + 1) * (prec + 1) + vert + 1;
                }
            }
        } else {
            indicesBuffer = BufferUtils.createIntBuffer(numIndices);
            for (int ring = 0; ring < prec; ring++) {
                for (int vert = 0; vert < prec; vert++) {
                    indicesBuffer.put(ring * (prec + 1) + vert);
                    indicesBuffer.put((ring + 1) * (prec + 1) + vert);
                    indicesBuffer.put(ring * (prec + 1) + vert + 1);
                    indicesBuffer.put(ring * (prec + 1) + vert + 1);
                    indicesBuffer.put((ring + 1) * (prec + 1) + vert);
                    indicesBuffer.put((ring + 1) * (prec + 1) + vert + 1);
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
        return indicesBuffer;
    }
}
