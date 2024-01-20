package net.bowen.engine.sceneComponents.models;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import static java.lang.Math.*;

// this sphere is modified to be a "half" sphere for use as a skydome

public class HalfSphere extends Sphere {
    public HalfSphere() {
        this(48);
    }

    public HalfSphere(int p) {
        prec = p;
        initSphere();

        verticesInBuf = BufferUtils.createFloatBuffer(numVertices * 3);
        tcInBuf = BufferUtils.createFloatBuffer(numVertices * 2);

        indicesInBuffer = BufferUtils.createIntBuffer(numIndices);
        indicesInBuffer.put(indices);

        putDataIntoBuffer();
        storeDataToVBOs(verticesInBuf, tcInBuf);
        storeIndicesToEBO(indicesInBuffer);
    }

    @Override
    protected void initSphere() {
        numVertices = (prec + 1) * (prec + 1);
        numIndices = prec * prec * 6;
        indices = new int[numIndices];
        vertices = new Vector3f[numVertices];
        texCoords = new Vector2f[numVertices];
        normals = new Vector3f[numVertices];
        tangents = new Vector3f[numVertices];

        for (int i = 0; i < numVertices; i++) {
            vertices[i] = new Vector3f();
            texCoords[i] = new Vector2f();
            normals[i] = new Vector3f();
            tangents[i] = new Vector3f();
        }

        // calculate triangle vertices
        for (int i = 0; i <= prec; i++) {
            for (int j = 0; j <= prec; j++) {
                float y = (float) cos(toRadians(90 - (float)i * 90 / prec));
                double abs = abs(cos(asin(y)));
                float x = -(float) cos(toRadians(j * 360.0 / prec)) * (float) abs;
                float z = (float) sin(toRadians(j * 360.0f / (float) (prec))) * (float) abs;
                vertices[i * (prec + 1) + j].set(x, y, z);
                texCoords[i * (prec + 1) + j].set((float) j / prec, (float) i / prec);
                normals[i * (prec + 1) + j].set(x, y, z);

                // calculate tangent vector
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
}