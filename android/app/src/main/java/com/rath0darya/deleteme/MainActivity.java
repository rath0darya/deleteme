package com.rath0darya.deleteme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
    private EditText identifier;
    private LinearLayout results;
    private TextView summary, status;
    private Button scan;
    private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String XON = "https://api.xposedornot.com/v1/check-email/";
    private static final String BROKERS = "https://raw.githubusercontent.com/Persprotect/data-broker-opt-out-list/main/data-brokers.json";
    private int black=Color.rgb(20,20,20), gray=Color.rgb(100,100,105), soft=Color.rgb(247,247,249), green=Color.rgb(25,130,75), red=Color.rgb(190,45,45);

    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);buildUi();}

    private void buildUi(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(Color.WHITE);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(22,18,22,34);scroll.addView(root);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView logo=text("delete",28,black);logo.setTypeface(null,1);top.addView(logo,new LinearLayout.LayoutParams(0,56,1));status=text("● Ready",13,green);top.addView(status);root.addView(top);
        TextView title=text("Find your data",30,black);title.setTypeface(null,1);title.setPadding(0,24,0,5);root.addView(title);
        TextView sub=text("Search for your information in public breach sources. Then decide what you want to remove.",15,gray);sub.setLineSpacing(3,1);sub.setPadding(0,0,0,22);root.addView(sub);
        LinearLayout inputCard=card();TextView label=text("WHAT SHOULD WE CHECK?",12,gray);label.setTypeface(null,1);inputCard.addView(label);
        identifier=new EditText(this);identifier.setHint("Email, phone number, or username");identifier.setTextSize(16);identifier.setSingleLine(true);identifier.setTextColor(black);identifier.setHintTextColor(Color.rgb(145,145,150));identifier.setPadding(16,0,16,0);GradientDrawable field=new GradientDrawable();field.setColor(Color.WHITE);field.setStroke(2,Color.rgb(225,225,230));field.setCornerRadius(16);identifier.setBackground(field);LinearLayout.LayoutParams ip=lp(-1,58);ip.topMargin=9;inputCard.addView(identifier,ip);root.addView(inputCard);
        scan=new Button(this);scan.setText("Find my data");scan.setTextSize(16);scan.setTextColor(Color.WHITE);scan.setAllCaps(false);scan.setTypeface(null,1);scan.setBackground(round(black,16));scan.setOnClickListener(v->startScan());LinearLayout.LayoutParams sp=lp(-1,58);sp.topMargin=12;root.addView(scan,sp);
        summary=text("",15,gray);summary.setPadding(4,18,4,12);root.addView(summary);
        results=new LinearLayout(this);results.setOrientation(LinearLayout.VERTICAL);root.addView(results);
        TextView privacy=text("Private by design · No account · Results stay on this device",12,Color.rgb(125,125,130));privacy.setGravity(Gravity.CENTER);privacy.setPadding(0,28,0,0);root.addView(privacy);
        setContentView(scroll);
    }

    private void startScan(){String value=identifier.getText().toString().trim();if(value.isEmpty()){identifier.setError("Enter an email, phone number, or username");return;}scan.setEnabled(false);scan.setText("Searching…");status.setText("● Searching");status.setTextColor(Color.rgb(185,110,20));summary.setText("Checking public sources…");results.removeAllViews();executor.execute(()->{try{ArrayList<Result> out=new ArrayList<>();if(isEmail(value))out.addAll(scanBreaches(value));ArrayList<Broker> brokers=loadBrokers();runOnUiThread(()->{render(out,brokers);summary.setText(out.size()==0?"No confirmed breach matches found.":out.size()+" confirmed breach match"+(out.size()==1?"":"es")+" found.");status.setText("● Complete");status.setTextColor(green);scan.setEnabled(true);scan.setText("Search again");});}catch(Exception e){runOnUiThread(()->{summary.setText("We couldn't complete the search. Check your connection and try again.");status.setText("● Error");status.setTextColor(red);scan.setEnabled(true);scan.setText("Try again");});}});}

    private ArrayList<Result> scanBreaches(String email)throws Exception{ArrayList<Result> list=new ArrayList<>();String body=get(XON+URLEncoder.encode(email,"UTF-8")+"?details=true");if(body==null||body.isEmpty())return list;JSONObject o=new JSONObject(body);JSONArray a=o.optJSONArray("breaches");if(a==null)a=o.optJSONArray("data");if(a==null)return list;HashSet<String> seen=new HashSet<>();for(int i=0;i<a.length();i++){Object x=a.get(i);String name=x instanceof JSONObject?((JSONObject)x).optString("name",((JSONObject)x).optString("title","Breach")):String.valueOf(x);if(!name.isEmpty()&&seen.add(name))list.add(new Result(name,"Your email was reported in this breach."));}return list;}

    private ArrayList<Broker> loadBrokers()throws Exception{ArrayList<Broker> list=new ArrayList<>();Object root=new JSONTokener(get(BROKERS)).nextValue();JSONArray a=root instanceof JSONArray?(JSONArray)root:((JSONObject)root).optJSONArray("data");if(a==null&&root instanceof JSONObject)a=((JSONObject)root).optJSONArray("brokers");if(a==null)return list;for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;String company=first(o,"company","name","broker"),domain=first(o,"domain","website"),url=first(o,"optOutUrl","opt_out_url","optout_url","opt_out","url");String exposed=first(o,"dataExposed","data_exposed","exposed_data");if(!company.isEmpty()||!url.isEmpty())list.add(new Broker(company,domain,url,exposed));}return list;}

    private void render(ArrayList<Result> breaches,ArrayList<Broker> brokers){results.removeAllViews();if(!breaches.isEmpty()){addSection("Confirmed matches",breaches.size()+" source"+(breaches.size()==1?"":"s")+" reported your identifier.");for(Result r:breaches)results.addView(breachCard(r));}else{LinearLayout empty=card();TextView icon=text("✓",28,green);icon.setGravity(Gravity.CENTER);empty.addView(icon);TextView t=text("No confirmed matches",18,black);t.setTypeface(null,1);t.setGravity(Gravity.CENTER);empty.addView(t);TextView d=text("This only means no match was returned by the checked breach source.",13,gray);d.setGravity(Gravity.CENTER);d.setPadding(0,6,0,0);empty.addView(d);results.addView(empty);}
        addSection("Removal directory",brokers.size()+" official opt-out routes available.");TextView note=text("These links are removal options, not proof that a broker has your data.",13,gray);note.setPadding(4,0,4,10);results.addView(note);for(Broker b:brokers)results.addView(brokerCard(b));}

    private void addSection(String title,String detail){TextView h=text(title,20,black);h.setTypeface(null,1);h.setPadding(2,20,2,3);results.addView(h);TextView d=text(detail,13,gray);d.setPadding(2,0,2,10);results.addView(d);}
    private View breachCard(Result r){LinearLayout c=card();TextView tag=text("BREACH MATCH",11,Color.WHITE);tag.setGravity(Gravity.CENTER);tag.setPadding(9,5,9,5);tag.setBackground(round(red,30));LinearLayout.LayoutParams tp=lp(-2,32);tp.bottomMargin=9;c.addView(tag,tp);TextView n=text(r.name,17,black);n.setTypeface(null,1);c.addView(n);c.addView(text(r.detail,13,gray));return c;}
    private View brokerCard(Broker b){LinearLayout c=card();TextView n=text(b.company.isEmpty()?"Data broker":b.company,17,black);n.setTypeface(null,1);c.addView(n);if(!b.domain.isEmpty())c.addView(text(b.domain,12,gray));if(!b.exposed.isEmpty())c.addView(text("May expose: "+b.exposed,13,gray));if(!b.url.isEmpty()){Button x=new Button(this);x.setText("View removal option");x.setAllCaps(false);x.setTextColor(black);x.setTextSize(14);x.setBackground(round(Color.WHITE,12));x.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(b.url)));}catch(Exception ignored){}});LinearLayout.LayoutParams xp=lp(-1,48);xp.topMargin=10;c.addView(x,xp);}return c;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(16,16,16,16);c.setBackground(round(soft,18));LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=10;c.setLayoutParams(p);return c;}
    private GradientDrawable round(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);return g;}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private boolean isEmail(String s){return android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches();}
    private static String first(JSONObject o,String...keys){for(String k:keys){String v=o.optString(k,"");if(!v.trim().isEmpty())return v.trim();}return "";}
    private static String get(String url)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("Accept","application/json");int code=c.getResponseCode();if(code==404)return "{}";if(code<200||code>=300)throw new IOException("HTTP "+code);InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);in.close();return out.toString(StandardCharsets.UTF_8.name());}
    static class Result{String name,detail;Result(String n,String d){name=n;detail=d;}}
    static class Broker{String company,domain,url,exposed;Broker(String c,String d,String u,String e){company=c;domain=d;url=u;exposed=e;}}
    @Override protected void onDestroy(){executor.shutdownNow();super.onDestroy();}
}
