package gydes.gyde.controllers;

import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.Drawer;

import java.util.ArrayList;
import java.util.Calendar;

import gydes.gyde.R;
import gydes.gyde.models.TourDetailsDialogFragment;
import gydes.gyde.models.Tour;
import gydes.gyde.models.TourListAdapter;

public class SearchResults extends AppCompatActivity {
    Context context = this;
    SearchView searchView;

    final ArrayList<Tour> tours = new ArrayList<Tour>();

    public static final int SEARCH_RESULTS_BUTTON_OPT = 1;
    private static final String MAX_DURATION = "10000";
    private static final String MIN_DURATION = "0";
    final static int MIN_HOUR = 0;
    final static int MAX_HOUR = 11;
    final static String[] hourStrs = {"12:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00"};
    final static int AM = 0;
    final static int PM = 1;
    final static String[] periods = {"AM", "PM"};
    final static String SEPARATOR = "/";
    final static int MONTH_IND = 0;
    final static int DAY_IND = 1;
    final static int YEAR_IND = 2;
    final static int HOURS_IN_HALF_DAY = 12;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = findViewById(R.id.filter_toolbar);
        setSupportActionBar(toolbar);

        // Setup navigation drawer, action bar and status bar
        final Drawer result = NavigationDrawerBuilder.build(this, savedInstanceState);
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

