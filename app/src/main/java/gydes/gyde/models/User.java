package gydes.gyde.models;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
     * @throws IllegalStateException if user singleton is initialized the second time.
     */
    public void init(FirebaseUser user) {
        if(userRef != null) {
            throw new IllegalStateException("User singleton has been initialized." +
                    "Please check for existing code for where it was initialized.");
        }

        uid = user.getUid();
        phoneNumber = user.getPhoneNumber();
        displayName = user.getDisplayName();
        email = user.getEmail();

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
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
        }

        this.displayName = displayName;
    }

    public void setEmail(String email) {
        if(userRef == null) {
            throw new IllegalStateException("Must initialize User signleton before setting its properties.");
        }

        // TODO: check email format
        if(this.email != null) {
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
