package gydes.gyde.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import gydes.gyde.R;
import gydes.gyde.models.Tour;

public class DesignTour extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design_tour);

        Button saveButton = findViewById(R.id.design_tour_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final DatabaseReference toursRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_tours_path));

                String name = ((EditText) findViewById(R.id.name_box)).getText().toString();
                String location = "San Diego";
                String stops = ((EditText) findViewById(R.id.stops_box)).getText().toString();
                int duration = Integer.parseInt(((EditText) findViewById(R.id.duration_box)).getText().toString());
                boolean walking = !((ToggleButton) findViewById(R.id.transport_button)).isChecked();
                int capacity = Integer.parseInt(((EditText) findViewById(R.id.capacity_box)).getText().toString());
                String tags = ((EditText) findViewById(R.id.tags_box)).getText().toString();
                String tourID = toursRef.push().getKey();
                String creatorID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Tour t = new Tour(name, location, duration, stops, walking, capacity, tags, tourID, creatorID);
                toursRef.child(location).child(tourID).setValue(t);

                Login.currentUserRef.child("guide").child("tourIDs").child(tourID).setValue(tourID);
                finish();
            }
        });

        Button cancelButton = findViewById(R.id.design_tour_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

}
