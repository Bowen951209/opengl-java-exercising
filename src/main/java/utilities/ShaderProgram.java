package utilities;


import utilities.exceptions.ProgramLinkedFailedException;
import utilities.exceptions.ShaderCompiledFailedException;
import utilities.readers.GLSLReader;

import java.nio.file.Path;
import java.util.HashMap;

import static org.lwjgl.opengl.GL43.*;


public class ShaderProgram {
    private final int id;

    public int getID() {
        return id;
    }

    public int use() {
        glUseProgram(id);
        return id;
    }

    private final HashMap<String, Integer> uniformLocMap = new HashMap<>();

    public int getUniformLoc(String uniformName) {
        if(this.uniformLocMap.get(uniformName) != null) {
            return this.uniformLocMap.get(uniformName);
        }
        else {
            // No defined uniform location.
            return -1;
        }
    }

    public void getAllUniformLocs(String[] uniforms) {
        for (String i : uniforms) {
            int loc = glGetUniformLocation(this.id, i);
            if (loc == -1) {
                System.err.println("Uniform \"" + i + "\" not found");
            }
            this.uniformLocMap.put(i, loc);
        }
    }


    // Only vertex & fragment
    public ShaderProgram(Path vertexShaderPath, Path fragmentShaderPath) {
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
        compileAndCatchShaderErr(vertShaderID, vertShaderCode);

        int fragShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        compileAndCatchShaderErr(fragShaderID, fragShaderCode);

        int geoShaderID = glCreateShader(GL_GEOMETRY_SHADER);
        compileAndCatchShaderErr(geoShaderID, geoShaderCode);

        this.id = setupProgram(vertShaderID, fragShaderID, geoShaderID);
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
        compileAndCatchShaderErr(vertexShaderID, vertexShaderCode);
        // 設定fragment shader來源、編譯
        int fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        compileAndCatchShaderErr(fragmentShaderID, fragmentShaderCode);

        int tcsID = glCreateShader(GL_TESS_CONTROL_SHADER);
        compileAndCatchShaderErr(tcsID, tcsShaderCode);
        int tesID = glCreateShader(GL_TESS_EVALUATION_SHADER);
        compileAndCatchShaderErr(tesID, tesShaderCode);

        // 設定program
        id = setupProgram(vertexShaderID, fragmentShaderID, tesID, tcsID);
    }

    public ShaderProgram(String vertexShaderPath, String fragmentShaderPath, String tessellationControlShaderPath, String tessellationEvaluationShaderPath) {
        this(Path.of(vertexShaderPath), Path.of(fragmentShaderPath), Path.of(tessellationControlShaderPath), Path.of(tessellationEvaluationShaderPath));
    }

    private static int setupProgram(int vertexShaderID, int fragmentShaderID) {
        // This method returns the programID
        int programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glLinkProgram(programID);
        checkLinkStatus(programID);

        return programID;
    }

    private static int setupProgram(int vertexShaderID, int fragmentShaderID, int geometryShaderID) {
        int programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glAttachShader(programID, geometryShaderID);
        glLinkProgram(programID);
        checkLinkStatus(programID);

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

        return programID;
    }

    public static void checkLinkStatus(int programID) {
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            throw new ProgramLinkedFailedException("App" + programID + " linked failed\n" + glGetProgramInfoLog(programID));
        } else {
            System.out.println("ProgramID:" + programID + " linked succeeded.");
        }
    }

    private static void compileAndCatchShaderErr(int shaderID, String source) {
        glShaderSource(shaderID, source);
        glCompileShader(shaderID);

        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            // TODO: 2023/7/11 Add a shader type detection so it is easier to spot what shader throws error.
            throw new ShaderCompiledFailedException("Shader" + shaderID + " compiled failed\n" + glGetShaderInfoLog(shaderID));
        } else {
            System.out.println("    Shader ID:" + shaderID + " compiled succeeded.");
        }
    }

}
