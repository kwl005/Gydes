package gydes.gyde;

import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.*;

import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

//    private Button cardIOButton;
    private static final int RC_SIGN_IN = 123;
    private SignInButton loginButton;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build()
    );

     // Listener for login button
    private OnClickListener loginListener = new OnClickListener() {
         @Override
         public void onClick(View view) {
             startActivityForResult(
                     AuthUI.getInstance()
                             .createSignInIntentBuilder()
                             .setAvailableProviders(providers)
                             .build(),
                     RC_SIGN_IN
             );
         }
     };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (SignInButton) findViewById(R.id.login_button);
        loginButton.setOnClickListener(loginListener);

//        cardIOButton = (Button) findViewById(R.id.cardIOPage);
//        cardIOButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v){
//                Intent intent = new Intent(MainActivity.this, CardIOExample.class);
//                startActivity(intent);
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if(resultCode == RESULT_OK) {
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            } else {
                // Check error code
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        // Delete super call to avoid an unknown bug
    }
}
