package gydes.gyde.controllers;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
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
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;

import gydes.gyde.R;

/**
 * Created by kelvinlui1 on 5/17/18.
 */

public class NavigationDrawerBuilder  {

    private static final String TAG = NavigationDrawerBuilder.class.getSimpleName();

    private enum DrawerItemConstant {
        PROFILE(1, "Account"),
        PAYMENTS(2, "Payments"),
        TOURS(3, "My Tours"),
        REPORT(4, "Report");

        private final int index;
        private final String name;

        private static DrawerItemConstant getItem(int index) {
            switch(index) {
                case 1:
                    return PROFILE;
                case 2:
                    return PAYMENTS;
                case 3:
                    return TOURS;
                case 4:
                    return REPORT;
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
        PrimaryDrawerItem profileItem = new PrimaryDrawerItem().withIcon(R.drawable.account).withSelectable(false).withIdentifier(DrawerItemConstant.PROFILE.getIndex()).withName(DrawerItemConstant.PROFILE.getName());
        PrimaryDrawerItem paymentItem = new PrimaryDrawerItem().withIcon(R.drawable.payments).withSelectable(false).withIdentifier(DrawerItemConstant.PAYMENTS.getIndex()).withName(DrawerItemConstant.PAYMENTS.getName());
        PrimaryDrawerItem reportItem = new PrimaryDrawerItem().withIcon(R.drawable.report).withSelectable(false).withIdentifier(DrawerItemConstant.REPORT.getIndex()).withName(DrawerItemConstant.REPORT.getName());
        PrimaryDrawerItem tourItem = new PrimaryDrawerItem().withIcon(R.drawable.tours).withSelectable(false).withIdentifier(DrawerItemConstant.TOURS.getIndex()).withName(DrawerItemConstant.TOURS.getName());

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
                        reportItem
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
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .withSelectionListEnabledForSingleProfile(false)
                .build();

        return header;
    }

    private static Drawer.OnDrawerItemClickListener getDrawerItemClickListener(final AppCompatActivity activity) {
        return new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                switch(DrawerItemConstant.getItem(position)) {
                    case PROFILE:
                        break;
                    case PAYMENTS:
                        activity.startActivity(new Intent(activity, PaymentActivity.class));
                        break;
                    case TOURS:
                        break;
                    case REPORT:
                        break;
                    default:
                        break;
                }

                return false;
            }
        };
    }
}













