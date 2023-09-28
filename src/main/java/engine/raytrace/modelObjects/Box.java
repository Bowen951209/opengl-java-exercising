package engine.raytrace.modelObjects;

public class Box extends ModelObject {
    public Box(float[] ambient, float[] diffuse, float[] specular, float shininess, float minsX,
               float minsY, float minsZ, float maxsX, float maxsY, float maxsZ) {
        super(ambient, diffuse, specular, shininess);
        super.mins.set(minsX, minsY, minsZ);
        super.maxs.set(maxsX, maxsY, maxsZ);
    }
}
