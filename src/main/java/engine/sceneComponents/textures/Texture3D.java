package engine.sceneComponents.textures;

import engine.util.Timer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL43.*;

public abstract class Texture3D extends Thread {
    private final Timer timer = new Timer();
    private final int usingUnit;

    protected ByteBuffer data;
    private int textureID;
    protected int textureWidth = 256;
    protected int textureHeight = 256;
    protected int textureDepth = 256;
    protected int zoom = 1;

    public void setResolution(int width, int height, int depth) {
        this.textureWidth = width;
        this.textureHeight = height;
        this.textureDepth = depth;
    }

    public int getTextureDepth() {
        return textureDepth;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public Texture3D(int usingUnit) {
        this.usingUnit = usingUnit;
    }

    @Override
    public void run() {
        timer.start();
        System.out.println("\"" + this.getClass().getSimpleName() + "\" thread start.");
        data = BufferUtils.createByteBuffer(textureWidth * textureHeight * textureDepth * 4);
        fillDataArray(); // This take quite long.
        timer.end("\"" + this.getClass().getSimpleName() + "\" fills data in array takes: ");
    }

    public void end() {
        try {
            join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        loadToTexture();
    }

    private void loadToTexture() {
        textureID = glGenTextures();
        bind();
        glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, textureWidth, textureHeight, textureDepth);
        glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0, textureWidth, textureHeight, textureDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, data);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }

    public void bind() {
        glActiveTexture(GL_TEXTURE0 + usingUnit);
        glBindTexture(GL_TEXTURE_3D, textureID);
    }

    protected abstract void fillDataArray();

}
