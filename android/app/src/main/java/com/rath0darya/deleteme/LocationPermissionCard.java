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
 * In-app explanation and action surface shown before DeleteMe asks for location.
 * Only foreground location is requested. Approximate location is sufficient for
 * regional source selection, so the app continues to work when the user grants
 * coarse location only.
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

        title = text("Use your region to find relevant removal routes", 18, Color.rgb(25, 22, 30), true);
        root.addView(title, lp(0, 0, 0, 5));

        message = text("DeleteMe uses your approximate location only to select regional privacy and removal sources. It is not used as proof of identity.", 13, Color.rgb(100, 96, 110), false);
        root.addView(message, lp(0, 0, 0, 12));

        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);

        action = new MaterialButton(context);
        action.setText("Allow location");
        action.setTextAllCaps(false);
        action.setTextSize(14);
        action.setMinHeight(dp(46));
        action.setPadding(dp(18), 0, dp(18), 0);
        action.setOnClickListener(v -> requestLocation());
        row.addView(action, new LinearLayout.LayoutParams(0, dp(48), 1f));

        root.addView(row, lp(0, 0, 0, 0));
        addView(root, new LayoutParams(-1, -2));
        refresh();
    }

    private void requestLocation() {
        if (hasLocation()) {
            refresh();
            return;
        }

        boolean rationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (rationale) {
            new androidx.appcompat.app.AlertDialog.Builder(activity)
                    .setTitle("Why does DeleteMe need location?")
                    .setMessage("Your region helps DeleteMe show the right data-broker and privacy-removal routes. Approximate location is enough. Location is used while the app is in use and is not required for the actual identifier search.")
                    .setNegativeButton("Not now", null)
                    .setPositiveButton("Continue", (d, w) -> askSystemPermission())
                    .show();
        } else {
            askSystemPermission();
        }
    }

    private void askSystemPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
        action.setText("Waiting for permission…");
        action.setEnabled(false);
        postDelayed(this::refresh, 700);
        postDelayed(this::refresh, 1500);
    }

    public void refresh() {
        if (hasLocation()) {
            setCardBackgroundColor(Color.rgb(232, 250, 242));
            setStrokeColor(Color.rgb(166, 220, 199));
            title.setText("Regional location is enabled ✓");
            message.setText("DeleteMe can use your approximate region to prioritize relevant removal sources. You can change this permission in Android Settings at any time.");
            action.setText("Location enabled");
            action.setEnabled(false);
        } else {
            setCardBackgroundColor(Color.rgb(245, 240, 255));
            setStrokeColor(Color.rgb(205, 197, 235));
            title.setText("Use your region to find relevant removal routes");
            message.setText("DeleteMe uses your approximate location only to select regional privacy and removal sources. It is not used as proof of identity.");
            action.setEnabled(true);
            if (isPermanentlyDenied()) {
                action.setText("Open location settings");
                action.setOnClickListener(v -> openSettings());
            } else {
                action.setText("Allow location");
                action.setOnClickListener(v -> requestLocation());
            }
        }
    }

    private boolean hasLocation() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isPermanentlyDenied() {
        return !hasLocation()
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
