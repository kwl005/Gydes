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

import gydes.gyde.R;

public class MyTours extends AppCompatActivity {

    private static final int RC_TOUR_MADE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tours);

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
                //update list of tours
            }
            else {
                Log.d("MyTours","MyTours.java: tour creation failed. Code: " + resultCode);
            }
        }
    }
}
