package gydes.gyde.controllers;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import gydes.gyde.R;
import gydes.gyde.models.Tour;
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
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    int x = (int)dragEvent.getX();
                    int y = (int)dragEvent.getY();
                    LatLng latLng = map.getProjection().fromScreenLocation(new Point(x, y));
                    CreateTourDialogSequence sequence = new CreateTourDialogSequence(latLng);
                    sequence.begin();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
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

    private class CreateTourDialogSequence {
        private final LatLng startingPoint;
        private Tour newTour;

        private CreateTourDialogSequence(final LatLng startingPoint) {
            this.startingPoint = startingPoint;
            newTour = new Tour();
        }

        private void begin() {
            Geocoder geocoder = new Geocoder(HomeActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(startingPoint.latitude, startingPoint.longitude, 1);
            } catch(IOException e) {
                e.printStackTrace();
            }
            final String location = addresses == null ? "Unknown" : addresses.get(0).getLocality();
            newTour.setLocation(location);

            final String dialogMessage = "Do you want to create a tour starting at " + addresses.get(0).getAddressLine(0) + "?";
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setMessage(dialogMessage)
                    .setTitle("Confirmation")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            displayDialogToGetName();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void displayDialogToGetName() {
            final EditText editText = new EditText(HomeActivity.this);
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20)});
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setView(editText)
                    .setTitle("Enter name of the tour")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(editText.getText().length() == 0) {
                                Toast.makeText(HomeActivity.this, "Can not leave name empty. Please try again.", Toast.LENGTH_LONG).show();
                                displayDialogToGetName();
                                return;
                            }

                            newTour.setName(editText.getText().toString());
                            displayDialogToGetCapacity();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            KeyboardManager.hideKeyboard(HomeActivity.this);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
            KeyboardManager.showKeyboard(HomeActivity.this);
        }

        private void displayDialogToGetCapacity() {
            final EditText editText = new EditText(HomeActivity.this);
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20)});
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setView(editText)
                    .setTitle("How many people are coming with you?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(editText.getText().length() == 0) {
                                Toast.makeText(HomeActivity.this, "Can not leave name empty. Please try again.", Toast.LENGTH_LONG).show();
                                displayDialogToGetCapacity();
                                return;
                            }

                            newTour.setCapacity(Integer.parseInt(editText.getText().toString()) + 1);
                            displayDialogToGetDuration();
                        }
                    })
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            displayDialogToGetName();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
            KeyboardManager.showKeyboard(HomeActivity.this);
        }

        private void displayDialogToGetDuration() {
            final EditText editText = new EditText(HomeActivity.this);
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20)});
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setView(editText)
                    .setTitle("How many hours would you like to spend?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(editText.getText().length() == 0) {
                                Toast.makeText(HomeActivity.this, "Can not leave name empty. Please try again.", Toast.LENGTH_LONG).show();
                                displayDialogToGetDuration();
                                return;
                            }

                            final int hours = Integer.parseInt(editText.getText().toString());
                            if(hours < 1) {
                                Toast.makeText(HomeActivity.this, "Hours must be greater or equal to 1. Please try again.", Toast.LENGTH_LONG).show();
                            }

                            newTour.setDuration(Integer.parseInt(editText.getText().toString()));
                            displayDialogToGetMethod();
                        }
                    })
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            displayDialogToGetCapacity();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
            KeyboardManager.showKeyboard(HomeActivity.this);
        }

        private void displayDialogToGetMethod() {
            KeyboardManager.hideKeyboard(HomeActivity.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            final CharSequence[] methods = {"Walking", "Driving"};
            builder.setItems(methods, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    newTour.setWalking(i == 0);
                    displayDialogToGetTags();
                }
            })
                    .setTitle("What is your preferred exploration method?")
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            displayDialogToGetDuration();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
        }

        private void displayDialogToGetTags() {
            final EditText editText = new EditText(HomeActivity.this);
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20)});
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setView(editText)
                    .setTitle("Do you want to type in any tags for your tour?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            newTour.setTags(editText.getText().toString());
                            displayDialogToPickDestination();
                        }
                    })
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            displayDialogToGetMethod();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
            KeyboardManager.showKeyboard(HomeActivity.this);
        }

        private void displayDialogToPickDestination() {
            KeyboardManager.hideKeyboard(HomeActivity.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("Please pick a destination on the map")
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Toast.makeText(getBaseContext(), "Work in Progress", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            displayDialogToGetTags();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    private class BlackView extends View {
        private Paint paint = new Paint();
        private Paint transparentPaint = new Paint();
        private BlackView(Context context) {
            super(context);
        }

        @Override
        public void onDraw(Canvas canvas) {
            paint.setColor(0xff);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
            transparentPaint.setAlpha(0xFF);
            transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            Rect rect = new Rect(20, 20, 40, 40);//make this your rect!
            canvas.drawRect(rect,transparentPaint);
        }
    }
}
