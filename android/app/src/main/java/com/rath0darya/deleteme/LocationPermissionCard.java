package com.rath0darya.deleteme;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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

/**
 * DeleteMe foreground-location permission surface.
 * Precise location is requested because regional source selection is a core
 * part of the current search workflow. Android 12+ requires fine and coarse
 * to be requested together, so the system can let the user choose Precise.
 */
public class LocationPermissionCard extends MaterialCardView {
    private static final int REQUEST_LOCATION = 8127;
    private final Activity activity;
    private final TextView title;
    private final TextView message;
    private final MaterialButton action;

    public LocationPermissionCard(Context context) {
        super(context);
        activity = (Activity) context;
        setRadius(dp(22));
        setCardElevation(1);
        setStrokeWidth(dp(1));
        setStrokeColor(Color.rgb(205, 197, 235));
        setCardBackgroundColor(Color.rgb(245, 240, 255));
        setUseCompatPadding(false);
        setContentPadding(dp(17), dp(16), dp(17), dp(16));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        TextView eyebrow = text("LOCATION & PRIVACY", 11, Color.rgb(91, 46, 255), true);
        eyebrow.setLetterSpacing(0.08f);
        root.addView(eyebrow, lp(0, 0, 0, 5));

        title = text("Use precise location for regional search", 18, Color.rgb(25, 22, 30), true);
        root.addView(title, lp(0, 0, 0, 5));

        message = text("DeleteMe uses your precise foreground location only while you use the app to select the correct regional privacy and removal sources. It is not used as proof of identity and is not requested in the background.", 13, Color.rgb(100, 96, 110), false);
        root.addView(message, lp(0, 0, 0, 12));

        action = new MaterialButton(context);
        action.setText("Use precise location");
        action.setTextAllCaps(false);
        action.setTextSize(14);
        action.setMinHeight(dp(48));
        action.setPadding(dp(18), 0, dp(18), 0);
        action.setOnClickListener(v -> requestLocation());
        root.addView(action, lp(0, 0, 0, 0));

        addView(root, new LayoutParams(-1, -2));
        refresh();
    }

    private void requestLocation() {
        if (hasFineLocation()) {
            refresh();
            return;
        }

        if (hasCoarseOnly()) {
            new androidx.appcompat.app.AlertDialog.Builder(activity)
                    .setTitle("Precise location is needed")
                    .setMessage("DeleteMe uses your precise location to select the correct regional search and removal sources. Android currently gives DeleteMe approximate location only. Choose Precise location in the next system dialog.")
                    .setNegativeButton("Use approximate", (d, w) -> refresh())
                    .setPositiveButton("Continue", (d, w) -> askSystemPermission())
                    .show();
            return;
        }

        boolean rationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (rationale) {
            new androidx.appcompat.app.AlertDialog.Builder(activity)
                    .setTitle("Why does DeleteMe need precise location?")
                    .setMessage("Your region determines which privacy and data-removal sources are relevant. DeleteMe requests foreground precise location only while you use the search. It does not request background location.")
                    .setNegativeButton("Not now", null)
                    .setPositiveButton("Continue", (d, w) -> askSystemPermission())
                    .show();
        } else {
            askSystemPermission();
        }
    }

    private void askSystemPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION);
        action.setText("Waiting for permission…");
        action.setEnabled(false);
        postDelayed(this::refresh, 700);
        postDelayed(this::refresh, 1500);
    }

    public void refresh() {
        if (hasFineLocation()) {
            setCardBackgroundColor(Color.rgb(232, 250, 242));
            setStrokeColor(Color.rgb(166, 220, 199));
            title.setText("Precise location is enabled ✓");
            message.setText("DeleteMe can use your precise foreground location to select regional removal sources. Location is only used while the app is in use. You can change this permission in Android Settings at any time.");
            action.setText("Location enabled");
            action.setEnabled(false);
            return;
        }

        if (hasCoarseOnly()) {
            setCardBackgroundColor(Color.rgb(255, 244, 222));
            setStrokeColor(Color.rgb(231, 194, 122));
            title.setText("Approximate location is enabled");
            message.setText("DeleteMe has approximate location, but this search is configured to use precise location for regional source selection. Upgrade the permission to Precise when you want region-aware results.");
            action.setText("Enable precise location");
            action.setEnabled(true);
            action.setOnClickListener(v -> requestLocation());
            return;
        }

        setCardBackgroundColor(Color.rgb(245, 240, 255));
        setStrokeColor(Color.rgb(205, 197, 235));
        title.setText("Use precise location for regional search");
        message.setText("DeleteMe uses your precise foreground location only while you use the app to select the correct regional privacy and removal sources. It is not used as proof of identity and is not requested in the background.");
        action.setEnabled(true);
        if (isPermanentlyDenied()) {
            action.setText("Open location settings");
            action.setOnClickListener(v -> openSettings());
        } else {
            action.setText("Use precise location");
            action.setOnClickListener(v -> requestLocation());
        }
    }

    private boolean hasFineLocation() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCoarseOnly() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && !hasFineLocation();
    }

    private boolean isPermanentlyDenied() {
        return !hasCoarseOnly() && !hasFineLocation()
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivity(intent);
        postDelayed(this::refresh, 500);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView v = new TextView(getContext());
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(Gravity.START);
        v.setTypeface(android.graphics.Typeface.DEFAULT, bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        return v;
    }

    private LinearLayout.LayoutParams lp(int w, int l, int t, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w == 0 ? -1 : w, -2);
        p.setMargins(l, t, 0, b);
        return p;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
