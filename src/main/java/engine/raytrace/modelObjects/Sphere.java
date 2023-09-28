package engine.raytrace.modelObjects;

public class Sphere extends ModelObject {
    public float getRadius() {
        return radius;
    }

    public Sphere(float[] ambient, float[] diffuse,
                  float[] specular, float shininess, float radius) {
        super(ambient, diffuse, specular, shininess);
        super.radius = radius;
    }
}
