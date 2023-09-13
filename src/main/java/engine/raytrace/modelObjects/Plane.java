package engine.raytrace.modelObjects;

public class Plane extends ModelObject {

    public Plane(float[] ambient, float[] diffuse,
                 float[] specular, float shininess, float yPosition, float width, float depth) {
        super(ambient, diffuse, specular, shininess);
        super.mins.set(width, yPosition, depth);
    }
}
