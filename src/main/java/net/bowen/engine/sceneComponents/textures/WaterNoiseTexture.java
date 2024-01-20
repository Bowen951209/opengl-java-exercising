package net.bowen.engine.sceneComponents.textures;

import net.bowen.engine.util.NoiseGenerator;

public class WaterNoiseTexture extends Texture3D {
    private final NoiseGenerator noiseGenerator = new NoiseGenerator();

    public WaterNoiseTexture(int usingUnit) {
        super(usingUnit);
    }

    @Override
    protected void fillDataArray() {
        noiseGenerator.tileTurbulence(data, textureWidth, textureHeight, textureDepth, zoom, 1);
        data.flip();
    }
}
