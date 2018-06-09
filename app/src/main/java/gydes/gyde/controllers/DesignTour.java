package gydes.gyde.controllers;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.RadioGroup;
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
                EditText nameBox = findViewById(R.id.name_box);
                EditText locBox = findViewById(R.id.location_box);
                EditText priceBox = findViewById(R.id.price_box);
                EditText stopsBox = findViewById(R.id.stops_box);
                EditText durBox = findViewById(R.id.duration_box);
                RadioGroup transportButtons = findViewById(R.id.transport_buttons);
                EditText capBox = findViewById(R.id.capacity_box);
                EditText tagsBox = findViewById(R.id.tags_box);

                if(nameBox.getText().toString().trim().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DesignTour.this);
                    builder.setMessage(R.string.name_prompt);
                    builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    builder.create().show();
                    return;
                }
                if(locBox.getText().toString().trim().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DesignTour.this);
                    builder.setMessage(R.string.location_prompt);
                    builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    builder.create().show();
                    return;
                }
                if(priceBox.getText().toString().trim().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DesignTour.this);
                    builder.setMessage(R.string.price_prompt);
                    builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    builder.create().show();
                    return;
                }
                if(durBox.getText().toString().trim().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DesignTour.this);
                    builder.setMessage(R.string.duration_prompt);
                    builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    builder.create().show();
                    return;
                }
                if(transportButtons.getCheckedRadioButtonId() == -1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DesignTour.this);
                    builder.setMessage(R.string.transport_prompt);
                    builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    builder.create().show();
                    return;
                }

                final DatabaseReference toursRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_tours_path));

                String name = nameBox.getText().toString().trim();
                String location = SearchResults.toCamelCase(locBox.getText().toString().trim());
                int price = Integer.parseInt(priceBox.getText().toString());
                String stops = stopsBox.getText().toString().trim();
                int duration = Integer.parseInt(durBox.getText().toString());
                boolean walking = transportButtons.getCheckedRadioButtonId() == R.id.walk_button;
                int capacity = Integer.parseInt(capBox.getText().toString());
                String tags = tagsBox.getText().toString().trim();
                String tourID = toursRef.push().getKey();
                String creatorID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Tour t = new Tour(name, location, price, duration, stops, walking, capacity, tags, tourID, creatorID);
                toursRef.child(location).child(tourID).setValue(t);

                Login.currentUserRef.child(getString(R.string.firebase_guide_path)).child(getString(R.string.firebase_tourIDs_path)).child(tourID).setValue(tourID);
                Login.currentUserRef.child(getString(R.string.firebase_guide_path)).child(getString(R.string.firebase_tourIDs_path)).child(tourID).child(getString(R.string.firebase_loc_path)).setValue(location);
                setResult(RESULT_OK);
                finish();
            }
        });

        Button cancelButton = findViewById(R.id.design_tour_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

}
