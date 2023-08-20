package engine.sceneComponents.models;

import engine.sceneComponents.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL43.*;

public abstract class Model extends Thread {
    protected final Matrix4f mMat = new Matrix4f();

    public Matrix4f getMMat() {
        return mMat;
    }

    public Matrix4f getMvMat() {
        return mvMat;
    }

    protected final Matrix4f mvMat = new Matrix4f();

    public Matrix4f getInvTrMat() {
        return invTrMat;
    }

    private final Matrix4f invTrMat = new Matrix4f();

    protected final Vector3f position;

    public Vector3f getPos() {
        return position;
    }

    private final int vao, verticesVBO, normalsVBO;
    private int ebo, tcVBO, tangentsVBO;
    protected FloatBuffer verticesInBuf;
    protected FloatBuffer normalsInBuf;
    protected FloatBuffer tcInBuf;

    protected Model(Vector3f position, boolean isUsingEBO, boolean isUsingTextureCoordinate, boolean isUsingTangents) {
        this.position = position;
        vao = glGenVertexArrays();
        bindVAO();

        verticesVBO = glGenBuffers();
        normalsVBO = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, normalsVBO);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        if (isUsingEBO) {
            ebo = glGenBuffers();
        }
        if (isUsingTextureCoordinate) {
            tcVBO = glGenBuffers();

            glBindBuffer(GL_ARRAY_BUFFER, tcVBO);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
        }
        if (isUsingTangents) {
            tangentsVBO = glGenBuffers();

            glBindBuffer(GL_ARRAY_BUFFER, tangentsVBO);
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
        }
    }

    private void bindEBO() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    }

    public void bindVAO() {
        glBindVertexArray(vao);
    }

    private void storeVertices(FloatBuffer vertices) {
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }

    private void storeVertices(float[] vertices) {
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }

    private void storeNormals(FloatBuffer normals) {
        glBindBuffer(GL_ARRAY_BUFFER, normalsVBO);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
    }

    private void storeNormals(float[] normals) {
        glBindBuffer(GL_ARRAY_BUFFER, normalsVBO);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
    }

    private void storeTextureCoords(FloatBuffer tcs) {
        glBindBuffer(GL_ARRAY_BUFFER, tcVBO);
        glBufferData(GL_ARRAY_BUFFER, tcs, GL_STATIC_DRAW);
    }

    private void storeTextureCoords(float[] tcs) {
        glBindBuffer(GL_ARRAY_BUFFER, tcVBO);
        glBufferData(GL_ARRAY_BUFFER, tcs, GL_STATIC_DRAW);
    }

    private void storeTangents(FloatBuffer tangents) {
        glBindBuffer(GL_ARRAY_BUFFER, tangentsVBO);
        glBufferData(GL_ARRAY_BUFFER, tangents, GL_STATIC_DRAW);
    }

    protected void storeDataToVBOs(FloatBuffer vertices, FloatBuffer normals) {
        storeVertices(vertices);
        storeNormals(normals);
    }

    protected void storeDataToVBOs(float[] vertices, float[] normals) {
        storeVertices(vertices);
        storeNormals(normals);
    }

    protected void storeDataToVBOs(FloatBuffer vertices, FloatBuffer normals, FloatBuffer tcs) {
        if (normals != null) {
            storeDataToVBOs(vertices, normals);
        } else {
            storeVertices(vertices);
        }
        storeTextureCoords(tcs);
    }

    protected void storeDataToVBOs(float[] vertices, float[] normals, float[] tcs) {
        storeDataToVBOs(vertices, normals);
        storeTextureCoords(tcs);
    }

    protected void storeDataToVBOs(FloatBuffer vertices, FloatBuffer normals, FloatBuffer tcs, FloatBuffer tangents) {
        storeDataToVBOs(vertices, normals, tcs);
        storeTangents(tangents);
    }

    protected void storeIndicesToEBO(IntBuffer indices) {
        bindEBO();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    private void updateMvMat(Camera camera) {
        mvMat.set(camera.getVMat()).mul(mMat);
    }

    private void updateInvTrMat() {
        invTrMat.set(mvMat).invert().transpose();
    }

    protected abstract void updateMMat();

    public void updateState(Camera camera) {
        updateMMat();
        updateMvMat(camera);
        updateInvTrMat();
    }

    public abstract void draw(int mode);
}
