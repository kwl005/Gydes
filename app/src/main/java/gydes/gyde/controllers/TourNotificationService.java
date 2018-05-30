package gydes.gyde.controllers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

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

import gydes.gyde.models.Tour;

/**
 * Created by rix on 5/29/18.
 */

public class TourNotificationService extends Service {

    private static final String USER_PATH = "users";
    private static final String TRAVELLER_PATH = "traveler";
    private static final String BOOKING_PATH = "bookings";
    private static final String TAG = TourNotificationService.class.getSimpleName();

    private FirebaseUser currentUser;
    private DatabaseReference bookingRef;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "TourNotification onCreate");
        // TODO: signup for time based notification
        attachCheckTour();
    }

    private void attachCheckTour() {
        bookingRef = getCurrentRef();
        bookingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Tour currentTour = objToTour(dataSnapshot.child("tour"));
                if (currentTour.getTourID() == null)
                    return;
                Log.d(TAG, currentTour.getTourID());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO
            }
        });
    }

    private Tour objToTour(DataSnapshot obj) {
        Tour t = new Tour();
        if (obj.child("tourID").getValue() == null)
            return t;

        t.setName((String) obj.child("name").getValue());
        t.setCapacity(((Long) obj.child("capacity").getValue()).intValue());
        t.setCreatorID((String) obj.child("creatorID").getValue());
        t.setDuration(((Long) obj.child("duration").getValue()).intValue());
        t.setLocation((String) obj.child("location").getValue());
        t.setStops((String) obj.child("stops").getValue());
        t.setTags((String) obj.child("tags").getValue());
        t.setTourID((String) obj.child("tourID").getValue());
        t.setWalking((boolean) obj.child("walking").getValue());
        return t;
    }

    private DatabaseReference getCurrentRef() {
        String bookingPath = USER_PATH + "/" + currentUser.getUid() + "/" + TRAVELLER_PATH + "/" + BOOKING_PATH;
        bookingPath += "/" + getDayOfWeek();
        bookingPath += "/" + getTimeOfDay();
        Log.d(TAG, bookingPath);
        // Only look for current hour
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(bookingPath);
        return ref;
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
