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

public class LocationPermissionCard extends MaterialCardView {
 private static final int REQUEST_LOCATION=8127;
 private final Activity activity;
 private final TextView title,message,status;
 private final MaterialButton action;

 public LocationPermissionCard(Context context){
  super(context);
  activity=(Activity)context;
  setRadius(dp(22)); setCardElevation(1); setStrokeWidth(dp(1)); setUseCompatPadding(false);
  setContentPadding(dp(17),dp(16),dp(17),dp(16));
  LinearLayout root=new LinearLayout(context); root.setOrientation(LinearLayout.VERTICAL);
  TextView e=text("LOCATION & PRIVACY",11,Color.rgb(91,46,255),true); e.setLetterSpacing(.08f);
  root.addView(e,lp(0,0,0,5));
  title=text("Use precise location for regional search",18,Color.rgb(25,22,30),true); root.addView(title,lp(0,0,0,5));
  message=text("DeleteMe uses your precise foreground location only while you use the app to select the correct regional privacy and removal sources. It is not used as proof of identity or requested in the background.",13,Color.rgb(100,96,110),false); root.addView(message,lp(0,0,0,9));
  status=text("Location not enabled",12,Color.rgb(91,46,255),true); root.addView(status,lp(0,0,0,11));
  action=new MaterialButton(context); action.setText("Use precise location"); action.setAllCaps(false); action.setTextSize(14); action.setMinHeight(dp(48)); action.setOnClickListener(v->requestLocation()); root.addView(action,lp(0,0,0,0));
  addView(root,new LayoutParams(-1,-2)); refresh();
 }
 private void requestLocation(){if(hasFine()){refresh();return;} if(hasCoarse()){showExplanation(true);return;} if(ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.ACCESS_FINE_LOCATION)||ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.ACCESS_COARSE_LOCATION))showExplanation(false);else ask();}
 private void showExplanation(boolean upgrade){new androidx.appcompat.app.AlertDialog.Builder(activity).setTitle(upgrade?"Precise location is needed":"Why does DeleteMe need precise location?").setMessage(upgrade?"DeleteMe currently has approximate location. This feature is configured for precise regional matching. Enable Precise location for DeleteMe in Android Settings.":"Your region determines which privacy and data-removal sources are relevant. DeleteMe requests foreground precise location only while you use the search. It does not request background location.").setNegativeButton("Not now",null).setPositiveButton(upgrade?"Open settings":"Continue",(d,w)->{if(upgrade)openSettings();else ask();}).show();}
 private void ask(){ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_LOCATION);action.setText("Waiting for permission…");action.setEnabled(false);postDelayed(this::refresh,700);postDelayed(this::refresh,1500);}
 public void refresh(){if(hasFine()){setCardBackgroundColor(Color.rgb(232,250,242));setStrokeColor(Color.rgb(166,220,199));title.setText("Precise location enabled ✓");message.setText("DeleteMe can use your precise foreground location to select regional removal sources. Location is only used while the app is in use.");status.setText("Ready for precise regional matching");action.setText("Location enabled");action.setEnabled(false);}else if(hasCoarse()){setCardBackgroundColor(Color.rgb(255,244,222));setStrokeColor(Color.rgb(231,194,122));title.setText("Approximate location enabled");message.setText("DeleteMe has approximate location, but this feature requires precise location for regional source selection.");status.setText("Precise location required");action.setText("Enable precise location");action.setEnabled(true);action.setOnClickListener(v->showExplanation(true));}else{setCardBackgroundColor(Color.rgb(245,240,255));setStrokeColor(Color.rgb(205,197,235));title.setText("Use precise location for regional search");message.setText("DeleteMe uses your precise foreground location only while you use the app to select the correct regional privacy and removal sources. It is not used as proof of identity or requested in the background.");status.setText("Location not enabled");action.setEnabled(true);action.setText(isPermanentlyDenied()?"Open location settings":"Use precise location");action.setOnClickListener(v->{if(isPermanentlyDenied())openSettings();else requestLocation();});}}
 private boolean hasFine(){return ContextCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;}
 private boolean hasCoarse(){return ContextCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED&&!hasFine();}
 private boolean isPermanentlyDenied(){return !hasFine()&&!hasCoarse()&&!ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.ACCESS_FINE_LOCATION)&&!ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.ACCESS_COARSE_LOCATION);}
 private void openSettings(){Intent i=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);i.setData(Uri.parse("package:"+activity.getPackageName()));activity.startActivity(i);postDelayed(this::refresh,500);}
 private TextView text(String s,int z,int c,boolean b){TextView v=new TextView(getContext());v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setGravity(Gravity.START);v.setTypeface(android.graphics.Typeface.DEFAULT,b?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL);return v;}
 private LinearLayout.LayoutParams lp(int w,int l,int t,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w==0?-1:w,-2);p.setMargins(l,t,0,b);return p;}
 private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
