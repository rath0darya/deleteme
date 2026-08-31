package com.rath0darya.deleteme;

import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends AppCompatActivity {
    private FrameLayout container;
    private BottomNavigationView nav;
    private TextInputEditText input;
    private TextInputLayout inputLayout;
    private TextView searchHelp;
    private String searchType="email";
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private final ArrayList<BreachResult> breaches=new ArrayList<>();
    private final ArrayList<String> publicMatches=new ArrayList<>();

    private static final String XON="https://api.xposedornot.com/v1/";
    private static final String GH="https://api.github.com/";
    private static final String GL="https://gitlab.com/api/v4/";
    private static final String BROKERS="https://raw.githubusercontent.com/Persprotect/data-broker-opt-out-list/main/data-brokers.json";
    private static final int INK=Color.rgb(25,28,31), MUTED=Color.rgb(92,101,108), BG=Color.rgb(246,248,249), WHITE=Color.WHITE;
    private static final int GREEN=Color.rgb(0,128,91), GREEN_BG=Color.rgb(220,248,239), RED=Color.rgb(190,50,55), RED_BG=Color.rgb(255,235,236), ORANGE=Color.rgb(198,111,0), ORANGE_BG=Color.rgb(255,241,220), BLUE=Color.rgb(45,105,180), BLUE_BG=Color.rgb(231,241,255), OUTLINE=Color.rgb(207,214,219);

    private static final String[][] INDIA_SOURCES={
        {"Truecaller","Phone directory","https://www.truecaller.com/unlisting","Official phone-number unlisting route"},
        {"Sulekha","Local-services directory","https://www.sulekha.com/collateral/privacy","Privacy and deletion request route"},
        {"Shaadi.com","Matrimonial profile","https://support.shaadi.com/support/solutions/articles/48000755467-i-want-to-hide-or-delete-my-profile","Hide or delete profile"},
        {"Jeevansathi","Matrimonial profile","https://www.jeevansathi.com/privacy-policy","Privacy and profile deletion information"},
        {"BharatMatrimony","Matrimonial profile","https://paymentsnewstage.bharatmatrimony.com/faq.php","Profile deletion information"},
        {"Naukri FastForward","Resume / recruitment","https://resume.naukri.com/frequently-asked-questions-faq/can-i-delete-my-profile-from-your-website-once-i-get-a-job/","Delete or hide profile"},
        {"MarkSpace Media Pvt Ltd","Registered data broker","mailto:privacy@markspacemedia.com","Listed as a data broker; removal contact"}
    };

    @Override protected void onCreate(Bundle state){
        super.onCreate(state);
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat ic=WindowCompat.getInsetsController(getWindow(),getWindow().getDecorView());
        ic.setAppearanceLightStatusBars(true); ic.setAppearanceLightNavigationBars(true);
        setContentView(R.layout.activity_main);
        View root=findViewById(R.id.rootLayout);
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.displayCutout());v.setPadding(v.getPaddingLeft(),bars.top,v.getPaddingRight(),bars.bottom);return insets;});
        ViewCompat.requestApplyInsets(root);
        MaterialToolbar bar=findViewById(R.id.topAppBar); bar.setTitle("DeleteMe");
        container=findViewById(R.id.screenContainer); nav=findViewById(R.id.bottomNavigation);
        nav.setOnItemSelectedListener(item->{if(item.getItemId()==R.id.nav_scan){showScan();return true;}if(item.getItemId()==R.id.nav_results){showResults();return true;}if(item.getItemId()==R.id.nav_settings){showSettings();return true;}return false;});
        nav.setSelectedItemId(R.id.nav_scan);
    }

    private void showScan(){
        View v=getLayoutInflater().inflate(R.layout.screen_scan,container,false); container.removeAllViews(); container.addView(v);
        input=v.findViewById(R.id.identifierInput); inputLayout=v.findViewById(R.id.identifierLayout); searchHelp=v.findViewById(R.id.searchHelp);
        Chip email=v.findViewById(R.id.typeEmail), mobile=v.findViewById(R.id.typeMobile), username=v.findViewById(R.id.typeUsername), name=v.findViewById(R.id.typeName);
        email.setOnClickListener(x->selectType("email")); mobile.setOnClickListener(x->selectType("mobile")); username.setOnClickListener(x->selectType("username")); name.setOnClickListener(x->selectType("name"));
        MaterialButton find=v.findViewById(R.id.findButton); find.setOnClickListener(x->scan(find));
    }

    private void selectType(String type){
        searchType=type; input.setText(""); inputLayout.setError(null);
        if(type.equals("email")){inputLayout.setHint("Email address");input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);searchHelp.setText("Free breach intelligence. Detailed exposure data is loaded when available.");}
        else if(type.equals("mobile")){inputLayout.setHint("Mobile number");input.setInputType(InputType.TYPE_CLASS_PHONE);searchHelp.setText("Public phone exposure and official removal routes. A route is not proof that your number was found.");}
        else if(type.equals("username")){inputLayout.setHint("Username / handle");input.setInputType(InputType.TYPE_CLASS_TEXT);searchHelp.setText("Checks public GitHub and GitLab profiles. A public match is only a possible match.");}
        else{inputLayout.setHint("Full name");input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);searchHelp.setText("Checks public developer profiles. Name matches are potential matches, not identity confirmation.");}
    }

    private void scan(MaterialButton button){
        String value=input.getText()==null?"":input.getText().toString().trim();
        if(value.isEmpty()){inputLayout.setError("Enter something to search");return;}
        if(searchType.equals("email")&&!android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()){inputLayout.setError("Enter a valid email address");return;}
        if(searchType.equals("mobile")&&value.replaceAll("\\D","").length()<8){inputLayout.setError("Enter a valid mobile number");return;}
        if((searchType.equals("username")||searchType.equals("name"))&&value.length()<2){inputLayout.setError("Enter at least 2 characters");return;}
        inputLayout.setError(null); button.setEnabled(false); button.setText("Searching…");
        executor.execute(()->{try{
            breaches.clear();publicMatches.clear();
            if(searchType.equals("email")) loadEmailExposure(value); else if(searchType.equals("username")) loadUsername(value); else if(searchType.equals("name")) loadName(value); else loadMobile(value);
            runOnUiThread(()->{button.setEnabled(true);button.setText("Find my data  →");showResults();});
        }catch(Exception e){runOnUiThread(()->{button.setEnabled(true);button.setText("Try again");Toast.makeText(this,"Search failed. Check your connection and try again.",Toast.LENGTH_LONG).show();});}});
    }

    private void loadEmailExposure(String email)throws Exception{
        String body=get(XON+"breach-analytics?email="+URLEncoder.encode(email,"UTF-8"));
        JSONObject root=new JSONObject(body); JSONObject eb=root.optJSONObject("ExposedBreaches"); JSONArray details=eb==null?null:eb.optJSONArray("breaches_details");
        if(details!=null){for(int i=0;i<details.length();i++){JSONObject x=details.optJSONObject(i);if(x!=null)breaches.add(new BreachResult(x.optString("breach","Unknown source"),x.optString("domain",""),x.optString("xposed_date",""),x.optString("xposed_data",""),x.optLong("xposed_records",0),x.optString("details",""),"",x.optString("password_risk","")));}}
        if(breaches.isEmpty()){
            String check=get(XON+"check-email/"+URLEncoder.encode(email,"UTF-8")+"?details=true"); JSONObject c=new JSONObject(check); JSONArray a=c.optJSONArray("breaches"); if(a!=null&&a.length()>0&&a.opt(0) instanceof JSONArray){JSONArray n=a.optJSONArray(0);for(int i=0;i<n.length();i++)breaches.add(new BreachResult(n.optString(i),"","","",0,"Detailed breach metadata was not returned for this source.","",""));}
        }
    }

    private void loadUsername(String username)throws Exception{
        if(code(GH+"users/"+URLEncoder.encode(username,"UTF-8"))==200)publicMatches.add("GitHub  ·  @"+username);
        String gl=GL+"users?username="+URLEncoder.encode(username,"UTF-8"); if(code(gl)==200){JSONArray a=new JSONArray(get(gl));if(a.length()>0)publicMatches.add("GitLab  ·  @"+username);}
    }
    private void loadName(String name)throws Exception{
        JSONObject o=new JSONObject(get(GH+"search/users?q="+URLEncoder.encode(name,"UTF-8")+"&per_page=10")); JSONArray a=o.optJSONArray("items");
        if(a!=null)for(int i=0;i<a.length();i++){JSONObject u=a.optJSONObject(i);if(u!=null)publicMatches.add("GitHub profile  ·  @"+u.optString("login"));}
    }
    private void loadMobile(String mobile){publicMatches.add("Mobile removal routes available");}

    private void showResults(){
        nav.setSelectedItemId(R.id.nav_results); container.removeAllViews();
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(BG); LinearLayout root=column(20,18,20,30);scroll.addView(root);container.addView(scroll);
        label(root,"SCAN RESULTS"); TextView title=text((breaches.size()+publicMatches.size())+" result"+((breaches.size()+publicMatches.size())==1?"":"s")+" found",30,INK,true);root.addView(title,margin(0,7,0,5));
        TextView sub=text(breaches.size()>0?"Review where each exposure happened and exactly what data was reported before deciding what you want removed.":"Review each public match before using a removal route. A public match is not proof of identity.",15,MUTED,false);root.addView(sub,margin(0,0,0,15));
        LinearLayout stats=row();root.addView(stats,margin(0,0,0,12));stats.addView(statCard("CONFIRMED EXPOSURES",String.valueOf(breaches.size()),RED_BG,RED),weight());addSpace(stats,10);stats.addView(statCard("PUBLIC MATCHES",String.valueOf(publicMatches.size()),BLUE_BG,BLUE),weight());
        MaterialButton directory=primaryButton("🇮🇳  Browse India & global removal directory");directory.setOnClickListener(v->showDirectory());root.addView(directory,margin(0,2,0,15));
        if(breaches.size()>0){label(root,"CONFIRMED EXPOSURE");for(BreachResult b:breaches)addBreachCard(root,b);}
        if(publicMatches.size()>0){label(root,"POSSIBLE PUBLIC MATCHES");for(String s:publicMatches)addPublicCard(root,s);}
        if(breaches.isEmpty()&&publicMatches.isEmpty())addEmpty(root);
        TextView foot=text("Removal opportunities are separate from confirmed exposure. A directory listing does not mean your data was found there.",12,MUTED,false);root.addView(foot,margin(2,14,2,0));
    }

    private void addBreachCard(LinearLayout root,BreachResult b){
        MaterialCardView card=card(WHITE,RED); LinearLayout box=column(17,16,17,16); card.addView(box);root.addView(card,margin(0,0,0,12));
        TextView badge=badge("🔴  CONFIRMED EXPOSURE",RED);box.addView(badge,margin(0,0,0,11));
        TextView source=text(b.name,21,INK,true);box.addView(source,margin(0,0,0,2));
        if(!b.domain.isEmpty())box.addView(text(b.domain,13,MUTED,false),margin(0,0,0,12));
        LinearLayout info=row();box.addView(info,margin(0,0,0,8));
        if(!b.date.isEmpty())info.addView(meta("📅","Breach date",b.date),weight());
        if(b.records>0){addSpace(info,10);info.addView(meta("👥","Records affected",formatNumber(b.records)),weight());}
        if(!b.data.isEmpty()){box.addView(text("DATA EXPOSED",11,MUTED,true),margin(0,7,0,5));box.addView(chips(b.data),margin(0,0,0,10));}
        if(!b.description.isEmpty()){box.addView(text("WHAT HAPPENED",11,MUTED,true),margin(0,2,0,4));TextView desc=text(b.description,13,MUTED,false);desc.setMaxLines(6);box.addView(desc,margin(0,0,0,9));}
        if(!b.passwordRisk.isEmpty()){box.addView(text("PASSWORD RISK  ·  "+b.passwordRisk,12,b.passwordRisk.toLowerCase().contains("plain")?RED:ORANGE,true),margin(0,0,0,8));}
        LinearLayout actions=row();box.addView(actions);MaterialButton review=primaryButton("Review removal  →");review.setOnClickListener(v->showDirectory());actions.addView(review,weight());addSpace(actions,8);MaterialButton keep=secondaryButton("Keep");keep.setOnClickListener(v->card.setVisibility(View.GONE));actions.addView(keep,weight());
    }

    private void addPublicCard(LinearLayout root,String match){MaterialCardView card=card(BLUE_BG,BLUE);LinearLayout box=column(16,15,16,15);card.addView(box);root.addView(card,margin(0,0,0,12));box.addView(badge("🔵  POSSIBLE PUBLIC MATCH",BLUE),margin(0,0,0,9));box.addView(text(match,18,INK,true),margin(0,0,0,4));box.addView(text("This was returned by a public source. It does not confirm that the profile belongs to you.",13,MUTED,false),margin(0,0,0,9));MaterialButton b=primaryButton("Review removal  →");b.setOnClickListener(v->showDirectory());box.addView(b);}
    private void addEmpty(LinearLayout root){MaterialCardView card=card(GREEN_BG,GREEN);LinearLayout box=column(20,24,20,24);card.addView(box);root.addView(card,margin(0,0,0,10));TextView check=text("✓",36,GREEN,true);check.setGravity(Gravity.CENTER);box.addView(check);TextView h=text("No confirmed exposure found",19,INK,true);h.setGravity(Gravity.CENTER);box.addView(h,margin(0,6,0,4));TextView d=text("The checked source did not return a matching exposure. A clean result does not prove an identifier was never compromised.",13,MUTED,false);d.setGravity(Gravity.CENTER);box.addView(d);}

    private void showDirectory(){
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(BG);LinearLayout root=column(20,18,20,30);scroll.addView(root);container.removeAllViews();container.addView(scroll);
        label(root,"REMOVAL DIRECTORY");root.addView(text("India-first removal",29,INK,true),margin(0,7,0,5));root.addView(text("Official routes are shown separately from confirmed exposure. Choose only the services you actually use.",14,MUTED,false),margin(0,0,0,15));
        label(root,"🇮🇳  INDIA SOURCES");for(String[] s:INDIA_SOURCES)addRoute(root,s[0],s[1],s[2],s[3],ORANGE_BG,ORANGE);
        label(root,"GLOBAL BROKER REGISTRY");executor.execute(()->{try{JSONArray a=brokerArray();runOnUiThread(()->{for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;String n=first(o,"company","name","broker"),u=first(o,"optOutUrl","opt_out_url","optout_url","opt_out","url");if(!n.isEmpty()&&!u.isEmpty())addRoute(root,n,"Data broker",u,"Official opt-out route",WHITE,MUTED);}});}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Global registry unavailable right now.",Toast.LENGTH_LONG).show());}});
    }
    private void addRoute(LinearLayout root,String name,String category,String url,String note,int bg,int accent){MaterialCardView c=card(bg,accent);LinearLayout b=column(16,15,16,15);c.addView(b);root.addView(c,margin(0,0,0,10));b.addView(text(name,17,INK,true));b.addView(text(category,12,accent,true),margin(0,3,0,4));b.addView(text(note,13,MUTED,false),margin(0,0,0,8));MaterialButton open=primaryButton("Open official route  →");open.setOnClickListener(v->open(url));b.addView(open);}

    private void showSettings(){View v=getLayoutInflater().inflate(R.layout.screen_settings,container,false);container.removeAllViews();container.addView(v);}

    private MaterialCardView card(int background,int stroke){MaterialCardView c=new MaterialCardView(this);c.setRadius(20);c.setCardElevation(0);c.setCardBackgroundColor(background);c.setStrokeWidth(1);c.setStrokeColor(stroke);return c;}
    private MaterialButton primaryButton(String s){MaterialButton b=new MaterialButton(this);b.setText(s);b.setTextSize(14);b.setAllCaps(false);b.setTextColor(WHITE);b.setBackgroundColor(GREEN);return b;}
    private MaterialButton secondaryButton(String s){MaterialButton b=new MaterialButton(this);b.setText(s);b.setTextSize(14);b.setAllCaps(false);b.setTextColor(GREEN);b.setBackgroundColor(Color.TRANSPARENT);return b;}
    private TextView badge(String s,int color){TextView t=text(s,11,WHITE,true);t.setPadding(10,6,10,6);t.setBackgroundColor(color);return t;}
    private TextView text(String s,float size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,1);t.setIncludeFontPadding(true);return t;}
    private void label(LinearLayout root,String s){TextView t=text(s,12,GREEN,true);t.setLetterSpacing(.09f);root.addView(t,margin(0,13,0,8));}
    private LinearLayout column(int l,int top,int r,int bottom){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(l,top,r,bottom);return x;}
    private LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.HORIZONTAL);x.setGravity(Gravity.CENTER_VERTICAL);return x;}
    private LinearLayout.LayoutParams margin(int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(l,t,r,b);return p;}
    private LinearLayout.LayoutParams weight(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);return p;}
    private void addSpace(LinearLayout p,int w){Space s=new Space(this);p.addView(s,new LinearLayout.LayoutParams(w,1));}
    private LinearLayout meta(String icon,String title,String value){LinearLayout c=column(0,0,0,0);c.addView(text(icon+"  "+title,11,MUTED,true));c.addView(text(value,14,INK,true),margin(0,2,0,0));return c;}
    private LinearLayout chips(String data){LinearLayout wrap=row();wrap.setGravity(Gravity.TOP);String[] parts=data.split("[;,]");LinearLayout current=row();current.setGravity(Gravity.TOP);LinearLayout outer=column(0,0,0,0);outer.addView(current);for(String raw:parts){String s=raw.trim();if(s.isEmpty())continue;TextView chip=text(s,12,GREEN,true);chip.setPadding(12,7,12,7);chip.setBackgroundColor(GREEN_BG);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,0,6,6);current.addView(chip,p);}return outer;}

    private void open(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception e){Toast.makeText(this,"No app can open this route.",Toast.LENGTH_SHORT).show();}}
    private String formatNumber(long n){return String.format(Locale.US,"%,d",n);}
    private int code(String u)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(12000);c.setReadTimeout(15000);c.setRequestProperty("Accept","application/json");return c.getResponseCode();}
    private static String get(String u)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("Accept","application/json");int z=c.getResponseCode();if(z<200||z>=300)throw new IOException("HTTP "+z);InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);in.close();return out.toString(StandardCharsets.UTF_8.name());}
    private JSONArray brokerArray()throws Exception{Object r=new JSONTokener(get(BROKERS)).nextValue();if(r instanceof JSONArray)return(JSONArray)r;if(r instanceof JSONObject){JSONObject o=(JSONObject)r;JSONArray a=o.optJSONArray("data");if(a!=null)return a;return o.optJSONArray("brokers");}return new JSONArray();}
    private static String first(JSONObject o,String...keys){for(String k:keys){String v=o.optString(k,"");if(!v.trim().isEmpty())return v.trim();}return "";}
    @Override protected void onDestroy(){executor.shutdownNow();super.onDestroy();}

    static class BreachResult{
        String name,domain,date,data,description,passwordRisk,reference;
        long records;
        BreachResult(String n,String d,String dt,String x,long r,String desc,String ref,String risk){name=n;domain=d;date=dt;data=x;records=r;description=desc;reference=ref;passwordRisk=risk;}
    }
}
