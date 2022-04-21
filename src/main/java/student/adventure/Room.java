package student.adventure;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Room {
    private String name;
    private String description;
    private List<String> items = new ArrayList<>();
    private List<Direction> directions;

    public Room(String name, String description, List<String> items, List<Direction> directions) {
        this.name = name;
        this.description = description;
        this.items = items;
        this.directions = directions;
    }

    /**
     * This method is used to construct a direction map (key: move name, value: direction name).
     * @return A String to String map showing all directions.
     */
    public Map<String, String> constructDirectionMap() {
        Map<String, String> directionMaps = new HashMap<>();
        for (Direction direction:this.directions) {
            directionMaps.put(direction.getDirectionName().toLowerCase(), direction.getRoom());
        }
        return directionMaps;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * This method is used to return all the direction from the room in lower case.
     * @return A list of String represents all the possible direction.
     */
    public List<String> getDirections() {
        List<String> directions = new ArrayList<>();
        for (Direction direction:this.directions) {
            directions.add(direction.getDirectionName().toLowerCase());
        }
        return directions;
    }

    /**
     * This method is used to return all the items in the room in lower case.
     * @return A list of String represents all the possible items.
     */
    public List<String> getItems() {
        List<String> itemsList = new ArrayList<>();
        if (this.items == null) {
            return null;
        }
        for (String item:this.items) {
            itemsList.add(item.toLowerCase());
        }
        return itemsList;
    }

    /**
     * This method is used to remove the input item from the item list.
     * @param inputItem A string represents the item to take.
     */
    public void takeItem(String inputItem) {
        List<String> itemsList = new ArrayList<>();
        boolean alreadyDropped = false;
        for (String item:this.items) {
            if (item.equalsIgnoreCase(inputItem) && !alreadyDropped) {
                alreadyDropped = true;
            } else {
                itemsList.add(item);
            }
        }
        this.items = itemsList;
    }

    /**
     * This method is used to drop the input item back to the item list.
     * @param inputItem A string represents the item to drop.
     */
    public void dropItem(String inputItem) {
        List<String> itemsList = new ArrayList<>();
        for (String item:this.items) {
            itemsList.add(item);
        }
        itemsList.add(inputItem);
        this.items = itemsList;
    }
}