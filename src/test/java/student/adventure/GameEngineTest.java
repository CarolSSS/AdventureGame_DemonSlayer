package student.adventure;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.contrib.java.lang.system.SystemOutRule;
import student.server.AdventureState;
import student.server.Command;
import student.server.GameStatus;

public class GameEngineTest {
    Layout layout;
    GameEngine game;

    // Reference: https://stefanbirkner.github.io/system-rules/#EnvironmentVariables
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Before
    public void setUp() throws IOException {
        game = new GameEngine();
        layout = game.loadJson("game.json");

    }

    @Test
    public void testProcessGameValid() {
        Room nextRoom = game.goGame("Preparation", "gO PRACtIce");
        assertEquals(nextRoom.getName(), "Practice");
    }

    // Test input results in "I don't understand...."
    @Test
    public void testProcessGameAbsentGo() {
        game.setRunLocalFlag(true);
        Room nextRoom = game.goGame("Preparation", "practice more");
        assertThat(systemOutRule.getLog(), CoreMatchers.containsString("I don't understand"));
        assertEquals(nextRoom.getName(), "Preparation");
    }

    // Test input results when goes to multiple place
    @Test
    public void testProcessGameMultiple() {
        game.setRunLocalFlag(true);
        game.goGame("Preparation", "gO PRACtIce more and vacation");
        assertThat(systemOutRule.getLog(), CoreMatchers.containsString("I can't go"));
    }

    // Test input results in "I can't go...."
    @Test
    public void testProcessGameInvalidPlace() {
        game.setRunLocalFlag(true);
        game.goGame("Preparation", "go ccccssss126");
        assertThat(systemOutRule.getLog(), CoreMatchers.containsString("I can't go"));
    }

    // test the equalsIgnoreCase functionality in checkExit function
    @Test
    public void testCheckExit() {
        boolean exitFlag = game.checkExit("Exit");
        assertThat(exitFlag, CoreMatchers.is(Boolean.TRUE));
    }

    // test the equalsIgnoreCase and trim functionality in checkExamine function
    @Test
    public void testCheckExamine() {
        boolean examineFlag = game.checkExamine(" eXAmine");
        assertThat(examineFlag, CoreMatchers.is(Boolean.TRUE));
    }

    // Take valid item out
    @Test
    public void testTakeItem() {
        List<String> items = Arrays.asList("dog", "cat", "rabbit");
        Room currRoom = new Room("zoo","Have Fun", items, new ArrayList<>());
        boolean itemFlag = game.checkOperateItem(currRoom, "take CAT");
        assertThat(itemFlag, CoreMatchers.is(Boolean.TRUE));
    }

    // Take invalid item
    @Test
    public void testTakeInvalidItem() {
        List<String> items = Arrays.asList("dog", "cat", "rabbit");
        Room currRoom = new Room("zoo","Have Fun", items, new ArrayList<>());
        game.setRunLocalFlag(true);
        boolean itemFlag = game.checkOperateItem(currRoom, "take cat   and     dog");
        assertThat(itemFlag, CoreMatchers.is(Boolean.TRUE));
        assertThat(systemOutRule.getLog(), CoreMatchers.containsString("There is no"));
    }

    // Drop Item not picked
    @Test
    public void testDropInvalidItem() {
        List<String> items = Arrays.asList("dog", "cat", "rabbit");
        Room currRoom = new Room("zoo","Have Fun", items, new ArrayList<>());
        game.setRunLocalFlag(true);
        boolean itemFlag = game.checkOperateItem(currRoom, "drop catDogRAAABIt");
        assertThat(systemOutRule.getLog(), CoreMatchers.containsString("You don't have"));
    }

    // Drop item just picked
    @Test
    public void testDropValidItem() {
        List<String> items = Arrays.asList("dog", "cat", "rabbit");
        Room currRoom = new Room("zoo","Have Fun", items, new ArrayList<>());
        game.checkOperateItem(currRoom, "take cat");
        boolean itemFlag = game.checkOperateItem(currRoom, "drop cat");
        assertThat(itemFlag, CoreMatchers.is(Boolean.TRUE));
    }

    // Below are specifically testing on server
    // Test if the player will be at the start room while calling startGame
    @Test
    public void testStartGame() throws Exception {
        int currentId = 0;
        GameStatus newStatus = game.startGame("smallTest.json", currentId);
        assertEquals(newStatus.getCommandOptions().get("go"), Arrays.asList("win", "practice"));
    }

    // Both player should act the same at beginning
    @Test
    public void testStartGameMultiplePlayer() throws Exception {
        GameStatus newStatus1 = game.startGame("smallTest.json", 1);
        GameStatus newStatus2 = game.startGame("smallTest.json", 2);
        assertEquals(newStatus1.getCommandOptions().get("go"), newStatus2.getCommandOptions().get("go"));
    }

