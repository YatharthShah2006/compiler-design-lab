package visitor;

public class ResolveException extends RuntimeException {
    private String message;

    public ResolveException(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}

