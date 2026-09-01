package com.rath0darya.deleteme;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.*;

public class TraiHeaderActivity extends AppCompatActivity {
 private LinearLayout results; private TextInputEditText query; private TextView count;
 private static final int BG=Color.rgb(247,245,255),INK=Color.rgb(25,22,30),MUTED=Color.rgb(100,96,110),PRIMARY=Color.rgb(91,46,255),GREEN=Color.rgb(0,128,91);
 @Override public void onCreate(Bundle b){super.onCreate(b);setTitle("TRAI Header Lookup");LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);root.setPadding(dp(18),dp(18),dp(18),dp(24));TextView h=t("TRAI Header Lookup",28,INK,true);root.addView(h,lp(0,0,0,4));root.addView(t("Search TRAI SMS headers and inspect the header → prefix → entity → service relationship.",14,MUTED,false),lp(0,0,0,14));TextInputLayout box=new TextInputLayout(this);box.setHint("Header, prefix, entity or service");box.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);query=new TextInputEditText(this);query.setSingleLine(true);box.addView(query);root.addView(box,lp(0,0,0,8));count=t("Showing official-source lookup structure",12,MUTED,true);root.addView(count,lp(0,0,0,12));ScrollView scroll=new ScrollView(this);results=new LinearLayout(this);results.setOrientation(LinearLayout.VERTICAL);scroll.addView(results);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);render("");query.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){}public void onTextChanged(CharSequence s,int a,int b,int c){render(s.toString());}public void afterTextChanged(Editable e){}});}
 private void render(String q){results.removeAllViews();String needle=q.trim().toLowerCase(Locale.ROOT);int shown=0;for(String[] x:DATA){String all=String.join(" ",x).toLowerCase(Locale.ROOT);if(!needle.isEmpty()&&!all.contains(needle))continue;results.addView(tree(x));shown++;}count.setText(shown+" matching header record"+(shown==1?"":"s"));if(shown==0)results.addView(t("No bundled record matches this search. Use the official TRAI XLSX/PDF sources below for the complete current dataset.",14,MUTED,false));MaterialButton pdf=button("Open TRAI header prefix PDF");pdf.setOnClickListener(v->open("https://trai.gov.in/sites/default/files/2024-09/Detail_Header_Prefixes_16062020_0.pdf"));results.addView(pdf,lp(0,12,0,6));MaterialButton xlsx=button("Open TRAI official XLSX");xlsx.setOnClickListener(v->open("https://trai.gov.in/sites/default/files/2024-09/List_SMS_Headers_16062020_0.xlsx"));results.addView(xlsx);}
 private MaterialCardView tree(String[] x){MaterialCardView c=new MaterialCardView(this);c.setCardBackgroundColor(Color.WHITE);c.setStrokeColor(Color.rgb(221,215,238));c.setStrokeWidth(dp(1));c.setRadius(dp(18));LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(16),dp(14),dp(16),dp(14));r.addView(t("HEADER",10,PRIMARY,true));r.addView(t(x[0],20,INK,true),lp(0,3,0,10));r.addView(node("Prefix",x[1],PRIMARY));r.addView(node("Entity / Organisation",x[2],GREEN));r.addView(node("Service / Purpose",x[3],INK));r.addView(node("Category",x[4],MUTED));c.addView(r);return c;}
 private TextView node(String k,String v,int col){return t("│  ├─ "+k+"\n│  │     "+v,13,col,k.equals("Prefix")||k.equals("Entity / Organisation"));}
 private MaterialButton button(String s){MaterialButton b=new MaterialButton(this);b.setText(s);b.setAllCaps(false);b.setTextSize(13);return b;}
 private TextView t(String s,float z,int c,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setTypeface(android.graphics.Typeface.DEFAULT,bold?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL);return v;}
 private LinearLayout.LayoutParams lp(int w,int l,int t,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w==0?-1:w,-2);p.setMargins(l,t,0,b);return p;}
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private void open(String u){try{startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse(u)));}catch(Exception ignored){}}
 private static final String[][] DATA={{"VM-TRAI","VM","Telecom Regulatory Authority of India","Regulatory / informational SMS","TRAI"},{"AD-TRAI","AD","Telecom Regulatory Authority of India","Regulatory / informational SMS","TRAI"},{"AX-TRAI","AX","Telecom Regulatory Authority of India","Regulatory / informational SMS","TRAI"}};
}
