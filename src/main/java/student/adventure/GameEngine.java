package student.adventure;

import java.io.File;
import org.apache.commons.io.FileUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import student.server.AdventureState;
import student.server.Command;
import student.server.GameStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GameEngine {
    private final int GO_PREFIX = 3;
    private final int TAKE_PREFIX = 5;
    // Only setting runLocalFlag to true will result in print out the comments in terminal
    private boolean runLocalFlag = false;
    private Map<String, Room> roomsMap = new HashMap<>();
    // Record the items picked by the player in terminal
    private List<String> itemsCollected;
    // Record the items picked by the multiple players by ID in server
    private Map<Integer, List<String>> playerItemsCollected = new HashMap<>();
    // Record multiple players' position by ID in server
    private Map<Integer, Room> playerPosition = new HashMap<>();
    // Record history traversal of each player
    private Map<Integer, List<String>> playerTraversalHistory = new HashMap<>();
    private List<String> traversalHistory = new ArrayList<>();
    private final String imageURL  = "https://www.ft.com/__origami/service/image/v2/images/raw/https%253A%252F%252Fs3-ap-" +
            "northeast-1.amazonaws.com%252Fpsh-ex-ftnikkei-3937bb4%252Fimages%252F7%252F3%252F4%252F1%252F30221" +
            "437-1-eng-GB%252F%25E3%2580%258C%25E9%25AC%25BC%25E6%25BB%2585%25E3%2581%25AE%25E5%2588%2583%25E3%" +
            "2580%258D%25EF%25BC%2593%25E6%2597%25A5%25E3%2581%25A7%25E2%2596%25A0%25E5%2584%2584%25E5%2586%2586" +
            "20201027010904_Data.jpg?width=700&fit=cover&gravity=faces&dpr=2&quality=medium&source=nar-cms";

    public GameEngine() {
        itemsCollected = new ArrayList<>();
    }

    /**
     * This method is used to load json file into the class objects.
     * @param jsonName A string represents name of the input json file
     * @return A Layout variable parse from json showing the map of this adventure game.
     * @throws IOException throws exception for reading json file
     */
    public Layout loadJson(String jsonName) throws IOException {
        Gson gson = new GsonBuilder().create();
        File file = new File(jsonName);
        String content = FileUtils.readFileToString(file);
        Layout layout = gson.fromJson(content, Layout.class);
        // Check null and invalid map including missing start / ending / room
        gameValidity(layout);
        setMapLayout(layout);
        return layout;
    }

    /**
     * This method is used to convert layout game map into a map of rooms with key(String: room name).
     * @param layoutInput A Layout variable parses from json showing the map of this adventure game.
     * @return The map with room name as key and room object as value.
     */
    public Map<String, Room> setMapLayout(Layout layoutInput) {
        Room[] roomArray = layoutInput.getRooms();
        this.roomsMap = new HashMap<>();
        for (Room room:roomArray) {
            this.roomsMap.put(room.getName(), room);
        }
        return this.roomsMap;
    }

    /**
     * This method is called by MyAdventureService for starting a game of a player.
     * @param inputFile A string represents name of the input json file
     * @param currentId The playerId who starts this game
     * @return Game status after starting
     * @throws Exception Throw adventure exceptions
     */
    public GameStatus startGame(String inputFile, int currentId) throws Exception {
        Layout layout = loadJson(inputFile);
        Room startRoom = roomsMap.get(layout.getStartingRoom());
        AdventureState currentState = new AdventureState();
        GameStatus currentGame = new GameStatus(false, currentId, startRoom.getDescription(),
                imageURL, null, currentState, getCommandOptions(startRoom, currentId));
        playerPosition.put(currentId, startRoom);
        List<String> traversed = new ArrayList<>();
        traversed.add(startRoom.getName());
        playerTraversalHistory.put(currentId, traversed);
        return currentGame;
    }

    /**
     * This method is used to create map for all the commands of current status.
     * @param currentRoom The room where the player stay
     * @param playerID The playerId who send this command
     * @return Map of all possible commands
     */
    private Map<String, List<String>> getCommandOptions(Room currentRoom, int playerID) {
        Map<String, List<String>> allCommands = new HashMap<>();
        List<String> goList = currentRoom.getDirections();
        allCommands.put("go", goList);
        List<String> itemList = currentRoom.getItems();
        allCommands.put("take", itemList);
        List<String> examineList = new ArrayList<>();
        examineList.add("");
        allCommands.put("examine", examineList);
        allCommands.put("view history", examineList);
        allCommands.put("drop", playerItemsCollected.get(playerID));
        return allCommands;
    }

    /**
     * This method is used to run the game in server.
     * @param currentGame A string represents name of the input json file
     * @param currentCommand The command sent from service by player
     * @return The game status after current command
     */
    public GameStatus runGameServer(GameStatus currentGame, Command currentCommand) {
        String convertedCommand;
        int currentId = currentGame.getId();
        Room currentRoom = playerPosition.get(currentGame.getId());
        AdventureState newState = new AdventureState();
        if (currentCommand.getCommandName().equals("examine")) {
            convertedCommand = "examine";
        } else {
            // Convert the input command from web to the one that can be accepted by functions for terminal.
            convertedCommand = currentCommand.getCommandName() + " " + currentCommand.getCommandValue();
        }
        if (currentCommand.getCommandName().equals("go")) {
            Room newRoom = goGame(currentRoom.getName(), convertedCommand);
            GameStatus newStatus = new GameStatus(false, currentId, newRoom.getDescription(),
                    imageURL, null, newState, getCommandOptions(newRoom, currentId));
            playerPosition.put(currentId, newRoom);
            // Update current room to the player history
            List<String> traversed = playerTraversalHistory.get(currentId);
            traversed.add(newRoom.getName());
            playerTraversalHistory.put(currentId, traversed);
            return newStatus;
        } else if (currentCommand.getCommandName().equals("view history")) {
            // Customized function. Print all traversed history
            String description =  playerTraversalHistory.get(currentId).stream().map(n -> String.valueOf(n))
                    .collect(Collectors.joining("->", "Your traversed history is: {", "}"));
            return new GameStatus(false, currentId, description,
                    null, null, newState, getCommandOptions(currentRoom, currentId));
        } else if (currentCommand.getCommandName().equals("take") || currentCommand.getCommandName().equals("drop")) {
            // Run wrapper of take/drop function to get update on player item list.
            checkOperateItemForWeb(currentRoom, convertedCommand, currentId);
            playerPosition.put(currentGame.getId(), currentRoom);
            return new GameStatus(false, currentId, currentRoom.getDescription(),
                    imageURL, null, newState, getCommandOptions(currentRoom, currentId));
        } else {
            // Case for examine, print current room information even has view history as last stage.
            return new GameStatus(false, currentId, currentRoom.getDescription(),
                    imageURL, null, newState, getCommandOptions(currentRoom, currentId));
        }
    }

    /**
     * This method is used to run the game in terminal.
     * @param inputFile A string represents name of the input json file.
     * @throws IOException
     */
    public void runGameTerminal(String inputFile) throws IOException {
        // Entering this function means this game will run locally
        runLocalFlag = true;
        Layout layout = loadJson(inputFile);
        String endingRoomName = layout.getEndingRoom();
        Room startRoom = roomsMap.get(layout.getStartingRoom());
        Room currRoom = startRoom;
        traversalHistory.add(currRoom.getName());

        System.out.println("Welcome to the adventure in Slayer. Wish you good luck!");
        printSpecification(currRoom, endingRoomName);
        boolean quitFlag = false;
        Scanner inputString = new Scanner(System.in);
        while (!(currRoom.getName()).equalsIgnoreCase(endingRoomName) && !quitFlag) {
            System.out.print("> ");
            // Read and operate user input in command line into userDirection
            String userDirection = inputString.nextLine().trim();
            // Check whether the command is exit, examine, or pick/take item. If not, go to process go command.
            if (checkExit(userDirection)) {
                quitFlag = true;
            } else if (checkExamine(userDirection)) {
                printSpecification(currRoom, endingRoomName);
            } else if (checkOperateItem(currRoom, userDirection)) {
                continue;
            } else if (checkHistory(userDirection)) {
                System.out.println(traversalHistory.stream().map(n -> String.valueOf(n))
                        .collect(Collectors.joining("->", "Your traversed history is: {", "}")));
            } else {
                currRoom = goGame(currRoom.getName(), userDirection);
                traversalHistory.add(currRoom.getName());
                printSpecification(currRoom, endingRoomName);
            }
        }
        checkIfExit(quitFlag);
    }

    /**
     * This method is used to run the game based on the map, current room name and the user direction.
     * @param roomInput A string represents the name of current room.
     * @param userDirection The input of user in command line.
     * @return A room variable represents the name of the room after running the player's command.
     */
    public Room goGame(String roomInput, String userDirection) {
        Room currRoom = roomsMap.get(roomInput);
        // GO_PREFIX is the length of string "go ", so a valid command should have length larger than that
        if (userDirection.length() <= GO_PREFIX) {
            if (runLocalFlag) {
                System.out.println("I don't understand \"" + userDirection + "\"!");
            }
        } else {
            if (userDirection.toLowerCase().startsWith("go ")) {
                // The direction word without the prefix "go " and ignore starting space and case
                String cleanedUserDirection = userDirection.substring(3).trim().toLowerCase();
                // currDirections is the map of directions in current room with lower case room name as the key
                Map<String, String> currDirections = currRoom.constructDirectionMap();
                String newRoomKey = currDirections.get(cleanedUserDirection);
                if (newRoomKey == null) {
                    if (runLocalFlag) {
                        System.out.println("I can't go \"" + userDirection + "\" !");
                    }
                    return currRoom;
                }
                currRoom = roomsMap.get(newRoomKey);
            } else {
                if (runLocalFlag) {
                    System.out.println("I don't understand \"" + userDirection + "\".");
                }
            }
        }
        return currRoom;
    }

    /**
     * This method checks whether the userInput string is an exit command or not.
     * @param userInput The input of user in command line.
     * @return A boolean variable shows whether current command is an exit command.
     */
    public boolean checkExit(String userInput) {
        boolean exitFlag = userInput.equalsIgnoreCase("exit")
                || userInput.equalsIgnoreCase("quit");
        return exitFlag;
    }

    /**
     * This method checks whether the userInput string is an examine command or not.
     * @param userInput The input of user in command line.
     * @return A boolean variable shows whether current command is an examine command.
     */
    public boolean checkExamine(String userInput) {
        boolean examineFlag = userInput.trim().equalsIgnoreCase("examine");
        return examineFlag;
    }

    /**
     * This method checks whether the userInput string is a view history command or not.
     * @param userInput The input of user in command line.
     * @return A boolean variable shows whether current command is a view history command.
     */
    public boolean checkHistory(String userInput) {
        boolean historyFlag = userInput.equalsIgnoreCase("view history");
        return historyFlag;
    }

    /**
     * This method is a wrapper of checkOperateItem for dealing with multiple player
     * @param currRoom A Room variable represent current room
     * @param userInput The input of user in command line
     * @param playerID ID of the player who sends this command
     */
    public void checkOperateItemForWeb(Room currRoom, String userInput, int playerID) {
        boolean pickSuccessful = checkOperateItem(currRoom, userInput);
        if (!pickSuccessful) {
            return;
        }
        String item = userInput.substring(TAKE_PREFIX);
        if (userInput.toLowerCase().startsWith("take ")) {
            if (!playerItemsCollected.containsKey(playerID)) {
                List<String> items = new ArrayList<>();
                playerItemsCollected.put(playerID, items);
            }
            List<String> PlayerCollected = playerItemsCollected.get(playerID);
            PlayerCollected.add(item);
            playerItemsCollected.put(playerID, PlayerCollected);
        } else {
            List<String> PlayerCollected = playerItemsCollected.get(playerID);
            PlayerCollected.remove(item);
            playerItemsCollected.put(playerID, PlayerCollected);
        }
    }

    /**
     * This method checks whether the userInput string is an item operation command or not.
     * @param currRoom A Room variable represent current room.
     * @param userInput The input of user in command line.
     * @return A boolean variable shows whether current command is a take/drop item command.
     */
    public boolean checkOperateItem(Room currRoom, String userInput) {
        boolean findItem = true;
        //
        if (userInput.length() <= TAKE_PREFIX) {
            findItem = false;
        } else {
            String itemString = userInput.substring(TAKE_PREFIX).trim();
            if (userInput.toLowerCase().startsWith("take ")) {
                // Take case, check if current room contains the item
                // allItems is the list of item in current room with all lower case
                List<String> allItems = currRoom.getItems();
                if (allItems.contains(itemString.toLowerCase().trim())) {
                    currRoom.takeItem(itemString);
                    itemsCollected.add(itemString);
                } else {
                    if (runLocalFlag) {
                        System.out.println("There is no item \"" + itemString + "\" in the room.");
                    }
                }
            } else if (userInput.toLowerCase().startsWith("drop ")) {
                // Drop case: need to check if current player hold the item
                if (itemsCollected.contains(itemString.toLowerCase())) {
                    currRoom.dropItem(itemString.toLowerCase());
                    itemsCollected.remove(itemString);
                } else {
                    if (runLocalFlag) {
                        System.out.println("You don't have \"" + itemString + "\".");
                    }
                }
            } else {
                findItem = false;
            }
        }
        return findItem;
    }

    /**
     * A helper function uses to check if the json file parsed is valid.
     * @param layoutInput A Layout variable parse from json showing the map of this adventure game.
     * @throws IllegalArgumentException
     */
    private void gameValidity(Layout layoutInput) throws IllegalArgumentException {
        String[] roomNames = layoutInput.getRoomsName();
        if (layoutInput.getRooms() == null || layoutInput.getRooms().length == 0) {
            throw new IllegalArgumentException("No room available");
        }
        if (layoutInput.getStartingRoom() == null) {
            throw new IllegalArgumentException("Starting room is null");
        } else {
            if (!Arrays.asList(roomNames).contains(layoutInput.getStartingRoom())) {
                throw new IllegalArgumentException("Starting room does not exist");
            }
        }
        if (layoutInput.getEndingRoom() == null) {
            throw new IllegalArgumentException("Ending room is null");
        } else {
            if (!Arrays.asList(roomNames).contains(layoutInput.getEndingRoom())) {
                throw new IllegalArgumentException("Ending room does not exist");
            }
        }
    }

    /**
     * This method is used to check whether the player win or exit.
     * @param quitFlag A boolean variable shows whether current game is exited.
     */
    private void checkIfExit(boolean quitFlag) {
        if (quitFlag) {
            System.out.println("You successfully quit the game.");
        } else {
            System.out.println("Congratulation! You win the game!");
        }
    }

    /**
     * This method is used to print the list in current room.
     * @param inputList The String list to print
     */
    private void printList(List<String> inputList) {
        if (inputList == null || inputList.size() == 0) {
            System.out.println("No item in this room");
        } else {
            for (int i = 0; i < inputList.size(); i++) {
                // No comma for first direction string
                if (i == 0) {
                    System.out.print(inputList.get(i));
                } else if (i == inputList.size() - 1){
                    // Only the last item has an "or" before comma
                    System.out.print(", or ");
                    System.out.print(inputList.get(i));
                } else {
                    System.out.print(", ");
                    System.out.print(inputList.get(i));
                }
            }
            System.out.println();
        }
    }

    /**
     * This method is used to print the information of current room.
     * @param currRoom A Room variable represents current room.
     * @param endingRoomName A String variable represents the name of winning room.
     */
    private void printSpecification(Room currRoom, String endingRoomName) {
        if (endingRoomName.equalsIgnoreCase(currRoom.getName())) {
            // if currRoom is the winning state, no needs for direction and separate line.
            System.out.println(currRoom.getDescription());
        } else {
            System.out.println(currRoom.getDescription());
            System.out.print("From here, you can go: ");
            printList(currRoom.getDirections());
            System.out.print("Items visible: ");
            printList(currRoom.getItems());
        }
    }

    public Map<Integer, List<String>> getPlayerItemsCollected() {
        return playerItemsCollected;
    }

    public void setRunLocalFlag(boolean runLocalFlag) {
        this.runLocalFlag = runLocalFlag;
    }
}
