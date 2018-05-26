package gydes.gyde.controllers;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import gydes.gyde.R;
import gydes.gyde.models.Tour;

public class SearchResults extends ListActivity {

    final static String name_prefix = "Name: %s";
    final static String stops_prefix = "Stops: %s";
    final static String tags_prefix = "Tags: %s";
    final static String duration_prefix = "Duration: %s";
    final static String transport_prefix = "Transport: %s";
    final static String capacity_prefix = "Capacity: %s";
    final static long MILLIS_IN_SIX_DAYS = 518400000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        findViewById(R.id.details_window).setVisibility(View.GONE);
        Button exitDetailsButton = findViewById(R.id.tour_details_exit_button);
        exitDetailsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(R.id.details_window).setVisibility(View.GONE);
            }
        });

        findViewById(R.id.calendar_window).setVisibility(View.GONE);
        Button exitCalButton = findViewById(R.id.calendar_exit_button);
        exitCalButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(R.id.calendar_window).setVisibility(View.GONE);
            }
        });

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = SearchResults.toCamelCase(intent.getStringExtra(SearchManager.QUERY));
            final ArrayList<Tour> tours = new ArrayList<Tour>();

            final ButtonAdapter adapter = new ButtonAdapter(this, R.layout.tour_list_item, tours);
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

    class ButtonAdapter extends ArrayAdapter<Tour> {
        private Context context;
        private int resourceID;
        private ArrayList<Tour> tourList;

        public ButtonAdapter(@NonNull Context c, int rID, ArrayList<Tour> list) {
            super(c, 0, list);
            context = c;
            resourceID = rID;
            tourList = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(context).inflate(resourceID, parent, false);
            }

            final Tour currTour = tourList.get(position);

            TextView name = listItem.findViewById(R.id.textView_name);
            name.setText(currTour.getName());

            TextView stops = listItem.findViewById(R.id.textView_stops);
            stops.setText(currTour.getStops());

            TextView tags = listItem.findViewById(R.id.textView_tags);
            tags.setText(currTour.getTags());

            Button viewButton = listItem.findViewById(R.id.view_button);
            viewButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ((TextView)findViewById(R.id.tour_name)).setText(String.format(name_prefix, currTour.getName()));
                    ((TextView)findViewById(R.id.tour_stops)).setText(String.format(stops_prefix, currTour.getStops()));
                    ((TextView)findViewById(R.id.tour_tags)).setText(String.format(tags_prefix, currTour.getTags()));
                    ((TextView)findViewById(R.id.tour_duration)).setText(String.format(duration_prefix, currTour.getDuration()));
                    if(currTour.getWalking()) {
                        ((TextView)findViewById(R.id.tour_transport)).setText(String.format(transport_prefix, "Walk"));
                    }
                    else {
                        ((TextView)findViewById(R.id.tour_transport)).setText(String.format(transport_prefix, "Drive"));
                    }
                    ((TextView)findViewById(R.id.tour_capacity)).setText(String.format(capacity_prefix, currTour.getCapacity()));

                    Button bookButton = findViewById(R.id.book_button);
                    bookButton.setOnClickListener(null);
                    bookButton.setOnClickListener(new View.OnClickListener() {
                       public void onClick(View v) {
                           findViewById(R.id.details_window).setVisibility(View.GONE);

                           CalendarView cal = findViewById(R.id.date_calendar);
                           cal.setMinDate(System.currentTimeMillis());
                           cal.setMaxDate(cal.getMinDate() + MILLIS_IN_SIX_DAYS);
                           cal.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                               @Override
                               public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                                   Calendar calendar = Calendar.getInstance();
                                   calendar.set(year, month, dayOfMonth);
                                   final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                                   //TODO open a time picker and receive time input
                               }
                           });

                           findViewById(R.id.calendar_window).setVisibility(View.VISIBLE);
                       }
                    });

                    findViewById(R.id.details_window).setVisibility(View.VISIBLE);
                }
            });

            return listItem;
        }
    }
}