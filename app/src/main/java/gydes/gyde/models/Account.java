package gydes.gyde.models;

import java.util.ArrayList;

public class Account {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePic;
    private String bio;
    private Traveler t;
    private Guide g;

    public Account(String uid, String fName, String lName, String eMail, String pPic, String b) {
        id = uid;
        firstName = fName;
        lastName = lName;
        email = eMail;
        profilePic = pPic;
        bio = b;
        t = new Traveler();
        g = new Guide();
    }

    public void tAddNewTourID(String id) { t.addNewTourID(id); }
    public String[] tGetTourIDs() { return t.getTourIDs(); }
    public float tCalcNewRating(int rate) { return t.calcNewRating(rate); }
    public float tGetAvgRating() { return t.getAvgRating(); }
    public boolean tBookTour(Tour tour, int day, int time, String gID) { return t.bookTour(tour, day, time, this.id, gID); }
    public void tCancelBooking(int day, int time) { t.cancelBooking(day, time); }
    public Booking[] tGetDayBookings(int day) { return t.getDayBookings(day); }
    public Booking tGetBooking(int day, int time) { return t.getBooking(day, time); }

    public void gAddNewTourID(String id) { g.addNewTourID(id); }
    public String[] gGetTourIDs() { return g.getTourIDs(); }
    public float gCalcNewRating(int rate) { return g.calcNewRating(rate); }
    public float gGetAvgRating() { return g.getAvgRating(); }
    public void gSetAvailability(int day, int time, boolean avail) { g.setAvailability(day, time, avail); }
    public boolean gCheckAvailable(int day, int time) { return g.checkAvailable(day, time); }
    public boolean gBookTour(Tour tour , int day, int time, String tID) { return g.bookTour(tour, day, time, tID, this.id); }
    public void gCancelBooking(int day, int time) { g.cancelBooking(day, time); }
    public Booking[] gGetDayBookings(int day) { return g.getDayBookings(day); }
    public Booking gGetBooking(int day, int time) { return g.getBooking(day, time); }

    public String getFirstName() { return firstName; }
    public String getLastName() {return lastName; }
    public void setFirstName(String fName) { firstName = fName; }
    public void setLastname(String lName) { lastName = lName; }
    public String getEmail() { return email; }
    public void setEmail(String mail) { email = mail; }
    public String getPic() { return profilePic; }
    public void setPic(String pic) { profilePic = pic; }
    public String getBio() { return bio; }
    public void setBio(String b) { bio = b; }

    private class Traveler {
        private ArrayList<String> tourIDs;
        //Stripe
        private float avgRating;
        private int numRatings;
        private Book tourBook;

        public Traveler() {
            tourIDs = new ArrayList<String>();
            avgRating = 0;
            numRatings = 0;
            tourBook = new Book();
        }

        public void addNewTourID(String id) { tourIDs.add(id); }
        public String[] getTourIDs() { return (String[])tourIDs.toArray(); }
        public float calcNewRating(int rate) {
            avgRating = ((avgRating * numRatings) + rate) / numRatings + 1;
            numRatings++;
            return avgRating;
        }
        public float getAvgRating() { return avgRating; }

        public boolean bookTour(Tour tour, int day, int time, String tID, String gID) {
            return tourBook.bookATour(tour, day, time, tID, gID);
        }
        public void cancelBooking(int day, int time) { tourBook.cancelTour(day, time); }
        public Booking[] getDayBookings(int day) { return tourBook.getDayBookings(day); }
        public Booking getBooking(int day, int time) { return tourBook.getBooking(day, time); }
    }

    private class Guide {
        private ArrayList<String> tourIDs;
        //Stripe
        private float avgRating;
        private int numRatings;
        private boolean[][] schedule;
        private Book tourBook;

        public Guide() {
            tourIDs = new ArrayList<String>();
            avgRating = 0;
            numRatings = 0;
            schedule = new boolean[7][24];
            tourBook = new Book();
        }

        public void addNewTourID(String id) { tourIDs.add(id); }
        public String[] getTourIDs() { return (String[])tourIDs.toArray(); }
        public float calcNewRating(int rate) {
            avgRating = ((avgRating * numRatings) + rate) / numRatings + 1;
            numRatings++;
            return avgRating;
        }
        public float getAvgRating() { return avgRating; }
        public void setAvailability(int day, int time, boolean avail) { schedule[day][time] = avail; }
        public boolean checkAvailable(int day, int time) { return schedule[day][time]; }
        public boolean bookTour(Tour tour, int day, int time, String tID, String gID) {
            return tourBook.bookATour(tour, day, time, tID, gID);
        }
        public void cancelBooking(int day, int time) { tourBook.cancelTour(day, time); }
        public Booking[] getDayBookings(int day) { return tourBook.getDayBookings(day); }
        public Booking getBooking(int day, int time) { return tourBook.getBooking(day, time); }
    }

    private class Book {
        private Booking[][] bookedTours;

        public Book() {
            bookedTours = new Booking[7][24];
        }

        public boolean bookATour(Tour tour, int day, int time, String tID, String gID) {
            if(bookedTours[day][time] != null) return false;
            bookedTours[day][time] = new Booking(tour, tID, gID);
            return true;
        }
        public void cancelTour(int day, int time) { bookedTours[day][time] = null; }

        public Booking[] getDayBookings(int day) { return bookedTours[day]; }
        public Booking getBooking(int day, int time) { return bookedTours[day][time]; }
    }

    private class Booking {
        private Tour bookedTour;
        private String travelerID;
        private String guideID;

        public Booking(Tour tour, String tID, String gID) {
            bookedTour = tour;
            travelerID = tID;
            guideID = gID;
        }

        public Tour getTour() { return bookedTour; }
        public String getTravID() { return travelerID; }
        public String getGuideID() { return guideID; }
    }
}


