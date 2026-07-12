package com.termux.x11.utils;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import android.view.ViewGroup;
import android.graphics.Rect;
import android.widget.FrameLayout;
import android.view.View;
import android.app.Activity;

import com.termux.x11.MainActivity;
import com.termux.x11.Prefs;
import com.termux.x11.R;

public class FullscreenWorkaround {
    public static void assistActivity(Activity activity) {
        new FullscreenWorkaround(activity);
    }

    private final Activity mActivity;
    private int usableHeightPrevious;
    private static boolean x11Focused = true;

    public static void setX11Focused(boolean focused) {
        x11Focused = focused;
    }

    public static boolean getX11Focused() {
        return x11Focused;
    }

    private FullscreenWorkaround(Activity activity) {
        mActivity = activity;
        FrameLayout content = activity.findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
    }

    private void possiblyResizeChildOfContent() {
        Prefs p = MainActivity.getPrefs();
        if (
                !mActivity.hasWindowFocus() ||
                !((mActivity.getWindow().getAttributes().flags & FLAG_FULLSCREEN) == FLAG_FULLSCREEN) ||
                !p.Reseed.get() || !p.fullscreen.get() || SamsungDexUtils.checkDeXEnabled(mActivity)
        )
            return;

        // Find the actual content FrameLayout (the one that holds the LorieView)
        FrameLayout content = mActivity.findViewById(R.id.id_display_window);
        if (content == null) return;   // safety check

        ViewGroup.LayoutParams layoutParams = content.getLayoutParams();

        int usableHeightNow = computeUsableHeight(content);
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = content.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard/4)) {
                layoutParams.height = usableHeightSansKeyboard - heightDifference;
            } else {
                layoutParams.height = usableHeightSansKeyboard;
            }
            content.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight(View v) {
        Rect r = new Rect();
        v.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }
}

