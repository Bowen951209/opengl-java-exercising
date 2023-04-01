package chapter4;


import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import utilities.Color;
import utilities.GLFWWindow;
import utilities.ShaderProgramSetter;

import java.nio.file.Path;

import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.glfw.GLFW.*;

public class Main {
    private static long windowHandle;
    private static int program;

    private static float cameraX;
    private static float cameraY;
    private static float cameraZ;

    private static Matrix4f pMat;
    private static final float[] vals = new float[16];// utility buffer for transferring matrices
    private static int vbo;
    private static final GLFWFramebufferSizeCallbackI resizeGlViewportAndResetAspect = (long window, int w, int h) -> {
        System.out.println("GLFW Window Resized to: " + w + "*" + h);
        glViewport(0, 0, w, h);
        createProjMat(w, h);
    };


    public static void main(String[] args) {
        init();
        // 迴圈
        loop();
        // 釋出
        GLFW.glfwTerminate();
        System.out.println("Program exit and freed glfw.");


    }

    private static void init() {
        final int windowCreatedW = 800, windowCreatedH = 600;
        GLFWWindow glfwWindow = new GLFWWindow(windowCreatedW, windowCreatedH, "第4章");
        windowHandle = glfwWindow.getWindowHandle();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));
        // 一開始要先呼叫，才能以長、寬構建透視矩陣
        createProjMat(windowCreatedW, windowCreatedH);

        // 設定frameBuffer大小改變callback
        glfwSetFramebufferSizeCallback(windowHandle, resizeGlViewportAndResetAspect);

        program = new ShaderProgramSetter(Path.of("D:\\Desktop\\Bowen\\code\\JAVA\\Computer Graphics Programming In OpenGL With C++ Book Practice\\ComputerGraphicsProgrammingInOpenGLWithCpp\\src\\main\\java\\chapter4\\Shaders\\VertexShader.glsl")
                , Path.of("D:\\Desktop\\Bowen\\code\\JAVA\\Computer Graphics Programming In OpenGL With C++ Book Practice\\ComputerGraphicsProgrammingInOpenGLWithCpp\\src\\main\\java\\chapter4\\Shaders\\FragmentShader.glsl"))
                .getProgram();

        cameraX = 0f;
        cameraY = 0f;
        cameraZ = 32f;

        setupVertices();
        glUseProgram(program);
        System.out.println("Using ProgramID: " + program);
    }

    private static void loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // 獲取mv矩陣和投影矩陣的統一變量
            int vLoc = glGetUniformLocation(program, "v_matrix");
            int projLoc = glGetUniformLocation(program, "proj_matrix");
            int tfLoc = glGetUniformLocation(program, "tf");


            // 建構視圖矩陣、模型矩陣和MV矩陣
            float tf = (float) (glfwGetTime()); // tf == "time factor"時間因子
            Matrix4f vMat = new Matrix4f().translate(-cameraX, -cameraY, -cameraZ);


            glUniformMatrix4fv(vLoc, false, vMat.get(vals));
            glUniform1f(tfLoc, tf);
            glUniformMatrix4fv(projLoc, false, pMat.get(vals));

            // 將vbo關聯給頂點著色器中相應的頂點屬性
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            // 調整OpenGL設置，繪製模型
            glEnable(GL_DEPTH_TEST); // 第二章: 深度測試
            glDepthFunc(GL_LEQUAL);
            glDrawArraysInstanced(GL_TRIANGLES, 0, 36, 500);


            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }


    private static void setupVertices() {
        float[] vertexPositions = { // 36個頂點，12個三角形; 2*2*2 正方體
                -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
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
                1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f,
        };

        int vao = glGenVertexArrays(); // vao: Vertex Array Object
        glBindVertexArray(vao);
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexPositions, GL_STATIC_DRAW);
    }

    private static void createProjMat(int w, int h) {
        float aspect = (float) w / (float) h;
        pMat = new Matrix4f().perspective(1.0472f, aspect, .1f, 1000f); // 1.0472 = 60度
    }

}
