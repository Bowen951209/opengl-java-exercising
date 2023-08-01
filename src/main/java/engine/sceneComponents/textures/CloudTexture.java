package engine.sceneComponents.textures;

import engine.util.NoiseGenerator;

public class CloudTexture extends Texture3D {
    private static final int DEFAULT_ZOOM = 64;
    private final NoiseGenerator noiseGenerator = new NoiseGenerator();

    public CloudTexture(int usingUnit) {
        super(usingUnit);
    }

    @Override
    protected void fillDataArray() {
        if (zoom == 1) // zoom value unset
            noiseGenerator.levelMixedNoise(data, textureWidth, textureHeight, textureDepth, DEFAULT_ZOOM, 1, "BLUE");
        else
            noiseGenerator.levelMixedNoise(data, textureWidth, textureHeight, textureDepth, zoom, 1, "BLUE");
    }
}
