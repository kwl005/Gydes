package gydes.gyde.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import gydes.gyde.R;
import gydes.gyde.models.BookingDetailsDialogFragment;

public class TourMode extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_mode);

        ((Button)findViewById(R.id.end_tour_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Login.currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot user) {
                        if(user.hasChild(getString(R.string.firebase_activetour_path))) {
                            String thisID = (String)user.child(getString(R.string.firebase_activetour_path)).child(getString(R.string.firebase_thisID_path)).getValue();
                            String otherID = (String)user.child(getString(R.string.firebase_activetour_path)).child(getString(R.string.firebase_otherID_path)).getValue();
                            String thisSide = (String)user.child(getString(R.string.firebase_activetour_path)).child(getString(R.string.firebase_thisside_path)).getValue();
                            String otherSide =(String)user.child(getString(R.string.firebase_activetour_path)).child(getString(R.string.firebase_otherside_path)).getValue();
                            int hour = (int)user.child(getString(R.string.firebase_activetour_path)).child(getString(R.string.firebase_hour_path)).getValue();
                            int day = (int)user.child(getString(R.string.firebase_activetour_path)).child(getString(R.string.firebase_day_path)).getValue();
                            BookingDetailsDialogFragment.deleteBooking(null, thisID, otherID, thisSide, otherSide, hour, day);
                            Toast toast = Toast.makeText(TourMode.this, "Tour finished. You have been charged for the tour.", Toast.LENGTH_LONG);
                            toast.show();
                            Login.currentUserRef.child(getString(R.string.firebase_activetour_path)).removeValue();
                            startActivity(new Intent(TourMode.this, HomeActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("TourMode.java", "error accessing database: " + databaseError.getCode());
                    }
                });
            }
        });
    }

}
