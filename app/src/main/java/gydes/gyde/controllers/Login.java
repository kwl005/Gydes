package gydes.gyde.controllers;

import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.*;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import gydes.gyde.R;

public class Login extends AppCompatActivity {

    private static String TAG = Login.class.getSimpleName();
    private static final int RC_SIGN_IN = 123;
    private SignInButton loginButton;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build()
    );

     // Listener for activity_login button
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
        setContentView(R.layout.activity_login);

        loginButton = (SignInButton) findViewById(R.id.login_button);
        loginButton.setOnClickListener(loginListener);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if(resultCode == RESULT_OK) {
                initializeNewUserInstance();
                startActivity(new Intent(Login.this, HomeActivity.class));
            } else {
                Log.d(TAG, "onActivityResult: result code " + resultCode);
            }
        }
    }

    private void initializeNewUserInstance() {
        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_users_path));
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(currentUser.getUid())) {
                    DatabaseReference currentUserRef = usersRef.child(currentUser.getUid());
                    currentUserRef.child("displayName").setValue(currentUser.getDisplayName());
                    currentUserRef.child("email").setValue(currentUser.getEmail());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: ");
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        // Delete super call to avoid an unknown bug
    }
}
