package gydes.gyde.controllers.addTour;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.mikepenz.materialdrawer.Drawer;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.security.Key;
import java.util.Calendar;

import gydes.gyde.R;
import gydes.gyde.controllers.KeyboardManager;
import gydes.gyde.controllers.NavigationDrawerBuilder;

/**
 * Created by kelvinlui1 on 6/3/18.
 */

public class AddTourConfirmDuration extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tour_confirm_duration);

        // Setup navigation drawer, action bar and status bar
        final Drawer result = NavigationDrawerBuilder.build(this, savedInstanceState);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.openDrawer();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.gydeYellow));

        // Display time picker as soon as the screen loads
        TimePickerDialog tpd = TimePickerDialog.newInstance(AddTourConfirmDuration.this, false);
        tpd.setCancelText("BACK");
        tpd.setMaxTime(11, 59, 59);
        tpd.enableMinutes(false);
        tpd.setVersion(TimePickerDialog.Version.VERSION_2);
        tpd.setTitle("Select duration of your tour");
        tpd.setInitialSelection(2, 0, 0);
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
                KeyboardManager.showKeyboard(AddTourConfirmDuration.this);
            }
        });
        tpd.show(getFragmentManager(), "timePickerDialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        Bundle bundle = getIntent().getExtras();
        bundle.putInt("duration", hourOfDay);
        Intent intent = new Intent();
    }
}
