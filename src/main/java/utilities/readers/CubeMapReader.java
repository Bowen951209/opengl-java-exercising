package utilities.readers;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;
import static org.lwjgl.opengl.GL43.*;

public class CubeMapReader {
    private final int texID;
    public void bind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, texID);
    }

    public CubeMapReader(String dirPath) {
        // The directory should have 6 files to read.
        String xp = dirPath + "/xp.jpg";
        String xn = dirPath + "/xn.jpg";
        String yp = dirPath + "/yp.jpg";
        String yn = dirPath + "/yn.jpg";
        String zp = dirPath + "/zp.jpg";
        String zn = dirPath + "/zn.jpg";

        texID = glGenTextures();
        bind();

        loadImageToTexture(xp, GL_TEXTURE_CUBE_MAP_POSITIVE_X);
        loadImageToTexture(xn, GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
        loadImageToTexture(yp, GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
        loadImageToTexture(yn, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
        loadImageToTexture(zp, GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
        loadImageToTexture(zn, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        // 經過測試，沒有這2行無法執行。沒有設定這2行，OpenGL會將這個Texture視為"不完整"!

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
    }

    private static void loadImageToTexture(String filePath, int face) {
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        ByteBuffer image = STBImage.stbi_load(filePath, width, height, channels, 0);
        assert image != null;
        switch (channels.get(0)) {
            case 3 -> glTexImage2D(face, 0, GL_RGB, width.get(0), height.get(0)
                    , 0, GL_RGB, GL_UNSIGNED_BYTE, image);
            case 4 -> glTexImage2D(face, 0, GL_RGBA, width.get(0), height.get(0)
                    , 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        }
        STBImage.stbi_image_free(image);
    }
}
