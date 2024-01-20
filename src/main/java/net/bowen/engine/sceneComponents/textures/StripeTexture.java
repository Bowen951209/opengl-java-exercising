package net.bowen.engine.sceneComponents.textures;

public class StripeTexture extends Texture3D {
    public StripeTexture(int usingUnit) {
        super(usingUnit);
    }

    @Override
    protected void fillDataArray() {
        fillStripe();
    }

    private void fillStripe() {
        for (int x = 0; x < textureWidth; x++) {
            for (int y = 0; y < textureHeight; y++) {
                for (int z = 0; z < textureDepth; z++) {
                    if ((y / 10) % 2 == 0) {
                        // yellow
                        data.put((byte) 255); // r
                        data.put((byte) 255); // g
                        data.put((byte) 0);// b
                        data.put((byte) 255); // a
                    } else {
                        // blue
                        data.put((byte) 0); // r
                        data.put((byte) 0); // g
                        data.put((byte) 255);// b
                        data.put((byte) 255); // a
                    }
                }
            }
        }

        data.flip();
    }
}
