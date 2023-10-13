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
    private static final ShaderProgram DEFAULT_PROGRAM = new ShaderProgram(
            "assets/shaders/skybox/vert.glsl",
            "assets/shaders/skybox/frag.glsl"
    );
    private final ShaderProgram shaderProgram;

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

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

    public Skybox(Camera camera, String textureFolder, ShaderProgram shaders) {
        this.shaderProgram = shaders;
        this.programID = shaders.getID();
        this.skyVmatLoc = shaders.getUniformLoc("v_matrix");
        this.skyPmatLoc = shaders.getUniformLoc("p_matrix");
        this.camera = camera;
        this.texture = new CubeMapTexture(textureFolder);

        VBO = glGenBuffers();
        storeDataToVBO();

        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

    }

    public Skybox(Camera camera, String textureFolder) {
        this(camera, textureFolder, DEFAULT_PROGRAM);
    }

    private void storeDataToVBO() {
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);
    }

    public void draw() {
        glUseProgram(programID);

        glBindVertexArray(VAO);

        glDisable(GL_DEPTH_TEST);
        glUniformMatrix4fv(skyVmatLoc, false, camera.getVMat().get(VALS_OF_16));
        glUniformMatrix4fv(skyPmatLoc, false, camera.getProjMat().get(VALS_OF_16));

        if (this.texture != null) {
            this.texture.bind();
        }
        glDrawArrays(GL_TRIANGLES, 0, 108);

        glEnable(GL_DEPTH_TEST);
    }
}
