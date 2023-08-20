package chapter16_ray_tracing;
import static org.lwjgl.opengl.GL43.*;
import engine.App;
import engine.GLFWWindow;
import engine.ShaderProgram;
import engine.util.Destroyer;

import java.util.Arrays;

public class Program16_1 extends App {
    private ShaderProgram shaderProgram;
    @Override
    protected void customizedInit() {
        glfwWindow = new GLFWWindow(10, 10, "");
        shaderProgram = new ShaderProgram("assets/shaders/ch16/16_1/simpleComputeShader.glsl");


        final float[] matrix0 = {10f, 12f, 18f, 51f, 21f, 10f};
        final float[] matrix1 = {10f, 60f, 11f, 7f, 55f, 32f};
        final float[] result = new float[6];

        final int[] storageBuffers = new int[3];
        glGenBuffers(storageBuffers);

        // Put data into buffers
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, storageBuffers[0]);
        glBufferData(GL_SHADER_STORAGE_BUFFER, matrix0, GL_STATIC_DRAW);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, storageBuffers[1]);
        glBufferData(GL_SHADER_STORAGE_BUFFER, matrix1, GL_STATIC_DRAW);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, storageBuffers[2]);
        glBufferData(GL_SHADER_STORAGE_BUFFER, 6 * Float.BYTES, GL_STATIC_READ); // empty 6 data

        computeSum(storageBuffers, result);

        System.out.println(Arrays.toString(matrix0));
        System.out.println(Arrays.toString(matrix1));
        System.out.println(Arrays.toString(result));
    }

    private void computeSum(int[] storageBuffers, float[] result) {
        shaderProgram.use();

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, storageBuffers[0]); // input 0
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, storageBuffers[1]); // input 1
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, storageBuffers[2]); // result

        glDispatchCompute(6, 1, 1);
        glFinish(); // ensure all computations done

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, storageBuffers[2]);
        glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, result);
    }

    @Override
    protected void drawScene() {
        // No loop is used int this program.
    }

    @Override
    protected void destroy() {
        Destroyer.destroyAll(glfwWindow.getID());
    }

    public static void main(String[] args) {
        new Program16_1().run(false);
    }
}
