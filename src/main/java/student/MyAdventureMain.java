package student;

import org.glassfish.grizzly.http.server.HttpServer;
import student.adventure.GameEngine;
import student.server.AdventureResource;

import static student.server.AdventureServer.createServer;

public class MyAdventureMain {
    // The flag to determine whether run game locally. Set to false will result in run game in server.
    private static final boolean RUN_LOCALLY = true;

    /**
     * This method launches and starts the game.
     */
    public static void main(String[] args) throws Exception {
        GameEngine game = new GameEngine();
        if (RUN_LOCALLY) {
            game.runGameTerminal("game.json");
        } else {
            HttpServer httpServer = createServer(AdventureResource.class);
            httpServer.start();
        }
    }
}