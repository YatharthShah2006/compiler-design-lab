package visitor;

public class TypeException extends RuntimeException {
    private String message;

    public TypeException(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}