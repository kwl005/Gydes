package gydes.gyde.controllers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import gydes.gyde.R;
import gydes.gyde.models.BookingDetailsDialogFragment;
import gydes.gyde.models.Tour;
import gydes.gyde.models.TourDetailsDialogFragment;

public class MyBookings extends Activity {

    public static final int MY_BOOKINGS_BUTTON_OPT = 3;
    final static long MILLIS_IN_SIX_DAYS = 518400000;
    final static int HOURS_IN_DAY = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        final Button pickDayButton = findViewById(R.id.pick_day_button);

        pickDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyBookings.this);
                View view = getLayoutInflater().inflate(R.layout.booking_day_picker, null);

                final DatePicker picker = view.findViewById(R.id.day_picker);
                picker.setMinDate(System.currentTimeMillis());
                picker.setMaxDate(picker.getMinDate() + MILLIS_IN_SIX_DAYS);

                builder.setView(view);
                builder.setPositiveButton(R.string.done_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());

                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                        String dayStr = Login.dayToStr(dayOfWeek);

                        pickDayButton.setText(String.format("%s, %d/%d/%d", SearchResults.toCamelCase(dayStr), picker.getMonth() + 1, picker.getDayOfMonth(), picker.getYear()));
                        updateBookingsList(dayOfWeek);
                    }
                });

                builder.create().show();
            }
        });
    }

    public void updateBookingsList(int day) {
        String dayStr = Login.dayToStr(day);
        Toast toast = Toast.makeText(this, "Updating list...", Toast.LENGTH_SHORT);
        toast.show();
        clearBookings();

        String sideStr;
        if (Login.isGuide) {
            sideStr = getString(R.string.firebase_guide_path);
        } else {
            sideStr = getString(R.string.firebase_trav_path);
        }

        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_users_path));
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onDataChange(DataSnapshot users) {
                DataSnapshot bookList = users.child(currentUser.getUid()).child(sideStr).child(getString(R.string.firebase_book_path)).child(dayStr);

                String hourStr;
                final int gydeBlue = getColor(R.color.gydeBlue);
                final int gydeYellow = getColor(R.color.gydeYellow);
                int currColor = gydeBlue;

                for (int hour = 0; hour < HOURS_IN_DAY; hour++) {
                    final int currHour = hour;
                    hourStr = Login.hourToStr(hour);
                    final DataSnapshot booking = bookList.child(hourStr);
                    if (booking.hasChild(getString(R.string.firebase_tour_path))) {
                        final Tour tour = booking.child(getString(R.string.firebase_tour_path)).getValue(Tour.class);

                        TextView bookText = findViewById(getViewIdOfHour(hour));

                        if (hour == 0 || !(boolean) booking.child(getString(R.string.firebase_sameasprev_path)).getValue()) {
                            bookText.setText(tour.getName());
                            currColor = currColor == gydeBlue ? gydeYellow : gydeBlue;
                        }
                        bookText.setBackgroundColor(currColor);
                        bookText.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                String otherID;
                                if (Login.isGuide) {
                                    otherID = (String) booking.child(getString(R.string.firebase_tID_path)).getValue();
                                } else {
                                    otherID = (String) booking.child(getString(R.string.firebase_gID_path)).getValue();
                                }
                                String name = (String) users.child(otherID).child(getString(R.string.firebase_displayname_path)).getValue();
                                String phoneNum = (String) users.child(otherID).child(getString(R.string.firebase_phonenumber_path)).getValue();
                                String email = (String) users.child(otherID).child(getString(R.string.firebase_email_path)).getValue();

                                BookingDetailsDialogFragment frag = BookingDetailsDialogFragment
                                        .newInstance(tour, otherID, name, phoneNum, email, sideStr, day, currHour);
                                frag.show(getFragmentManager(), "booking details");
                                return false;
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MyBookings.java", "Error accessing user's bookings" + databaseError.getCode());
            }
        });
    }

    void clearBookings() {
        for (int hour = 0; hour < HOURS_IN_DAY; hour++) {
            TextView bookText = findViewById(getViewIdOfHour(hour));
            bookText.setBackgroundColor(getColor(R.color.fui_transparent));
            bookText.setText("");
            bookText.setOnTouchListener(null);
        }
    }

    int getViewIdOfHour(int hour) {
        switch (hour) {
            case 0:
                return R.id.twelve_am_booking;
            case 1:
                return R.id.one_am_booking;
            case 2:
                return R.id.two_am_booking;
            case 3:
                return R.id.three_am_booking;
            case 4:
                return R.id.four_am_booking;
            case 5:
                return R.id.five_am_booking;
            case 6:
                return R.id.six_am_booking;
            case 7:
                return R.id.seven_am_booking;
            case 8:
                return R.id.eight_am_booking;
            case 9:
                return R.id.nine_am_booking;
            case 10:
                return R.id.ten_am_booking;
            case 11:
                return R.id.eleven_am_booking;
            case 12:
                return R.id.twelve_pm_booking;
            case 13:
                return R.id.one_pm_booking;
            case 14:
                return R.id.two_pm_booking;
            case 15:
                return R.id.three_pm_booking;
            case 16:
                return R.id.four_pm_booking;
            case 17:
                return R.id.five_pm_booking;
            case 18:
                return R.id.six_pm_booking;
            case 19:
                return R.id.seven_pm_booking;
            case 20:
                return R.id.eight_pm_booking;
            case 21:
                return R.id.nine_pm_booking;
            case 22:
                return R.id.ten_pm_booking;
            case 23:
                return R.id.eleven_pm_booking;
            default:
                return R.id.twelve_am_booking;
        }

    }
}
