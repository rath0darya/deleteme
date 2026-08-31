package com.rath0darya.deleteme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private EditText identifier; private LinearLayout results; private TextView summary,status; private Button scan;
    private final java.util.concurrent.ExecutorService executor=Executors.newSingleThreadExecutor();
    private static final String XON="https://api.xposedornot.com/v1/check-email/";
    private static final String BROKERS="https://raw.githubusercontent.com/Persprotect/data-broker-opt-out-list/main/data-brokers.json";
    private final int ink=Color.rgb(18,18,20),muted=Color.rgb(105,105,112),surface=Color.rgb(247,247,249),border=Color.rgb(225,225,230),green=Color.rgb(24,125,70),red=Color.rgb(190,42,42),amber=Color.rgb(185,112,20);
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);buildUi();}
    private void buildUi(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(Color.WHITE);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(22,14,22,28);scroll.addView(root);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView logo=text("delete",26,ink);logo.setTypeface(Typeface.DEFAULT,Typeface.BOLD);top.addView(logo,new LinearLayout.LayoutParams(0,52,1));status=text("● Ready",12,green);top.addView(status);root.addView(top);
        TextView title=text("Find your data",30,ink);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);title.setPadding(0,22,0,4);root.addView(title);
        TextView sub=text("Check public breach sources and find official removal options. You choose what to remove.",15,muted);sub.setLineSpacing(2,1);sub.setPadding(0,0,0,22);root.addView(sub);
        LinearLayout input=card(Color.WHITE);TextView label=text("YOUR INFORMATION",12,muted);label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);input.addView(label);
        identifier=new EditText(this);identifier.setHint("Email, phone number, or username");identifier.setTextSize(16);identifier.setSingleLine(true);identifier.setTextColor(ink);identifier.setHintTextColor(Color.rgb(145,145,150));identifier.setPadding(16,0,16,0);identifier.setBackground(round(Color.WHITE,16));identifier.getBackground().setAlpha(255);GradientDrawable stroke=round(Color.WHITE,16);stroke.setStroke(2,border);identifier.setBackground(stroke);LinearLayout.LayoutParams ip=lp(-1,58);ip.topMargin=8;input.addView(identifier,ip);root.addView(input);
        scan=new Button(this);scan.setText("Find my data");scan.setAllCaps(false);scan.setTextSize(16);scan.setTypeface(Typeface.DEFAULT,Typeface.BOLD);scan.setTextColor(Color.WHITE);scan.setBackground(round(ink,16));scan.setOnClickListener(v->startScan());LinearLayout.LayoutParams bp=lp(-1,56);bp.topMargin=12;root.addView(scan,bp);
        summary=text("",14,muted);summary.setPadding(4,18,4,10);root.addView(summary);
        results=new LinearLayout(this);results.setOrientation(LinearLayout.VERTICAL);root.addView(results);
        TextView privacy=text("Private by design  ·  No account  ·  Results stay on this device",12,Color.rgb(130,130,135));privacy.setGravity(Gravity.CENTER);privacy.setPadding(0,26,0,0);root.addView(privacy);setContentView(scroll);
    }
    private void startScan(){String value=identifier.getText().toString().trim();if(value.isEmpty()){identifier.setError("Enter an email, phone number, or username");return;}scan.setEnabled(false);scan.setText("Searching…");status.setText("● Searching");status.setTextColor(amber);summary.setText("Checking free public sources…");results.removeAllViews();executor.execute(()->{try{ArrayList<Result> matches=new ArrayList<>();if(isEmail(value))matches.addAll(scanBreaches(value));ArrayList<Broker> brokers=loadBrokers();runOnUiThread(()->{render(matches,brokers);summary.setText(matches.size()+" confirmed breach match"+(matches.size()==1?"":"es")+"  ·  "+brokers.size()+" removal options");status.setText("● Complete");status.setTextColor(green);scan.setEnabled(true);scan.setText("Search again");});}catch(Exception e){runOnUiThread(()->{summary.setText("Search failed. Check your connection and try again.");status.setText("● Error");status.setTextColor(red);scan.setEnabled(true);scan.setText("Try again");});}});}
    private ArrayList<Result> scanBreaches(String email)throws Exception{ArrayList<Result> list=new ArrayList<>();String body=get(XON+URLEncoder.encode(email,"UTF-8")+"?details=true");if(body==null||body.isEmpty())return list;JSONObject o=new JSONObject(body);JSONArray a=o.optJSONArray("breaches");if(a==null)a=o.optJSONArray("data");if(a==null)return list;HashSet<String> seen=new HashSet<>();for(int i=0;i<a.length();i++){Object x=a.get(i);String name=x instanceof JSONObject?((JSONObject)x).optString("name",((JSONObject)x).optString("title","Breach")):String.valueOf(x);if(!name.isEmpty()&&seen.add(name))list.add(new Result(name,"Your email was reported in this breach."));}return list;}
    private ArrayList<Broker> loadBrokers()throws Exception{ArrayList<Broker> list=new ArrayList<>();Object root=new JSONTokener(get(BROKERS)).nextValue();JSONArray a=root instanceof JSONArray?(JSONArray)root:((JSONObject)root).optJSONArray("data");if(a==null&&root instanceof JSONObject)a=((JSONObject)root).optJSONArray("brokers");if(a==null)return list;for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;String company=first(o,"company","name","broker"),domain=first(o,"domain","website"),url=first(o,"optOutUrl","opt_out_url","optout_url","opt_out","url"),exposed=first(o,"dataExposed","data_exposed","exposed_data");if(!company.isEmpty()||!url.isEmpty())list.add(new Broker(company,domain,url,exposed));}return list;}
    private void render(ArrayList<Result> matches,ArrayList<Broker> brokers){results.removeAllViews();section("Your results","Only confirmed matches are shown as matches.");if(matches.isEmpty()){LinearLayout c=card(surface);TextView icon=text("✓",28,green);icon.setGravity(Gravity.CENTER);c.addView(icon);TextView t=text("No confirmed breach matches",18,ink);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER);c.addView(t);TextView d=text("No match was returned by the checked breach source.",13,muted);d.setGravity(Gravity.CENTER);d.setPadding(0,6,0,0);c.addView(d);results.addView(c);}else for(Result r:matches)results.addView(matchCard(r));section("Removal options",brokers.size()+" official opt-out routes are available.");TextView note=text("These options do not prove a broker has your data. Open one only when you want to request removal.",13,muted);note.setPadding(4,0,4,10);results.addView(note);for(Broker b:brokers)results.addView(brokerCard(b));}
    private void section(String a,String b){TextView h=text(a,20,ink);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setPadding(2,18,2,3);results.addView(h);TextView d=text(b,13,muted);d.setPadding(2,0,2,9);results.addView(d);}
    private View matchCard(Result r){LinearLayout c=card(surface);TextView tag=text("CONFIRMED MATCH",11,Color.WHITE);tag.setGravity(Gravity.CENTER);tag.setTypeface(Typeface.DEFAULT,Typeface.BOLD);tag.setPadding(10,5,10,5);tag.setBackground(round(red,30));c.addView(tag,lp(-2,30));TextView n=text(r.name,17,ink);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);LinearLayout.LayoutParams np=lp(-1,-2);np.topMargin=9;c.addView(n,np);c.addView(text(r.detail,13,muted));return c;}
    private View brokerCard(Broker b){LinearLayout c=card(surface);TextView n=text(b.company.isEmpty()?"Data broker":b.company,17,ink);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);c.addView(n);if(!b.domain.isEmpty())c.addView(text(b.domain,12,muted));if(!b.exposed.isEmpty())c.addView(text("May expose: "+b.exposed,13,muted));if(!b.url.isEmpty()){Button x=new Button(this);x.setText("View removal option");x.setAllCaps(false);x.setTextSize(14);x.setTextColor(ink);x.setBackground(round(Color.WHITE,12));x.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(b.url)));}catch(Exception ignored){}});LinearLayout.LayoutParams xp=lp(-1,48);xp.topMargin=10;c.addView(x,xp);}return c;}
    private LinearLayout card(int color){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(16,16,16,16);c.setBackground(round(color,18));LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=10;c.setLayoutParams(p);return c;}
    private GradientDrawable round(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);return g;}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private boolean isEmail(String s){return android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches();}
    private static String first(JSONObject o,String...keys){for(String k:keys){String v=o.optString(k,"");if(!v.trim().isEmpty())return v.trim();}return "";}
    private static String get(String url)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("Accept","application/json");int code=c.getResponseCode();if(code==404)return "{}";if(code<200||code>=300)throw new IOException("HTTP "+code);InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);in.close();return out.toString(StandardCharsets.UTF_8.name());}
    static class Result{String name,detail;Result(String n,String d){name=n;detail=d;}} static class Broker{String company,domain,url,exposed;Broker(String c,String d,String u,String e){company=c;domain=d;url=u;exposed=e;}}
    @Override protected void onDestroy(){executor.shutdownNow();super.onDestroy();}
}
