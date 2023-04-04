package utilities;

import org.lwjgl.stb.STBImage;
import static org.lwjgl.opengl.GL43.*;

import java.nio.ByteBuffer;

public class TextureReader {
    private int texID;
    public TextureReader(String filepath) {
        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];// grey or rgb or rgba
        ByteBuffer image = STBImage.stbi_load(filepath, width, height, channels, 0);


        if (image != null) {
            if (channels[0] == 3) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width[0], height[0],
                        0, GL_RGB, GL_UNSIGNED_BYTE, image);
            } else if (channels[0] == 4) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0],
                        0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            } else {
                System.err.println("Error: (Texture) Unknown number of channels '" + channels[0] + "'");
            }
        } else {
            System.out.println("Error: (Texture) Could not load image '" + filepath + "'");
        }

        STBImage.stbi_image_free(image);

    }

    public int getTexID() {
        return texID;
    }
}
