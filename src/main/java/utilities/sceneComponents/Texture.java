package utilities.sceneComponents;

import utilities.readers.TextureReader;

import static org.lwjgl.opengl.GL43.*;
public class Texture extends TextureReader {
    public Texture(int usingUnit, String filepath) {
        super(filepath);
        glActiveTexture(GL_TEXTURE0 + usingUnit);
        config();


    }

    public void config() {
        // if needed, this can be overridden
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, getTexID());
    }
}
