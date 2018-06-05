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

import java.util.ArrayList;

import gydes.gyde.R;

public class BookingDetailsDialogFragment extends DialogFragment {

    final static String stops_prefix = "Stops: %s";
    final static String tags_prefix = "Tags: %s";
    final static String duration_prefix = "Duration: %s";
    final static String transport_prefix = "Transport: %s";
    final static String capacity_prefix = "Capacity: %s";
    final static String name_prefix = "With: %s";
    final static String phone_prefix = "Phone: %s";
    final static String email_prefix = "Email: %s";

    Tour tour;
    String otherID;
    String name;
    String phoneNum;
    String email;

    public static BookingDetailsDialogFragment newInstance(Tour t, String id, String nam, String phone, String mail) {
        BookingDetailsDialogFragment frag = new BookingDetailsDialogFragment();
        Bundle b = new Bundle();
        b.putParcelable("tour", t);
        ArrayList<String> list = new ArrayList<>();
        list.add(id);
        list.add(nam);
        list.add(phone);
        list.add(mail);
        b.putStringArrayList("list", list);
        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tour = getArguments().getParcelable("tour");
        ArrayList<String> list = getArguments().getStringArrayList("list");
        int ind = 0;
        otherID = list.get(ind);
        ind++;
        name = list.get(ind);
        ind++;
        phoneNum = list.get(ind);
        phoneNum = phoneNum == null ? "None" : phoneNum;
        ind++;
        email = list.get(ind);
        email = email == null ? "None" : email;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.booking_details_dialog, null);

        builder.setTitle(tour.getName());

        ((TextView) view.findViewById(R.id.tour_stops)).setText(String.format(stops_prefix, tour.getStops()));
        ((TextView) view.findViewById(R.id.tour_tags)).setText(String.format(tags_prefix, tour.getTags()));
        ((TextView) view.findViewById(R.id.tour_duration)).setText(String.format(duration_prefix, tour.getDuration()));
        if (tour.getWalking()) {
            ((TextView) view.findViewById(R.id.tour_transport)).setText(String.format(transport_prefix, "Walk"));
        } else {
            ((TextView) view.findViewById(R.id.tour_transport)).setText(String.format(transport_prefix, "Drive"));
        }
        ((TextView) view.findViewById(R.id.tour_capacity)).setText(String.format(capacity_prefix, tour.getCapacity()));

        ((TextView) view.findViewById(R.id.other_person_name)).setText(String.format(name_prefix, name));
        ((TextView) view.findViewById(R.id.other_person_phone)).setText(String.format(phone_prefix, phoneNum));
        ((TextView) view.findViewById(R.id.other_person_email)).setText(String.format(email_prefix, email));

        builder.setView(view);

        builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });

        builder.setNegativeButton(R.string.cancel_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO cancel the booking
            }
        });

        return builder.create();
    }
}
