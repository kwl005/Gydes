package gydes.gyde.controllers;

import android.Manifest;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikepenz.materialdrawer.Drawer;
import java.util.HashMap;

import gydes.gyde.R;
import gydes.gyde.controllers.addTour.AddTourConfirmBeginning;
import gydes.gyde.models.User;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;
    private FusedLocationProviderClient locationProviderClient;
    private LocationManager locationManager;
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String TAG = HomeActivity.class.getSimpleName();
    private FloatingActionButton tourPinButton;
    private ImageView imageView;
    private FrameLayout homeFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toast.makeText(this, "Welcome! " + User.INSTANCE.getDisplayName(), Toast.LENGTH_SHORT).show();

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

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchBar = (SearchView)findViewById(R.id.search_bar);
        searchBar.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResults.class)));
        searchBar.setFocusable(true);
        searchBar.setIconifiedByDefault(true);
        findViewById(R.id.drawer_layout).requestFocus();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Fix cannot zoom to bounds until the map has a size
        final View mapView = mapFragment.getView();
        if(mapView != null && mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    subscribeToUpdates();
                }
            });
        }

        // Set up tour pin
        homeFrame = (FrameLayout) findViewById(R.id.home_frame);
        homeFrame.setOnDragListener(new TourPinDragListener());
        imageView = new ImageView(this);
        imageView.setVisibility(View.INVISIBLE);
        imageView.setImageResource(R.drawable.tour_pin);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        homeFrame.addView(imageView);
        tourPinButton = (FloatingActionButton) findViewById(R.id.tour_pin_button);
        tourPinButton.setOnTouchListener(new TourPinTouchListener());

        // Check GPS is enabled
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location service", Toast.LENGTH_LONG).show();
        }
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();  // If user has not permit location tracking, ask again.
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
    }

    private void startTrackerService() {
        startService(new Intent(this, TrackerService.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            finish();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMaxZoomPreference(64);
        map.setTrafficEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);
        map.setIndoorEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        // Move camera to user's location when map is ready
        locationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16f));
                }
            }
        });
    }

    private void subscribeToUpdates() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_user_locations_path));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Check whether the user is user themselves
//                setMarker(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                setMarker(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mMarkers.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Failed to read value.");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void setMarker(DataSnapshot dataSnapshot) {
        // When a location update is received, update the markers
        String key = dataSnapshot.getKey();
        HashMap<String, Object> value = (HashMap<String, Object>)dataSnapshot.getValue();
        double latitude = Double.parseDouble(value.get("latitude").toString());
        double longitude = Double.parseDouble(value.get("longitude").toString());
        LatLng location = new LatLng(latitude, longitude);
        if(!mMarkers.containsKey(key)) {
            mMarkers.put(key, map.addMarker(new MarkerOptions().title(key).position(location)));
        } else {
            mMarkers.get(key).setPosition(location);
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20));
    }

    private final class TourPinDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View view, DragEvent dragEvent) {
            int action = dragEvent.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
//                    Toast.makeText(getBaseContext(), "Drag started", Toast.LENGTH_SHORT).show();
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
//                    Toast.makeText(getBaseContext(), "Drag entered", Toast.LENGTH_SHORT).show();
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
//                    Toast.makeText(getBaseContext(), "Drag exited", Toast.LENGTH_SHORT).show();
                    break;
                case DragEvent.ACTION_DROP:
                    int x = (int)dragEvent.getX();
                    int y = (int)dragEvent.getY();
                    LatLng latLng = map.getProjection().fromScreenLocation(new Point(x, y));
//                    Toast.makeText(getBaseContext(),
//                            latLng.latitude + ", " + latLng.longitude,
//                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(), AddTourConfirmBeginning.class);
                    intent.putExtra("latitude", latLng.latitude);
                    intent.putExtra("longitude", latLng.longitude);
                    startActivity(intent);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
//                    Log.d(TAG, "onDrag: drag ended.");
                    break;
            }

            return true;
        }
    }

    private final class TourPinTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(imageView);
                imageView.startDragAndDrop(data, shadowBuilder, null, 0);
                return true;
            }

            return false;
        }
    }
}
