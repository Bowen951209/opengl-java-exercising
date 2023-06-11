package utilities;


import utilities.readers.GLSLReader;

import java.nio.file.Path;

import static org.lwjgl.opengl.GL43.*;


public class ShaderProgramSetter {

    public int getProgram() {
        return programID;
    }

    private final int programID;

    public ShaderProgramSetter(Path vertexShaderPath, Path fragmentShaderPath) {
        // 讀取Shader(glsl檔案)

        String vertexShaderCode = new GLSLReader(vertexShaderPath).getString();
        String fragmentShaderCode = new GLSLReader(fragmentShaderPath).getString();

        // 設定vertex shader來源、編譯
        int vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
        compileAndCatchShaderErr(vertexShaderID, vertexShaderCode);
        // 設定fragment shader來源、編譯
        int fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        compileAndCatchShaderErr(fragmentShaderID, fragmentShaderCode);

        // 設定program
        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Program" + programID + " linked failed\n" + glGetProgramInfoLog(programID));
        } else {
            System.out.println("ProgramID:"+ programID +" linked succeeded.");
        }
        // 記得在程式裡使用glUseProgram();
    }

    private static void compileAndCatchShaderErr(int shaderID, String source) {
        glShaderSource(shaderID, source);
        glCompileShader(shaderID);

        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Shader" + shaderID + " compiled failed\n" + glGetShaderInfoLog(shaderID));
        } else {
            System.out.println("    Shader ID:" +shaderID + " compiled succeeded.");
        }
    }
}
