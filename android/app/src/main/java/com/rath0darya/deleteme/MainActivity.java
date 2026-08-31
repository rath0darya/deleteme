package com.rath0darya.deleteme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends AppCompatActivity {
    private FrameLayout container;
    private BottomNavigationView nav;
    private TextInputEditText input;
    private TextInputLayout inputLayout;
    private ArrayList<String> matches = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String XON = "https://api.xposedornot.com/v1/check-email/";
    private static final String BROKERS = "https://raw.githubusercontent.com/Persprotect/data-broker-opt-out-list/main/data-brokers.json";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_main);
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        bar.setNavigationIcon(null);
        container = findViewById(R.id.screenContainer);
        nav = findViewById(R.id.bottomNavigation);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_scan) { showScan(); return true; }
            if (item.getItemId() == R.id.nav_results) { showResults(); return true; }
            if (item.getItemId() == R.id.nav_settings) { showSettings(); return true; }
            return false;
        });
        showScan();
    }

    private void showScan() {
        nav.setSelectedItemId(R.id.nav_scan);
        getSupportActionBar();
        View v = getLayoutInflater().inflate(R.layout.screen_scan, container, false);
        container.removeAllViews(); container.addView(v);
        input = v.findViewById(R.id.identifierInput);
        inputLayout = v.findViewById(R.id.identifierLayout);
        MaterialButton find = v.findViewById(R.id.findButton);
        find.setOnClickListener(x -> scan(find));
    }

    private void scan(MaterialButton button) {
        String email = input.getText() == null ? "" : input.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayout.setError("Enter a valid email address"); return;
        }
        inputLayout.setError(null); button.setEnabled(false); button.setText("Checking…");
        executor.execute(() -> {
            try {
                ArrayList<String> found = breachScan(email);
                matches = found;
                runOnUiThread(() -> { button.setEnabled(true); button.setText("Find my data"); showResults(); });
            } catch (Exception e) {
                runOnUiThread(() -> { button.setEnabled(true); button.setText("Try again"); Toast.makeText(this, "Could not complete the check.", Toast.LENGTH_LONG).show(); });
            }
        });
    }

    private void showResults() {
        nav.setSelectedItemId(R.id.nav_results);
        View v = getLayoutInflater().inflate(R.layout.screen_results, container, false);
        container.removeAllViews(); container.addView(v);
        TextView title = v.findViewById(R.id.resultsTitle), sub = v.findViewById(R.id.resultsSubtitle);
        MaterialCardView empty = v.findViewById(R.id.emptyCard);
        LinearLayout list = v.findViewById(R.id.resultsContent);
        if (matches.isEmpty()) {
            title.setText("No confirmed matches");
            sub.setText("No match was returned by the checked breach source.");
            empty.setVisibility(View.VISIBLE);
        } else {
            title.setText(matches.size() + " confirmed breach match" + (matches.size() == 1 ? "" : "es"));
            sub.setText("These are reported breach matches. They are not proof that your data is currently public.");
            empty.setVisibility(View.GONE);
            for (String name : matches) addMatchCard(list, name);
        }
        LinearLayout removal = new LinearLayout(this); removal.setOrientation(LinearLayout.VERTICAL); removal.setPadding(0, 18, 0, 20);
        TextView h = new TextView(this); h.setText("Removal options"); h.setTextSize(20); h.setTextColor(getColor(R.color.text_primary)); h.setTypeface(null, 1); removal.addView(h);
        TextView d = new TextView(this); d.setText("Browse official opt-out routes and choose what you want to remove."); d.setTextSize(14); d.setTextColor(getColor(R.color.text_secondary)); d.setPadding(0, 4, 0, 8); removal.addView(d);
        MaterialButton b = new MaterialButton(this); b.setText("View removal directory"); b.setAllCaps(false); b.setOnClickListener(x -> showDirectory()); removal.addView(b);
        list.addView(removal);
    }

    private void addMatchCard(LinearLayout parent, String name) {
        MaterialCardView card = new MaterialCardView(this); card.setRadius(18); card.setCardElevation(0); card.setStrokeWidth(1); card.setStrokeColor(getColor(R.color.outline));
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(18, 16, 18, 16);
        TextView tag = new TextView(this); tag.setText("CONFIRMED MATCH"); tag.setTextSize(11); tag.setTextColor(Color.WHITE); tag.setTypeface(null,1); tag.setPadding(10,5,10,5); tag.setBackgroundColor(getColor(R.color.danger)); box.addView(tag, new LinearLayout.LayoutParams(-2, -2));
        TextView n = new TextView(this); n.setText(name); n.setTextSize(17); n.setTextColor(getColor(R.color.text_primary)); n.setTypeface(null,1); n.setPadding(0,10,0,3); box.addView(n);
        TextView d = new TextView(this); d.setText("Your email was reported in this breach."); d.setTextSize(14); d.setTextColor(getColor(R.color.text_secondary)); box.addView(d);
        card.addView(box); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=10; parent.addView(card,p);
    }

    private void showDirectory() {
        nav.setSelectedItemId(R.id.nav_results);
        ScrollView scroll = new ScrollView(this); LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(20,20,20,24); scroll.addView(root);
        TextView k = new TextView(this); k.setText("REMOVAL DIRECTORY"); k.setTextSize(12); k.setTextColor(getColor(R.color.primary)); k.setTypeface(null,1); root.addView(k);
        TextView h = new TextView(this); h.setText("Choose what to remove"); h.setTextSize(28); h.setTextColor(getColor(R.color.text_primary)); h.setTypeface(null,1); h.setPadding(0,8,0,5); root.addView(h);
        TextView d = new TextView(this); d.setText("Official opt-out routes. A route being listed does not mean your record was found there."); d.setTextSize(14); d.setTextColor(getColor(R.color.text_secondary)); d.setPadding(0,0,0,16); root.addView(d);
        container.removeAllViews(); container.addView(scroll);
        executor.execute(() -> { try { JSONArray a = brokerArray(); runOnUiThread(() -> { for(int i=0;i<a.length();i++){ JSONObject o=a.optJSONObject(i); if(o==null)continue; String n=first(o,"company","name","broker"),u=first(o,"optOutUrl","opt_out_url","optout_url","opt_out","url"); if(n.isEmpty()||u.isEmpty())continue; addBroker(root,n,u); } }); } catch(Exception e) { runOnUiThread(() -> Toast.makeText(this,"Removal directory unavailable.",Toast.LENGTH_LONG).show()); }});
    }

    private void addBroker(LinearLayout root,String name,String url){ MaterialCardView c=new MaterialCardView(this); c.setRadius(18); c.setCardElevation(0); c.setStrokeWidth(1); c.setStrokeColor(getColor(R.color.outline)); LinearLayout b=new LinearLayout(this); b.setOrientation(LinearLayout.VERTICAL); b.setPadding(17,15,17,15); TextView n=new TextView(this);n.setText(name);n.setTextSize(16);n.setTextColor(getColor(R.color.text_primary));n.setTypeface(null,1);b.addView(n);MaterialButton x=new MaterialButton(this);x.setText("Open official removal option");x.setAllCaps(false);x.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));}catch(Exception ignored){}});b.addView(x);c.addView(b);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=10;root.addView(c,p); }

    private void showSettings(){ nav.setSelectedItemId(R.id.nav_settings); View v=getLayoutInflater().inflate(R.layout.screen_settings,container,false);container.removeAllViews();container.addView(v); }

    private ArrayList<String> breachScan(String email)throws Exception{ArrayList<String> out=new ArrayList<>();String body=get(XON+URLEncoder.encode(email,"UTF-8")+"?details=true");JSONObject o=new JSONObject(body);JSONArray a=o.optJSONArray("breaches");if(a==null)a=o.optJSONArray("data");if(a==null)return out;for(int i=0;i<a.length();i++){Object x=a.get(i);String n=x instanceof JSONObject?((JSONObject)x).optString("name",((JSONObject)x).optString("title","Breach")):String.valueOf(x);if(!n.isEmpty()&&!out.contains(n))out.add(n);}return out;}
    private JSONArray brokerArray()throws Exception{Object r=new JSONTokener(get(BROKERS)).nextValue();if(r instanceof JSONArray)return (JSONArray)r;if(r instanceof JSONObject){JSONObject o=(JSONObject)r;JSONArray a=o.optJSONArray("data");if(a!=null)return a;return o.optJSONArray("brokers");}return new JSONArray();}
    private static String first(JSONObject o,String...keys){for(String k:keys){String v=o.optString(k,"");if(!v.trim().isEmpty())return v.trim();}return "";}
    private static String get(String u)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("Accept","application/json");int z=c.getResponseCode();if(z<200||z>=300)throw new IOException("HTTP "+z);InputStream i=c.getInputStream();ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=i.read(b))!=-1)o.write(b,0,n);i.close();return o.toString(StandardCharsets.UTF_8.name());}
    @Override protected void onDestroy(){executor.shutdownNow();super.onDestroy();}
}
