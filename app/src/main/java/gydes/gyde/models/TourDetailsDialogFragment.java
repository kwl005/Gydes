package gydes.gyde.models;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import gydes.gyde.R;

public class TourDetailsDialogFragment extends DialogFragment {

    final static String stops_prefix = "Stops: %s";
    final static String tags_prefix = "Tags: %s";
    final static String duration_prefix = "Duration: %s";
    final static String transport_prefix = "Transport: %s";
    final static String capacity_prefix = "Capacity: %s";

    Tour tour;

    public static TourDetailsDialogFragment newInstance(Tour t) {
        TourDetailsDialogFragment frag = new TourDetailsDialogFragment();
        Bundle b = new Bundle();
        b.putParcelable("tour", t);
        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tour = getArguments().getParcelable("tour");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.tour_details_dialog, null);

        builder.setTitle(tour.getName());

        ((TextView) view.findViewById(R.id.tour_stops)).setText(String.format(stops_prefix, tour.getStops()));
        ((TextView) view.findViewById(R.id.tour_tags)).setText(String.format(tags_prefix, tour.getTags()));
        ((TextView) view.findViewById(R.id.tour_duration)).setText(String.format(duration_prefix, tour.getDuration()));
        if(tour.getWalking()) {
            ((TextView) view.findViewById(R.id.tour_transport)).setText(String.format(transport_prefix, "Walk"));
        }
        else {
            ((TextView) view.findViewById(R.id.tour_transport)).setText(String.format(transport_prefix, "Drive"));
        }
        ((TextView) view.findViewById(R.id.tour_capacity)).setText(String.format(capacity_prefix, tour.getCapacity()));
        builder.setView(view);

        builder.setPositiveButton(R.string.book_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                DateTimePickerDialogFragment frag = DateTimePickerDialogFragment.newInstance(tour);
                frag.show(getFragmentManager(), "date time picker");
            }
        });
        builder.setNegativeButton(R.string.cancel_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TourDetailsDialogFragment.this.getDialog().cancel();
            }
        });

        return builder.create();
    }
}
