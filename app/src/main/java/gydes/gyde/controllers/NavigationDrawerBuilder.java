package gydes.gyde.controllers;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondarySwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import gydes.gyde.R;

/**
 * Created by kelvinlui1 on 5/17/18.
 */

class NavigationDrawerBuilder  {

    private enum DrawerItemConstant {
        PROFILE(1, "Profile"),
        PAYMENT(2, "Payment"),
        SCHEDULE(3, "Schedule"),
        TOURS(4, "Tours"),
        REPORT(5, "Report"),
        LOGOUT(6, "Logout"),
        TOGGLE(7, "Guide");

        private final int index;
        private final String name;

        private static DrawerItemConstant getItem(int index) {
            switch(index) {
                case 1:
                    return PROFILE;
                case 2:
                    return PAYMENT;
                case 3:
                    return SCHEDULE;
                case 4:
                    return TOURS;
                case 5:
                    return REPORT;
                case 6:
                    return LOGOUT;
                case 7:
                    return TOGGLE;
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

    static Drawer build(final AppCompatActivity activity, final Bundle savedInstanceState) {
        // Set up items in the drawer
        PrimaryDrawerItem profileItem = new PrimaryDrawerItem().withIdentifier(DrawerItemConstant.PROFILE.getIndex()).withName(DrawerItemConstant.PROFILE.getName());
        PrimaryDrawerItem paymentItem = new PrimaryDrawerItem().withIdentifier(DrawerItemConstant.PAYMENT.getIndex()).withName(DrawerItemConstant.PAYMENT.getName());
        PrimaryDrawerItem reportItem = new PrimaryDrawerItem().withIdentifier(DrawerItemConstant.REPORT.getIndex()).withName(DrawerItemConstant.REPORT.getName());
        PrimaryDrawerItem logoutItem = new PrimaryDrawerItem().withIdentifier(DrawerItemConstant.LOGOUT.getIndex()).withName(DrawerItemConstant.LOGOUT.getName());
        PrimaryDrawerItem scheduleItem = new PrimaryDrawerItem().withIdentifier(DrawerItemConstant.SCHEDULE.getIndex()).withName(DrawerItemConstant.SCHEDULE.getName());
        PrimaryDrawerItem tourItem = new PrimaryDrawerItem().withIdentifier(DrawerItemConstant.TOURS.getIndex()).withName(DrawerItemConstant.TOURS.getName());
        SecondarySwitchDrawerItem toggle = new SecondarySwitchDrawerItem().withIdentifier(5).withName("Guide");

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
                .withDisplayBelowStatusBar(false)
                .withTranslucentStatusBar(false)
                .withTranslucentNavigationBar(false)
                .addDrawerItems(
                        profileItem,
                        paymentItem,
                        tourItem,
                        scheduleItem,
                        reportItem,
                        logoutItem,
                        new DividerDrawerItem(),
                        toggle
                )
                .withSelectedItem(-1)
                .withOnDrawerItemClickListener(getDrawerItemClickListener(activity))
                .build();

        // Setup action bar
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.app_name);
        actionBar.setIcon(R.drawable.gyde_logo);

        return result;
    }

    private static AccountHeader getAccountHeader(final AppCompatActivity activity, final Bundle savedInstanceState) {
        // Get user's information
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String displayName = user.getDisplayName() == null ? "Anonymous" : user.getDisplayName();
        String email = user.getEmail();
        String photoUrl = user.getPhotoUrl().getPath();

        // Create a profile header
        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(activity)
                .withSavedInstance(savedInstanceState)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(displayName)
                                .withEmail(email)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .build();

        return header;
    }

    private static Drawer.OnDrawerItemClickListener getDrawerItemClickListener(final AppCompatActivity activity) {
        return new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                drawerItem.withSetSelected(false);
                switch(DrawerItemConstant.getItem(position)) {
                    case PROFILE:
                        break;
                    case PAYMENT:
                        break;
                    case SCHEDULE:
                        break;
                    case TOURS:
                        break;
                    case REPORT:
                        break;
                    case LOGOUT:
                        FirebaseAuth.getInstance().signOut();
                        activity.startActivity(new Intent(activity, Login.class));
                        activity.finish();
                    case TOGGLE:
                        break;
                    default:
                        break;
                }

                return false;
            }
        };
    }
}













