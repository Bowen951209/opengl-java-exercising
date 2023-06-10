package utilities.sceneComponents;

import static org.lwjgl.opengl.GL43.*;
import static utilities.ValuesContainer.VALS_OF_16;

public class Skybox {
    private final int PROGRAM, SKY_VMAT_LOC, SKY_PMAT_LOC;
    private final Camera CAMERA;
    private final int VBO, VAO;
    private static final float[] VERTICES = {-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f
    };

    public Skybox(int program, int skyVMatLoc, int skyPMatLoc, Camera camera) {
        this.PROGRAM = program;
        this.SKY_VMAT_LOC = skyVMatLoc;
        this.SKY_PMAT_LOC = skyPMatLoc;
        this.CAMERA = camera;
        VBO = glGenBuffers();
        storeDataToVBO();

        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    }

    private void storeDataToVBO() {
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);
    }

    public void draw() {
        glUseProgram(PROGRAM);
        glBindVertexArray(VAO);

        glDisable(GL_DEPTH_TEST);
        glUniformMatrix4fv(SKY_VMAT_LOC, false, CAMERA.getVMat().get(VALS_OF_16));
        glUniformMatrix4fv(SKY_PMAT_LOC, false, CAMERA.getProjMat().get(VALS_OF_16));

        glDrawArrays(GL_TRIANGLES, 0, 108);

        glEnable(GL_DEPTH_TEST);
    }
}
