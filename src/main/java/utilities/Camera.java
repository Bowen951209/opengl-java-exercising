package utilities;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static utilities.ValuesContainer.VEC3_FOR_UTILS;

public class Camera {
    private static final Vector3f Y = new Vector3f(0f, 1f, 0f);
    private final Vector3f POSITION = new Vector3f(0f, 0f, 5f);
    private final Vector3f DIRECTION = new Vector3f(0f, 0f, -1f);
    private final Matrix4f VMat = new Matrix4f();

    public Matrix4f getVMat() {
        return VMat;
    }

    public Matrix4f getProjMat() {
        return projMat;
    }

    public void setProjMat(int w, int h) {
        float aspect = (float) w / (float) h;
        this.projMat = new Matrix4f().perspective(1.0472f, aspect, .1f, 1000f); // 1.0472 = 60度
    }

    private Matrix4f projMat = new Matrix4f();

    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;

    public Camera step(float step) {
        this.step = step;
        return this;
    }

    private float step = .05f;

    public Camera sensitive(float sensitive) {
        this.sensitive = sensitive;
        return this;
    }

    private float sensitive = .04f;

    public void updateVMat() {
        Vector3f lookAtPoint = VEC3_FOR_UTILS;
        lookAtPoint.set(POSITION).add(DIRECTION);
        VMat.identity();
        VMat.lookAt(this.POSITION, lookAtPoint, Y);
    }

    public void lookUp() {
        Vector3f leftVec = VEC3_FOR_UTILS;
        leftVec.set(DIRECTION).cross(Y);
        DIRECTION.rotateAxis(sensitive, leftVec.x,  leftVec.y, leftVec.z);

    }

    public void lookDown() {
        Vector3f leftVec = VEC3_FOR_UTILS;
        leftVec.set(DIRECTION).cross(Y);
        DIRECTION.rotateAxis(-sensitive, leftVec.x,  leftVec.y, leftVec.z);
    }

    public void lookLeft() {
        DIRECTION.rotateY(sensitive);
    }

    public void lookRight() {
        DIRECTION.rotateY(-sensitive);
    }

    public void forward() {
        forward = true;
    }

    public void backward() {
        backward = true;
    }

    public void left() {
        left = true;
    }

    public void right() {
        right = true;
    }

    public void cancelF() {
        forward = false;
    }

    public void cancelB() {
        backward = false;
    }

    public void cancelL() {
        left = false;
    }

    public void cancelR() {
        right = false;
    }

    public void handle() {
        // TODO: 2023/6/7 Don't use util container, because it is super unsafe!
        final Vector3f DIRECTION_MUL_STEP = VEC3_FOR_UTILS.set(DIRECTION).mul(step);

        final Vector3f CAM_FRONT = VEC3_FOR_UTILS.set(DIRECTION_MUL_STEP.x, 0f, DIRECTION_MUL_STEP.z);

        if (forward) {
            POSITION.add(CAM_FRONT);
        }
        if (backward) {
            final Vector3f CAM_BACK = VEC3_FOR_UTILS.set(CAM_FRONT).mul(-1f);
            POSITION.add(CAM_BACK);
        }
        if (left) {
            final Vector3f CAM_LEFT = VEC3_FOR_UTILS.set(CAM_FRONT).rotateY(.5f * 3.14f); // 90 DEG
            POSITION.add(CAM_LEFT);
        }
        if (right) {
            final Vector3f CAM_RIGHT = VEC3_FOR_UTILS.set(CAM_FRONT).rotateY(1.5f * 3.14f);
            POSITION.add(CAM_RIGHT);
        }
    }
}