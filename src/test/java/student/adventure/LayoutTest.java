package student.adventure;

import org.junit.Before;
import org.junit.Test;
import org.hamcrest.CoreMatchers;
import static org.junit.Assert.assertThat;
import java.io.IOException;


public class LayoutTest {
    Layout layout;
    GameEngine game;

    @Before
    public void setUp() throws IOException {
        game = new GameEngine();
        layout = game.loadJson("game.json");
    }

    // If no room available, return empty array
    @Test
    public void testGetRoomsNameNull() {
        Layout layoutEmpty = new Layout("start","end", null);
        assertThat(layoutEmpty.getRoomsName(),CoreMatchers.is(new String[0]));
    }

    // Based on game.json dataset. The second room is Practice
    @Test
    public void testGetRoomsName(){
        assertThat(layout.getRoomsName()[1],CoreMatchers.is("Practice"));
    }
}