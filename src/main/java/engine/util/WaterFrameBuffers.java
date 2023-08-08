package engine.util;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL43.*;

public class WaterFrameBuffers {
    private final int reflectionFrameBuffer, refractionFrameBuffer;
    private final int reflectionImageTexture, reflectionDepthTexture, refractionImageTexture, refractionDepthTexture;

    public int getReflectionImageTexture() {
        return reflectionImageTexture;
    }

    public int getReflectionDepthTexture() {
        return reflectionDepthTexture;
    }

    public int getRefractionImageTexture() {
        return refractionImageTexture;
    }

    public int getRefractionDepthTexture() {
        return refractionDepthTexture;
    }

    public int getReflectionFrameBuffer() {
        return reflectionFrameBuffer;
    }

    public int getRefractionFrameBuffer() {
        return refractionFrameBuffer;
    }

    public WaterFrameBuffers(int textureWidth, int textureHeight) {
        this.reflectionFrameBuffer = generateFramebuffer();
        reflectionImageTexture = createImageTextureAttachment(textureWidth, textureHeight);
        reflectionDepthTexture = createDepthTextureAttachment(textureWidth, textureHeight);


        this.refractionFrameBuffer = generateFramebuffer();
        refractionImageTexture = createImageTextureAttachment(textureWidth, textureHeight);
        refractionDepthTexture = createDepthTextureAttachment(textureWidth, textureHeight);
    }

    private static int generateFramebuffer() {
        int framebufferID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        //specify frame buffer to render to color attachment 0
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        return framebufferID;
    }

    private static int createImageTextureAttachment(int width, int height) {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0,
                GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // get data from current frame buffer's color attachment 0 and putt to texture.
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureID, 0);

        return textureID;
    }

    private static int createDepthTextureAttachment(int width, int height) {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, width, height, 0,
                GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // get data from current frame buffer's color attachment 0 and putt to texture.
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureID, 0);

        return textureID;
    }

    public void resizeTo(int width, int height) {
        glBindTexture(GL_TEXTURE_2D, reflectionImageTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0,
                GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        glBindTexture(GL_TEXTURE_2D, refractionImageTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0,
                GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        glBindTexture(GL_TEXTURE_2D, reflectionDepthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, width, height, 0,
                GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);

        glBindTexture(GL_TEXTURE_2D, refractionDepthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, width, height, 0,
                GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
    }
}
