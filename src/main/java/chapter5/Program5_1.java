package chapter5;


import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import utilities.Color;
import utilities.GLFWWindow;
import utilities.ShaderProgram;
import utilities.readers.TextureReader;

import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

public class Program5_1 {
    private static long windowHandle;

    private static float cameraX;
    private static float cameraY;
    private static float cameraZ;

    private static Matrix4f pMat;
    private static final float[] vals = new float[16];// utility buffer for transferring matrices
    private static final int[] vbo = new int[2];
    private static final GLFWFramebufferSizeCallbackI resizeGlViewportAndResetAspect = (long window, int w, int h) -> {
        System.out.println("GLFW Window Resized to: " + w + "*" + h);
        glViewport(0, 0, w, h);
        createProjMat(w, h);
    };
    private static int projLoc;
    private static int mvLoc;
    private static int brickTexture;


    public static void main(String[] args) {
        init();
        // 迴圈
        loop();
        // 釋出
        GLFW.glfwTerminate();
        System.out.println("App exit and freed glfw.");


    }

    private static void init() {
        final int windowCreatedW = 800, windowCreatedH = 600;
        GLFWWindow glfwWindow = new GLFWWindow(windowCreatedW, windowCreatedH, "第5章");
        windowHandle = glfwWindow.getID();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));
        // 一開始要先呼叫，才能以長、寬構建透視矩陣
        createProjMat(windowCreatedW, windowCreatedH);

        // 設定frameBuffer大小改變callback
        glfwSetFramebufferSizeCallback(windowHandle, resizeGlViewportAndResetAspect);

        int program = new ShaderProgram(Path.of("src/main/java/chapter5/Shaders/for5_1/VertexShader.glsl")
                , Path.of("src/main/java/chapter5/Shaders/for5_1/FragmentShader.glsl"))
                .getID();

        cameraX = 0f; cameraY = 0f; cameraZ = 8f;
        setupVertices();
        brickTexture = new TextureReader("src/main/java/chapter5/textures/brick.jpg").getTexID();
        
        glUseProgram(program);
        System.out.println("Using ProgramID: " + program);

        // 獲取mv矩陣和投影矩陣的統一變量
        mvLoc = glGetUniformLocation(program, "mv_matrix");
        projLoc = glGetUniformLocation(program, "proj_matrix");
    }

    private static void loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            float currentTime = (float) glfwGetTime();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_CULL_FACE);


            glUniformMatrix4fv(projLoc, false, pMat.get(vals));

            // 將視圖矩陣壓入栈
            Matrix4fStack mvStack = new Matrix4fStack(4);
            mvStack.pushMatrix();
            mvStack.translate(-cameraX, -cameraY, -cameraZ);

            // ------------------四角錐-------------------------------------------
            mvStack.pushMatrix();
            mvStack.translate(0f, 0f, 0f); // 位置
            mvStack.pushMatrix();
            mvStack.rotateX(currentTime); // 旋轉
            mvStack.scale(2f);

            glUniformMatrix4fv(mvLoc, false, mvStack.get(vals));

            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, brickTexture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glGenerateMipmap(GL_TEXTURE_2D);


            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            glDrawArrays(GL_TRIANGLES, 0, 18); // 繪製


            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }


    private static void setupVertices() {

        float[] pyramidPositions = { // 四角椎
                -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f
        };

        float[ ] pyrTextureCoordinates = {
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
        };

        int vao = glGenVertexArrays(); // vao: Vertex Array Object
        glBindVertexArray(vao);

        vbo[0] = glGenBuffers();
        vbo[1] = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(GL_ARRAY_BUFFER, pyramidPositions, GL_STATIC_DRAW);
        
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        glBufferData(GL_ARRAY_BUFFER, pyrTextureCoordinates, GL_STATIC_DRAW);
    }

    private static void createProjMat(int w, int h) {
        float aspect = (float) w / (float) h;
        pMat = new Matrix4f().perspective(1.0472f, aspect, .1f, 1000f); // 1.0472 = 60度
    }

}
