package utilities;


import utilities.readers.GLSLReader;

import java.nio.file.Path;

import static org.lwjgl.opengl.GL43.*;


public class ShaderProgramSetter {
    private final int programID;
    public int getProgram() {return programID;}

    // Only vertex & fragment
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
        programID = setupProgram(vertexShaderID, fragmentShaderID);
    }
    public ShaderProgramSetter(String vertexShaderPath, String fragmentShaderPath) {this(Path.of(vertexShaderPath), Path.of(fragmentShaderPath));}


    // vertex, fragment, tessellation control shader & tessellation evaluation shader
    public ShaderProgramSetter(Path vertexShaderPath, Path fragmentShaderPath, Path tessellationControlShaderPath, Path tessellationEvaluationShaderPath) {
        // 讀取Shader(glsl檔案)
        String vertexShaderCode = new GLSLReader(vertexShaderPath).getString();
        String fragmentShaderCode = new GLSLReader(fragmentShaderPath).getString();
        String tcsShaderCode = new GLSLReader(tessellationControlShaderPath).getString();
        String tesShaderCode = new GLSLReader(tessellationEvaluationShaderPath).getString();

        // 設定vertex shader來源、編譯
        int vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
        compileAndCatchShaderErr(vertexShaderID, vertexShaderCode);
        // 設定fragment shader來源、編譯
        int fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        compileAndCatchShaderErr(fragmentShaderID, fragmentShaderCode);

        int tcsID = glCreateShader(GL_TESS_CONTROL_SHADER);
        compileAndCatchShaderErr(tcsID, tcsShaderCode);
        int tesID = glCreateShader(GL_TESS_EVALUATION_SHADER);
        compileAndCatchShaderErr(tesID, tesShaderCode);

        // 設定program
        programID = setupProgram(vertexShaderID, fragmentShaderID, tesID, tcsID);
    }
    public ShaderProgramSetter(String vertexShaderPath, String fragmentShaderPath, String tessellationControlShaderPath, String tessellationEvaluationShaderPath) {
        this(Path.of(vertexShaderPath), Path.of(fragmentShaderPath), Path.of(tessellationControlShaderPath), Path.of(tessellationEvaluationShaderPath));
    }

    public static int setupProgram(int vertexShaderID, int fragmentShaderID) {
        // This method returns the programID
        int programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glLinkProgram(programID);
        checkLinkStatus(programID);

        return programID;
    }
    public static int setupProgram(int vertexShaderID, int fragmentShaderID, int tessellationControlShaderID, int tessellationEvaluationShaderID) {
        // This method returns the programID
        int programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glAttachShader(programID, tessellationControlShaderID);
        glAttachShader(programID, tessellationEvaluationShaderID);
        glLinkProgram(programID);
        checkLinkStatus(programID);

        return programID;
    }
    public static void checkLinkStatus(int programID) {
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Program" + programID + " linked failed\n" + glGetProgramInfoLog(programID));
        } else {
            System.out.println("ProgramID:"+ programID +" linked succeeded.");
        }
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
