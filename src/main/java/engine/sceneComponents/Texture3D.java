package engine.sceneComponents;

import engine.util.Timer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL43.*;

public abstract class Texture3D extends Thread {
    private final Timer timer = new Timer();
    private int textureID;
    private final int usingUnit;
    protected final int textureWidth = 256;
    protected final int textureHeight = 256;
    protected final int textureDepth = 256;
    protected int zoom = 1;

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    protected ByteBuffer data;

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

    protected abstract void fillDataArray();

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
}
