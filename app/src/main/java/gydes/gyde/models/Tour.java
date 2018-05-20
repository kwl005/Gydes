package gydes.gyde.models;

public class Tour {
    String name;
    //location data
    private int duration; //hours
    String stops;
    private boolean walking;
    private int capacity;
    String tags;
    private String tourID;
    private String creatorID;
    private boolean[][] schedule;

    public Tour() {
        schedule = new boolean[7][24];
    }

    public Tour(String Name, int Duration, String Stops,
                boolean Walking, int Capacity, String Tags, String TourID, String CreatorID) {
        name = Name;
        duration = Duration;
        stops = Stops;
        walking = Walking;
        capacity = Capacity;
        tags = Tags;
        tourID = TourID;
        creatorID = CreatorID;
        schedule = new boolean[7][24];
    }

    public boolean checkAvailable(int day, int time) { return schedule[day][time]; }
    public void setAvailability(int day, int time, boolean avail) { schedule[day][time] = avail; }

    public String getName() { return name; }
    public void setName(String n) { name = n; }
    public int getDuration() { return duration; }
    public void setDuration(int d) { duration = d; }
    public boolean getWalking() { return walking; }
    public void setWalking(boolean w) { walking = w; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int c) { capacity = c; }
    public String getTourID() { return tourID; }
    public void setTourID(String tID) { tourID = tID; }
    public String getCreatorID() { return creatorID; }
    public void setCreatorID(String cID) { creatorID = cID; }
}
