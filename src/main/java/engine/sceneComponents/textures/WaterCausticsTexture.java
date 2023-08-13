package engine.sceneComponents.textures;

import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;

public class WaterCausticsTexture extends Texture3D {
    private final PerlinNoiseGenerator perlinNoiseGenerator = PerlinNoiseGenerator.newBuilder().setSeed(1515115).build();
    public WaterCausticsTexture(int usingUnit) {
        super(usingUnit);
    }

    @Override
    protected void fillDataArray() {

        for (double x = 0; x < textureWidth; x++) {
            for (double y = 0; y < textureHeight; y++) {
                for (double z = 0; z < textureDepth; z++) {
                    double noiseValue = perlinNoiseGenerator.evaluateNoise(x ,y, z);
                    data.put((byte) noiseValue);
                }
            }
        }

        data.flip();
    }
}