        searchView = (SearchView) findViewById(R.id.filter_search_bar);
        searchView.setFocusable(false);


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchBar = (SearchView)findViewById(R.id.filter_search_bar);
        searchBar.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResults.class)));
        searchBar.setFocusable(true);
        searchBar.setIconifiedByDefault(true);
        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = SearchResults.toCamelCase(intent.getStringExtra(SearchManager.QUERY).trim());
            final TourListAdapter adapter = new TourListAdapter(this, R.layout.tour_list_item, tours, SEARCH_RESULTS_BUTTON_OPT);
            ListView listView = findViewById(android.R.id.list);
            listView.setAdapter(adapter);

            final DatabaseReference toursRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_tours_path));
            toursRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(query)) {
                        for (DataSnapshot child : dataSnapshot.child(query).getChildren()) {
                            tours.add(child.getValue(Tour.class));
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("data access", "Error querying tours: " + databaseError.getCode());
                }
            });
        }
    }

    public static String toCamelCase(String str) {
        String[] words = str.split(" ");
        String camelCaseStr = "";
        for(int i = 0; i < words.length; i++) {
            camelCaseStr += words[i].substring(0,1).toUpperCase() + words[i].substring(1).toLowerCase();
            if(i < words.length - 1) {
                camelCaseStr += " ";
            }
        }
        return camelCaseStr;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.filter:
                openDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchResults.this);
        View view = getLayoutInflater().inflate(R.layout.tour_filter_layout_dialog, null);

        EditText startTime = (EditText) view.findViewById(R.id.startTime_box);
        EditText endTime = (EditText) view.findViewById(R.id.endTime_box);
        EditText date = (EditText) view.findViewById(R.id.dates_box);

        startTime.setOnTouchListener(
                new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SearchResults.this);
                            builder.setTitle("Select Time");
                            View layout = SearchResults.this.getLayoutInflater().inflate(R.layout.hour_picker, null);
                            final NumberPicker hourPicker = layout.findViewById(R.id.hour_picker);
                            final NumberPicker periodPicker = layout.findViewById(R.id.period_picker);

                            hourPicker.setDisplayedValues(hourStrs);
                            hourPicker.setMinValue(MIN_HOUR);
                            hourPicker.setMaxValue(MAX_HOUR);

                            periodPicker.setDisplayedValues(periods);
                            periodPicker.setMinValue(AM);
                            periodPicker.setMaxValue(PM);
                            builder.setView(layout);
                            builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startTime.setText(hourPicker.getValue() + ":00 " + periods[periodPicker.getValue()]);
                                }
                            });

                            builder.create().show();
                        }
                        return false;
                    }
                }
        );

        endTime.setOnTouchListener(
                new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SearchResults.this);
                            builder.setTitle("Select Time");
                            View layout = SearchResults.this.getLayoutInflater().inflate(R.layout.hour_picker, null);
                            final NumberPicker hourPicker = layout.findViewById(R.id.hour_picker);
                            final NumberPicker periodPicker = layout.findViewById(R.id.period_picker);

                            hourPicker.setDisplayedValues(hourStrs);
                            hourPicker.setMinValue(MIN_HOUR);
                            hourPicker.setMaxValue(MAX_HOUR);

                            periodPicker.setDisplayedValues(periods);
                            periodPicker.setMinValue(AM);
                            periodPicker.setMaxValue(PM);
                            builder.setView(layout);
                            builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    endTime.setText(hourPicker.getValue() + ":00 " + periods[periodPicker.getValue()]);
                                }
                            });

                            builder.create().show();
                        }
                        return false;
                    }
                }
        );


        date.setOnTouchListener(
                new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            Calendar calendar = Calendar.getInstance();
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            DatePickerDialog mDatePicker = new DatePickerDialog(SearchResults.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                                    date.setText((m+1) + SEPARATOR + d + SEPARATOR + y);
                                }
                            }, year, month, day);
                            mDatePicker.getDatePicker().setBackgroundColor(getColor(R.color.gydeBlue));
                            mDatePicker.setTitle("Select Date");
                            mDatePicker.show();
                        }

                        return false;
                    }
                }
        );


        builder.setView(view)
                .setTitle("Filter")
                .setNegativeButton(R.string.cancel_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Filter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(SearchResults.this, "Filtering...", Toast.LENGTH_SHORT).show();
                        Login.currentUserRef.getParent().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot users) {
                                boolean filterTime = false;
                                int startHour = 0;
                                int endHour = 0;
                                int dayOfWeek = 0;
                                if(!date.getText().toString().equals("") && !startTime.getText().toString().equals("") && !endTime.getText().toString().equals("")) {
                                    filterTime = true;
                                    String[] dayInfo = date.getText().toString().split(SEPARATOR);
                                    int month = Integer.parseInt(dayInfo[MONTH_IND])-1;
                                    int day = Integer.parseInt(dayInfo[DAY_IND]);
                                    int year = Integer.parseInt(dayInfo[YEAR_IND]);
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(year, month, day);
                                    dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

                                    String[] startHourInfo = startTime.getText().toString().split(" ");
                                    startHour = Integer.parseInt(startHourInfo[0].split(":")[0]);
                                    if (startHourInfo[1].equals(periods[1]))
                                        startHour += HOURS_IN_HALF_DAY;

                                    String[] endHourInfo = endTime.getText().toString().split(" ");
                                    endHour = Integer.parseInt(endHourInfo[0].split(":")[0]);
                                    if (endHourInfo[1].equals(periods[1]))
                                        endHour += HOURS_IN_HALF_DAY;
                                }

                                EditText durationBoxMin = (EditText) view.findViewById(R.id.duration_box_min);
                                EditText durationBoxMax = (EditText) view.findViewById(R.id.duration_box_max);
                                EditText tagsBox = (EditText) view.findViewById(R.id.tags_box);
                                EditText capacityBox = (EditText) view.findViewById(R.id.capacity_box);
                                EditText priceBox = (EditText) view.findViewById(R.id.price_box);

                                final ArrayList<Tour> toursCopy = new ArrayList<>(tours);
                                final TourListAdapter adapter2 = new TourListAdapter(context, R.layout.tour_list_item, toursCopy, SEARCH_RESULTS_BUTTON_OPT);
                                ListView listView2 = findViewById(android.R.id.list);
                                listView2.setAdapter(adapter2);

                                if(filterTime) {
                                    for (int hour = startHour; hour < endHour; hour++) {
                                        String dayStr = Login.dayToStr(dayOfWeek);
                                        String hourStr = Login.hourToStr(hour);
                                        toursCopy.removeIf((Tour tour) ->
                                                users.child(tour.getCreatorID()).child(getString(R.string.firebase_guide_path))
                                                        .child(getString(R.string.firebase_book_path)).child(dayStr).child(hourStr)
                                                        .hasChild(getString(R.string.firebase_tour_path)));
                                    }
                                }

                                String inputDurationMinText = durationBoxMin.getText().toString().trim();
                                String inputDurationMaxText = durationBoxMax.getText().toString().trim();
                                String inputCapacityText = capacityBox.getText().toString().trim();
                                String inputTagsText = tagsBox.getText().toString().trim();
                                String inputPriceText = priceBox.getText().toString().trim();

                                if(!inputDurationMinText.isEmpty() || !inputDurationMaxText.isEmpty()) {
                                    if(inputDurationMinText.equals("")) inputDurationMinText = MIN_DURATION;
                                    if(inputDurationMaxText.equals("")) inputDurationMaxText = MAX_DURATION;
                                    int inputDurationMin = Integer.parseInt(inputDurationMinText);
                                    int inputDurationMax = Integer.parseInt(inputDurationMaxText);
                                    toursCopy.removeIf((Tour tour) -> tour.getDuration() < inputDurationMin
                                            || tour.getDuration() > inputDurationMax);
                                }

                                if(!inputCapacityText.isEmpty()) {
                                    int inputCapacity = Integer.parseInt(inputCapacityText);
                                    toursCopy.removeIf((Tour tour) -> tour.getCapacity() < inputCapacity);
                                }

                                if(!inputPriceText.isEmpty()) {
                                    double inputPrice = Double.parseDouble(inputPriceText);
                                    toursCopy.removeIf((Tour tour) -> tour.getPrice() > inputPrice);
                                }

                                if(!inputTagsText.isEmpty()) {
                                    String[] tags = inputTagsText.toLowerCase().split(",");

                                    for(int i=0; i<tags.length; i++) {
                                        String tag = tags[i].trim();
                                        toursCopy.removeIf((Tour tour) -> !tour.getTags().toLowerCase().contains(tag));
                                    }
                                }

                                adapter2.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });


        AlertDialog dialog = builder.create();
        dialog.show();

    }
}