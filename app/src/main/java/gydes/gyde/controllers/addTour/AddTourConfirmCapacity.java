package gydes.gyde.controllers.addTour;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;

import gydes.gyde.R;
import gydes.gyde.controllers.KeyboardManager;
import gydes.gyde.controllers.NavigationDrawerBuilder;

/**
 * Created by kelvinlui1 on 5/31/18.
 */

public class AddTourConfirmCapacity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tour_confirm_capacity);

        // Open keyboard as soon as view is loaded
        KeyboardManager.showKeyboard(this);

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

        final EditText editText = (EditText) findViewById(R.id.edit_text);
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.continue_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText().length() == 0) {
                    Toast.makeText(getBaseContext(), "Can not leave text field empty. If you would like to explore alone, please type 0.", Toast.LENGTH_LONG).show();;
                    return;
                }

                Intent intent = new Intent(getBaseContext(), AddTourConfirmDuration.class);
                Bundle bundle = getIntent().getExtras();
                // capacity = #friends + 1 for yourself
                bundle.putInt("capacity", Integer.parseInt(editText.getText().toString()) + 1);
                intent.putExtras(bundle);
                KeyboardManager.hideKeyboard(AddTourConfirmCapacity.this);
                startActivity(intent);
            }
        });
    }
}
