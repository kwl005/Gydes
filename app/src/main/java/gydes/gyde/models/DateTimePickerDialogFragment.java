package gydes.gyde.models;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.NumberPicker;
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

public class DateTimePickerDialogFragment extends DialogFragment {

    final static long MILLIS_IN_SIX_DAYS = 518400000;
    final static String TITLE_STR = "Pick a time to meet";
    final static int MIN_HOUR = 0;
    final static int MAX_HOUR = 11;
    final static String[] hourStrs = {"12:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00"};
    final static int AM = 0;
    final static int PM = 1;
    final static String[] periods = {"AM", "PM"};
    final static int HOURS_IN_HALF_DAY = 12;
    final static int HOURS_IN_FULL_DAY = 24;
    final static int DOUBLE_DIGITS = 10;

    final String USERS = "users";
    final String TRAVELER = "traveler";
    final String GUIDE = "guide";
    final String BOOKINGS = "bookings";
    final String TOUR = "tour";
    final String GUIDEID = "guideID";
    final String TRAVID = "travelerID";
    final String SAMEASPREV = "sameAsPrev";

    Tour tour;

    public static DateTimePickerDialogFragment newInstance(Tour t) {
        DateTimePickerDialogFragment frag = new DateTimePickerDialogFragment();
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
        View view = getActivity().getLayoutInflater().inflate(R.layout.date_time_picker, null);

        final DatePicker datePicker = view.findViewById(R.id.date_picker);
        final NumberPicker hourPicker = view.findViewById(R.id.hour_picker);
        final NumberPicker periodPicker = view.findViewById(R.id.period_picker);

        datePicker.setMinDate(System.currentTimeMillis());
        datePicker.setMaxDate(datePicker.getMinDate() + MILLIS_IN_SIX_DAYS);

        hourPicker.setDisplayedValues(hourStrs);
        hourPicker.setMinValue(MIN_HOUR);
        hourPicker.setMaxValue(MAX_HOUR);

        periodPicker.setDisplayedValues(periods);
        periodPicker.setMinValue(AM);
        periodPicker.setMaxValue(PM);

        builder.setTitle(TITLE_STR);
        builder.setView(view);
        builder.setPositiveButton(R.string.book_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final Activity act = DateTimePickerDialogFragment.this.getActivity();

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child(USERS);
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot users) {
                        DataSnapshot guideBookings = users.child(tour.getCreatorID()).child(GUIDE).child(BOOKINGS);
                        DataSnapshot travBookings = users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(TRAVELER).child(BOOKINGS);

                        Calendar cal = Calendar.getInstance();
                        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                        String dayStr = Login.dayToStr(dayOfWeek);

                        String hourStr;
                        int startHour = hourPicker.getValue();
                        if(periodPicker.getValue() == PM) startHour += HOURS_IN_HALF_DAY;
                        int currHour = startHour;

                        int checkDay = dayOfWeek;
                        int checkHour = currHour;
                        for(int i = 0; i < tour.getDuration(); i++) {
                            hourStr = Login.hourToStr(checkHour);

                            if(guideBookings.child(dayStr).child(hourStr).hasChild(TOUR)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                                builder.setMessage(R.string.guide_busy_msg);
                                builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Do nothing
                                    }
                                });
                                builder.create().show();
                                return;
                            } else if(travBookings.child(dayStr).child(hourStr).hasChild("tour")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                                builder.setMessage(R.string.trav_busy_msg);
                                builder.setPositiveButton(R.string.ok_txt, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Do nothing
                                    }
                                });
                                builder.create().show();
                                return;
                            }

                            checkHour++;
                            if(checkHour == HOURS_IN_FULL_DAY) {
                                checkHour = 0;
                                checkDay++;
                                if(checkDay > Calendar.SATURDAY) dayOfWeek = Calendar.SUNDAY;
                                dayStr = Login.dayToStr(dayOfWeek);
                            }
                        }

                        for(int i = 0; i < tour.getDuration(); i++) {
                            hourStr = Login.hourToStr(currHour);

                            DatabaseReference travBookSpot = Login.currentUserRef.child(TRAVELER)
                                    .child(BOOKINGS).child(dayStr).child(hourStr);
                            travBookSpot.child(GUIDEID).setValue(tour.getCreatorID());
                            travBookSpot.child(TOUR).setValue(tour);
                            if(currHour == startHour) travBookSpot.child(SAMEASPREV).setValue(false);
                            else travBookSpot.child(SAMEASPREV).setValue(true);

                            DatabaseReference guideBookSpot = FirebaseDatabase.getInstance().getReference()
                                    .child(USERS).child(tour.getCreatorID()).child(GUIDE)
                                    .child(BOOKINGS).child(dayStr).child(hourStr);
                            guideBookSpot.child(TRAVID).setValue(Login.currentUserRef.getKey());
                            guideBookSpot.child(TOUR).setValue(tour);
                            if(currHour == startHour) guideBookSpot.child(SAMEASPREV).setValue(false);
                            else guideBookSpot.child(SAMEASPREV).setValue(true);

                            currHour++;
                            if(currHour == HOURS_IN_FULL_DAY) {
                                currHour = 0;
                                dayOfWeek++;
                                if(dayOfWeek > Calendar.SATURDAY) dayOfWeek = Calendar.SUNDAY;
                                dayStr = Login.dayToStr(dayOfWeek);
                            }
                        }
                        Toast toast = Toast.makeText(act, "Tour booked", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        builder.setNegativeButton(R.string.cancel_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DateTimePickerDialogFragment.this.getDialog().cancel();
            }
        });

        return builder.create();
    }
}


