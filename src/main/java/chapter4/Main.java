package chapter4;


import static org.joml.Math.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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
    private static float cubeLocX;
    private static float cubeLocY;
    private static float cubeLocZ;
    private static Matrix4f pMat;
    private static final float[] vals = new float[16];// utility buffer for transferring matrices
    private static int vbo;
    private static int frameBufferW, frameBufferH;
    private static final GLFWFramebufferSizeCallbackI resizeGlViewportAndResetAspect = (long window, int w, int h) -> {
        frameBufferW = w;
        frameBufferH = h;
        System.out.println("GLFW Window Resized to: " + frameBufferW + "*" + frameBufferH);
        glViewport(0, 0, frameBufferW, frameBufferH);
        createProjMat(frameBufferW ,frameBufferH);
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
        final int windowCreatedW= 800, windowCreatedH = 600;
        GLFWWindow glfwWindow = new GLFWWindow(windowCreatedW, windowCreatedH, "第4章");
        windowHandle = glfwWindow.getWindowHandle();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));
        // 一開始要先呼叫，才能以長、寬構建透視矩陣
        createProjMat(windowCreatedW, windowCreatedH);

        // 設定frameBuffer大小改變callback
        glfwSetFramebufferSizeCallback(windowHandle, resizeGlViewportAndResetAspect);

        program = new ShaderProgramSetter(Path.of("D:\\Desktop\\Bowen\\code\\JAVA\\Computer Graphics Programming In OpenGL With C++ Book Practice\\ComputerGraphicsProgrammingInOpenGLWithCpp\\src\\main\\java\\chapter4\\Shaders\\VertexShader.glsl")
                ,Path.of("D:\\Desktop\\Bowen\\code\\JAVA\\Computer Graphics Programming In OpenGL With C++ Book Practice\\ComputerGraphicsProgrammingInOpenGLWithCpp\\src\\main\\java\\chapter4\\Shaders\\FragmentShader.glsl"))
                .getProgram();

        cameraX = 0f; cameraY = 0f; cameraZ = 8f;
        cubeLocX = 0f; cubeLocY = -2f; cubeLocZ = 0f; // 沿y軸下一以展示透視

        setupVertices();
        glUseProgram(program);
        System.out.println("Using ProgramID: " + program);
    }

    private static void loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//            glClear(GL_DEPTH_BUFFER_BIT);

            // 獲取mv矩陣和投影矩陣的統一變量
            int mvLoc = glGetUniformLocation(program, "mv_matrix");
            int projLoc = glGetUniformLocation(program, "proj_matrix");




            for (int i = 0; i < 24; i++) {
                // 建構視圖矩陣、模型矩陣和MV矩陣
                float tf = (float) (glfwGetTime() + i); // tf == "time factor"時間因子
                Matrix4f vMat = new Matrix4f().translate(-cameraX, -cameraY, -cameraZ);
                Matrix4f mMat = new Matrix4f().rotateY(1.75f*tf).rotateX(1.75f*tf).rotateZ(1.75f*tf)
                        .translate(new Vector3f(sin(.35f*tf) * 8f, cos(.52f*tf) * 8f, sin(.7f*tf)*8f));

                Matrix4f mvMat = vMat.mul(mMat);
                // 將透視矩陣和mv矩陣複製給相應的統一變量
                glUniformMatrix4fv(mvLoc, false, mvMat.get(vals));
                glUniformMatrix4fv(projLoc, false, pMat.get(vals));

                // 將vbo關聯給頂點著色器中相應的頂點屬性
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
                glEnableVertexAttribArray(0);

                // 調整OpenGL設置，繪製模型
                glEnable(GL_DEPTH_TEST); // 第二章: 深度測試
                glDepthFunc(GL_LEQUAL);
                glDrawArrays(GL_TRIANGLES, 0, 36);
            }





            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }



    private static void setupVertices() {
        float[] vertexPositions = { // 36個頂點，12個三角形; 2*2*2 正方體
                -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f,  1.0f, -1.0f,
                1.0f, -1.0f,  1.0f,  1.0f,  1.0f,  1.0f,  1.0f,  1.0f, -1.0f,
                1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f,  1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f,
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
