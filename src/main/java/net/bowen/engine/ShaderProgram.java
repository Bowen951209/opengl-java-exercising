package net.bowen.engine;


import net.bowen.engine.exceptions.ProgramLinkedFailedException;
import net.bowen.engine.exceptions.ShaderCompiledFailedException;
import net.bowen.engine.readers.GLSLReader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.HashMap;

import static org.lwjgl.opengl.GL43.*;


public class ShaderProgram {
    private final int id;
    private final HashMap<String, Integer> uniformLocMap = new HashMap<>();

    public int getID() {
        return id;
    }

    public int getUniformLoc(String uniformName) {
        if (this.uniformLocMap.get(uniformName) != null) {
            // Given.
            return this.uniformLocMap.get(uniformName);
        } else {
            // Not given.
            int loc = glGetUniformLocation(this.id, uniformName);
            this.uniformLocMap.put(uniformName, loc);
            if (loc == -1)
                System.err.println("Uniform \"" + uniformName + "\" not found");
            return loc;
        }
    }

    public void putUniformMatrix4f(String uniformName, FloatBuffer values) {
        glUniformMatrix4fv(getUniformLoc(uniformName), false, values);
    }

    public void putUniform1f(String uniformName, float value) {
        glProgramUniform1f(id, getUniformLoc(uniformName), value);
    }

    /**
     * This method doesn't mean you can only pass in buffer/array whose length is 1.
     * <pre>
     * For example, we have a array uniform:
     *     uniform float[50] a;
     * You can pass in a buffer/array of length 50, it'll be translated to the uniform
     * and each index matches.
     * </pre>
     */
    public void putUniform1fv(String uniformName, FloatBuffer value) {
        glProgramUniform1fv(id, getUniformLoc(uniformName), value);
    }

    public void putUniform1i(String uniformName, int value) {
        glProgramUniform1i(id, getUniformLoc(uniformName), value);
    }

    public void putUniform1iv(String uniformName, IntBuffer value) {
        glProgramUniform1iv(id, getUniformLoc(uniformName), value);
    }

    public void putUniform3f(String uniformName, float[] value) {
        glProgramUniform3fv(id, getUniformLoc(uniformName), value);
    }

    public void putUniform3f(String uniformName, FloatBuffer value) {
        glProgramUniform3fv(id, getUniformLoc(uniformName), value);
    }

    public void putUniform4f(String uniformName, float[] value) {
        glProgramUniform4fv(id, getUniformLoc(uniformName), value);
    }

    public int use() {
        glUseProgram(id);
        return id;
    }

    public void delete() {
        glDeleteProgram(this.id);
    }

    // Compute shader
    public ShaderProgram(String computeShaderPath) {
        String shaderSource = new GLSLReader(Path.of(computeShaderPath)).getString();
        int shaderID = glCreateShader(GL_COMPUTE_SHADER);
        compileAndCatchShaderErr(shaderID, shaderSource, GL_COMPUTE_SHADER);

        this.id = setupProgram(shaderID);
    }

    // Only vertex & fragment
    public ShaderProgram(Path vertexShaderPath, Path fragmentShaderPath) {
        // 讀取Shader(glsl檔案)
        String vertexShaderCode = new GLSLReader(vertexShaderPath).getString();
        String fragmentShaderCode = new GLSLReader(fragmentShaderPath).getString();

        // 設定vertex shader來源、編譯
        int vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
        compileAndCatchShaderErr(vertexShaderID, vertexShaderCode, GL_VERTEX_SHADER);
        // 設定fragment shader來源、編譯
        int fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        compileAndCatchShaderErr(fragmentShaderID, fragmentShaderCode, GL_FRAGMENT_SHADER);

        // 設定program
        id = setupProgram(vertexShaderID, fragmentShaderID);
    }

    public ShaderProgram(String vertexShaderPath, String fragmentShaderPath) {
        this(Path.of(vertexShaderPath), Path.of(fragmentShaderPath));
    }

    // vertex, fragment, geometry shader
    public ShaderProgram(Path vertexShaderPath, Path fragmentShaderPath, Path geometryShaderPath) {
        String vertShaderCode = new GLSLReader((vertexShaderPath)).getString();
        String fragShaderCode = new GLSLReader((fragmentShaderPath)).getString();
        String geoShaderCode = new GLSLReader((geometryShaderPath)).getString();

        int vertShaderID = glCreateShader(GL_VERTEX_SHADER);
        compileAndCatchShaderErr(vertShaderID, vertShaderCode, GL_VERTEX_SHADER);

        int fragShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        compileAndCatchShaderErr(fragShaderID, fragShaderCode, GL_FRAGMENT_SHADER);

        int geoShaderID = glCreateShader(GL_GEOMETRY_SHADER);
        compileAndCatchShaderErr(geoShaderID, geoShaderCode, GL_GEOMETRY_SHADER);

        this.id = setupProgram(vertShaderID, fragShaderID, geoShaderID);
    }

