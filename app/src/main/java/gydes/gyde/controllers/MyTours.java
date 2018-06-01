package gydes.gyde.controllers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import gydes.gyde.R;
import gydes.gyde.models.Tour;
import gydes.gyde.models.TourListAdapter;

public class MyTours extends AppCompatActivity {

    public static final int MY_TOURS_BUTTON_OPT = 2;
    private static final int RC_TOUR_MADE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tours);

        updateMyTours();

        Button designTourButton = findViewById(R.id.design_tour_button);
        designTourButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(MyTours.this, DesignTour.class), RC_TOUR_MADE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_TOUR_MADE) {
            if(resultCode == RESULT_OK) {
                updateMyTours();
            }
            else {
                Log.d("MyTours","MyTours.java: tour creation failed. Code: " + resultCode);
            }
        }
    }

    void updateMyTours() {
        final ArrayList<Tour> tours = new ArrayList<Tour>();

        final TourListAdapter adapter = new TourListAdapter(this, R.layout.tour_list_item, tours, MY_TOURS_BUTTON_OPT);
        ListView listView = findViewById(android.R.id.list);
        listView.setAdapter(null);
        listView.setAdapter(adapter);

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot root) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DataSnapshot idList = root.child(getString(R.string.firebase_users_path)).child(uid).child(getString(R.string.firebase_guide_path)).child("tourIDs");

                for (DataSnapshot child : idList.getChildren()) {

                    final String id = child.getKey();
                    final String location = (String)child.child("location").getValue();
                    DataSnapshot locTourList = root.child(getString(R.string.firebase_tours_path)).child(location);

                    if(locTourList.hasChild(id)) {
                        tours.add(locTourList.child(id).getValue(Tour.class));
                    }
                    else {
                        Log.d("MyTours.java", "Error: Guide's tour id not found under tours/location");
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MyTours.java", "Error querying root: " + databaseError.getCode());
            }
        });
    }
}
