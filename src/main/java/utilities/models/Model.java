package utilities.models;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import utilities.sceneComponents.Camera;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL43.*;

public abstract class Model {
    protected final Matrix4f M_MAT = new Matrix4f();

    public Matrix4f getMV_MAT() {
        return MV_MAT;
    }

    protected final Matrix4f MV_MAT = new Matrix4f();

    public Matrix4f getINV_TR_MAT() {
        return INV_TR_MAT;
    }

    private final Matrix4f INV_TR_MAT = new Matrix4f();

    protected final Vector3f POSITION;
    private final int VAO, VERTICES_VBO, NORMALS_VBO;
    private int ebo, tcVBO;

    protected Model(Vector3f position, boolean isUsingEBO, boolean isUsingTTextureCoordinate) {
        this.POSITION = position;
        VAO = glGenVertexArrays();
        bindVAO();

        VERTICES_VBO = glGenBuffers();
        NORMALS_VBO = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, VERTICES_VBO);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, NORMALS_VBO);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        if (isUsingEBO) {
            ebo = glGenBuffers();
        }
        if (isUsingTTextureCoordinate) {
            tcVBO = glGenBuffers();

            glBindBuffer(GL_ARRAY_BUFFER, tcVBO);
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(3, 2, GL_FLOAT, false, 0, 0);
        }
    }

    private void binEBO() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    }

    public void bindVAO() {
        glBindVertexArray(VAO);
    }

    private void storeVertices(FloatBuffer vertices) {
        glBindBuffer(GL_ARRAY_BUFFER, VERTICES_VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }

    private void storeNormals(FloatBuffer normals) {
        glBindBuffer(GL_ARRAY_BUFFER, NORMALS_VBO);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
    }

    private void storeTextureCoords(FloatBuffer tcs) {
        glBindBuffer(GL_ARRAY_BUFFER, tcVBO);
        glBufferData(GL_ARRAY_BUFFER, tcs, GL_STATIC_DRAW);
    }

    protected void storeDataToVBOs(FloatBuffer vertices, FloatBuffer normals) {
        storeVertices(vertices);
        storeNormals(normals);
    }

    protected void storeDataToVBOs(FloatBuffer vertices, FloatBuffer normals, FloatBuffer tcs) {
        storeDataToVBOs(vertices, normals);
        storeTextureCoords(tcs);
    }

    protected void storeIndicesToEBO(IntBuffer indices) {
        binEBO();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    protected void updateMvMat(Camera camera) {
        MV_MAT.set(camera.getVMat()).mul(M_MAT);
    }

    protected void updateInvTrMat() {
        INV_TR_MAT.set(MV_MAT).invert().transpose();
    }

    protected abstract void updateMMat();

    public abstract void updateState(Camera camera);
    public abstract void draw(int mode);
}
