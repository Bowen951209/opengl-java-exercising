package engine.sceneComponents.textures;

import engine.util.NoiseGenerator;

public class WaterCausticTexture extends Texture3D {
    private final NoiseGenerator noiseGenerator = new NoiseGenerator();

    public WaterCausticTexture(int usingUnit) {
        super(usingUnit);
    }

    @Override
    protected void fillDataArray() {
        noiseGenerator.turbulence(data, textureWidth, textureHeight, textureDepth, zoom, 1);
        data.flip();
    }
}
