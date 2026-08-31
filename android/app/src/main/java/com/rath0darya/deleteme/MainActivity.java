package com.rath0darya.deleteme;

import android.content.Intent;
import android.graphics.Color;
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

    private static final String[][] INDIA_SOURCES = {
        {"Truecaller", "Caller ID / phone directory", "https://www.truecaller.com/unlisting", "Unlist a searchable phone number"},
        {"Sulekha", "Local-services platform", "https://www.sulekha.com/collateral/privacy", "Privacy rights and deletion request"},
        {"Shaadi.com", "Matrimonial / profile directory", "https://support.shaadi.com/support/solutions/articles/48000755467-i-want-to-hide-or-delete-my-profile", "Hide or permanently delete profile"},
        {"Jeevansathi", "Matrimonial / profile directory", "https://www.jeevansathi.com/privacy-policy", "Delete profile and exercise privacy rights"},
        {"BharatMatrimony", "Matrimonial / profile directory", "https://paymentsnewstage.bharatmatrimony.com/faq.php", "Delete profile from account settings"},
        {"Naukri FastForward", "Recruitment / resume directory", "https://resume.naukri.com/frequently-asked-questions-faq/can-i-delete-my-profile-from-your-website-once-i-get-a-job/", "Delete profile or hide recruiter visibility"},
        {"MarkSpace Media Pvt Ltd", "Registered data broker", "mailto:privacy@markspacemedia.com", "Government-listed California data broker; India-based company"}
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_main);
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        bar.setTitle("DeleteMe");
        container = findViewById(R.id.screenContainer);
        nav = findViewById(R.id.bottomNavigation);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_scan) { showScan(); return true; }
            if (item.getItemId() == R.id.nav_results) { showResults(); return true; }
            if (item.getItemId() == R.id.nav_settings) { showSettings(); return true; }
            return false;
        });
        nav.setSelectedItemId(R.id.nav_scan);
    }

    private void showScan() {
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
                matches = breachScan(email);
                runOnUiThread(() -> { button.setEnabled(true); button.setText("Find my data  →"); showResults(); });
            } catch (Exception e) {
                runOnUiThread(() -> { button.setEnabled(true); button.setText("Try again"); Toast.makeText(this, "Could not complete the check.", Toast.LENGTH_LONG).show(); });
            }
        });
    }

    private void showResults() {
        View v = getLayoutInflater().inflate(R.layout.screen_results, container, false);
        container.removeAllViews(); container.addView(v);
        TextView title = v.findViewById(R.id.resultsTitle), sub = v.findViewById(R.id.resultsSubtitle);
        TextView stat = v.findViewById(R.id.breachStat);
        MaterialCardView empty = v.findViewById(R.id.emptyCard);
        LinearLayout list = v.findViewById(R.id.resultsContent);
        MaterialButton directory = v.findViewById(R.id.directoryButton);
        stat.setText(String.valueOf(matches.size()));
        directory.setOnClickListener(x -> showDirectory());
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
    }

    private void addMatchCard(LinearLayout parent, String name) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(18); card.setCardElevation(0); card.setStrokeWidth(1); card.setStrokeColor(getColor(R.color.danger));
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(18, 16, 18, 16);
        TextView tag = new TextView(this); tag.setText("CONFIRMED MATCH"); tag.setTextSize(11); tag.setTextColor(Color.WHITE); tag.setTypeface(null,1); tag.setPadding(10,5,10,5); tag.setBackgroundColor(getColor(R.color.danger)); box.addView(tag, new LinearLayout.LayoutParams(-2,-2));
        TextView n = new TextView(this); n.setText(name); n.setTextSize(17); n.setTextColor(getColor(R.color.text_primary)); n.setTypeface(null,1); n.setPadding(0,10,0,3); box.addView(n);
        TextView d = new TextView(this); d.setText("Your email was reported in this breach source."); d.setTextSize(14); d.setTextColor(getColor(R.color.text_secondary)); box.addView(d);
        card.addView(box); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=10; parent.addView(card,p);
    }

    private void showDirectory() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(20,20,20,28); scroll.addView(root);
        TextView k = new TextView(this); k.setText("REMOVAL DIRECTORY"); k.setTextSize(12); k.setTextColor(getColor(R.color.primary)); k.setTypeface(null,1); k.setLetterSpacing(.10f); root.addView(k);
        TextView h = new TextView(this); h.setText("India-first data removal"); h.setTextSize(29); h.setTextColor(getColor(R.color.text_primary)); h.setTypeface(null,1); h.setPadding(0,8,0,5); root.addView(h);
        TextView d = new TextView(this); d.setText("Verified official routes for Indian services, followed by the broader global broker registry. A listing does not mean your record was found there."); d.setTextSize(14); d.setTextColor(getColor(R.color.text_secondary)); d.setPadding(0,0,0,16); root.addView(d);

        addSectionHeader(root, "🇮🇳  INDIA SOURCES", "Data-holding services, directories and the one currently verified registered data-broker entry.");
        for (String[] s : INDIA_SOURCES) addIndiaCard(root, s[0], s[1], s[2], s[3]);

        addSectionHeader(root, "GLOBAL BROKER REGISTRY", "Additional international opt-out routes from the public broker registry.");
        container.removeAllViews(); container.addView(scroll);
        executor.execute(() -> {
            try {
                JSONArray a = brokerArray();
                runOnUiThread(() -> {
                    for (int i=0;i<a.length();i++) {
                        JSONObject o=a.optJSONObject(i); if(o==null) continue;
                        String n=first(o,"company","name","broker"),u=first(o,"optOutUrl","opt_out_url","optout_url","opt_out","url");
                        if(n.isEmpty()||u.isEmpty()) continue;
                        addBroker(root,n,u);
                    }
                });
            } catch(Exception e) { runOnUiThread(() -> Toast.makeText(this,"Global directory unavailable. India sources remain available.",Toast.LENGTH_LONG).show()); }
        });
    }

    private void addSectionHeader(LinearLayout root,String title,String detail){
        TextView h=new TextView(this); h.setText(title); h.setTextSize(13); h.setTextColor(getColor(R.color.primary)); h.setTypeface(null,1); h.setPadding(0,14,0,3); root.addView(h);
        TextView d=new TextView(this); d.setText(detail); d.setTextSize(12); d.setTextColor(getColor(R.color.text_secondary)); d.setPadding(0,0,0,8); root.addView(d);
    }

    private void addIndiaCard(LinearLayout root,String name,String category,String url,String note){
        MaterialCardView c=new MaterialCardView(this); c.setRadius(18); c.setCardElevation(0); c.setStrokeWidth(1); c.setStrokeColor(getColor(R.color.india)); c.setCardBackgroundColor(getColor(R.color.india_container));
        LinearLayout b=new LinearLayout(this); b.setOrientation(LinearLayout.VERTICAL); b.setPadding(17,15,17,15);
        TextView n=new TextView(this); n.setText(name); n.setTextSize(17); n.setTextColor(getColor(R.color.text_primary)); n.setTypeface(null,1); b.addView(n);
        TextView cat=new TextView(this); cat.setText(category); cat.setTextSize(12); cat.setTextColor(getColor(R.color.india)); cat.setPadding(0,3,0,3); b.addView(cat);
        TextView info=new TextView(this); info.setText(note); info.setTextSize(13); info.setTextColor(getColor(R.color.text_secondary)); info.setPadding(0,0,0,7); b.addView(info);
        MaterialButton x=new MaterialButton(this); x.setText("Open official route  →"); x.setAllCaps(false); x.setOnClickListener(v->open(url)); b.addView(x);
        c.addView(b); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=10; root.addView(c,p);
    }

    private void addBroker(LinearLayout root,String name,String url){
        MaterialCardView c=new MaterialCardView(this); c.setRadius(18); c.setCardElevation(0); c.setStrokeWidth(1); c.setStrokeColor(getColor(R.color.outline));
        LinearLayout b=new LinearLayout(this); b.setOrientation(LinearLayout.VERTICAL); b.setPadding(17,15,17,15);
        TextView n=new TextView(this); n.setText(name); n.setTextSize(16); n.setTextColor(getColor(R.color.text_primary)); n.setTypeface(null,1); b.addView(n);
        MaterialButton x=new MaterialButton(this); x.setText("Open official removal option"); x.setAllCaps(false); x.setOnClickListener(v->open(url)); b.addView(x);
        c.addView(b); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=10; root.addView(c,p);
    }

    private void open(String url){ try{ startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }catch(Exception ignored){ Toast.makeText(this,"No app can open this route.",Toast.LENGTH_SHORT).show(); } }
    private void showSettings(){ View v=getLayoutInflater().inflate(R.layout.screen_settings,container,false);container.removeAllViews();container.addView(v); }
    private ArrayList<String> breachScan(String email)throws Exception{ArrayList<String> out=new ArrayList<>();String body=get(XON+URLEncoder.encode(email,"UTF-8")+"?details=true");JSONObject o=new JSONObject(body);JSONArray a=o.optJSONArray("breaches");if(a==null)a=o.optJSONArray("data");if(a==null)return out;for(int i=0;i<a.length();i++){Object x=a.get(i);String n=x instanceof JSONObject?((JSONObject)x).optString("name",((JSONObject)x).optString("title","Breach")):String.valueOf(x);if(!n.isEmpty()&&!out.contains(n))out.add(n);}return out;}
    private JSONArray brokerArray()throws Exception{Object r=new JSONTokener(get(BROKERS)).nextValue();if(r instanceof JSONArray)return (JSONArray)r;if(r instanceof JSONObject){JSONObject o=(JSONObject)r;JSONArray a=o.optJSONArray("data");if(a!=null)return a;return o.optJSONArray("brokers");}return new JSONArray();}
    private static String first(JSONObject o,String...keys){for(String k:keys){String v=o.optString(k,"");if(!v.trim().isEmpty())return v.trim();}return "";}
    private static String get(String u)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("Accept","application/json");int z=c.getResponseCode();if(z<200||z>=300)throw new IOException("HTTP "+z);InputStream i=c.getInputStream();ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=i.read(b))!=-1)o.write(b,0,n);i.close();return o.toString(StandardCharsets.UTF_8.name());}
    @Override protected void onDestroy(){executor.shutdownNow();super.onDestroy();}
}
