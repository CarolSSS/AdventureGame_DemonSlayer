package student.adventure;

public class Layout {
    // the String name of the room the player starts in
    private String startingRoom = null;
    // the String name of the room the player must reach to win
    private String endingRoom = null;
    private Room[] rooms = {};

    public Layout(String inputStartingRoom, String inputEndingRoom, Room[] inputRooms) {
        this.startingRoom = inputStartingRoom;
        this.endingRoom = inputEndingRoom;
        this.rooms = inputRooms;
    }

    public String getStartingRoom() {
        return startingRoom;
    }

    public String getEndingRoom() {
        return endingRoom;
    }

    public Room[] getRooms() {
        return rooms;
    }

    public String[] getRoomsName() {
        if (this.rooms == null) {
            return new String[0];
        }
        String[] roomNames = new String[rooms.length];
        for (int i = 0; i < rooms.length; i++) {
            roomNames[i] = rooms[i].getName();
        }
        return roomNames;
    }
}
