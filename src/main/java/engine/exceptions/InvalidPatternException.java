package engine.exceptions;

public class InvalidPatternException extends RuntimeException {
    public InvalidPatternException() {super();}
    public InvalidPatternException(String msg) {
        super(msg);
    }
}
