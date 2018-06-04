package gydes.gyde.controllers;

import android.os.Bundle;
import android.app.Activity;

import gydes.gyde.R;

public class MyBookings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        //query database for all bookings under currentUserRef's traveler or guide, depending on isGuide
        //TODO figure out best way to display bookings logically to user
    }

}
