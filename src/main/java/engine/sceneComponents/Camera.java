package engine.sceneComponents;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

import static engine.util.ValuesContainer.VEC3_FOR_UTILS;

public class Camera {
    private static final Vector3f Y = new Vector3f(0f, 1f, 0f);
    private static final float CEIL_LIMIT = 1.57f;

    private final Vector3f position = new Vector3f(0f, 0f, 5f);
    private final Vector3f direction = new Vector3f(0f, 0f, -1f);
    private float pitch;
    private final Vector3f lookAtPoint = new Vector3f();
    private final Vector3f leftVec = new Vector3f();
    private final Vector3f directionMulStep = new Vector3f();
    private final Vector3f camFront = new Vector3f();
    private final Matrix4f vMat = new Matrix4f();
    private final Matrix4f invVMat = new Matrix4f();
    private final Set<Runnable> cameraUpdateCallbacks = new HashSet<>();
    private final Matrix4f projMat = new Matrix4f();

    private float step = .05f;
    private float sensitive = .04f;
    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;
    private boolean fly;
    private boolean land;

    public Vector3f getPos() {
        return position;
    }

    public void setPos(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public Matrix4f getInvVMat() {
        invVMat.set(vMat).invert();
        return invVMat;
    }

    public Matrix4f getVMat() {
        return vMat;
    }

    public Matrix4f getProjMat() {
        return projMat;
    }

    public void setProjMat(int w, int h) {
        float aspect = (float) w / (float) h;
        this.projMat.identity().perspective(1.0472f, aspect, .1f, 1000f); // 1.0472 = 60度
    }

    public Camera step(float step) {
        this.step = step;
        return this;
    }

    public Camera sensitive(float sensitive) {
        this.sensitive = sensitive;
        return this;
    }

    public void addCameraUpdateCallBack(Runnable cb) {
        cameraUpdateCallbacks.add(cb);
    }

    public Camera() {
    }

    public Camera(int frameWidth, int frameHeight) {
        setProjMat(frameWidth, frameHeight);
    }

    public void updateVMat() {
        lookAtPoint.set(position).add(direction);
        vMat.identity();
        vMat.lookAt(this.position, lookAtPoint, Y);
    }

    // looking direction
    public void lookUp() {
        if (pitch < CEIL_LIMIT) {
            pitch += sensitive;
            // we want to rotate around camera's x-axis.
            leftVec.set(direction).cross(Y);
            direction.rotateAxis(sensitive, leftVec.x, leftVec.y, leftVec.z);

            cameraUpdateCallbacks.forEach(Runnable::run);
        }
    }

    public void lookDown() {
        if (pitch > -CEIL_LIMIT) {
            pitch -= sensitive;
            // we want to rotate around camera's x-axis.
            leftVec.set(direction).cross(Y);
            cameraUpdateCallbacks.forEach(Runnable::run);

            direction.rotateAxis(-sensitive, leftVec.x, leftVec.y, leftVec.z);
        }
    }

    public void lookLeft() {
        direction.rotateY(sensitive);
        cameraUpdateCallbacks.forEach(Runnable::run);
    }

    public void lookRight() {
        direction.rotateY(-sensitive);
        cameraUpdateCallbacks.forEach(Runnable::run);
    }


    // movement
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

    public void fly() {
        fly = true;
    }

    public void land() {
        land = true;
    }

    // cancel move
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

    public void cancelFly() {
        fly = false;
    }

    public void cancelLand() {
        land = false;
    }

    public void reflect(float referenceY) {
        // NOTE: ONLY FOR X-Z PLANE
        float distFromCamToRef = position.y - referenceY;
        // 1. For position
        setPos(position.x, position.y - 2 * distFromCamToRef, position.z);

        // 2. For pitch
        direction.reflect(Y);
    }

    public void handle() {
        directionMulStep.set(direction).mul(step);

        camFront.set(directionMulStep.x, 0f, directionMulStep.z);

        if (forward) {
            position.add(camFront);
            cameraUpdateCallbacks.forEach(Runnable::run);
        }
        if (backward) {
            final Vector3f CAM_BACK = VEC3_FOR_UTILS.set(camFront).mul(-1f);
            position.add(CAM_BACK);
            cameraUpdateCallbacks.forEach(Runnable::run);
        }
        if (left) {
            final Vector3f CAM_LEFT = VEC3_FOR_UTILS.set(camFront).rotateY(.5f * (float) Math.PI); // 90 DEG
            position.add(CAM_LEFT);
            cameraUpdateCallbacks.forEach(Runnable::run);
        }
        if (right) {
            final Vector3f CAM_RIGHT = VEC3_FOR_UTILS.set(camFront).rotateY(1.5f * (float) Math.PI);
            position.add(CAM_RIGHT);
            cameraUpdateCallbacks.forEach(Runnable::run);
        }
        if (fly) {
            position.add(0f, step, 0f);
            cameraUpdateCallbacks.forEach(Runnable::run);
        }
        if (land) {
            position.add(0f, -step, 0f);
            cameraUpdateCallbacks.forEach(Runnable::run);
        }
    }
}