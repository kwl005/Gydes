package gydes.gyde.models;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import gydes.gyde.R;

public class DateTimePickerDialogFragment extends DialogFragment {

    final static long MILLIS_IN_SIX_DAYS = 518400000;
    final static String TITLE_STR = "Pick a day and time";
    final static boolean IS_24_HOUR_VIEW = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.date_time_picker, null);

        final DatePicker datePicker = view.findViewById(R.id.date_picker);
        final TimePicker timePicker = view.findViewById(R.id.time_picker);

        datePicker.setMinDate(System.currentTimeMillis());
        datePicker.setMaxDate(datePicker.getMinDate() + MILLIS_IN_SIX_DAYS);

        timePicker.setIs24HourView(IS_24_HOUR_VIEW);

        builder.setTitle(TITLE_STR);
        builder.setView(view);
        builder.setPositiveButton(R.string.book_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Calendar cal = Calendar.getInstance();
                cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                Date date = cal.getTime();
                Log.d("date time picker", cal.get(Calendar.YEAR) + " " + cal.get(Calendar.MONTH) + " " + cal.get(Calendar.DATE) + " " + cal.get(Calendar.HOUR_OF_DAY));
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
