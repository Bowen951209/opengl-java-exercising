package engine.raytrace.modelObjects;

import engine.sceneComponents.Camera;

public class RoomBox extends ModelObject {
    private final float sideLength;

    public RoomBox(float[] ambient, float[] diffuse, float[] specular, float shininess,
                   boolean hasLighting, float sideLength) {
        super(ambient, diffuse, specular, shininess);
        super.isReflective = hasLighting;

        this.sideLength = sideLength;
        float halfSideLength = sideLength / 2f;
        super.mins.set(-halfSideLength);
        super.maxs.set(halfSideLength);
    }

    public void centerToCamera(Camera camera) {
        float halfSideLength = sideLength / 2f;
        super.mins.set(camera.getPos().x - halfSideLength, camera.getPos().y - halfSideLength,
                camera.getPos().z - halfSideLength);
        super.maxs.set(camera.getPos().x + halfSideLength, camera.getPos().y + halfSideLength,
                camera.getPos().z + halfSideLength);
    }
}
