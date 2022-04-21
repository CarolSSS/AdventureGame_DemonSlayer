package student.server;

import student.adventure.GameEngine;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

public class MyAdventureService implements AdventureService {
    GameEngine game = new GameEngine();
    // A map keep track of all game players and their status;
    private Map<Integer, GameStatus> allGamesID = new HashMap<>();
    private int currentID = 0;

    /**
     * Resets the service to its initial state.
     */
    @Override
    public void reset() {
        currentID = 0;
        game = new GameEngine();
        allGamesID = new HashMap<>();
    }

    /**
     * Creates a new Adventure game and stores it.
     * @return the id of the game.
     */
    @Override
    public int newGame() throws AdventureException {
        GameStatus currentGame;
        this.currentID++;
        try {
            currentGame = game.startGame("game.json", currentID);
        } catch (Exception ioException) {
            throw new AdventureException("Invalid Import JSON file");
        }
        this.allGamesID.put(currentGame.getId(), currentGame);
        return currentGame.getId();
    }

    /**
     * Returns the state of the game instance associated with the given ID.
     * @param id the instance id
     * @return the current state of the game
     */
    @Override
    public GameStatus getGame(int id) {
        return this.allGamesID.get(id);
    }

    /**
     * Removes & destroys a game instance with the given ID.
     * @param id the instance id
     * @return false if the instance could not be found and/or was not deleted
     */
    @Override
    public boolean destroyGame(int id) {
        // If not found
        if (!this.allGamesID.containsKey(id)) {
            return false;
        }
        this.allGamesID.remove(id);
        // If remove failed
        if (this.allGamesID.containsKey(id)) {
            return false;
        }
        return true;
    }

    /**
     * Executes a command on the game instance with the given id, changing the game state if applicable.
     * @param id the instance id
     * @param command the issued command
     */
    @Override
    public void executeCommand(int id, Command command) {
        GameStatus currentStatus = this.allGamesID.get(id);
        GameStatus newStatus = this.game.runGameServer(currentStatus, command);
        this.allGamesID.put(newStatus.getId(), newStatus);
    }

    /**
     * Returns a sorted leaderboard of player "high" scores.
     * @return a sorted map of player names to scores
     */
    @Override
    public SortedMap<String, Integer> fetchLeaderboard() {
        return null;
    }
}
