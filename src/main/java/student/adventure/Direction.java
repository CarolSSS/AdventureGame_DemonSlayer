package student.adventure;

public class Direction {
    private String directionName = null;
    // the String name of the room this direction points to
    private String room = null;

    public Direction(String directionInput, String roomInput) {
        this.directionName = directionInput;
        this.room = roomInput;
    }

    public String getDirectionName() {
        return directionName;
    }

    public String getRoom() {
        return room;
    }
}
