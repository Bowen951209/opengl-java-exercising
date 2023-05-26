package utilities.buffers;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

public class ShadowFrameBuffer {
    public int getShadowFrameBuffer() {
        return shadowFrameBuffer;
    }

    private final int shadowFrameBuffer;

    private final int shadowTex;

    public ShadowFrameBuffer(long window) {
        IntBuffer frameBufW = BufferUtils.createIntBuffer(1), frameBufH = BufferUtils.createIntBuffer(1);
        glfwGetFramebufferSize(window, frameBufW, frameBufH);

        // 創建自定義frame buffer
        shadowFrameBuffer = glGenFramebuffers();

        // 創建陰影紋理儲存深度訊息
        shadowTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, shadowTex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, frameBufW.get(0), frameBufH.get(0), 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        config();
    }

    private void config() {
        // 使用自定義幀緩衝區，將紋理附著到其上
        glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBuffer);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex, 0);
        // 關閉繪製顏色
        glDrawBuffer(GL_NONE);
    }
    public void resetTex(int width, int height) {
        glBindTexture(GL_TEXTURE_2D, shadowTex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
    }
}
