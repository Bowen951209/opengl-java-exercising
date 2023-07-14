package engine.models;

import engine.readers.ModelReader;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL43.*;

public class FileModel extends Model {
    private final ModelReader file;
    public FileModel(String filePath) {
        this(filePath, new Vector3f());
    }

    public FileModel( String filePath, Vector3f position) {
        super(position, false, true, false);
        file = new ModelReader(filePath);

        storeDataToVBOs(file.getPvalue(), file.getNvalue(), file.getTvalue());
    }

    @Override
    protected void updateMMat() {
        mMat.identity()
                .translate(position)
                .scale(1f);
    }

    @Override
    public void draw(int mode) {
        bindVAO();
        glDrawArrays(mode, 0, file.getNumOfVertices());
    }
}
