package engine.raytrace.modelObjects;

public class RoomBox extends ModelObject {

    public RoomBox(float[] ambient, float[] diffuse, float[] specular, float shininess,
                   boolean hasLighting, float sideLength) {
        super(ambient, diffuse, specular, shininess);
        super.isReflective = hasLighting;

        float halfSideLength = sideLength / 2f;
        super.mins.set(-halfSideLength);
        super.maxs.set(halfSideLength);
    }
}
