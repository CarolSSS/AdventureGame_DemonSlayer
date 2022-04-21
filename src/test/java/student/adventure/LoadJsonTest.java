package student.adventure;

import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.io.FileNotFoundException;
import static org.junit.Assert.assertThat;
import org.hamcrest.CoreMatchers;

public class LoadJsonTest {
    Layout layout;
    GameEngine game;

    @Before
    public void setUp() throws IOException {
        game = new GameEngine();
    }


    // Null input
    @Test(expected = NullPointerException.class)
    public void loadJsonNull() throws IOException {
        layout = game.loadJson(null);
    }

    // Invalid type or not found
    @Test(expected = FileNotFoundException.class)
    public void loadJsonInvalidFileName() throws IOException {
        layout = game.loadJson("game");
    }

    // Starting room is null
    @Test(expected = IllegalArgumentException.class)
    public void loadJsonInvalidStart() throws IOException {
        layout = game.loadJson("gameInvalidStarting.json");
    }

    // Ending room do not exist in the room list
    @Test(expected = IllegalArgumentException.class)
    public void loadJsonInvalidEnd() throws IOException {
        layout = game.loadJson("gameInvalidEnding.json");
    }

    // Normal Import
    @Test
    public void loadJsonValid() throws IOException {
        layout = game.loadJson("game.json");
        assertThat(layout.getStartingRoom(), CoreMatchers.is("Home"));
    }
}