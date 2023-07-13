package engine.sceneComponents;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static engine.ValuesContainer.VEC3_FOR_UTILS;

public class Camera {
    private static final Vector3f Y = new Vector3f(0f, 1f, 0f);
    private static final float CEIL_LIMIT = 1.57f;


    private final Vector3f position = new Vector3f(0f, 0f, 5f);

    public void setPos(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    private final Vector3f direction = new Vector3f(0f, 0f, -1f);
    private float xAngleToCam = 0f;
    private final Vector3f lookAtPoint = new Vector3f();
    private final Vector3f leftVec = new Vector3f();
    private final Vector3f directionMulStep = new Vector3f();
    private final Vector3f camFront = new Vector3f();
    private final Matrix4f VMat = new Matrix4f();

    public Camera() {
    }

    public Camera(int frameWidth, int frameHeight) {
        setProjMat(frameWidth, frameHeight);
    }

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
    private boolean fly;
    private boolean land;

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
        lookAtPoint.set(position).add(direction);
        VMat.identity();
        VMat.lookAt(this.position, lookAtPoint, Y);
    }

    // looking direction
    public void lookUp() {
        if (xAngleToCam < CEIL_LIMIT) {
            xAngleToCam += sensitive;
            // we want to rotate around camera's x-axis.
            leftVec.set(direction).cross(Y);
            direction.rotateAxis(sensitive, leftVec.x, leftVec.y, leftVec.z);
        }
    }

    public void lookDown() {
        if (xAngleToCam > -CEIL_LIMIT) {
            xAngleToCam -= sensitive;
            // we want to rotate around camera's x-axis.
            leftVec.set(direction).cross(Y);
            direction.rotateAxis(-sensitive, leftVec.x, leftVec.y, leftVec.z);
        }
    }

    public void lookLeft() {
        direction.rotateY(sensitive);
    }

    public void lookRight() {
        direction.rotateY(-sensitive);
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


    public void handle() {
        directionMulStep.set(direction).mul(step);

        camFront.set(directionMulStep.x, 0f, directionMulStep.z);

        if (forward) {
            position.add(camFront);
        }
        if (backward) {
            final Vector3f CAM_BACK = VEC3_FOR_UTILS.set(camFront).mul(-1f);
            position.add(CAM_BACK);
        }
        if (left) {
            final Vector3f CAM_LEFT = VEC3_FOR_UTILS.set(camFront).rotateY(.5f * 3.14f); // 90 DEG
            position.add(CAM_LEFT);
        }
        if (right) {
            final Vector3f CAM_RIGHT = VEC3_FOR_UTILS.set(camFront).rotateY(1.5f * 3.14f);
            position.add(CAM_RIGHT);
        }
        if (fly) {
            position.add(0f, step, 0f);
        }
        if (land) {
            position.add(0f, -step, 0f);
        }
    }
}