    public ShaderProgram(String vertexShaderPath, String fragmentShaderPath, String geometryShaderPath) {
        this(Path.of(vertexShaderPath), Path.of(fragmentShaderPath), Path.of(geometryShaderPath));
    }

    // vertex, fragment, tessellation control shader & tessellation evaluation shader
    public ShaderProgram(Path vertexShaderPath, Path fragmentShaderPath, Path tessellationControlShaderPath, Path tessellationEvaluationShaderPath) {
        // 讀取Shader(glsl檔案)
        String vertexShaderCode = new GLSLReader(vertexShaderPath).getString();
        String fragmentShaderCode = new GLSLReader(fragmentShaderPath).getString();
        String tcsShaderCode = new GLSLReader(tessellationControlShaderPath).getString();
        String tesShaderCode = new GLSLReader(tessellationEvaluationShaderPath).getString();

        // 設定vertex shader來源、編譯
        int vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
        compileAndCatchShaderErr(vertexShaderID, vertexShaderCode, GL_VERTEX_SHADER);
        // 設定fragment shader來源、編譯
        int fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        compileAndCatchShaderErr(fragmentShaderID, fragmentShaderCode, GL_FRAGMENT_SHADER);

        int tcsID = glCreateShader(GL_TESS_CONTROL_SHADER);
        compileAndCatchShaderErr(tcsID, tcsShaderCode, GL_TESS_CONTROL_SHADER);
        int tesID = glCreateShader(GL_TESS_EVALUATION_SHADER);
        compileAndCatchShaderErr(tesID, tesShaderCode, GL_TESS_EVALUATION_SHADER);

        // 設定program
        id = setupProgram(vertexShaderID, fragmentShaderID, tesID, tcsID);
    }

    public ShaderProgram(String vertexShaderPath, String fragmentShaderPath, String tessellationControlShaderPath, String tessellationEvaluationShaderPath) {
        this(Path.of(vertexShaderPath), Path.of(fragmentShaderPath), Path.of(tessellationControlShaderPath), Path.of(tessellationEvaluationShaderPath));
    }

    private static int setupProgram(int computeShaderID) {
        int programID = glCreateProgram();
        glAttachShader(programID, computeShaderID);
        glLinkProgram(programID);
        checkLinkStatus(programID);

        glDeleteShader(computeShaderID);
        return programID;
    }

    private static int setupProgram(int vertexShaderID, int fragmentShaderID) {
        // This method returns the programID
        int programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glLinkProgram(programID);
        checkLinkStatus(programID);

        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        return programID;
    }

    private static int setupProgram(int vertexShaderID, int fragmentShaderID, int geometryShaderID) {
        int programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glAttachShader(programID, geometryShaderID);
        glLinkProgram(programID);
        checkLinkStatus(programID);

        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        glDeleteShader(geometryShaderID);
        return programID;
    }

    private static int setupProgram(int vertexShaderID, int fragmentShaderID, int tessellationControlShaderID, int tessellationEvaluationShaderID) {
        // This method returns the programID
        int programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glAttachShader(programID, tessellationControlShaderID);
        glAttachShader(programID, tessellationEvaluationShaderID);
        glLinkProgram(programID);
        checkLinkStatus(programID);

        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        glDeleteShader(tessellationControlShaderID);
        glDeleteShader(tessellationEvaluationShaderID);
        return programID;
    }

    private static void checkLinkStatus(int programID) {
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            throw new ProgramLinkedFailedException("App" + programID + " linked failed\n" + glGetProgramInfoLog(programID));
        } else {
            System.out.println("ProgramID:" + programID + " linked succeeded.");
        }
    }

    private static void compileAndCatchShaderErr(int shaderID, String source, int shaderType) {
        glShaderSource(shaderID, source);
        glCompileShader(shaderID);

        String shader = getString(shaderType);
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            throw new ShaderCompiledFailedException(shader + " (ID=" + shaderID + ") compiled failed\n" + glGetShaderInfoLog(shaderID));
        } else {
            System.out.println(shader + "(ID=" + shaderID + ") compiled succeeded.");
        }
    }

    private static String getString(int shaderType) {
        String shader = null;
        switch (shaderType) {
            case GL_VERTEX_SHADER -> shader = "Vertex Shader";
            case GL_FRAGMENT_SHADER -> shader = "Fragment Shader";
            case GL_GEOMETRY_SHADER -> shader = "Geometry Shader";
            case GL_TESS_CONTROL_SHADER -> shader = "Tess Control Shader";
            case GL_TESS_EVALUATION_SHADER -> shader = "Tess Evaluation Shader";
            case GL_COMPUTE_SHADER -> shader = "Compute Shader";
        }
        return shader;
    }

}
