package utilities.exceptions;

public class ShaderCompiledFailedException extends RuntimeException {
    public ShaderCompiledFailedException(String message) {
        super(message);
    }
}
