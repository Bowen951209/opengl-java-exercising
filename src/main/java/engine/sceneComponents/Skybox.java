package engine.sceneComponents;

import static org.lwjgl.opengl.GL43.*;
import static engine.util.ValuesContainer.VALS_OF_16;

public class Skybox {
    private final int program, skyVmatLoc, skyPmatLoc;
    private final Camera camera;
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
        this.program = program;
        this.skyVmatLoc = skyVMatLoc;
        this.skyPmatLoc = skyPMatLoc;
        this.camera = camera;
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
        glUseProgram(program);
        glBindVertexArray(VAO);

        glDisable(GL_DEPTH_TEST);
        glUniformMatrix4fv(skyVmatLoc, false, camera.getVMat().get(VALS_OF_16));
        glUniformMatrix4fv(skyPmatLoc, false, camera.getProjMat().get(VALS_OF_16));

        glDrawArrays(GL_TRIANGLES, 0, 108);

        glEnable(GL_DEPTH_TEST);
    }
}
