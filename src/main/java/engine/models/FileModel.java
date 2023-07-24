package engine.models;

import engine.readers.ModelReader;
import engine.util.Timer;
import org.joml.Vector3f;

import java.nio.file.Path;

import static org.lwjgl.opengl.GL43.*;

public class FileModel extends Model {
    private ModelReader file;
    private final String filepath, fileName;
    private final boolean isUsingTexture;

    public FileModel(String filePath, boolean isUsingTexture) {
        this(filePath, new Vector3f(), isUsingTexture);
    }

    public FileModel( String filePath, Vector3f position, boolean isUsingTexture) {
        super(position, false, isUsingTexture, false);
        this.filepath = filePath;
        this.fileName = Path.of(filePath).getFileName().toString();
        this.isUsingTexture = isUsingTexture;
    }

    @Override
    public void run() {
        System.out.println("\"" + fileName + "\" thread start.");
        Timer timer = new Timer();
        timer.start();
        file = new ModelReader(filepath);
        timer.end("\"" + fileName + "\" thread end in: ");
    }

    public void end() {
        try {
            join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

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