    @Test
    public void testRunGameServerGo() throws Exception {
        layout = game.loadJson("smallTest.json");
        Map<String, Room> layoutMap = game.setMapLayout(layout);
        Room currRoom = layoutMap.get(layout.getStartingRoom());
        AdventureState newState = new AdventureState();
        Map<String, List<String>> allCommands = new HashMap<>();
        List<String> goList = Arrays.asList("win", "practice");
        allCommands.put("go", goList);
        int currentId = 0;
        game.startGame("smallTest.json", currentId);
        GameStatus currentGame = new GameStatus(false, currentId, currRoom.getDescription(),
                null, null, newState, allCommands);
        Command currentCommand = new Command("go", "practice");
        GameStatus newStatus = game.runGameServer(currentGame, currentCommand);
        assertEquals(newStatus.getCommandOptions().get("go"), Arrays.asList("win"));
    }

    // Test if the first player could still move after initialize the second player
    @Test
    public void testRunGameServerGoMultiplePlayerFirst() throws Exception {
        layout = game.loadJson("smallTest.json");
        Map<String, Room> layoutMap = game.setMapLayout(layout);
        Room currRoom = layoutMap.get(layout.getStartingRoom());
        AdventureState newState = new AdventureState();
        Map<String, List<String>> allCommands = new HashMap<>();
        List<String> goList = Arrays.asList("win", "practice");
        allCommands.put("go", goList);
        int currentId1 = 1;
        game.startGame("smallTest.json", currentId1);
        GameStatus currentGame1 = new GameStatus(false, currentId1, currRoom.getDescription(),
                null, null, newState, allCommands);
        int currentId2 = 2;
        game.startGame("smallTest.json", currentId2);
        GameStatus currentGame2 = new GameStatus(false, currentId2, currRoom.getDescription(),
                null, null, newState, allCommands);
        Command currentCommand = new Command("go", "practice");
        GameStatus newStatus = game.runGameServer(currentGame1, currentCommand);
        assertEquals(newStatus.getCommandOptions().get("go"), Arrays.asList("win"));
    }

    // Test if the second player could still move after initialize the second player
    @Test
    public void testRunGameServerGoMultiplePlayerSecond() throws Exception {
        layout = game.loadJson("smallTest.json");
        Map<String, Room> layoutMap = game.setMapLayout(layout);
        Room currRoom = layoutMap.get(layout.getStartingRoom());
        AdventureState newState = new AdventureState();
        Map<String, List<String>> allCommands = new HashMap<>();
        List<String> goList = Arrays.asList("win", "practice");
        allCommands.put("go", goList);
        int currentId1 = 1;
        game.startGame("smallTest.json", currentId1);
        GameStatus currentGame1 = new GameStatus(false, currentId1, currRoom.getDescription(),
                null, null, newState, allCommands);
        int currentId2 = 2;
        game.startGame("smallTest.json", currentId2);
        GameStatus currentGame2 = new GameStatus(false, currentId2, currRoom.getDescription(),
                null, null, newState, allCommands);
        Command currentCommand = new Command("go", "practice");
        GameStatus newStatus = game.runGameServer(currentGame2, currentCommand);
        assertEquals(newStatus.getCommandOptions().get("go"), Arrays.asList("win"));
    }

    @Test
    public void testRunGameServerTake() throws Exception {
        layout = game.loadJson("smallTest.json");
        Map<String, Room> layoutMap = game.setMapLayout(layout);
        Room currRoom = layoutMap.get(layout.getStartingRoom());
        AdventureState newState = new AdventureState();
        Map<String, List<String>> allCommands = new HashMap<>();
        List<String> goList = Arrays.asList("Nezuko","dog");
        allCommands.put("take", goList);
        int currentId = 0;
        game.startGame("smallTest.json", currentId);
        GameStatus currentGame = new GameStatus(false, currentId, currRoom.getDescription(),
                null, null, newState, allCommands);
        Command currentCommand = new Command("take", "Nezuko");
        game.runGameServer(currentGame, currentCommand);
        assertEquals(game.getPlayerItemsCollected().get(0), Arrays.asList("Nezuko"));
    }

    // Test if customized function ViewHistory will stay in current room and change description to the history
    @Test
    public void testRunGameServerViewHistory() throws Exception {
        layout = game.loadJson("smallTest.json");
        Map<String, Room> layoutMap = game.setMapLayout(layout);
        Room currRoom = layoutMap.get(layout.getStartingRoom());
        AdventureState newState = new AdventureState();
        Map<String, List<String>> allCommands = new HashMap<>();
        List<String> historyList = Arrays.asList("");
        allCommands.put("view history", historyList);
        List<String> goList = Arrays.asList("win", "practice");
        allCommands.put("go", goList);
        int currentId = 0;
        game.startGame("smallTest.json", currentId);
        GameStatus currentGame = new GameStatus(false, currentId, currRoom.getDescription(),
                null, null, newState, allCommands);
        currentGame = game.runGameServer(currentGame, new Command("go", "practice"));
        Command currentCommand = new Command("view history", "");
        GameStatus newStatus = game.runGameServer(currentGame, currentCommand);
        assertEquals(newStatus.getMessage(), "Your traversed history is: {Home->Practice}");
    }
}