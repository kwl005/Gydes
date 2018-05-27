package gydes.gyde.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Tour implements Parcelable {

    final static int NUM_STRING_ARGS = 6;
    final static int NUM_INT_ARGS = 2;
    final static int NUM_BOOL_ARGS = 1;

    final static int NAME_IND = 0;
    final static int LOC_IND = 1;
    final static int DUR_IND = 0;
    final static int STOPS_IND = 2;
    final static int WALK_IND = 0;
    final static int CAP_IND = 1;
    final static int TAGS_IND = 3;
    final static int tID_IND = 4;
    final static int cID_IND = 5;

    String name;
    String location;
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

    public Tour(String Name, String Location, int Duration, String Stops,
                boolean Walking, int Capacity, String Tags, String TourID, String CreatorID) {
        name = Name;
        location = Location;
        duration = Duration;
        stops = Stops;
        walking = Walking;
        capacity = Capacity;
        tags = Tags;
        tourID = TourID;
        creatorID = CreatorID;
    }

    public Tour(Parcel p) {
        String[] strings = new String[NUM_STRING_ARGS];
        int[] ints = new int[NUM_INT_ARGS];
        boolean[] bools = new boolean[NUM_BOOL_ARGS];

        p.readStringArray(strings);
        p.readIntArray(ints);
        p.readBooleanArray(bools);

        name = strings[NAME_IND];
        location = strings[LOC_IND];
        duration = ints[DUR_IND];
        stops = strings[STOPS_IND];
        walking = bools[WALK_IND];
        capacity = ints[CAP_IND];
        tags = strings[TAGS_IND];
        tourID = strings[tID_IND];
        creatorID = strings[cID_IND];
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Tour createFromParcel(Parcel in) {
            return new Tour(in);
        }

        public Tour[] newArray(int size) {
            return new Tour[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.name, this.location, this.stops, this.tags, this.tourID, this.creatorID});
        dest.writeIntArray(new int[] {this.duration, this.capacity});
        dest.writeBooleanArray(new boolean[] {this.walking});
    }

    public boolean checkAvailable(int day, int time) { return schedule[day][time]; }
    public void setAvailability(int day, int time, boolean avail) { schedule[day][time] = avail; }

    public String getName() { return name; }
    public void setName(String n) { name = n; }
    public String getLocation() { return location; }
    public void setLocation(String l) { location = l; }
    public int getDuration() { return duration; }
    public void setDuration(int d) { duration = d; }
    public String getStops() { return stops; }
    public void setStops(String s) { stops = s; }
    public boolean getWalking() { return walking; }
    public void setWalking(boolean w) { walking = w; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int c) { capacity = c; }
    public String getTags() { return tags; }
    public void setTags(String t) { tags = t; }
    public String getTourID() { return tourID; }
    public void setTourID(String tID) { tourID = tID; }
    public String getCreatorID() { return creatorID; }
    public void setCreatorID(String cID) { creatorID = cID; }

    @Override
    public int describeContents() {
        return 0;
    }
}
