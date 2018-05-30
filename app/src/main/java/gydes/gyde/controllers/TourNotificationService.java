package gydes.gyde.controllers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by rix on 5/29/18.
 */

public class TourNotificationService extends Service {

    private static final String USER_PATH = "users";
    private static final String TRAVELLER_PATH = "traveler";
    private static final String BOOKING_PATH = "bookings";

    private FirebaseUser currentUser;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // TODO: signup for time based notification
        attachCheckTour();
    }

    private void attachCheckTour() {
        DatabaseReference bookingRef = getCurrentRef();
        bookingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // TODO
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO
            }
        });
    }

    private DatabaseReference getCurrentRef() {
        String bookingPath = USER_PATH + "/" + currentUser.getUid() + TRAVELLER_PATH + "/" + BOOKING_PATH;
        bookingPath += getDayOfWeek();
        bookingPath += getTimeOfDay();
        // Only look for current hour
        DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference(bookingPath);
        return bookingRef;
    }

    private String getDayOfWeek() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d);
        dayOfTheWeek = dayOfTheWeek.toLowerCase();
        return dayOfTheWeek;
    }

    private String getTimeOfDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        Date d = new Date();
        String timeOfDay = sdf.format(d) + ":00";
        return timeOfDay;
    }
}
