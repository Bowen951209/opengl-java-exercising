package engine.sceneComponents.textures;

import engine.util.NoiseGenerator;

public class CloudTexture extends Texture3D {
    private static final int DEFAULT_ZOOM = 128;
    private final NoiseGenerator noiseGenerator = new NoiseGenerator();

    public CloudTexture(int usingUnit) {
        super(usingUnit);
    }

    @Override
    protected void fillDataArray() {
        if (zoom == 1) // zoom value unset
            noiseGenerator.levelMixedNoise(data, textureWidth, textureHeight, textureDepth, DEFAULT_ZOOM, DEFAULT_ZOOM, "BLUE");
    }
}
