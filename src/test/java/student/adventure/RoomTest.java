package student.adventure;

import org.junit.Before;
import org.junit.Test;
import org.hamcrest.CoreMatchers;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RoomTest {
    Layout layout;
    Room currRoom;
    GameEngine game;

    @Before
    public void setUp() throws IOException {
        game = new GameEngine();
        layout = game.loadJson("game.json");
    }

    @Test
    public void testGetRoomsName() {
        assertThat(layout.getRoomsName()[1], CoreMatchers.is("Practice"));
    }

    // No item in room should return an empty list
    @Test
    public void testGetItemEmpty() {
        List<String> items = new ArrayList<>();
        currRoom = new Room("zoo", "Have Fun", items, new ArrayList<>());
        List<String> expected = Arrays.asList();
        assertEquals(currRoom.getItems(), expected);
    }

    @Test
    public void testGetItemValid() {
        List<String> items = Arrays.asList("zebra", "zoo keeper");
        currRoom = new Room("zoo", "Have Fun", items, new ArrayList<>());
        List<String> expected = Arrays.asList("zebra", "zoo keeper");
        assertEquals(currRoom.getItems(), expected);
    }

    // Take valid item in a new room should result in losing an item
    @Test
    public void testTakeItem() {
        List<String> items = Arrays.asList("dog", "cat", "rabbit");
        currRoom = new Room("zoo", "Have Fun", items, new ArrayList<>());
        currRoom.takeItem("DOG");
        List<String> expected = Arrays.asList("cat", "rabbit");
        assertEquals(currRoom.getItems(), expected);
    }

    // Pick item not exist should have no influence to the item list
    @Test
    public void testTakeItemInvalid() {
        List<String> items = Arrays.asList("dog", "cat", "rabbit");
        currRoom = new Room("zoo", "Have Fun", items, new ArrayList<>());
        currRoom.takeItem("a cute little cs126");
        List<String> expected = Arrays.asList("dog", "cat", "rabbit");
        assertEquals(currRoom.getItems(), expected);
    }

    // Drop an item already exist should result in having two of the same string in the item list
    @Test
    public void testDropItem() {
        List<String> items = Arrays.asList("dog", "cat", "rabbit");
        currRoom = new Room("zoo", "Have Fun", items, new ArrayList<>());
        currRoom.dropItem("DOG");
        List<String> expected = Arrays.asList("dog", "cat", "rabbit", "dog");
        assertEquals(currRoom.getItems(), expected);
    }

    // Test get more than one possible direction
    @Test
    public void testGetDirections() {
        List<String> expected = Arrays.asList("vacation", "practice");
        assertEquals(layout.getRooms()[0].getDirections(), expected);
    }
}