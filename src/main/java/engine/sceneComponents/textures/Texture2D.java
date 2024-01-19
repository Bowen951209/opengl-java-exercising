package engine.sceneComponents.textures;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import engine.readers.TextureReader;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL43.*;

public class Texture2D extends TextureReader {
    private final int usingUnit;

    public Texture2D(int usingUnit) {
        super();
        this.usingUnit = usingUnit;
        config(GL_NEAREST);
        glActiveTexture(GL_TEXTURE0 + usingUnit);
    }

    public Texture2D(int usingUnit, String filepath) {
        super(filepath);
        this.usingUnit = usingUnit;
        config(GL_LINEAR_MIPMAP_LINEAR);
        glActiveTexture(GL_TEXTURE0 + usingUnit);
    }

    public void config(int mipMapSampleMode) {
        // if needed, this can be overridden
        genMipMap(mipMapSampleMode); // GL_LINEAR_MIPMAP_LINEAR

        GLCapabilities capabilities = GL.getCapabilities();
        // if graphics card has anisotropic
        if (capabilities.GL_EXT_texture_filter_anisotropic) {
            enableAnisotropic();
        } else {
            System.err.println("Your graphics card does not support anisotropic filtering. So it is disabled.");
        }

    }

    private void genMipMap(int sampleMode) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, sampleMode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, sampleMode);
        glGenerateMipmap(GL_TEXTURE_2D);
        printInfo("generated mipmap.");
    }

    private void enableAnisotropic() {
        FloatBuffer anisoset = BufferUtils.createFloatBuffer(1);
        glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset.get(0));
        printInfo("enabled anisotropic.");
    }

    public void fill(int width, int height, Color color) {
        if (color == null) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        } else {
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
            for (int i = 0; i < buffer.capacity() / 4; i++) {
                buffer.put((byte) color.getRed());
                buffer.put((byte) color.getGreen());
                buffer.put((byte) color.getBlue());
                buffer.put((byte) color.getAlpha());
            }
            buffer.flip();

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        }
    }

    private void printInfo(String message) {
        System.out.println("Texture2D on unit " + usingUnit + "(id: " + getTexID() + ") " + message);
    }

    public void bind() {
        glActiveTexture(GL_TEXTURE0 + usingUnit);
        glBindTexture(GL_TEXTURE_2D, getTexID());
    }

    public static void putToUniform(int unit, int id) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, id);
    }
}
