package com.rath0darya.deleteme;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class LocationPermissionCard extends MaterialCardView {
    private static final int REQUEST_LOCATION = 8127;
    private final Activity activity;
    private final TextView title, message, status;
    private final MaterialButton action;

    public LocationPermissionCard(Context context) {
        super(context);
        activity = findActivity(context);
        setRadius(dp(22));
        setCardElevation(1);
        setStrokeWidth(dp(1));
        setUseCompatPadding(false);
        setContentPadding(dp(17), dp(16), dp(17), dp(16));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        TextView eyebrow = text("LOCATION & PRIVACY", 11, Color.rgb(91, 46, 255), true);
        eyebrow.setLetterSpacing(.08f);
        root.addView(eyebrow, lp(0, 0, 0, 5));

        title = text("Use precise location for regional search", 18, Color.rgb(25, 22, 30), true);
        root.addView(title, lp(0, 0, 0, 5));

        message = text("DeleteMe uses precise foreground location only while you use the app to select relevant regional privacy and removal sources. It is not used as proof of identity or requested in the background.", 13, Color.rgb(100, 96, 110), false);
        root.addView(message, lp(0, 0, 0, 9));

        status = text("Location not enabled", 12, Color.rgb(91, 46, 255), true);
        root.addView(status, lp(0, 0, 0, 11));

        action = new MaterialButton(context);
        action.setText("Use precise location");
        action.setAllCaps(false);
        action.setTextSize(14);
        action.setMinHeight(dp(48));
        action.setOnClickListener(v -> requestLocation());
        root.addView(action, lp(0, 0, 0, 0));

        addView(root, new LayoutParams(-1, -2));
        refresh();
    }

    private static Activity findActivity(Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) return (Activity) current;
            Context next = ((ContextWrapper) current).getBaseContext();
            if (next == current) break;
            current = next;
        }
        return null;
    }

    private void requestLocation() {
        if (activity == null) {
            status.setText("Open DeleteMe from an Android Activity to enable location");
            return;
        }
        if (hasFine()) {
            refresh();
            return;
        }
        if (hasCoarse()) {
            showExplanation(true);
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            showExplanation(false);
        } else {
            ask();
        }
    }

    private void showExplanation(boolean upgrade) {
        if (activity == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle(upgrade ? "Precise location is needed" : "Why does DeleteMe need precise location?")
                .setMessage(upgrade
                        ? "DeleteMe currently has approximate location. Enable Precise location for regional source matching."
                        : "Your region helps DeleteMe select relevant privacy and removal sources. Location is requested only while you use the app and is not requested in the background.")
                .setNegativeButton("Not now", null)
                .setPositiveButton(upgrade ? "Open settings" : "Continue", (d, w) -> {
                    if (upgrade) openSettings(); else ask();
                }).show();
    }

    private void ask() {
        if (activity == null) return;
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION);
        action.setText("Waiting for permission…");
        action.setEnabled(false);
        postDelayed(this::refresh, 700);
        postDelayed(this::refresh, 1500);
    }

    public void refresh() {
        if (hasFine()) {
            setCardBackgroundColor(Color.rgb(232, 250, 242));
            setStrokeColor(Color.rgb(166, 220, 199));
            title.setText("Precise location enabled");
            message.setText("DeleteMe can use precise foreground location for regional source matching while the app is in use.");
            status.setText("Ready for precise regional matching");
            action.setText("Location enabled");
            action.setEnabled(false);
        } else if (hasCoarse()) {
            setCardBackgroundColor(Color.rgb(255, 244, 222));
            setStrokeColor(Color.rgb(231, 194, 122));
            title.setText("Approximate location enabled");
            message.setText("DeleteMe has approximate location. Precise location is required for this regional matching feature.");
            status.setText("Precise location required");
            action.setText("Enable precise location");
            action.setEnabled(true);
            action.setOnClickListener(v -> showExplanation(true));
        } else {
            setCardBackgroundColor(Color.rgb(245, 240, 255));
            setStrokeColor(Color.rgb(205, 197, 235));
            title.setText("Use precise location for regional search");
            message.setText("DeleteMe uses precise foreground location only while you use the app to select relevant regional privacy and removal sources. It is not used as proof of identity or requested in the background.");
            status.setText("Location not enabled");
            action.setEnabled(activity != null);
            action.setText(isPermanentlyDenied() ? "Open location settings" : "Use precise location");
            action.setOnClickListener(v -> {
                if (isPermanentlyDenied()) openSettings(); else requestLocation();
            });
        }
    }

    private boolean hasFine() {
        return activity != null && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCoarse() {
        return activity != null
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && !hasFine();
    }

    private boolean isPermanentlyDenied() {
        return activity != null && !hasFine() && !hasCoarse()
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void openSettings() {
        if (activity == null) return;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivity(intent);
        postDelayed(this::refresh, 500);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(getContext());
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private LinearLayout.LayoutParams lp(int width, int left, int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width == 0 ? -1 : width, -2);
        params.setMargins(left, top, 0, bottom);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
