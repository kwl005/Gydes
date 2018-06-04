package gydes.gyde.controllers;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by kelvinlui1 on 6/3/18.
 *
 * Simplifies keyboard operations.
 */

public class KeyboardManager {
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imgr != null) {
            imgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    public static void showKeyboard(Activity activity) {
        InputMethodManager imgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imgr != null) {
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
}
