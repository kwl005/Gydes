package gydes.gyde.models;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import gydes.gyde.R;
import gydes.gyde.controllers.Login;
import gydes.gyde.controllers.MyTours;
import gydes.gyde.controllers.SearchResults;

public class TourDetailsDialogFragment extends DialogFragment {

    final static String stops_prefix = "Stops: %s";
    final static String tags_prefix = "Tags: %s";
    final static String duration_prefix = "Duration: %s";
    final static String transport_prefix = "Transport: %s";
    final static String capacity_prefix = "Capacity: %s";

    Tour tour;
    int buttonOpt;

    public static TourDetailsDialogFragment newInstance(Tour t, double opt) {
        TourDetailsDialogFragment frag = new TourDetailsDialogFragment();
        Bundle b = new Bundle();
        b.putParcelable("tour", t);
        b.putDouble("opt", opt);
        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tour = getArguments().getParcelable("tour");
        buttonOpt = (int)getArguments().getDouble("opt");
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

        switch (buttonOpt) {
            case SearchResults.SEARCH_RESULTS_BUTTON_OPT:
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
                break;
            case MyTours.MY_TOURS_BUTTON_OPT:
                builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                });
                builder.setNegativeButton(R.string.delete_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(TourDetailsDialogFragment.this.getActivity());
                        builder.setMessage(R.string.delete_tour_confirmation);
                        builder.setPositiveButton(R.string.delete_txt, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Login.currentUserRef.child("guide").child("tourIDs").child(tour.getTourID()).removeValue();
                                DatabaseReference toursRef = Login.currentUserRef.getRoot().child("tours");
                                toursRef.child(tour.getLocation()).child(tour.getTourID()).removeValue();
                            }
                        });
                        builder.setNegativeButton(R.string.cancel_txt, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing
                            }
                        });
                        builder.create().show();
                    }
                });
                break;
            default:
                builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("TourDetails", "invalid button opt");
                    }
                });
                builder.setNegativeButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("TourDetails", "invalid button opt");
                    }
                });
        }



        return builder.create();
    }
}
