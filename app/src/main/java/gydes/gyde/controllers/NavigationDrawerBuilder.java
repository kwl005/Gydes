package gydes.gyde.controllers;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;

import gydes.gyde.R;
import gydes.gyde.models.User;

/**
 * Created by kelvinlui1 on 5/17/18.
 */

public class NavigationDrawerBuilder {

    private static final String TAG = NavigationDrawerBuilder.class.getSimpleName();
    private static final String TO_TRAV_STR = "To Traveler's View";
    private static final String TO_GUIDE_STR = "To Guide's View";

    private enum DrawerItemConstant {
        PROFILE(1, "Account"),
        PAYMENTS(2, "Payments"),
        BOOKINGS(3, "My Bookings"),
        TOGGLE(4, "Toggle"),
        LOGOUT(5, "Logout");

        private final int index;
        private final String name;

        private static DrawerItemConstant getItem(int index) {
            switch (index) {
                case 1:
                    return PROFILE;
                case 2:
                    return PAYMENTS;
                case 3:
                    return BOOKINGS;
                case 4:
                    return TOGGLE;
                case 5:
                    return LOGOUT;
                default:
                    throw new IndexOutOfBoundsException("DrawerItemConstant: index " + index + " does not exist.");
            }
        }

        DrawerItemConstant(int index, String name) {
            this.index = index;
            this.name = name;
        }

        private String getName() {
            return name;
        }

        private int getIndex() {
            return index;
        }
    }

    public static Drawer build(final AppCompatActivity activity, final Bundle savedInstanceState) {
        // Set up items in the drawer
        PrimaryDrawerItem profileItem = new PrimaryDrawerItem().withSelectable(false).withIdentifier(DrawerItemConstant.PROFILE.getIndex()).withName(DrawerItemConstant.PROFILE.getName());
        PrimaryDrawerItem paymentItem = new PrimaryDrawerItem().withSelectable(false).withIdentifier(DrawerItemConstant.PAYMENTS.getIndex()).withName(DrawerItemConstant.PAYMENTS.getName());
        PrimaryDrawerItem tourItem = new PrimaryDrawerItem().withSelectable(false).withIdentifier(DrawerItemConstant.BOOKINGS.getIndex()).withName(DrawerItemConstant.BOOKINGS.getName());
        PrimaryDrawerItem toggleItem = new PrimaryDrawerItem().withSelectable(false).withIdentifier(DrawerItemConstant.TOGGLE.getIndex());
        PrimaryDrawerItem logoutItem = new PrimaryDrawerItem().withSelectable(false).withIdentifier(DrawerItemConstant.LOGOUT.getIndex()).withName(DrawerItemConstant.LOGOUT.getName());
        if (Login.isGuide) {
            toggleItem.withName(TO_TRAV_STR);
        } else {
            toggleItem.withName(TO_GUIDE_STR);
        }

        // Setup profile image
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                // TODO
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }
                return super.placeholder(ctx, tag);
            }
        });

        // Build the drawer
        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withSavedInstance(savedInstanceState)
                .withAccountHeader(getAccountHeader(activity, savedInstanceState))
                .withActionBarDrawerToggle(true)
                .withTranslucentStatusBar(false)
                .withSliderBackgroundColorRes(R.color.gydeBlue)
                .addDrawerItems(
                        profileItem,
                        paymentItem,
                        tourItem,
                        toggleItem,
                        logoutItem
                )
                .withMultiSelect(false)
                .withSelectedItem(-1)
                .withOnDrawerItemClickListener(getDrawerItemClickListener(activity))
                .build();

        return result;
    }

    private static AccountHeader getAccountHeader(final AppCompatActivity activity, final Bundle savedInstanceState) {
        // Get user's information
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String displayName = user.getDisplayName() == null ? "Anonymous" : user.getDisplayName();
        String email = user.getEmail();
        //String photoUrl = user.getPhotoUrl().getPath();

        // Create a profile header
        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(activity)
                .withSavedInstance(savedInstanceState)
                .withHeaderBackground(R.color.gydeYellow)
                .withTextColorRes(R.color.gydeBlue)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName("Hi! " + displayName)
                                .withEmail(email)
                )
                .withOnAccountHeaderListener((view, profile, current) -> {
                    return false;
                })
                .withSelectionListEnabledForSingleProfile(false)
                .build();

//        setupProfileChangeListener(header);

        return header;
    }

//    private static void setupProfileChangeListener(AccountHeader header) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
//        userRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                //
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                switch(dataSnapshot.getKey()) {
//                    case "displayName":
//                        header.getActiveProfile().withName((String)dataSnapshot.getValue());
//                        break;
//                    default:
//                        break;
//                }
//                Log.d(TAG, "onChildChanged: " + dataSnapshot);
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    private static Drawer.OnDrawerItemClickListener getDrawerItemClickListener(final AppCompatActivity activity) {
        return (view, position, drawerItem) -> {
            switch (DrawerItemConstant.getItem(position)) {
                case PROFILE:
                    activity.startActivity(new Intent(activity, EditProfile.class));
                    break;
                case PAYMENTS:
                    activity.startActivity(new Intent(activity, PaymentActivity.class));
                    break;
                case BOOKINGS:
                    activity.startActivity(new Intent(activity, MyBookings.class));
                    break;
                case TOGGLE:
                    if (!Login.isGuide) {
                        Login.isGuide = true;
                        Login.currentUserRef.child("isGuide").setValue(true);
                        activity.startActivity(new Intent(activity, GuideHome.class));
                    } else {
                        Login.isGuide = false;
                        Login.currentUserRef.child("isGuide").setValue(false);
                        activity.startActivity(new Intent(activity, HomeActivity.class));
                    }
                    activity.finish();
                    break;
                case LOGOUT:
                    User.INSTANCE.logout();
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(R.string.logout_confirmation)
                            .setPositiveButton(R.string.yes_txt, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                activity.startActivity(new Intent(activity, Login.class));
                                activity.finish();
                            })
                            .setNegativeButton(R.string.no_txt, ((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }));
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();
                    break;
                default:
                    break;
            }
            return false;
        };
    }
}
