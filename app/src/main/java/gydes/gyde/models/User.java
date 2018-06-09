package gydes.gyde.models;

import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

/**
 * Created by kelvinlui1 on 5/28/18.
 * A singleton for the User that set properties of the user and update to Firebase automatically.
 */

public enum User {
    INSTANCE;

    private String uid;
    private String phoneNumber;
    private String displayName;
    private String email;
    private String photoUrl;
    private DatabaseReference userRef;

    /**
     * Initialize user singleton. All properties can not be set before user instance has been initialized.
     * @param user Firebase user
     */
    public void init(FirebaseUser user) {
        uid = user.getUid();
        phoneNumber = user.getPhoneNumber();
        displayName = user.getDisplayName();
        email = user.getEmail();

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    public void setPhoneNumber(String phoneNumber) {
        if(userRef == null) {
            throw new IllegalStateException("Must initialize User signleton before setting its properties.");
        }

        // TODO: check format of phone number
        if(this.phoneNumber != null) {
            userRef.child("phoneNumber").setValue(phoneNumber);
        }
        this.phoneNumber = phoneNumber;
    }

    public void setDisplayName(String displayName) {
        if(userRef == null) {
            throw new IllegalStateException("Must initialize User signleton before setting its properties.");
        }

        if(this.displayName != null) {
            userRef.child("displayName").setValue(displayName);

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();
            FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates);
        }

        this.displayName = displayName;
    }

    public void setEmail(String email) {
        if(userRef == null) {
            throw new IllegalStateException("Must initialize User signleton before setting its properties.");
        }

        // TODO: check email format
        if(this.email != null) {
            FirebaseAuth.getInstance().getCurrentUser().updateEmail(email)
                .addOnCompleteListener((task) -> {
                    if (task.isSuccessful()) {
                        // TODO
                    }
                });
            userRef.child("email").setValue(email);
        }
        this.email = email;
    }

    public void setPhotoUrl(String photoUrl) {
        if(userRef == null) {
            throw new IllegalStateException("Must initialize User signleton before setting its properties.");
        }

        if(this.photoUrl != null) {
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photoUrl").setValue(photoUrl);
        }
        this.photoUrl = photoUrl;
    }


    public String getUid() {
        return uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
