package engine.sceneComponents.textures;

import engine.util.NoiseGenerator;

public class WaterCausticTexture extends Texture3D {
    private final NoiseGenerator noiseGenerator = new NoiseGenerator();

    public WaterCausticTexture(int usingUnit) {
        super(usingUnit);
    }

    @Override
    protected void fillDataArray() {
//        for (int x = 0; x < textureWidth; x++) {
//            for (int z = 0; z < textureDepth; z++) {
//                double sineWave = (Math.sin((1.0 / 512.0) * (8 * Math.PI) * (x + z)) + 1) * 8.0;
//                data.put((byte) (128.0*sineWave / zoom));
//            }
//        }
//        noiseGenerator.turbulence(data, textureWidth, textureHeight, textureDepth, zoom, 1);
        noiseGenerator.tileTurbulence(data, textureWidth, textureHeight, textureDepth, zoom, 1);
        data.flip();
    }
}
