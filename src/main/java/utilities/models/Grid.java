package utilities.models;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import utilities.readers.ModelReader;

import static org.lwjgl.opengl.GL11.glDrawArrays;

public class Grid extends Model{
    public Grid(Vector3f position) {
        super(position, false, true, false);
        ModelReader grid = new ModelReader("assets/models/grid.obj");

        verticesInBuf = BufferUtils.createFloatBuffer(grid.getNumOfVertices() * 3);
        verticesInBuf.put(grid.getPvalue());
        verticesInBuf.flip();
        normalsInBuf = BufferUtils.createFloatBuffer(grid.getNumOfVertices() * 3);
        normalsInBuf.put(grid.getNvalue());
        normalsInBuf.flip();
        tcInBuf = BufferUtils.createFloatBuffer(grid.getNumOfVertices() * 2);
        tcInBuf.put(grid.getTvalue());
        tcInBuf.flip();

        storeDataToVBOs(verticesInBuf, normalsInBuf, tcInBuf);
    }

    @Override
    protected void updateMMat() {
        mMat.identity().translate(position);
    }

    @Override
    public void draw(int mode) {
        bindVAO();
        glDrawArrays(mode, 0, verticesInBuf.limit());
    }
}
