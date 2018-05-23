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
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import gydes.gyde.R;
import gydes.gyde.models.Tour;

public class SearchResults extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
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

        Tour currTour = tourList.get(position);

        TextView name = listItem.findViewById(R.id.textView_name);
        name.setText(currTour.getName());

        TextView stops = listItem.findViewById(R.id.textView_stops);
        stops.setText(currTour.getStops());

        TextView tags = listItem.findViewById(R.id.textView_tags);
        tags.setText(currTour.getTags());

        Button viewButton = listItem.findViewById(R.id.view_button);
        viewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO
                Log.d("BUTTON", "view tour details button clicked");
            }
        });

        return listItem;
    }
}
