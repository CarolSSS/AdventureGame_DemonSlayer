package student.server;

import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

// Since execute command has already been tested via other functions in game engine.
// This file only test about new, destroy, and reset.
public class MyAdventureServiceTest {
    MyAdventureService service;

    @Before
    public void setUp() throws IOException {
        service = new MyAdventureService();
    }

    // Test if the id will increment for each time
    @Test
    public void testNewGame() throws Exception{
        service.newGame();
        service.newGame();
        assertEquals(service.newGame(), 3);
    }

    // Test if destroy not existed id will result in false
    @Test
    public void testDestroyGameInvalid() throws Exception{
        service.newGame();
        service.newGame();
        service.destroyGame(1);
        assertEquals(service.destroyGame(1), false);
    }

    // Test if reset will make everything back to the original (reset id to 0)
    @Test
    public void testReset() throws Exception{
        service.newGame();
        service.newGame();
        service.reset();
        assertEquals(service.newGame(), 1);
    }

}
