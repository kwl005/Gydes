package gydes.gyde.models;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import gydes.gyde.R;
import gydes.gyde.controllers.Login;
import gydes.gyde.controllers.MyBookings;

public class BookingDetailsDialogFragment extends DialogFragment {

    final static int LAST_HOUR_OF_DAY = 23;
    final static String stops_prefix = "Stops: %s";
    final static String tags_prefix = "Tags: %s";
    final static String duration_prefix = "Duration: %s";
    final static String transport_prefix = "Transport: %s";
    final static String capacity_prefix = "Capacity: %s";
    final static String name_prefix = "With: %s";
    final static String phone_prefix = "Phone: %s";
    final static String email_prefix = "Email: %s";

    Tour tour;
    String otherUserID;
    String name;
    String phoneNum;
    String email;
    String thisSideStr;
    String otherSideStr;
    int day;
    int hour;

    public static BookingDetailsDialogFragment newInstance(Tour t, String id, String nam, String phone, String mail,
                                                           String side, int day, int hour) {
        Log.d("AAAAA", "side: " + side);
        BookingDetailsDialogFragment frag = new BookingDetailsDialogFragment();
        Bundle b = new Bundle();
        b.putParcelable("tour", t);
        b.putString("id", id);
        b.putString("name", nam);
        b.putString("phone", phone);
        b.putString("email", mail);
        b.putString("side", side);
        b.putInt("day", day);
        b.putInt("hour", hour);
        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tour = getArguments().getParcelable("tour");
        otherUserID = getArguments().getString("id");
        name = getArguments().getString("name");
        phoneNum = getArguments().getString("phone");
        phoneNum = phoneNum == null ? "None" : phoneNum;
        email = getArguments().getString("email");
        email = email == null ? "None" : email;
        thisSideStr = getArguments().getString("side");
        otherSideStr = thisSideStr.equals("traveler") ? "guide" : "traveler";
        Log.d("AAAAA", "thisSideStr: " + thisSideStr);
        Log.d("AAAAA", "otherSideStr: " + otherSideStr);
        day = getArguments().getInt("day");
        hour = getArguments().getInt("hour");
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
                final Activity act = BookingDetailsDialogFragment.this.getActivity();
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                builder.setMessage(R.string.cancel_booking_confirmation);
                builder.setPositiveButton(R.string.yes_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBooking((MyBookings)act);
                    }
                });
                builder.setNegativeButton(R.string.no_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                });
                builder.create().show();
            }
        });

        return builder.create();
    }

    void deleteBooking(MyBookings act) {
        final String thisID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String otherID = otherUserID;
        final String thisSide = thisSideStr;
        final String otherSide = otherSideStr;
        final int startHour = hour;
        final int startDay = day;

        Toast toast = Toast.makeText(act, R.string.cancel_booking_toast, Toast.LENGTH_SHORT);
        toast.show();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot users) {
                Log.d("AAAAA", "thisSide: " + thisSide);
                Log.d("AAAAA", "otherSide: " + otherSide);
                DataSnapshot thisBookings = users.child(thisID).child(thisSide).child("bookings");
                DataSnapshot otherBookings = users.child(otherID).child(otherSide).child("bookings");

                int currDay = startDay;
                String dayStr = Login.dayToStr(currDay);
                int currHour = startHour;
                String hourStr = Login.hourToStr(currHour);

                while ((boolean) thisBookings.child(dayStr).child(hourStr).child("sameAsPrev").getValue() &&
                        (boolean) otherBookings.child(dayStr).child(hourStr).child("sameAsPrev").getValue()) {
                    currHour--;
                    if(currHour < 0) {
                        currHour = LAST_HOUR_OF_DAY;
                        currDay--;
                        if(currDay < Calendar.SUNDAY) currDay = Calendar.SATURDAY;
                        dayStr = Login.dayToStr(currDay);
                    }
                    hourStr = Login.hourToStr(currHour);
                }

                do {
                    thisBookings.child(dayStr).child(hourStr).child("tour").getRef().removeValue();
                    thisBookings.child(dayStr).child(hourStr).child("sameAsPrev").getRef().setValue(false);
                    otherBookings.child(dayStr).child(hourStr).child("tour").getRef().removeValue();
                    otherBookings.child(dayStr).child(hourStr).child("sameAsPrev").getRef().setValue(false);

                    currHour++;
                    if(currHour > LAST_HOUR_OF_DAY) {
                        currHour = 0;
                        currDay++;
                        if(currDay > Calendar.SATURDAY) currDay = Calendar.SUNDAY;
                        dayStr = Login.dayToStr(currDay);
                    }
                    hourStr = Login.hourToStr(currHour);
                } while ((boolean) thisBookings.child(dayStr).child(hourStr).child("sameAsPrev").getValue() &&
                        (boolean) otherBookings.child(dayStr).child(hourStr).child("sameAsPrev").getValue());

                act.updateBookingsList(day);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("BookingDetailsDialog", "Error accessing user bookings: " + databaseError.getCode());
            }
        });
    }
}
