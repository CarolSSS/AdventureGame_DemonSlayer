package student.server;

/**
 * An exception thrown if there was an issue starting a new adventure game.
 */
class AdventureException extends Exception {
    AdventureException(String message) {
        super(message);
    }

    AdventureException(String message, Throwable cause) {
        super(message, cause);
    }
}
