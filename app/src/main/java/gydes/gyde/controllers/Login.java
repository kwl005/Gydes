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
import com.google.android.gms.tasks.Task;
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
import gydes.gyde.models.*;

public class Login extends AppCompatActivity {

    public static DatabaseReference currentUserRef;
    public static boolean isGuide;
    private static String TAG = Login.class.getSimpleName();
    private static final int RC_SIGN_IN = 123;
    private SignInButton loginButton;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build()
    );

    private static final String[] days = {"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
    private static final String[] times = {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00",
        "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00",
        "22:00", "23:00"};

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

                if(!Login.isGuide) {
                    startActivity(new Intent(Login.this, HomeActivity.class));
                }
                else {
                    startActivity(new Intent(Login.this, GuideHome.class));
                }
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
                    Login.currentUserRef = usersRef.child(currentUser.getUid());
                    currentUserRef.child("displayName").setValue(currentUser.getDisplayName());
                    currentUserRef.child("uid").setValue(currentUser.getUid());
                    currentUserRef.child("email").setValue(currentUser.getEmail());
                    currentUserRef.child("isGuide").setValue(false);

                    final DatabaseReference traveler = currentUserRef.child(getString(R.string.firebase_trav_path));
                    traveler.child("avgRating").setValue(0);
                    traveler.child("numRatings").setValue(0);
                    for(int i = 0; i < days.length; i++) {
                        for(int j = 0; j < times.length; j++) {
                            DatabaseReference ref = traveler.child(getString(R.string.firebase_book_path)).child(days[i]).child(times[j]);
                            ref.child(getString(R.string.firebase_tID_path)).setValue(currentUser.getUid());
                            ref.child(getString(R.string.firebase_gID_path)).setValue("");
                            ref.child(getString(R.string.firebase_tour_path)).setValue(null);
                            ref.child(getString(R.string.firebase_sameasprev_path)).setValue(false);
                        }
                    }

                    final DatabaseReference guide = currentUserRef.child(getString(R.string.firebase_guide_path));
                    guide.child("tourIDs");
                    guide.child("bio").setValue("");
                    guide.child("avgRating").setValue(0);
                    guide.child("numRatings").setValue(0);
                    for(int i = 0; i < days.length; i++) {
                        for(int j = 0; j < times.length; j++) {
                            DatabaseReference ref = guide.child(getString(R.string.firebase_book_path)).child(days[i]).child(times[j]);
                            ref.child(getString(R.string.firebase_tID_path)).setValue("");
                            ref.child(getString(R.string.firebase_gID_path)).setValue(currentUser.getUid());
                            ref.child(getString(R.string.firebase_tour_path)).setValue(null);
                            ref.child(getString(R.string.firebase_sameasprev_path)).setValue(false);
                        }
                    }
                    for(int i = 0; i < days.length; i++) {
                        for(int j = 0; j < times.length; j++) {
                            traveler.child("schedule").child(days[i]).child(times[j]).setValue(false);
                        }
                    }
                    isGuide = false;
                }

                Login.currentUserRef = usersRef.child(currentUser.getUid());
                Login.currentUserRef.child("isGuide").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Login.isGuide = (boolean)dataSnapshot.getValue();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("Data Access", "Login.java: Error accessing isGuide upon login");
                    }
                });
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