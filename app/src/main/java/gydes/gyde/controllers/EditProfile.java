package gydes.gyde.controllers;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import gydes.gyde.R;
import gydes.gyde.models.User;

public class EditProfile extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Login.isGuide) {
            setContentView(R.layout.activity_edit_profile_guide);
        } else {
            setContentView(R.layout.activity_edit_profile);
        }

        Login.currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final EditText displayNameBox = findViewById(R.id.display_name_box);
                final EditText emailBox = findViewById(R.id.email_box);
                final EditText phoneNumBox = findViewById(R.id.phone_number_box);
                final EditText bioBox = findViewById(R.id.bio_box);

                displayNameBox.setText((String)dataSnapshot.child(getString(R.string.firebase_displayname_path)).getValue());
                emailBox.setText((String)dataSnapshot.child(getString(R.string.firebase_email_path)).getValue());
                phoneNumBox.setText((String)dataSnapshot.child(getString(R.string.firebase_phonenumber_path)).getValue());

                if(Login.isGuide) {
                    bioBox.setText((String)dataSnapshot.child(getString(R.string.firebase_guide_path)).child(getString(R.string.firebase_bio_path)).getValue());
                }

                findViewById(R.id.save_profile_button).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        User.INSTANCE.setDisplayName(displayNameBox.getText().toString());
                        User.INSTANCE.setEmail(emailBox.getText().toString());
                        User.INSTANCE.setPhoneNumber(phoneNumBox.getText().toString());
//                        Login.currentUserRef.child(getString(R.string.firebase_displayname_path)).setValue(displayNameBox.getText().toString());
//                        Login.currentUserRef.child(getString(R.string.firebase_email_path)).setValue(emailBox.getText().toString());
//                        Login.currentUserRef.child(getString(R.string.firebase_phonenumber_path)).setValue(phoneNumBox.getText().toString());
                        if(Login.isGuide) {
                            Login.currentUserRef.child(getString(R.string.firebase_guide_path)).child(getString(R.string.firebase_bio_path)).setValue(bioBox.getText().toString());
                        }
                        Toast toast = Toast.makeText(EditProfile.this, "Updating profile...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("EditProfile", "error accessing user account: " + databaseError.getCode());
            }
        });
    }

}
