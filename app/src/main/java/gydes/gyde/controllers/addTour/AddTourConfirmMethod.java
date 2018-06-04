package gydes.gyde.controllers.addTour;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.mikepenz.materialdrawer.Drawer;
import gydes.gyde.R;
import gydes.gyde.controllers.KeyboardManager;
import gydes.gyde.controllers.NavigationDrawerBuilder;

/**
 * Created by kelvinlui1 on 6/3/18.
 */

public class AddTourConfirmMethod extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tour_confirm_method);

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

        // Show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final CharSequence[] methods = {"Walking", "Driving", "Back"};
        builder.setTitle("Select your travel method")
                .setItems(methods, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle bundle = getIntent().getExtras();
                        switch(i) {
                            case 0:
                                bundle.putString("walking", "true");
                                break;
                            case 1:
                                bundle.putString("walking", "false");
                                break;
                            case 2:
                                finish();
                                break;
                            default:
                                throw new IllegalStateException("Selected method must be either index 0, 1 or 2.");
                        }

                        // TODO
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
