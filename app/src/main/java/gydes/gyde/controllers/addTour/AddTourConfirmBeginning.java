package gydes.gyde.controllers.addTour;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.mikepenz.materialdrawer.Drawer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import gydes.gyde.R;
import gydes.gyde.controllers.NavigationDrawerBuilder;

/**
 * Created by kelvinlui1 on 5/30/18.
 */

public class AddTourConfirmBeginning extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tour_confirm_beginning);

        // Setup navigation drawer, action bar and status bar
        final Drawer result = NavigationDrawerBuilder.build(this, savedInstanceState);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.openDrawer();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.gydeYellow));

        final Bundle bundle = getIntent().getExtras();
        // Get location from lat and lng using Map API
        final Double longitude = bundle.getDouble("longitude");
        final Double latitude = bundle.getDouble("latitude");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch(IOException e) {
            e.printStackTrace();
        }
        final String location = addresses == null ? "Unknown" : addresses.get(0).getLocality();

        // Setup text view
        textView = (TextView) findViewById(R.id.text_view);
        if(addresses != null) {
            textView.setText("Do you want to create a tour starting at " + addresses.get(0).getAddressLine(0) + "?");
        }

        // Setup cancel button
        FloatingActionButton cancelButton = (FloatingActionButton) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Setup continue button
        FloatingActionButton continueButton = (FloatingActionButton) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AddTourConfirmName.class);
                bundle.putString("location", location);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
