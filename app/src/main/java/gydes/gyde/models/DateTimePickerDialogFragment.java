package gydes.gyde.models;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.NumberPicker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import gydes.gyde.R;
import gydes.gyde.controllers.Login;

public class DateTimePickerDialogFragment extends DialogFragment {

    final static long MILLIS_IN_SIX_DAYS = 518400000;
    final static String TITLE_STR = "Pick a time to meet";
    final static int MIN_HOUR = 0;
    final static int MAX_HOUR = 11;
    final static String[] hours = {"12:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00"};
    final static int AM = 0;
    final static int PM = 1;
    final static String[] periods = {"AM", "PM"};
    final static int HOURS_IN_HALF_DAY = 12;
    final static int HOURS_IN_FULL_DAY = 24;
    final static int DOUBLE_DIGITS = 10;

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

        hourPicker.setDisplayedValues(hours);
        hourPicker.setMinValue(MIN_HOUR);
        hourPicker.setMaxValue(MAX_HOUR);

        periodPicker.setDisplayedValues(periods);
        periodPicker.setMinValue(AM);
        periodPicker.setMaxValue(PM);

        builder.setTitle(TITLE_STR);
        builder.setView(view);
        builder.setPositiveButton(R.string.book_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Calendar cal = Calendar.getInstance();
                cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                String dayStr = getDayStr(dayOfWeek);

                String timeStr;
                int startHour = hourPicker.getValue();
                if(periodPicker.getValue() == PM) startHour += HOURS_IN_HALF_DAY;
                int currHour = startHour;

                for(int i = 0; i < tour.getDuration(); i++) {
                    timeStr = "";

                    if (currHour < DOUBLE_DIGITS) timeStr += "0";
                    timeStr += currHour + ":00";

                    DatabaseReference travBookSpot = Login.currentUserRef.child(getString(R.string.firebase_trav_path))
                            .child(getString(R.string.firebase_book_path)).child(dayStr).child(timeStr);
                    travBookSpot.child(getString(R.string.firebase_gID_path)).setValue(tour.getCreatorID());
                    travBookSpot.child(getString(R.string.firebase_tour_path)).setValue(tour);
                    if(currHour != startHour) travBookSpot.child(getString(R.string.firebase_sameasprev_path)).setValue(true);

                    DatabaseReference guideBookSpot = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.firebase_users_path)).child(tour.getCreatorID())
                            .child(getString(R.string.firebase_guide_path)).child(getString(R.string.firebase_book_path))
                            .child(dayStr).child(timeStr);
                    guideBookSpot.child(getString(R.string.firebase_tID_path)).setValue(Login.currentUserRef.getKey());
                    guideBookSpot.child(getString(R.string.firebase_tour_path)).setValue(tour);
                    if(currHour != startHour) guideBookSpot.child(getString(R.string.firebase_sameasprev_path)).setValue(true);

                    currHour++;
                    if(currHour == HOURS_IN_FULL_DAY) {
                        currHour = 0;
                        dayOfWeek++;
                        if(dayOfWeek > Calendar.SATURDAY) dayOfWeek = Calendar.SUNDAY;
                        dayStr = getDayStr(dayOfWeek);
                    }
                }
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

    static String getDayStr (int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "sunday";
            case Calendar.MONDAY:
                return "monday";
            case Calendar.TUESDAY:
                return "tuesday";
            case Calendar.WEDNESDAY:
                return "wednesday";
            case Calendar.THURSDAY:
                return "thursday";
            case Calendar.FRIDAY:
                return "friday";
            case Calendar.SATURDAY:
                return "saturday";
        }
        Log.d("getDayStr()", "dayOfWeek out of bounds");
        return "";
    }
}


