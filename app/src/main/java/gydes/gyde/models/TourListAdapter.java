package gydes.gyde.models;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import gydes.gyde.R;

public class TourListAdapter extends ArrayAdapter<Tour> {
    private Context context;
    private int resourceID;
    private ArrayList<Tour> tourList;
    private int posButtonOpt;

    public TourListAdapter(@NonNull Context c, int rID, ArrayList<Tour> list, int opt) {
        super(c, 0, list);
        context = c;
        resourceID = rID;
        tourList = list;
        posButtonOpt = opt;
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
                TourDetailsDialogFragment frag = TourDetailsDialogFragment.newInstance(currTour, (double)posButtonOpt);
                frag.show(((Activity)context).getFragmentManager(), "tour details");
            }
        });

        return listItem;
    }
}
