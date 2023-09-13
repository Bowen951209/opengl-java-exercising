package engine.raytrace.modelObjects;

import org.joml.Vector3f;

public class Box extends ModelObject {
    /**
     * @param mins the minor corner of a box.
     * @param maxs the larger corner of a box.
     */
    public Box(float[] ambient, float[] diffuse,
               float[] specular,float shininess, Vector3f mins, Vector3f maxs) {
        super(ambient, diffuse, specular, shininess);
        super.mins.set(mins);
        super.maxs.set(maxs);
    }
}
