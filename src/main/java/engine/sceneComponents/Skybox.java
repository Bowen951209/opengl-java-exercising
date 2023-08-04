package engine.sceneComponents;

import engine.ShaderProgram;
import engine.sceneComponents.textures.CubeMapTexture;

import static engine.util.ValuesContainer.VALS_OF_16;
import static org.lwjgl.opengl.GL43.*;

public class Skybox {
    private final int programID, skyVmatLoc, skyPmatLoc;
    private final Camera camera;
    private final int VBO, VAO;
    private CubeMapTexture texture;
    private static final ShaderProgram SHADER_PROGRAM = new ShaderProgram(
            "assets/shaders/skybox/vert.glsl",
            "assets/shaders/skybox/frag.glsl"
    );
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

    public Skybox(int programID, int skyVMatLoc, int skyPMatLoc, Camera camera) {
        this.programID = programID;
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

    public Skybox(Camera camera, String textureFolder) {
        this.programID = SHADER_PROGRAM.getID();
        this.skyVmatLoc = SHADER_PROGRAM.getUniformLoc("v_matrix");
        this.skyPmatLoc = SHADER_PROGRAM.getUniformLoc("p_matrix");
        this.camera = camera;
        this.texture = new CubeMapTexture(textureFolder);

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
        glUseProgram(programID);
        if (this.texture != null) {
            this.texture.bind();
        }
        glBindVertexArray(VAO);

        glDisable(GL_DEPTH_TEST);
        glUniformMatrix4fv(skyVmatLoc, false, camera.getVMat().get(VALS_OF_16));
        glUniformMatrix4fv(skyPmatLoc, false, camera.getProjMat().get(VALS_OF_16));

        glDrawArrays(GL_TRIANGLES, 0, 108);

        glEnable(GL_DEPTH_TEST);
    }
}
