package net.bowen.engine.exceptions;

public class IllegalShaderProgramException extends RuntimeException {
    public IllegalShaderProgramException() {
        super("Invalid shader set. A shader program should consist of:" +
                " 1) Only compute shader." +
                " 2) Only vertex & fragment shader." +
                " 3) Only vertex, fragment, and geometry shader." +
                " 4) Only vertex, fragment, tessellation control, and tesselation evaluation shader.");
    }
}
