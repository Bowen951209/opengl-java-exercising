package engine.raytrace.modelObjects;

import org.joml.Vector3f;

public class RoomBox extends ModelObject{
    public RoomBox(float[] ambient, float[] diffuse, float[] specular, float shininess,
                      boolean hasLighting) {
        super(ambient, diffuse, specular, shininess);
        super.isReflective = hasLighting;
    }
}
