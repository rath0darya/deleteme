package com.rath0darya.deleteme;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class MainActivity extends android.app.Activity {
    private EditText identifier;
    private LinearLayout results;
    private TextView summary;
    private Button scan;
    private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String XON = "https://api.xposedornot.com/v1/check-email/";
    private static final String BROKERS = "https://raw.githubusercontent.com/Persprotect/data-broker-opt-out-list/main/data-brokers.json";

    @Override public void onCreate(Bundle b) { super.onCreate(b); buildUi(); }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,32,28,40); root.setBackgroundColor(Color.WHITE); scroll.addView(root);
        TextView brand = text("delete", 32, Color.rgb(18,18,18)); brand.setTypeface(null,1); root.addView(brand);
        TextView title = text("Find your data.", 30, Color.BLACK); title.setPadding(0,42,0,4); root.addView(title);
        TextView sub = text("Check free public sources, see where your data appears, then choose what to remove.",16,Color.DKGRAY); sub.setPadding(0,0,0,24); root.addView(sub);
        TextView label = text("Email, phone, or username",14,Color.DKGRAY); root.addView(label);
        identifier = new EditText(this); identifier.setHint("you@example.com"); identifier.setSingleLine(true); identifier.setPadding(18,14,18,14); root.addView(identifier, lp(-1,60));
        scan = new Button(this); scan.setText("FIND MY DATA"); scan.setAllCaps(false); scan.setTextSize(16); scan.setOnClickListener(v -> startScan()); LinearLayout.LayoutParams bp=lp(-1,58); bp.topMargin=18; root.addView(scan,bp);
        summary = text("",16,Color.DKGRAY); summary.setPadding(0,24,0,16); root.addView(summary);
        results = new LinearLayout(this); results.setOrientation(LinearLayout.VERTICAL); root.addView(results);
        TextView privacy=text("Privacy: no DeleteMe account or central profile is created. Only the identifier you submit is sent to a checked third-party source.",13,Color.GRAY); privacy.setPadding(0,30,0,0); root.addView(privacy);
        setContentView(scroll);
    }

    private void startScan() {
        String value=identifier.getText().toString().trim(); if(value.isEmpty()){identifier.setError("Enter an identifier");return;}
        scan.setEnabled(false); scan.setText("Checking…"); summary.setText("Checking free sources…"); results.removeAllViews();
        executor.execute(() -> { try {
            ArrayList<Result> out=new ArrayList<>();
            if(isEmail(value)) out.addAll(scanBreaches(value));
            ArrayList<Broker> brokers=loadBrokers();
            runOnUiThread(() -> { summary.setText(out.size()+" confirmed breach match"+(out.size()==1?"":"es")+" found\n"+brokers.size()+" official broker removal routes available"); render(out,brokers); scan.setEnabled(true); scan.setText("SCAN AGAIN"); });
        } catch(Exception e) { runOnUiThread(() -> { summary.setText("Scan could not complete: "+e.getMessage()); scan.setEnabled(true); scan.setText("TRY AGAIN"); }); }});
    }

    private ArrayList<Result> scanBreaches(String email) throws Exception {
        ArrayList<Result> list=new ArrayList<>(); String body=get(XON+URLEncoder.encode(email,"UTF-8")+"?details=true");
        if(body==null||body.isEmpty()) return list;
        JSONObject o=new JSONObject(body); JSONArray a=o.optJSONArray("breaches"); if(a==null) a=o.optJSONArray("data"); if(a==null) return list;
        HashSet<String> seen=new HashSet<>(); for(int i=0;i<a.length();i++){Object x=a.get(i); String name=x instanceof JSONObject?((JSONObject)x).optString("name",((JSONObject)x).optString("title","Breach")):String.valueOf(x); if(!name.isEmpty()&&seen.add(name)) list.add(new Result("BREACH FOUND",name,"Your email appears in this reported breach."));}
        return list;
    }

    private ArrayList<Broker> loadBrokers() throws Exception {
        ArrayList<Broker> list=new ArrayList<>(); String body=get(BROKERS); Object root=new JSONTokener(body).nextValue(); JSONArray a=root instanceof JSONArray?(JSONArray)root:((JSONObject)root).optJSONArray("data"); if(a==null&&root instanceof JSONObject) a=((JSONObject)root).optJSONArray("brokers"); if(a==null) return list;
        for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;String company=first(o,"company","name","broker");String domain=first(o,"domain","website");String url=first(o,"optOutUrl","opt_out_url","optout_url","opt_out","url");String exposed=first(o,"dataExposed","data_exposed","exposed_data");if(!company.isEmpty()||!url.isEmpty())list.add(new Broker(company,domain,url,exposed));}
        return list;
    }

    private void render(ArrayList<Result> breaches, ArrayList<Broker> brokers){
        if(!breaches.isEmpty()){TextView h=text("CONFIRMED MATCHES",14,Color.rgb(180,30,30));h.setPadding(0,12,0,8);results.addView(h);for(Result r:breaches)results.addView(card(r));}
        TextView h=text("BROKER REMOVAL DIRECTORY",14,Color.DKGRAY);h.setPadding(0,24,0,8);results.addView(h);
        TextView note=text("These are removal routes, not proof that the broker has your record.",13,Color.GRAY);note.setPadding(0,0,0,10);results.addView(note);
        for(Broker b:brokers) results.addView(brokerCard(b));
    }

    private View card(Result r){LinearLayout c=box();c.addView(text("● "+r.status,13,Color.rgb(180,30,30)));c.addView(text(r.name,18,Color.BLACK));c.addView(text(r.detail,14,Color.DKGRAY));return c;}
    private View brokerCard(Broker b){LinearLayout c=box();c.addView(text(b.company.isEmpty()?"Data broker":b.company,18,Color.BLACK));if(!b.domain.isEmpty())c.addView(text(b.domain,13,Color.GRAY));if(!b.exposed.isEmpty())c.addView(text("May expose: "+b.exposed,13,Color.DKGRAY));if(!b.url.isEmpty()){Button x=new Button(this);x.setText("Open official removal page");x.setAllCaps(false);x.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(b.url)));}catch(Exception ignored){}});c.addView(x);}return c;}
    private LinearLayout box(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(18,16,18,16);c.setBackgroundColor(Color.rgb(247,247,247));LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=10;c.setLayoutParams(p);return c;}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private boolean isEmail(String s){return android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches();}
    private static String first(JSONObject o,String...keys){for(String k:keys){String v=o.optString(k,"");if(v!=null&&!v.trim().isEmpty())return v.trim();}return "";}
    private static String get(String url) throws Exception {HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("Accept","application/json");int code=c.getResponseCode();if(code==404)return "{}";if(code<200||code>=300)throw new IOException("HTTP "+code);InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);in.close();return out.toString(StandardCharsets.UTF_8.name());}
    static class Result{String status,name,detail;Result(String s,String n,String d){status=s;name=n;detail=d;}}
    static class Broker{String company,domain,url,exposed;Broker(String c,String d,String u,String e){company=c;domain=d;url=u;exposed=e;}}
    @Override protected void onDestroy(){executor.shutdownNow();super.onDestroy();}
}
