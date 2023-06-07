package utilities.models;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import utilities.Camera;

public abstract class Model {
    // TODO: 2023/6/7 this class can maybe contain an instance's own vbo and method dealing with it.
    protected final Matrix4f mMat = new Matrix4f();

    public Matrix4f getMvMat() {
        return mvMat;
    }

    protected final Matrix4f mvMat = new Matrix4f();

    public Matrix4f getInvTrMat() {
        return invTrMat;
    }

    private final Matrix4f invTrMat = new Matrix4f();

    protected Vector3f position;

    protected void updateMvMat(Camera camera) {
        mvMat.set(camera.getVMat()).mul(mMat);
    }

    protected void updateInvTrMat() {
        invTrMat.set(mvMat).invert().transpose();
    }

    protected abstract void updateMMat();
    public abstract void updateState(Camera camera);
}
