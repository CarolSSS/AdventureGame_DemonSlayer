package student.server;

import java.util.SortedMap;

/**
 * An abstract store for any type of adventure game.
 */
public interface AdventureService {
    /**
     * Resets the service to its initial state.
     */
    void reset();

    /**
     * Creates a new Adventure game and stores it.
     * @return the id of the game.
     */
    int newGame() throws AdventureException;

    /**
     * Returns the state of the game instance associated with the given ID.
     * @param id the instance id
     * @return the current state of the game
     */
    GameStatus getGame(int id);

    /**
     * Removes & destroys a game instance with the given ID.
     * @param id the instance id
     * @return false if the instance could not be found and/or was not deleted
     */
    boolean destroyGame(int id);

    /**
     * Executes a command on the game instance with the given id, changing the game state if applicable.
     * @param id the instance id
     * @param command the issued command
     */
    void executeCommand(int id, Command command);

    /**
     * Returns a sorted leaderboard of player "high" scores.
     * @return a sorted map of player names to scores
     */
    SortedMap<String, Integer> fetchLeaderboard();
}
