package utilities.models;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import utilities.readers.ModelReader;

import static org.lwjgl.opengl.GL11.glDrawArrays;

public class Grid extends Model{
    public Grid(Vector3f position) {
        super(position, false, true, false);
        ModelReader grid = new ModelReader("assets/models/grid.obj");

        VERTICES_IN_BUF = BufferUtils.createFloatBuffer(grid.getNumOfVertices() * 3);
        VERTICES_IN_BUF.put(grid.getPvalue());
        VERTICES_IN_BUF.flip();
        NORMALS_IN_BUF = BufferUtils.createFloatBuffer(grid.getNumOfVertices() * 3);
        NORMALS_IN_BUF.put(grid.getNvalue());
        NORMALS_IN_BUF.flip();
        TC_IN_BUF = BufferUtils.createFloatBuffer(grid.getNumOfVertices() * 2);
        TC_IN_BUF.put(grid.getTvalue());
        TC_IN_BUF.flip();

        storeDataToVBOs(VERTICES_IN_BUF, NORMALS_IN_BUF, TC_IN_BUF);
    }

    @Override
    protected void updateMMat() {
        M_MAT.identity().translate(POSITION);
    }

    @Override
    public void draw(int mode) {
        bindVAO();
        glDrawArrays(mode, 0, VERTICES_IN_BUF.limit());
    }
}
