package engine.models;

import engine.readers.ModelReader;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL43.*;

// TODO: 2023/7/18 use thread
public class FileModel extends Model {
    private final ModelReader file;
    public FileModel(String filePath, boolean isUsingTexture) {
        this(filePath, new Vector3f(), isUsingTexture);
    }

    public FileModel( String filePath, Vector3f position, boolean isUsingTexture) {
        super(position, false, isUsingTexture, false);
        file = new ModelReader(filePath);

        if (isUsingTexture)
            storeDataToVBOs(file.getPvalue(), file.getNvalue(), file.getTvalue());
        else
            storeDataToVBOs(file.getPvalue(), file.getNvalue());
    }

    @Override
    protected void updateMMat() {
        mMat.identity().translate(position);
    }

    @Override
    public void draw(int mode) {
        bindVAO();
        glDrawArrays(mode, 0, file.getNumOfVertices());
    }
}
