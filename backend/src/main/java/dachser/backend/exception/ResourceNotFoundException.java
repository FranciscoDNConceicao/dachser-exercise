package dachser.backend.exception;

/**
 * Base class for "requested data does not exist" errors (answered with HTTP 404).
 */
public abstract class ResourceNotFoundException extends RuntimeException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
