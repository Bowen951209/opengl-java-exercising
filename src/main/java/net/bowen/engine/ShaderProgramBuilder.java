package net.bowen.engine;

import net.bowen.engine.exceptions.IllegalShaderProgramException;
import net.bowen.engine.readers.TextReader;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL43.*;

public class ShaderProgramBuilder {
    private final Set<Integer> shaderTypes = new HashSet<>();
    private final Set<Integer> shaderIDs = new HashSet<>();

    public ShaderProgramBuilder addShader(int type, String... sources) {
        shaderTypes.add(type);

        for (String src : sources) {
            String text = TextReader.readFile(src);
            int shaderID = glCreateShader(type);
            ShaderProgram.compileAndCatchShaderErr(shaderID, text, GL_COMPUTE_SHADER);
            shaderIDs.add(shaderID);
        }

        return this;
    }

    public ShaderProgram getProgram() {
        ShaderProgram shaderProgram = new ShaderProgram();
        int[] shaderIDsArray = shaderIDs.stream().mapToInt(i -> i).toArray();

        // Checking if the shaders are valid to assemble as a program.
        switch (shaderTypes.size()) {
            case 1 -> {
                if (shaderTypes.contains(GL_COMPUTE_SHADER))
                    shaderProgram.id = ShaderProgram.setupProgram(shaderIDsArray);

                else throw new IllegalShaderProgramException();
            }
            case 2 -> {
                if (shaderTypes.contains(GL_VERTEX_SHADER) && shaderTypes.contains(GL_FRAGMENT_SHADER))
                    shaderProgram.id = ShaderProgram.setupProgram(shaderIDsArray);

                else throw new IllegalShaderProgramException();
            }
            case 3 -> {
                if (shaderTypes.contains(GL_VERTEX_SHADER) && shaderTypes.contains(GL_FRAGMENT_SHADER)
                        && shaderTypes.contains(GL_GEOMETRY_SHADER))
                    shaderProgram.id = ShaderProgram.setupProgram(shaderIDsArray);

                else throw new IllegalShaderProgramException();
            }
            case 4 -> {
                if (shaderTypes.contains(GL_VERTEX_SHADER) && shaderTypes.contains(GL_FRAGMENT_SHADER)
                        && shaderTypes.contains(GL_TESS_CONTROL_SHADER) && shaderTypes.contains(GL_TESS_EVALUATION_SHADER))
                    shaderProgram.id = ShaderProgram.setupProgram(shaderIDsArray);

                else throw new IllegalShaderProgramException();
            }
            default -> throw new IllegalShaderProgramException();
        }

        return shaderProgram;
    }
}
